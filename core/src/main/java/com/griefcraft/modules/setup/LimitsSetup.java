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

package com.griefcraft.modules.setup;

import com.griefcraft.lwc.LWC;
import com.griefcraft.modules.limits.LimitsV2;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.util.Colors;
import com.griefcraft.util.config.Configuration;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class LimitsSetup extends JavaModule {

    @Override
    public void onCommand(LWCCommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!event.hasFlag("s", "setup")) {
            return;
        }

        LWC lwc = event.getLWC();
        CommandSender sender = event.getSender();
        String[] args = event.getArgs();

        if (!args[0].equals("limits")) {
            return;
        }

        // we have the right command
        event.setCancelled(true);

        // Load limits v2
        LimitsV2 limits = (LimitsV2) lwc.getModuleLoader().getModule(LimitsV2.class);

        // Send them the default limits
        if (args.length == 1) {
            sender.sendMessage("Default limits:");
            limits.sendLimits(sender, null, limits.getDefaultLimits());
        } else {
            String name = args[1];
            boolean isGroup = false;

            if (name.startsWith("g:")) {
                isGroup = true;
                name = name.substring(2);
            }

            // They just want to know the limits
            if (args.length == 2) {
                // if they're a player it is simple
                if (!isGroup) {
                    List<LimitsV2.Limit> found = limits.getPlayerLimits().get(name.toLowerCase());
                    Player target = lwc.getPlugin().getServer().getPlayer(name);

                    if (found == null) {
                        sender.sendMessage(Colors.Red + "Player override not found.");
                    } else {
                        limits.sendLimits(sender, target, found);
                    }
                } else {
                    List<LimitsV2.Limit> found = limits.getGroupLimits().get(name);

                    if (found == null) {
                        sender.sendMessage(Colors.Red + "Group override not found.");
                    } else {
                        limits.sendLimits(sender, null, found);
                    }
                }

            } else {
                Configuration configuration = limits.getConfiguration();

                // Generate the base path
                String path;
                boolean modified = false;

                if (name.equalsIgnoreCase("default")) {
                    path = "defaults.";
                } else if (isGroup) {
                    path = "groups." + name + ".";
                } else {
                    path = "players." + getCaseCorrectPlayerName(configuration, name) + ".";
                }

                // If they only gave an integer, we assume it's to be the default
                if (args.length == 3) {
                    Object value;

                    if (args[2].equalsIgnoreCase("unlimited")) {
                        value = "unlimited";
                    } else {
                        try {
                            value = Integer.parseInt(args[2]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage(Colors.Red + "Invalid number for \"" + args[2] + "\"");
                            return;
                        }
                    }

                    configuration.setProperty(path + "default", value);
                    modified = true;
                } else {
                    // Begin scanning
                    for (int index = 2; index < args.length; index += 2) {
                        if ((index + 1) > args.length) {
                            continue;
                        }

                        String key = args[index];
                        String value = args[index + 1];

                        Object intValue;

                        if (value.equalsIgnoreCase("unlimited")) {
                            intValue = value;
                        } else {
                            try {
                                // attempt to parse it as an integer (it must always be one)
                                intValue = Integer.parseInt(value);
                            } catch (NumberFormatException e) {
                                sender.sendMessage(Colors.Red + "Invalid number for: \"" + value + "\"!");
                                return;
                            }
                        }

                        // Is it a default?
                        if (key.equalsIgnoreCase("default")) {
                            configuration.setProperty(path + "default", intValue);
                            modified = true;
                        } else {
                            // it should be a material
                            Material material = Material.getMaterial(key.toUpperCase());

                            // if it's null, try to parse it as a block value
                            if (material == null) {
                                try {
                                    material = Material.getMaterial(Integer.parseInt(key));
                                } catch (NumberFormatException e) { }
                            }

                            if (material == null) {
                                sender.sendMessage(Colors.Red + "Invalid material/block: \"" + value + "\"!");
                                return;
                            }

                            // good good
                            configuration.setProperty(path + material.toString().toLowerCase(), intValue);
                            modified = true;
                        }
                    }
                }

                // If we modified the config we need to save it and reload
                if (modified) {
                    configuration.save();
                    limits.reload();
                    sender.sendMessage(Colors.Green + "Set the limits for \"" + name + "\" successfully.");
                }
            }

        }
    }

    /**
     * Gets the case correct player name in the limits config
     *
     * @param player
     * @return
     */
    private String getCaseCorrectPlayerName(Configuration configuration, String player) {
        try {
            List<String> keys = configuration.getKeys("players");

            for (String key : keys) {
                if (key.equalsIgnoreCase(player)) {
                    return key;
                }
            }
        } catch (NullPointerException e) { }

        // Not found, so we assume we're creating a new one
        return player;
    }

}
