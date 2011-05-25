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

package com.griefcraft.listeners;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.ContainerBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.model.Protection;
import com.griefcraft.util.Colors;

public class LWCPlayerListener extends PlayerListener {

	/**
	 * The plugin instance
	 */
	private LWCPlugin plugin;

	public LWCPlayerListener(LWCPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		LWC lwc = plugin.getLWC();
		Player player = event.getPlayer();
		Item droppedItem = event.getItemDrop();
		ItemStack itemStack = droppedItem.getItemStack();

		int protectionId = lwc.getPlayerDropTransferTarget(player.getName());

		if (protectionId == -1) {
			return;
		}

		if (!lwc.isPlayerDropTransferring(player.getName())) {
			return;
		}

		Protection protection = lwc.getPhysicalDatabase().loadProtection(protectionId);

		if (protection == null) {
			player.sendMessage(Colors.Red + "Protection no longer exists");
			lwc.getMemoryDatabase().unregisterMode(player.getName(), "dropTransfer");
			return;
		}

		// load the world and the inventory
		World world = player.getServer().getWorld(protection.getWorld());

		if (world == null) {
			player.sendMessage(Colors.Red + "Invalid world!");
			lwc.getMemoryDatabase().unregisterMode(player.getName(), "dropTransfer");
			return;
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
			droppedItem.remove();
		}

	}

	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		LWC lwc = plugin.getLWC();
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();

		/*
		 * Prevent players with lwc.blockinventories from opening inventories
		 */
		if (block.getState() instanceof ContainerBlock) {
			if (lwc.getPermissions() != null && !lwc.getPermissions().permission(player, "lwc.protect") && lwc.getPermissions().permission(player, "lwc.blockinventory") && !lwc.isAdmin(player) && !lwc.isMod(player)) {
				lwc.sendLocale(player, "protection.interact.error.blocked");
				event.setCancelled(true);
				return;
			}
		}

		boolean canAccess = lwc.enforceAccess(player, block);

		if (!canAccess) {
			event.setCancelled(true);
			event.setUseInteractedBlock(Result.DENY);
		}
	}

	@Override
	public void onPlayerQuit(PlayerQuitEvent event) {
		LWC lwc = plugin.getLWC();
		String player = event.getPlayer().getName();

		lwc.getMemoryDatabase().unregisterPlayer(player);
		lwc.getMemoryDatabase().unregisterUnlock(player);
		lwc.getMemoryDatabase().unregisterPendingLock(player);
		lwc.getMemoryDatabase().unregisterAllActions(player);
		lwc.getMemoryDatabase().unregisterAllModes(player);
	}

}
