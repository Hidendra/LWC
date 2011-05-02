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

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.ContainerBlock;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.command.ColouredConsoleSender;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.griefcraft.converters.ChastityChest;
import com.griefcraft.converters.ChestProtect;
import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCInfo;
import com.griefcraft.model.Limit;
import com.griefcraft.model.Protection;
import com.griefcraft.util.Colors;
import com.griefcraft.util.Config;
import com.griefcraft.util.ConfigValues;
import com.griefcraft.util.Performance;
import com.griefcraft.util.StringUtils;
import com.griefcraft.util.Updater;

public class Admin implements ICommand {

	/**
	 * Class that handles cleaning up the LWC database usage: /lwc admin cleanup
	 */
	private static class Admin_Cleanup_Thread implements Runnable {

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
						lwc.sendLocale(sender, "protection.admin.cleanup.noworld", "world", worldName);
						continue;
					}

					worldName = world.getName();

					// now we can check the world for the protection
					Block block = world.getBlockAt(protection.getX(), protection.getY(), protection.getZ());

					// remove protections not found in the world
					if (block == null || !lwc.isProtectable(block)) {
						protection.remove();
						// sender.sendMessage(Colors.Green + "Found:" + block.getType() + ". Removed protection #" + protection.getId() + " located in the world " + worldName);
						lwc.sendLocale(sender, "protection.admin.cleanup.removednoexist", "protection", protection.toString());
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

								protection.remove();
								lwc.sendLocale(sender, "protection.admin.cleanup.removeddupe", "protection", remove.toString());
								completed++;

								ignore.add(remove.getId());
							}
						}
					}

					iterator.remove();
				}
			} catch (Exception e) {
				sender.sendMessage("Uh-oh, something bad happened while cleaning up the LWC database!");
				lwc.sendLocale(sender, "protection.internalerror", "id", "cleanup");
				e.printStackTrace();
			}

			long finish = System.currentTimeMillis();
			float timeInSeconds = (finish - start) / 1000.0f;

			lwc.sendLocale(sender, "protection.admin.cleanup.complete", "count", completed, "seconds", timeInSeconds);
			
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
			ColouredConsoleSender console = null;
			boolean replaceTabs = false;
			
			if(sender instanceof Player) {
				console = new ColouredConsoleSender((CraftServer) Bukkit.getServer());
				replaceTabs = true;
			}
			
			for (String line : Performance.generateReport()) {
				line = Colors.Green + line;
				
				sender.sendMessage(replaceTabs ? line.replaceAll("\\t", " ") : line);
				
				if(console != null) {
					console.sendMessage(line);
				}
			}
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
					lwc.sendLocale(sender, "protection.find.invalidpage");
					return;
				}
			}

			int start = (page - 1) * perPage;

			List<Protection> protections = lwc.getPhysicalDatabase().loadProtectionsByPlayer(player, start, perPage);
			int results = lwc.getPhysicalDatabase().getProtectionCount(player);
			int max = protections.size(); // may not be the full perPage
			int ceil = start + max;

			lwc.sendLocale(sender, "protection.find.currentpage", "page", page);

			if (results != max) {
				lwc.sendLocale(sender, "protection.find.nextpage", "player", player, "page", page + 1);
			}

			lwc.sendLocale(sender, "protection.find.showing", "start", start, "ceil", ceil, "results", results);

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
				lwc.sendLocale(sender, "protection.admin.noconsole");
				return;
			}

			Player player = (Player) sender;
			String newOwner = args[2];

			lwc.getMemoryDatabase().registerAction("forceowner", player.getName(), newOwner);
			lwc.sendLocale(sender, "protection.admin.forceowner.finalize", "player", newOwner);
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
				lwc.sendLocale(sender, "protection.admin.remove.invalidid");
				return;
			}

			// FIXME: when this returns bool, have it send appropriate message
			lwc.getPhysicalDatabase().unregisterProtection(protectionId);
			lwc.sendLocale(sender, "protection.admin.remove.finalize");
		}

		else if (action.equals("view")) {
			// EXPERIMENTAL
			// USES DIRECT CRAFTBUKKIT CODE!

			if (!(sender instanceof Player)) {
				lwc.sendLocale(sender, "protection.admin.noconsole");
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
				lwc.sendLocale(sender, "protection.admin.view.noexist");
				return;
			}

			Block block = world.getBlockAt(protection.getX(), protection.getY(), protection.getZ());

			if (!(block.getState() instanceof ContainerBlock)) {
				lwc.sendLocale(sender, "protection.admin.view.noinventory");
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

			lwc.sendLocale(sender, "protection.admin.view.viewing", "id", protectionId);
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
				lwc.sendLocale(sender, "protection.admin.purge.finalize", "player", toRemove);
			}
		}

		// FIXME: reload locales
		else if (action.equals("reload")) {
			Plugin plugin = lwc.getPlugin();
			plugin.getPluginLoader().disablePlugin(plugin);
			plugin.getPluginLoader().enablePlugin(plugin);

			lwc.sendLocale(sender, "protection.admin.reload.finalize");
		}

		else if (action.equals("version")) {
			Updater updater = lwc.getPlugin().getUpdater();

			String pluginColor = Colors.Green;

			double currPluginVersion = LWCInfo.VERSION;

			// force a reload of the latest versions
			updater.loadVersions(false);
			
			double latestPluginVersion = updater.getLatestPluginVersion();

			if (latestPluginVersion > currPluginVersion) {
				pluginColor = Colors.Red;
			}
			
			lwc.sendLocale(sender, "protection.admin.version.finalize", "plugin_color", pluginColor, "plugin_version", LWCInfo.FULL_VERSION, "latest_plugin", latestPluginVersion);
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
			lwc.sendLocale(sender, "protection.admin.cleanup.start", "count", lwc.getPhysicalDatabase().getProtectionCount());
			
			// do the work in a seperate thread so we don't fully lock the server
			// new Thread(new Admin_Cleanup_Thread(lwc, sender)).start();
			new Admin_Cleanup_Thread(lwc, sender).run();
		}

		else if (action.equals("update")) {
			Updater updater = lwc.getPlugin().getUpdater();

			if (updater.checkDist()) {
				lwc.sendLocale(sender, "protection.admin.update.updated", "version", updater.getLatestPluginVersion());
			} else {
				lwc.sendLocale(sender, "protection.admin.update.noupdate");
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
					lwc.sendLocale(sender, "protection.admin.limit.global", "limit", limit);
					sender.sendMessage(Colors.Green + "Registered global limit of " + Colors.Gold + limit + Colors.Green + " protections");
				} else if (limit != -2) {
					String localeChild = isGroup ? "group" : "player";
					
					lwc.getPhysicalDatabase().registerProtectionLimit(type, limit, entity);
					lwc.sendLocale(sender, "protection.admin.limit." + localeChild, "limit", limit, "name", entity);
				} else {
					lwc.getPhysicalDatabase().unregisterProtectionLimit(type, entity);
					lwc.sendLocale(sender, "protection.admin.limit.remove", "name", entity);
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
			} else if (toClear.equals("rights")) {
				lwc.getPhysicalDatabase().unregisterProtectionRights();
			} else if (toClear.equals("limits")) {
				lwc.getPhysicalDatabase().unregisterProtectionLimits();
			}
			
			lwc.sendLocale(sender, "protection.admin.clear." + toClear);
		}
		
		else if (action.equalsIgnoreCase("getlimits")) {
			if (args.length < 3) {
				lwc.sendSimpleUsage(sender, "/lwc -admin getlimits <Groups/Users>");
				return;
			} 
			
			for (int i = 2; i < args.length; i++) {
				String entity = args[i];
				int type = Limit.PLAYER;
				int used = -1;
				boolean isGroup = entity.startsWith("g:");
				String displayQuota = "Unlimited";
				
				if (isGroup) {
					entity = entity.substring(2);
					type = Limit.GROUP;
				}

				int quota = lwc.getPhysicalDatabase().getLimit(type, entity);
				
				if(!isGroup) {
					used = lwc.getPhysicalDatabase().getProtectionCount(entity);
					quota = lwc.getProtectionLimits("", entity);
				}
				
				if(quota != -1) {
					displayQuota = quota + "";
				}
				
				if(isGroup) {
					lwc.sendLocale(sender, "protection.getlimits.group", "name", entity, "quota", displayQuota);
				} else {
					lwc.sendLocale(sender, "protection.getlimits.player", "name", entity, "used", used, "quota", displayQuota);
				}
			}
		}
		
		else if (action.equalsIgnoreCase("config")) {
			if (args.length < 4) {
				lwc.sendSimpleUsage(sender, "/lwc admin config <key> <value>");
				return;
			}
			
			String key = args[2];
			String value = args[3];
			
			Config.getInstance().setProperty(key, value);
			sender.sendMessage(key + "->" + value);
		}
		
		else if (action.equalsIgnoreCase("cache")) {
			if(args.length > 2) {
				String cmd = args[2].toLowerCase();
				
				if(cmd.equals("clear")) {
					lwc.getCaches().getProtections().clear();
					sender.sendMessage(Colors.Green + "Caches cleared.");
				}
				
				return;
			}

			int size = lwc.getCaches().getProtections().size();
			int max = ConfigValues.CACHE_SIZE.getInt();
			
			sender.sendMessage(Colors.Green + size + Colors.Yellow + "/" + Colors.Green + max);
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
