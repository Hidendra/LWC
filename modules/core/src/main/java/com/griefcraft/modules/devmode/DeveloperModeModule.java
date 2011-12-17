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

package com.griefcraft.modules.devmode;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Action;
import com.griefcraft.model.ConfirmAction;
import com.griefcraft.model.LWCPlayer;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.util.Colors;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.WeakHashMap;

public class DeveloperModeModule extends JavaModule {

    /**
     * CommandSender, PlayerName
     */
    private final Map<CommandSender, String> pending = new WeakHashMap<CommandSender, String>();

    @Override
    public void onCommand(LWCCommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!event.hasFlag("dev", "developer")) {
            return;
        }

        final LWC lwc = event.getLWC();
        final CommandSender sender = event.getSender();
        LWCPlayer lwcPlayer = lwc.wrapPlayer(sender);
        String[] args = event.getArgs();

        event.setCancelled(true);

        // Make sure they have access to this command
        if (!lwc.isAdmin(sender) && (lwcPlayer != null && !lwcPlayer.isDevMode())) {
            lwc.sendLocale(sender, "protection.accessdenied");
            return;
        }

        // Commands for the dev/devmode command
        if (event.hasFlag("dev", "developer")) {

            if (args.length == 0) {
                lwc.sendSimpleUsage(sender, "/lwc dev <Command>");
                return;
            }

            // The command they want to use
            Player player = null;
            String command = args[0].toLowerCase();

            if (!command.equals("enable") && !command.equals("disable")) {
                // Only in-game char, please..
                if (lwcPlayer == null) {
                    lwc.sendLocale(sender, "lwc.onlyrealplayers");
                    return;
                }

                // It is a player !
                player = (Player) sender;
            }

            // Enable dev mode on a player
            // Only LWC admins can enable dev mode
            if (command.equals("enable")) {
                // Don't let players in dev mode enable it on other players
                if (lwcPlayer != null && lwcPlayer.isDevMode()) {
                    lwc.sendLocale(sender, "protection.accessdenied");
                    return;
                }

                if (args.length < 2) {
                    lwc.sendSimpleUsage(sender, "/lwc dev enable <PlayerName>");
                    return;
                }

                // Search for the player
                String playerName = args[1];
                Player temp = lwc.getPlugin().getServer().getPlayer(playerName);

                if (temp == null) {
                    lwc.sendLocale(sender, "lwc.playernotfound");
                    return;
                }

                // ok, wait for them to confirm
                pending.put(sender, playerName);
                lwc.sendLocale(sender, "lwc.devmode.warning", "player", temp.getName());

                // create the callback
                Runnable callback = new Runnable() {
                    public void run() {
                        // Get the player they're confirming
                        String playerName = pending.get(sender);

                        // Wonder if the player logged out?
                        Player otherPlayer = lwc.getPlugin().getServer().getPlayer(playerName);

                        if (otherPlayer == null) {
                            lwc.sendLocale(sender, "lwc.playerloggedout");
                            return;
                        }

                        LWCPlayer lwcOtherPlayer = lwc.wrapPlayer(otherPlayer);

                        // Enable dev mode
                        lwcOtherPlayer.setDevMode(true);
                        lwcOtherPlayer.setPermissionMode(LWCPlayer.PermissionMode.ADMIN);

                        lwc.sendLocale(sender, "lwc.devmode.success", "player", otherPlayer.getName());
                        lwc.sendLocale(otherPlayer, "lwc.devmode.received");
                    }
                };

                // create the action
                LWCPlayer otherLwcPlayer = lwc.wrapPlayer(temp);
                Action action = new ConfirmAction(callback);
                action.setPlayer(otherLwcPlayer);

                // add it
                lwcPlayer.addAction(action);
            } else if (command.equals("disable")) {
                // Don't let players in dev mode disable it on other players
                if (lwcPlayer != null && lwcPlayer.isDevMode()) {
                    lwc.sendLocale(sender, "protection.accessdenied");
                    return;
                }

                if (args.length < 2) {
                    lwc.sendSimpleUsage(sender, "/lwc dev disable <PlayerName>");
                    return;
                }

                // Search for the player
                String playerName = args[1];
                Player temp = lwc.getPlugin().getServer().getPlayer(playerName);

                if (temp == null) {
                    sender.sendMessage(Colors.Red + "Player not found");
                    return;
                }

                LWCPlayer otherPlayer = lwc.wrapPlayer(temp);

                if (otherPlayer.isDevMode()) {
                    lwc.wrapPlayer(temp).setDevMode(false);
                    lwc.sendLocale(sender, "lwc.devmode.disabled", "player", temp.getName());
                    lwc.sendLocale(otherPlayer, "lwc.devmode.lost");
                } else {
                    lwc.sendLocale(sender, "lwc.devmode.nodevmode");
                }
            } else if (command.equals("permissions")) {
                if (args.length < 2) {
                    lwc.sendSimpleUsage(player, "/lwc dev permissions <PermissionMode>");
                    return;
                }

                // Are they in dev mode?
                if (!lwcPlayer.isDevMode()) {
                    lwc.sendLocale(player, "protection.accessdenied");
                    return;
                }

                for (LWCPlayer.PermissionMode permissionMode : LWCPlayer.PermissionMode.values()) {
                    if (permissionMode.toString().equalsIgnoreCase(args[1])) {
                        lwcPlayer.setPermissionMode(permissionMode);
                        lwc.sendLocale(player, "lwc.devmode.permissionsmode", "mode", permissionMode);
                        break;
                    }
                }
            }

        }
    }

}
