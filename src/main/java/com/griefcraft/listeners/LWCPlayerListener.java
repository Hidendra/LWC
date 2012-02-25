/*
 * Copyright 2011 Tyler Blair. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and contributors and should not be interpreted as representing official policies,
 * either expressed or implied, of anybody else.
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
import com.griefcraft.util.Colors;
import com.griefcraft.util.StopWatch;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.ContainerBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;

public class LWCPlayerListener implements Listener {

    /**
     * The plugin instance
     */
    private LWCPlugin plugin;

    public LWCPlayerListener(LWCPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (event.isCancelled() || !LWC.ENABLED) {
            return;
        }

        Player player = event.getPlayer();

        LWCDropItemEvent evt = new LWCDropItemEvent(player, event);
        plugin.getLWC().getModuleLoader().dispatchEvent(evt);

        if (evt.isCancelled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        if (event.isCancelled() || !LWC.ENABLED) {
            return;
        }

        LWC lwc = plugin.getLWC();
        if (!lwc.getConfiguration().getBoolean("core.filterunlock", true)) {
            return;
        }

        // We want to block messages starting with cunlock incase someone screws up /cunlock password.
        String message = event.getMessage();

        if (message.startsWith("cunlock") || message.startsWith("lcunlock") || message.startsWith(".cunlock")) {
            event.setCancelled(true);
            lwc.sendLocale(event.getPlayer(), "lwc.blockedmessage");
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!LWC.ENABLED) {
            return;
        }

        LWC lwc = plugin.getLWC();
        Player player = event.getPlayer();
        LWCPlayer lwcPlayer = lwc.wrapPlayer(player);

        // Send the block action to the player if they're in dev mode
        lwcPlayer.debug("Block Action: " + event.getAction());

        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        Material material = block.getType();

        // Timing start
        StopWatch stopWatch = new StopWatch(String.format("PLAYER_INTERACT Block=%s Location=[%d %d %d]", material, block.getX(), block.getY(), block.getZ()));
        stopWatch.start();

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
            Protection protection = lwc.findProtection(block, lwcPlayer);
            Module.Result result;
            boolean canAccess = lwc.canAccessProtection(player, protection);
            boolean canAdmin = lwc.canAdminProtection(player, protection);

            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                boolean ignoreLeftClick = Boolean.parseBoolean(lwc.resolveProtectionConfiguration(material, "ignoreLeftClick"));

                if (ignoreLeftClick) {
                    lwcPlayer.debug("ignoreLeftClick!");
                    lwc.completeStopwatch(stopWatch, player);
                    return;
                }
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                boolean ignoreRightClick = Boolean.parseBoolean(lwc.resolveProtectionConfiguration(material, "ignoreRightClick"));

                if (ignoreRightClick) {
                    lwcPlayer.debug("ignoreRightClick!");
                    lwc.completeStopwatch(stopWatch, player);
                    return;
                }
            }

            // If the event was cancelled and they have an action, warn them
            if (event.isCancelled()) {
                int actionCount = lwcPlayer.getActions().size();
                boolean hasInteracted = lwcPlayer.hasAction("interacted");

                // only send it if a non-"interacted" action is set which is always set on the player
                if ((hasInteracted && actionCount > 1) || (!hasInteracted && actionCount > 0)) {
                    lwc.sendLocale(player, "lwc.pendingaction");
                }

                // it's cancelled, do not continue !
                return;
            }

            // register in an action what protection they interacted with (if applicable.)
            if (protection != null) {
                com.griefcraft.model.Action action = new com.griefcraft.model.Action();
                action.setName("interacted");
                action.setPlayer(lwcPlayer);
                action.setProtection(protection);

                lwcPlayer.addAction(action);
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

            // optional.onlyProtectIfOwnerIsOnline
            if (protection != null && !canAccess && lwc.getConfiguration().getBoolean("optional.onlyProtectWhenOwnerIsOnline", false)) {
                Player owner = protection.getBukkitOwner();

                // If they aren't online, allow them in :P
                if (owner == null || !owner.isOnline()) {
                    return;
                }
            }

            // optional.onlyProtectIfOwnerIsOffline
            if (protection != null && !canAccess && lwc.getConfiguration().getBoolean("optional.onlyProtectWhenOwnerIsOffline", false)) {
                Player owner = protection.getBukkitOwner();

                // If they aren't online, allow them in :P
                if (owner != null && owner.isOnline()) {
                    return;
                }
            }

            // debugging
            lwcPlayer.debug(Colors.LightBlue + "Result => " + Colors.Yellow + result + Colors.LightBlue + "  Protection => " + Colors.Yellow + protection + Colors.LightBlue + "  Access/Admin => " + Colors.Yellow + canAccess + "/" + canAdmin + Colors.LightBlue + "  Actions => " + Colors.Yellow + actions);

            if (result == Module.Result.ALLOW) {
                lwc.completeStopwatch(stopWatch, player);
                return;
            }

            if (result == Module.Result.DEFAULT) {
                canAccess = lwc.enforceAccess(player, protection, block);
            }

            // Fix a bug where pre-1.8 chests were flipped directions in 1.8
            if (canAccess && block.getType() == Material.CHEST && block.getData() == 0) {
                lwc.adjustChestDirection(block, event.getBlockFace());
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

        lwc.completeStopwatch(stopWatch, player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!LWC.ENABLED) {
            return;
        }

        // remove the place from the player cache and reset anything they can access
        LWCPlayer.removePlayer(event.getPlayer());
    }

}
