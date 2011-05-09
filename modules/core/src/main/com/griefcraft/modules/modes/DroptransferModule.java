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

package com.griefcraft.modules.modes;

import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.ContainerBlock;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.util.Colors;
import com.griefcraft.util.StringUtils;

public class DroptransferModule extends JavaModule {

	/**
	 * Check if the player is currently drop transferring
	 * 
	 * @param player
	 * @return
	 */
	public boolean isPlayerDropTransferring(LWC lwc, String player) {
		return lwc.getMemoryDatabase().hasMode(player, "+dropTransfer");
	}

	@Override
	public Result onDropItem(LWC lwc, Player player, Item item, ItemStack itemStack) {
		int protectionId = lwc.getPlayerDropTransferTarget(player.getName());

		if (protectionId == -1) {
			return DEFAULT;
		}

		if (!isPlayerDropTransferring(lwc, player.getName())) {
			return DEFAULT;
		}

		Protection protection = lwc.getPhysicalDatabase().loadProtection(protectionId);

		if (protection == null) {
			player.sendMessage(Colors.Red + "Protection no longer exists");
			lwc.getMemoryDatabase().unregisterMode(player.getName(), "dropTransfer");
			return DEFAULT;
		}

		// load the world and the inventory
		World world = player.getServer().getWorld(protection.getWorld());

		if (world == null) {
			player.sendMessage(Colors.Red + "Invalid world!");
			lwc.getMemoryDatabase().unregisterMode(player.getName(), "dropTransfer");
			return DEFAULT;
		}

		Block block = world.getBlockAt(protection.getX(), protection.getY(), protection.getZ());
		BlockState blockState = null;

		if ((blockState = block.getState()) != null && (blockState instanceof ContainerBlock)) {
			Block doubleChestBlock = lwc.findAdjacentBlock(block, Material.CHEST);
			ContainerBlock containerBlock = (ContainerBlock) blockState;

			Map<Integer, ItemStack> remaining = containerBlock.getInventory().addItem(itemStack);

			// we have remainders, deal with it
			if (remaining.size() > 0) {
				int key = remaining.keySet().iterator().next();
				ItemStack remainingItemStack = remaining.get(key);

				// is it a double chest ?????
				if (doubleChestBlock != null) {
					ContainerBlock containerBlock2 = (ContainerBlock) doubleChestBlock.getState();
					remaining = containerBlock2.getInventory().addItem(remainingItemStack);
				}

				// recheck remaining in the event of double chest being used
				if (remaining.size() > 0) {
					key = remaining.keySet().iterator().next();
					remainingItemStack = remaining.get(key);

					player.sendMessage(Colors.Red + "Oh no! " + Colors.White + "Your chest is full! Have your items back.");
					player.getInventory().addItem(remainingItemStack);
				}
			}

			player.updateInventory(); // if they're in the chest and dropping
										// items, this is required
			item.remove();
		}
		
		return DEFAULT;
	}
	
	@Override
	public Result onProtectionInteract(LWC lwc, Player player, Protection protection, List<String> actions, boolean canAccess, boolean canAdmin) {
		if(!actions.contains("dropTransferSelect")) {
			return DEFAULT;
		}
		
		if (!canAccess) {
			lwc.sendLocale(player, "protection.interact.dropxfer.noaccess");
		} else {
			if (protection.getBlockId() != Material.CHEST.getId()) {
				lwc.sendLocale(player, "protection.interact.dropxfer.notchest");
				lwc.getMemoryDatabase().unregisterAllActions(player.getName());
				return CANCEL;
			}

			lwc.getMemoryDatabase().registerMode(player.getName(), "dropTransfer", protection.getId() + "");
			lwc.getMemoryDatabase().registerMode(player.getName(), "+dropTransfer");
			lwc.sendLocale(player, "protection.interact.dropxfer.finalize");
		}
		
		lwc.getMemoryDatabase().unregisterAllActions(player.getName()); // ignore the persist mode
		return DEFAULT;
	}
	
	@Override
	public Result onBlockInteract(LWC lwc, Player player, Block block, List<String> actions) {
		if(!actions.contains("dropTransferSelect")) {
			return DEFAULT;
		}
		
		lwc.sendLocale(player, "protection.interact.dropxfer.notprotected");
		lwc.getMemoryDatabase().unregisterAllActions(player.getName());
		
		return DEFAULT;
	}
	
	@Override
	public Result onCommand(LWC lwc, CommandSender sender, String command, String[] args) {
		if(!StringUtils.hasFlag(command, "p") && !StringUtils.hasFlag(command, "mode")) {
			return DEFAULT;
		}
		
		if (args.length == 0) {
			lwc.sendSimpleUsage(sender, "/lwc mode <mode>");
			return CANCEL;
		}

		String mode = args[0];
		Player player = (Player) sender;
		
		if(!mode.equalsIgnoreCase("droptransfer")) {
			return DEFAULT;
		}
		
		if(!lwc.isModeWhitelisted(player, mode)) {
			if (!lwc.isAdmin(sender) && lwc.isModeEnabled(mode)) {
				lwc.sendLocale(player, "protection.modes.disabled");
				return CANCEL;
			}
		}

		// internal name
		mode = "dropTransfer";

		if (args.length < 2) {
			lwc.sendLocale(player, "protection.modes.dropxfer.help");
			return CANCEL;
		}

		String action = args[1].toLowerCase();
		String playerName = player.getName();

		if (action.equals("select")) {
			if (isPlayerDropTransferring(lwc, playerName)) {
				lwc.sendLocale(player, "protection.modes.dropxfer.select.error");
				return CANCEL;
			}

			lwc.getMemoryDatabase().unregisterMode(playerName, mode);
			lwc.getMemoryDatabase().registerAction("dropTransferSelect", playerName, "");

			lwc.sendLocale(player, "protection.modes.dropxfer.select.finalize");
		} else if (action.equals("on")) {
			int target = lwc.getPlayerDropTransferTarget(playerName);

			if (target == -1) {
				lwc.sendLocale(player, "protection.modes.dropxfer.selectchest");
				return CANCEL;
			}

			lwc.getMemoryDatabase().registerMode(playerName, "+dropTransfer");
			lwc.sendLocale(player, "protection.modes.dropxfer.on.finalize");
		} else if (action.equals("off")) {
			int target = lwc.getPlayerDropTransferTarget(playerName);

			if (target == -1) {
				lwc.sendLocale(player, "protection.modes.dropxfer.selectchest");
				return CANCEL;
			}

			lwc.getMemoryDatabase().unregisterMode(playerName, "+dropTransfer");
			lwc.sendLocale(player, "protection.modes.dropxfer.off.finalize");
		} else if (action.equals("status")) {
			if (lwc.getPlayerDropTransferTarget(playerName) == -1) {
				lwc.sendLocale(player, "protection.modes.dropxfer.status.off");
			} else {
				if (isPlayerDropTransferring(lwc, playerName)) {
					lwc.sendLocale(player, "protection.modes.dropxfer.status.active");
				} else {
					lwc.sendLocale(player, "protection.modes.dropxfer.status.inactive");
				}
			}
		}
		
		return CANCEL;
	}
	
}
