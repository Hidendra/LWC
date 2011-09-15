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

import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.model.LWCPlayer;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.Module;
import com.griefcraft.scripting.event.LWCBlockInteractEvent;
import com.griefcraft.scripting.event.LWCDropItemEvent;
import com.griefcraft.scripting.event.LWCProtectionInteractEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.ContainerBlock;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

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
        if (!LWC.ENABLED) {
            return;
        }

        Player player = event.getPlayer();
        Item item = event.getItemDrop();
        ItemStack itemStack = item.getItemStack();

        LWCDropItemEvent evt = new LWCDropItemEvent(player, event);
        plugin.getLWC().getModuleLoader().dispatchEvent(evt);

        if (evt.isCancelled()) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onPlayerChat(PlayerChatEvent event) {
        if (!plugin.getLWC().getConfiguration().getBoolean("core.filterunlock", true)) {
            return;
        }

        /**
         * We want to block messages starting with cunlock incase someone screws up /cunlock password.
         */
        String message = event.getMessage();

        if (message.startsWith("cunlock")) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (!LWC.ENABLED) {
            return;
        }

        LWC lwc = plugin.getLWC();
        Player player = event.getPlayer();
        LWCPlayer lwcPlayer = lwc.wrapPlayer(player);
        Block clickedBlock = event.getClickedBlock();
        Location location = clickedBlock.getLocation();

        CraftWorld craftWorld = (CraftWorld) clickedBlock.getWorld();
        Block block = new CraftBlock((CraftChunk) craftWorld.getChunkAt(location), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        block.setTypeId(craftWorld.getBlockTypeIdAt(location));

        Material material = block.getType();

        // Prevent players with lwc.deny from interacting
        if (block.getState() instanceof ContainerBlock) {
            if (!lwc.hasPermission(player, "lwc.protect") && lwc.hasPermission(player, "lwc.deny") && !lwc.isAdmin(player) && !lwc.isMod(player)) {
                lwc.sendLocale(player, "protection.interact.error.blocked");
                event.setCancelled(true);
                return;
            }
        }

        try {
            List<String> actions = new ArrayList<String>(lwcPlayer.getActionNames());
            Protection protection = lwc.findProtection(block);
            Module.Result result = Module.Result.CANCEL;
            boolean canAccess = lwc.canAccessProtection(player, protection);
            boolean canAdmin = lwc.canAdminProtection(player, protection);

            // register in an action what protection they interacted with (if applicable.)
            if (protection != null) {
                com.griefcraft.model.Action action = new com.griefcraft.model.Action();
                action.setName("interacted");
                action.setPlayer(lwcPlayer);
                action.setProtection(protection);

                lwcPlayer.addAction(action);
            }

            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                boolean ignoreLeftClick = Boolean.parseBoolean(lwc.resolveProtectionConfiguration(material, "ignoreLeftClick"));

                if (ignoreLeftClick) {
                    return;
                }
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                boolean ignoreRightClick = Boolean.parseBoolean(lwc.resolveProtectionConfiguration(material, "ignoreRightClick"));

                if (ignoreRightClick) {
                    return;
                }
            }

            if (protection != null) {
                LWCProtectionInteractEvent evt = new LWCProtectionInteractEvent(event, protection, actions, canAccess, canAdmin);
                lwc.getModuleLoader().dispatchEvent(evt);

                result = evt.getResult();
            } else {
                LWCBlockInteractEvent evt = new LWCBlockInteractEvent(event, block, actions);
                lwc.getModuleLoader().dispatchEvent(evt);

                result = evt.getResult();
            }

            if (result == Module.Result.ALLOW) {
                return;
            }

            if (result == Module.Result.DEFAULT) {
                lwc.enforceAccess(player, protection != null ? protection.getBlock() : block);
            }

            if (!canAccess || result == Module.Result.CANCEL) {
                event.setCancelled(true);
                event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
            }
        } catch (Exception e) {
            event.setCancelled(true);
            event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
            lwc.sendLocale(player, "protection.internalerror", "id", "PLAYER_INTERACT");
            e.printStackTrace();
        }
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!LWC.ENABLED) {
            return;
        }

        LWCPlayer player = LWC.getInstance().wrapPlayer(event.getPlayer());
        player.removeAllAccessibleProtections();
        player.removeAllActions();
        player.disableAllModes();
    }

}
