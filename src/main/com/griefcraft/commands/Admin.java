/**
 * This file is part of LWC (https://github.com/Hidendra/LWC)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.griefcraft.commands;

import static com.griefcraft.util.StringUtils.hasFlag;
import static com.griefcraft.util.StringUtils.join;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.server.EntityHuman;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.ContainerBlock;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.Player;

import com.griefcraft.converters.ChastityChest;
import com.griefcraft.converters.ChestProtect;
import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCInfo;
import com.griefcraft.model.Limit;
import com.griefcraft.model.Protection;
import com.griefcraft.util.Colors;
import com.griefcraft.util.Config;
import com.griefcraft.util.Performance;
import com.griefcraft.util.StringUtils;
import com.griefcraft.util.Updater;

public class Admin implements ICommand {

	/**
	 * Class that handles cleaning up the LWC database usage: /lwc admin cleanup
	 */
	private class Admin_Cleanup_Thread implements Runnable {

		private LWC lwc;
		private CommandSender sender;

		public Admin_Cleanup_Thread(LWC lwc, CommandSender sender) {
			this.lwc = lwc;
			this.sender = sender;
		}

		public void run() {
			Server server = sender.getServer();
			long start = System.currentTimeMillis();
			int completed = 0;
			List<Protection> protections = lwc.getPhysicalDatabase().loadProtections();
			List<Integer> ignore = new ArrayList<Integer>(); // list of protect ids to ignore

			Iterator<Protection> iterator = protections.iterator();

			// we need to batch the updates to the database
			try {
				lwc.getPhysicalDatabase().getConnection().setAutoCommit(false);
			} catch (SQLException e) {
				e.printStackTrace();
			}

			try {
				while (iterator.hasNext()) {
					Protection protection = iterator.next();

					if (ignore.contains(protection.getId())) {
						continue;
					}

					String worldName = protection.getWorld();
					World world = (worldName == null || worldName.isEmpty()) ? server.getWorlds().get(0) : server.getWorld(worldName);

					if (world == null) {
						sender.sendMessage(Colors.Red + "Error: " + Colors.White + "The world \"" + worldName + "\" does not exist !");
						continue;
					}

					worldName = world.getName();

					// now we can check the world for the protection
					Block block = world.getBlockAt(protection.getX(), protection.getY(), protection.getZ());

					// remove protections not found in the world
					if (block == null || !lwc.isProtectable(block)) {
						lwc.getPhysicalDatabase().unregisterProtection(protection.getId());
						// sender.sendMessage(Colors.Green + "Found:" + block.getType() + ". Removed protection #" + protection.getId() + " located in the world " + worldName);
						sender.sendMessage("Removed (noexist): " + protection.toString());
						completed++;
					}

					// remove excess protections
					// (i.e, clean up the mess from conversions where most other protection plugins protected both chest blocks !!)
					else {
						List<Block> protectionSet = lwc.getProtectionSet(world, block.getX(), block.getY(), block.getZ());
						List<Protection> tmpProtections = new ArrayList<Protection>();

						for (Block protectableBlock : protectionSet) {
							if (!lwc.isProtectable(protectableBlock)) {
								continue;
							}

							// Protection tmp = lwc.getPhysicalDatabase().loadProtection(protectableBlock.getWorld().getName(), protectableBlock.getX(), protectableBlock.getY(), protectableBlock.getZ());
							List<Protection> tmp = getAll(protections, protectableBlock.getWorld().getName(), protectableBlock.getX(), protectableBlock.getY(), protectableBlock.getZ());
							tmpProtections.addAll(tmp);
						}

						if (tmpProtections.size() > 1) {
							int toRemove = tmpProtections.size() - 1;

							for (int i = 0; i < toRemove; i++) {
								Protection remove = tmpProtections.get(i);

								lwc.getPhysicalDatabase().unregisterProtection(remove.getId());
								sender.sendMessage("Removed (dupe): " + remove.toString());
								completed++;

								ignore.add(remove.getId());
							}
						}
					}

					iterator.remove();
				}
			} catch (Exception e) {
				sender.sendMessage("Uh-oh, something bad happened while cleaning up the LWC database!");
				e.printStackTrace();
			}

			long finish = System.currentTimeMillis();
			float timeInSeconds = (finish - start) / 1000.0f;

			sender.sendMessage(Colors.Red + "Done!");
			sender.sendMessage(String.format("LWC was successfully able to cleanup %s%d%s protections in %s%.2f%s seconds", Colors.Green, completed, Colors.White, Colors.Green, timeInSeconds, Colors.White));

			// commit the updates
			try {
				lwc.getPhysicalDatabase().getConnection().commit();
				lwc.getPhysicalDatabase().getConnection().setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		private List<Protection> getAll(List<Protection> protections, String world, int x, int y, int z) {
			List<Protection> tmp = new ArrayList<Protection>();

			for (Protection protection : protections) {
				if (protection.getWorld() != null && world != null && protection.getWorld().equals(world)) {
					if (protection.getX() == x && protection.getY() == y && protection.getZ() == z) {
						tmp.add(protection);
					}
				}
			}

			return tmp;
		}

	}

	@Override
	public void execute(LWC lwc, CommandSender sender, String[] args) {
		if (args.length < 2) {
			lwc.sendLocale(sender, "help.admin");
			return;
		}

		String action = args[1].toLowerCase();

		if (action.equals("report")) {
			Performance.setChestCount(lwc.getPhysicalDatabase().getProtectionCount());
			Performance.setPlayersOnline(lwc.getPlugin().getServer().getOnlinePlayers().length);

			for (String line : Performance.getGeneratedReport()) {
				sender.sendMessage(Colors.Green + line);
			}

			Performance.clear();
		}

		else if (action.equals("find")) {
			if (args.length < 3) {
				lwc.sendSimpleUsage(sender, "/lwc admin find <player> [page]");
				return;
			}

			final int perPage = 7; // listings per page

			String player = args[2];
			int page = 1;

			if (args.length > 3) {
				try {
					page = Integer.parseInt(args[3]);
				} catch (Exception e) {
					sender.sendMessage(Colors.Red + "Invalid page");
					return;
				}
			}

			int start = (page - 1) * perPage;

			List<Protection> protections = lwc.getPhysicalDatabase().loadProtectionsByPlayer(player, start, perPage);
			int results = lwc.getPhysicalDatabase().getProtectionCount(player);
			int max = protections.size(); // may not be the full perPage
			int ceil = start + max;

			sender.sendMessage(Colors.Red + "Page: " + Colors.White + page);

			if (results != max) {
				sender.sendMessage("Next page: " + Colors.Red + "/lwc admin find " + player + " " + (page + 1));
			}

			sender.sendMessage(Colors.Blue + "Showing " + Colors.Red + start + Colors.White + "-" + Colors.Red + ceil + Colors.White + " (total: " + Colors.Red + results + Colors.White + ")");
			sender.sendMessage("-----------------------------------------------------");

			for (Protection protection : protections) {
				sender.sendMessage(protection.toString());
			}
		}

		else if (action.equals("forceowner")) {
			if (args.length < 3) {
				lwc.sendSimpleUsage(sender, "/lwc admin forceowner <player>");
				return;
			}

			if (!(sender instanceof Player)) {
				sender.sendMessage(Colors.Red + "Only supported in-game!");
				return;
			}

			Player player = (Player) sender;
			String newOwner = args[2];

			lwc.getMemoryDatabase().registerAction("forceowner", player.getName(), newOwner);
			player.sendMessage(Colors.Blue + "Left click the protection to change the owner to: " + Colors.White + newOwner);
		}

		else if (action.equals("remove")) {
			if (args.length < 3) {
				lwc.sendSimpleUsage(sender, "/lwc admin remove <id>");
				return;
			}

			int protectionId;

			try {
				protectionId = Integer.parseInt(args[2]);
			} catch (Exception e) {
				sender.sendMessage(Colors.Red + "Invalid id");
				return;
			}

			// FIXME: when this returns bool, have it send appropriate message
			lwc.getPhysicalDatabase().unregisterProtection(protectionId);
			sender.sendMessage(Colors.Green + "Success");
		}

		else if (action.equals("view")) {
			// EXPERIMENTAL
			// USES DIRECT CRAFTBUKKIT CODE!

			if (!(sender instanceof Player)) {
				sender.sendMessage(Colors.Red + "Only supported in-game!");
				return;
			}

			Player player = (Player) sender;
			World world = player.getWorld();

			if (args.length < 3) {
				lwc.sendSimpleUsage(sender, "/lwc admin view <id>");
				return;
			}

			int protectionId = Integer.parseInt(args[2]);
			Protection protection = lwc.getPhysicalDatabase().loadProtection(protectionId);

			if (protection == null) {
				player.sendMessage("Does not exist !");
				return;
			}

			Block block = world.getBlockAt(protection.getX(), protection.getY(), protection.getZ());

			if (!(block.getState() instanceof ContainerBlock)) {
				player.sendMessage(Colors.Red + "That block doesn't have an inventory!");
				return;
			}

			net.minecraft.server.World handle = ((CraftWorld) block.getWorld()).getHandle();
			EntityHuman human = ((CraftHumanEntity) player).getHandle();
			int x = block.getX();
			int y = block.getY();
			int z = block.getZ();

			switch (block.getType()) {
			case CHEST:
				net.minecraft.server.Block.CHEST.a(handle, x, y, z, human);
				break;

			case FURNACE:
				net.minecraft.server.Block.FURNACE.a(handle, x, y, z, human);
				break;

			case DISPENSER:
				net.minecraft.server.Block.DISPENSER.a(handle, x, y, z, human);
				break;
			}

			/*
			 * // check for double chest Block doubleChest = lwc.findAdjacentBlock(block, Material.CHEST);
			 * 
			 * ContainerBlock containerBlock = (ContainerBlock) block.getState(); ContainerBlock containerBlock2 = doubleChest == null ? null : (ContainerBlock) doubleChest.getState();
			 * 
			 * // get the raw MC objects needed CraftPlayer craftPlayer = (CraftPlayer) player; net.minecraft.server.EntityPlayer entityPlayer = (net.minecraft.server.EntityPlayer) craftPlayer.getHandle(); int inventoryType = 0;
			 * 
			 * net.minecraft.server.IInventory blockInventory = ((CraftInventory) containerBlock.getInventory()).getInventory(); net.minecraft.server.IInventory blockInventory2 = containerBlock2 == null ? null : ((CraftInventory) containerBlock2.getInventory()).getInventory();
			 * 
			 * if (block.getType() == Material.FURNACE) { inventoryType = 2; } else if (block.getType() == Material.DISPENSER) { inventoryType = 3; }
			 * 
			 * // and now send the inventory switch (inventoryType) { case 0: // check for double chest, if it is, combine if (blockInventory2 != null) { blockInventory = new net.minecraft.server.InventoryLargeChest("LWC Administration", blockInventory, blockInventory2); }
			 * 
			 * ((CraftBlockState) containerBlock).
			 * 
			 * entityPlayer.a(blockInventory); break;
			 * 
			 * case 2: entityPlayer.a((net.minecraft.server.TileEntityFurnace) getInternalField(containerBlock, "furnace")); break;
			 * 
			 * case 3: entityPlayer.a((net.minecraft.server.TileEntityDispenser) getInternalField(containerBlock, "dispenser")); break; }
			 */

			player.sendMessage(Colors.Red + "Viewing inventory #" + Colors.Blue + protectionId);
		}

		else if (action.equals("locale")) {
			if (args.length < 3) {
				lwc.sendSimpleUsage(sender, "/lwc admin locale <key> [args]");
				return;
			}

			String locale = args[2];
			String[] localeArgs = new String[0];

			if (args.length > 3) {
				localeArgs = StringUtils.join(args, 3).split(" ");
			}

			if (localeArgs.length > 0) {
				lwc.sendLocale(sender, locale, (Object[]) localeArgs);
			} else {
				lwc.sendLocale(sender, locale);
			}
		}

		else if (action.equals("purge")) {
			if (args.length < 3) {
				lwc.sendSimpleUsage(sender, "/lwc admin purge <Players>");
				return;
			}

			String players = StringUtils.join(args, 2);

			for (String toRemove : players.split(" ")) {
				lwc.getPhysicalDatabase().unregisterProtectionByPlayer(toRemove);
				sender.sendMessage(Colors.Green + "Removed all protections created by " + Colors.Blue + toRemove);
			}
		}

		else if (action.equals("reload")) {
			Config.init();
			sender.sendMessage(Colors.Green + "Reloaded LWC config!");
		}

		else if (action.equals("version")) {
			sender.sendMessage("");
			sender.sendMessage(Colors.Red + "LWC version");
			sender.sendMessage("http://griefcraft.com");
			sender.sendMessage("");

			Updater updater = lwc.getPlugin().getUpdater();

			String pluginColor = Colors.Green;
			String updaterColor = Colors.Green;

			double currPluginVersion = LWCInfo.VERSION;
			double currSqlVersion = updater.getCurrentSQLiteVersion();

			// force a reload of the latest versions
			updater.loadVersions(false);
			
			double latestPluginVersion = updater.getLatestPluginVersion();
			double latestSqlVersion = updater.getLatestInternalVersion();

			if (latestPluginVersion > currPluginVersion) {
				pluginColor = Colors.Red;
			}

			if (latestSqlVersion > currSqlVersion) {
				pluginColor = Colors.Red;
			}

			sender.sendMessage(Colors.Blue + "Main plugin: " + pluginColor + LWCInfo.FULL_VERSION + Colors.Yellow + "/" + Colors.Green + latestPluginVersion);
			sender.sendMessage(Colors.Blue + "Internal: " + updaterColor + currSqlVersion + Colors.Yellow + "/" + Colors.Green + latestSqlVersion);

			sender.sendMessage("");
			sender.sendMessage(Colors.Green + "Green: Up to date");
			sender.sendMessage(Colors.Red + "Red: Out of date");
		}

		else if (action.equals("createjob")) {
			if (args.length < 5) {
				lwc.sendSimpleUsage(sender, "/lwc admin createjob <type> <owner> <payload>");
				return;
			}

			try {
				// admin createjob type owner payload

				int type = Integer.parseInt(args[2]);
				String owner = args[3];
				String payload = join(args, 4);

				lwc.getPhysicalDatabase().createJob(type, owner, payload);
				sender.sendMessage(Colors.Green + "Scheduled job");
			} catch (Exception e) {
				lwc.sendSimpleUsage(sender, "/lwc admin createjob <type> <owner> <payload>");
			}
		}

		else if (action.equals("flush")) {
			sender.sendMessage(Colors.Green + "Flushing Update Thread..");
			lwc.getUpdateThread().flush();
			sender.sendMessage(Colors.Green + "Done.");
		}

		else if (action.equals("cleanup")) {
			sender.sendMessage("You have: " + Colors.Green + lwc.getPhysicalDatabase().getProtectionCount() + Colors.White + " protections");
			sender.sendMessage(Colors.Red + "This may take a while depending on how many protections you have");

			// do the work in a seperate thread so we don't fully lock the
			// server
			new Thread(new Admin_Cleanup_Thread(lwc, sender)).start();
		}

		else if (action.equals("update")) {
			Updater updater = lwc.getPlugin().getUpdater();

			if (updater.checkDist()) {
				sender.sendMessage(Colors.Green + "Updated LWC successfully to version: " + updater.getLatestPluginVersion());
				sender.sendMessage(Colors.Green + "Please reload LWC to complete the update");
			} else {
				sender.sendMessage(Colors.Red + "No update found.");
			}
		}

		else if (action.equalsIgnoreCase("limits")) {
			if (args.length < 3) {
				lwc.sendSimpleUsage(sender, "/lwc admin limits <count> <Groups/Users>");
				sender.sendMessage(Colors.Red + "Note:" + Colors.White + " You can use " + Colors.LightBlue + "-global" + Colors.White + " in place of <Groups/Users> to allow it to apply to anyone without a protection limit.");
				return;
			}

			final int limit = Integer.parseInt(args[2]);

			for (int i = 3; i < args.length; i++) {
				String entity = args[i];
				int type = Limit.PLAYER;
				boolean isGroup = entity.startsWith("g:");

				if (isGroup) {
					entity = entity.substring(2);
					type = Limit.GROUP;
				}

				if (entity.equalsIgnoreCase("-global")) {
					type = Limit.GLOBAL;
				}

				if (type == Limit.GLOBAL) {
					lwc.getPhysicalDatabase().registerProtectionLimit(type, limit, "");
					sender.sendMessage(Colors.Green + "Registered global limit of " + Colors.Gold + limit + Colors.Green + " protections");
				} else if (limit != -2) {
					lwc.getPhysicalDatabase().registerProtectionLimit(type, limit, entity);
					sender.sendMessage(Colors.Green + "Registered limit of " + Colors.Gold + limit + Colors.Green + " protections to the " + (isGroup ? "group" : "user") + " " + Colors.Gold + entity);
				} else {
					lwc.getPhysicalDatabase().unregisterProtectionLimit(type, entity);
					sender.sendMessage(Colors.Green + "Unregistered limit for " + Colors.Gold + entity);
				}
			}
		}

		else if (action.equals("convert")) {
			if (args.length < 3) {
				lwc.sendSimpleUsage(sender, "/lwc admin convert <chestprotect|chastity>");
				return;
			}

			String pluginToConvert = args[1].toLowerCase();

			if (pluginToConvert.equals("chestprotect")) {
				new ChestProtect(sender);
			} else if (pluginToConvert.equals("chastity")) {
				new ChastityChest(sender);
			}
		}

		else if (action.equals("clear")) {
			if (args.length < 3) {
				lwc.sendSimpleUsage(sender, "/lwc admin clear <protections|limits|rights>");
				return;
			}

			String toClear = args[2].toLowerCase();

			if (toClear.equals("protections")) {
				lwc.getPhysicalDatabase().unregisterProtections();
				lwc.getPhysicalDatabase().unregisterProtectionRights();

				sender.sendMessage(Colors.Green + "Removed all protections + rights");
			} else if (toClear.equals("rights")) {
				lwc.getPhysicalDatabase().unregisterProtectionRights();

				sender.sendMessage(Colors.Green + "Removed all protection rights");
			} else if (toClear.equals("limits")) {
				lwc.getPhysicalDatabase().unregisterProtectionLimits();

				sender.sendMessage(Colors.Green + "Removed all protection limits");
			}
		}
	}

	@Override
	public String getName() {
		return "admin";
	}

	@Override
	public boolean supportsConsole() {
		return true;
	}

	@Override
	public boolean validate(LWC lwc, CommandSender player, String[] args) {
		return lwc.isAdmin(player) && (hasFlag(args, "a") || hasFlag(args, "admin"));
	}

	/**
	 * Reflect an internal/private field
	 * 
	 * @param object
	 * @param name
	 * @return
	 */
	private Object getInternalField(Object object, String name) {
		try {
			Field field = object.getClass().getDeclaredField(name);
			field.setAccessible(true);

			return field.get(object);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

}
