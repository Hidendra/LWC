/*
 * Copyright (c) 2011-2013 Tyler Blair
 * All rights reserved.
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

package org.getlwc.command;

import org.getlwc.Block;
import org.getlwc.Engine;
import org.getlwc.ProtectionManager;
import org.getlwc.component.Component;
import org.getlwc.component.RoleSetComponent;
import org.getlwc.entity.Player;
import org.getlwc.event.EventConsumer;
import org.getlwc.event.block.BlockInteractEvent;
import org.getlwc.event.protection.ProtectionInteractEvent;
import org.getlwc.model.Protection;
import org.getlwc.role.Role;
import org.getlwc.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.getlwc.I18n._;

/**
 * A test of commands used mainly for testing
 */
public class BaseCommands {

    /**
     * The LWC engine
     */
    private Engine engine;

    public BaseCommands(Engine engine) {
        this.engine = engine;
    }

    /**
     * The base command that is falled onto when no other commands are found. Also, of course, for just /lwc
     *
     * @param context
     */
    @Command(
            command = "lwc",
            description = "LWC!"
    )
    public void lwc(CommandContext context) {
        String fullCommand = (context.getCommand() + " " + context.getArguments()).trim();
        List<Command> similar = engine.getCommandHandler().findSimilar(fullCommand);

        // sort it alphabetically
        Collections.sort(similar, new CommandComparator());

        context.getCommandSender().sendTranslatedMessage("Similar commands:");

        if (similar.size() == 0) {
            context.getCommandSender().sendTranslatedMessage("&4No commands found.");
        }

        for (Command command : similar) {
            // block self
            if (command.command().equals("lwc")) {
                continue;
            }

            if (engine.getCommandHandler().canUseCommand(context.getCommandSender(), command)) {
                context.getCommandSender().sendTranslatedMessage("- &6/{0}&r: {1}", command.command(), command.description());
            }
        }
    }

    @Command(
            command = "lwc create",
            description = "Create a protection",
            permission = "lwc.create",
            aliases = {"cprivate", "clock", "protect","p"},
            accepts = SenderType.PLAYER
    )
    public void createProtection(CommandContext context) {
        final Player player = (Player) context.getCommandSender();
        player.sendTranslatedMessage("&eClick on a block to protect it!");

        player.onNextBlockInteract(new EventConsumer<BlockInteractEvent>() {
            @Override
            public void accept(BlockInteractEvent event) {
                event.markCancelled();

                ProtectionManager manager = engine.getProtectionManager();
                Block block = event.getBlock();

                if (!manager.isBlockProtectable(block)) {
                    player.sendTranslatedMessage("&4That block is not protectable");
                    return;
                }

                Protection protection = manager.createProtection(player.getUUID(), block.getLocation());

                if (protection != null) {
                    player.sendTranslatedMessage("&2Created a new protection successfully.\n" +
                            "Want to give another player access to your protection?\n" +
                            "Use: &e/lwc modify NAME (or: /cadd NAME)");
                } else {
                    player.sendTranslatedMessage("&4Failed to create the protection. Your block is most likely not protected.");
                }
            }
        });
    }

    @Command(
            command = "lwc delete",
            description = "Remove a protection",
            permission = "lwc.remove.protection",
            aliases = {"cremove", "remove"},
            accepts = SenderType.PLAYER
    )
    public void removeProtection(CommandContext context) {
        final Player player = (Player) context.getCommandSender();
        player.sendTranslatedMessage("&eClick on a protection to remove the lock!");

        player.onNextProtectionInteract(new EventConsumer<ProtectionInteractEvent>() {
            @Override
            public void accept(ProtectionInteractEvent event) {
                event.markCancelled();

                Protection protection = event.getProtection();

                if (protection.getAccess(player) == Protection.Access.OWNER) {
                    protection.remove();
                    player.sendTranslatedMessage("&2The protection has been removed successfully.");
                } else {
                    player.sendTranslatedMessage("&4You do not have the required access level to do that!");
                }
            }
        });
    }

    @Command(
            command = "lwc info",
            description = "Get information about a protection",
            permission = "lwc.info",
            aliases = {"cinfo", "info"},
            accepts = SenderType.PLAYER
    )
    // TODO Should be improved (re-do role text)
    public void info(CommandContext context) {
        final Player player = (Player) context.getCommandSender();
        player.sendTranslatedMessage("&eClick on a protection to view info on it.");

        player.onNextProtectionInteract(new EventConsumer<ProtectionInteractEvent>() {
            @Override
            public void accept(ProtectionInteractEvent event) {
                event.markCancelled();

                Protection protection = event.getProtection();
                Map<Protection.Access, List<Role>> accessMap = new LinkedHashMap<Protection.Access, List<Role>>();

                for (Protection.Access access : Protection.Access.USABLE_ACCESS_LEVELS) {
                    List<Role> roles = new ArrayList<>();

                    for (Role role : protection.getComponent(RoleSetComponent.class).getAll()) {
                        if (role.getAccess() == access) {
                            roles.add(role);
                        }
                    }

                    accessMap.put(access, roles);
                }

                String rolesText = "";
                for (Map.Entry<Protection.Access, List<Role>> entry : accessMap.entrySet()) {
                    String stringifiedList = "";

                    Protection.Access access = entry.getKey();
                    List<Role> roleList = entry.getValue();

                    if (roleList.size() == 0) {
                        continue;
                    }

                    for (Role role : roleList) {
                        stringifiedList += role.serialize() + ", ";
                    }

                    if (stringifiedList.length() > 0) {
                        stringifiedList = stringifiedList.substring(0, stringifiedList.length() - 2);
                    }

                    rolesText += _("&7{0}&f: {1}\n", StringUtils.capitalizeFirstLetter(access.toString()), stringifiedList);
                }

                // TODO change this to support any protection ...
                player.sendMessage("<<Place holder message until this is readded>>");

                player.sendMessage("Components:");
                for (Component component : protection.getComponents()) {
                    player.sendMessage(component.toString());
                }
            }
        });
    }

}
