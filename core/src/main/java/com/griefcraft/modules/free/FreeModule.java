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

package com.griefcraft.modules.free;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Action;
import com.griefcraft.model.ConfirmAction;
import com.griefcraft.model.History;
import com.griefcraft.model.LWCPlayer;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCBlockInteractEvent;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.scripting.event.LWCProtectionDestroyEvent;
import com.griefcraft.scripting.event.LWCProtectionInteractEvent;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FreeModule extends JavaModule {

    @Override
    public void onProtectionInteract(LWCProtectionInteractEvent event) {
        if (event.getResult() != Result.DEFAULT) {
            return;
        }

        if (!event.hasAction("free")) {
            return;
        }

        LWC lwc = event.getLWC();
        Protection protection = event.getProtection();
        Player player = event.getPlayer();
        event.setResult(Result.CANCEL);

        if (!lwc.isAdmin(player) && Boolean.parseBoolean(lwc.resolveProtectionConfiguration(protection.getBlock(), "readonly-remove"))) {
            lwc.sendLocale(player, "protection.accessdenied");
            return;
        }

        if (lwc.hasAdminPermission(player, "lwc.admin.remove") || protection.isOwner(player)) {
            LWCProtectionDestroyEvent evt = new LWCProtectionDestroyEvent(player, protection, LWCProtectionDestroyEvent.Method.COMMAND, true, true);
            lwc.getModuleLoader().dispatchEvent(evt);

            if (!evt.isCancelled()) {
                // bind the player of destroyed the protection
                // We don't need to save the history we modify because it will be saved anyway immediately after this
                for (History history : protection.getRelatedHistory(History.Type.TRANSACTION)) {
                    if (history.getStatus() != History.Status.ACTIVE) {
                        continue;
                    }

                    history.addMetaData("destroyer=" + player.getName());
                    history.addMetaData("destroyerTime=" + System.currentTimeMillis() / 1000L);
                }

                protection.remove();
                lwc.sendLocale(player, "protection.interact.remove.finalize", "block", LWC.materialToString(protection.getBlockId()));
            }

            lwc.removeModes(player);
        } else {
            lwc.sendLocale(player, "protection.interact.error.notowner", "block", LWC.materialToString(protection.getBlockId()));
            lwc.removeModes(player);
        }
    }

    @Override
    public void onBlockInteract(LWCBlockInteractEvent event) {
        if (!event.hasAction("free")) {
            return;
        }

        LWC lwc = event.getLWC();
        Block block = event.getBlock();
        Player player = event.getPlayer();
        event.setResult(Result.CANCEL);

        lwc.sendLocale(player, "protection.interact.error.notregistered", "block", LWC.materialToString(block));
        lwc.removeModes(player);
    }

    @Override
    public void onCommand(LWCCommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!event.hasFlag("r", "remove")) {
            return;
        }

        final LWC lwc = event.getLWC();
        CommandSender sender = event.getSender();
        String[] args = event.getArgs();

        if (!(sender instanceof Player)) {
            return;
        }

        event.setCancelled(true);

        if (!lwc.hasPlayerPermission(sender, "lwc.remove")) {
            lwc.sendLocale(sender, "protection.accessdenied");
            return;
        }

        if (args.length < 1) {
            lwc.sendSimpleUsage(sender, "/lwc -r <protection|modes>");
            return;
        }

        String type = args[0].toLowerCase();
        final LWCPlayer player = lwc.wrapPlayer(sender);

        if (type.equals("protection") || type.equals("chest") || type.equals("furnace") || type.equals("dispenser")) {
            Action action = new Action();
            action.setName("free");
            action.setPlayer(player);

            player.removeAllActions();
            player.addAction(action);

            lwc.sendLocale(sender, "protection.remove.protection.finalize");
        } else if (type.equals("modes")) {
            player.disableAllModes();
            lwc.sendLocale(sender, "protection.remove.modes.finalize");
        } else if (type.equals("allprotections")) {
            // Prompt them for /lwc confirm
            lwc.sendLocale(player, "lwc.remove.allprotections");

            // our callback (remove all of their protections :p)
            Runnable callback = new Runnable() {
                public void run() {
                    // Get all of the player's protections
                    for (Protection protection : lwc.getPhysicalDatabase().loadProtectionsByPlayer(player.getName())) {
                        // Remove the protection
                        protection.remove();
                    }

                    // Notify them
                    lwc.sendLocale(player, "lwc.remove.allprotections.success");
                }
            };

            // Create the action
            Action action = new ConfirmAction(callback);
            action.setPlayer(player);

            // bind it to the player
            player.addAction(action);
        } else {
            lwc.sendSimpleUsage(sender, "/lwc -r <protection|allprotections|modes>");
        }

    }

}
