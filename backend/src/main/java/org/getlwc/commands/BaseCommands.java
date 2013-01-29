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

package org.getlwc.commands;

import org.getlwc.Block;
import org.getlwc.Engine;
import org.getlwc.ProtectionAccess;
import org.getlwc.ProtectionManager;
import org.getlwc.Role;
import org.getlwc.command.Command;
import org.getlwc.command.CommandContext;
import org.getlwc.command.SenderType;
import org.getlwc.entity.Player;
import org.getlwc.event.events.BlockEvent;
import org.getlwc.event.events.ProtectionEvent;
import org.getlwc.event.notifiers.BlockEventNotifier;
import org.getlwc.event.notifiers.ProtectionEventNotifier;
import org.getlwc.model.Protection;
import org.getlwc.util.TimeUtil;

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
            command = "lwc"
    )
    public void lwc(CommandContext context) {
        context.getCommandSender().sendMessage("/lwc");
    }

    @Command(
            command = "lwc create",
            permission = "lwc.create",
            aliases = {"cprivate","clock"},
            accepts = SenderType.PLAYER
    )
    public void createProtection(CommandContext context) {
        final Player player = (Player) context.getCommandSender();
        player.sendMessage(_("&eClick on a block to protect it!"));

        player.onAnyInteract(new BlockEventNotifier() {
            @Override
            public boolean call(BlockEvent event) {
                ProtectionManager manager = engine.getProtectionManager();
                Block block = event.getBlock();

                if (!manager.isBlockProtectable(block)) {
                    player.sendMessage(_("&4That block is not protectable"));
                    return false;
                }

                Protection protection = manager.createProtection(player.getName(), block.getLocation());
                if (protection != null) {
                    player.sendMessage(_("&2Created a new protection successfully.\n" +
                            "Want to give another player access to your protection?\n" +
                            "Use: &e/lwc modify member NAME"));
                } else {
                    player.sendMessage(_("&4Failed to create the protection. Your block is most likely not protected."));
                }

                return true;
            }
        });
    }

    @Command(
            command = "lwc delete",
            permission = "lwc.remove.protection",
            aliases = {"cremove","cunlock"},
            accepts = SenderType.PLAYER
    )
    public void removeProtection(CommandContext context) {
        final Player player = (Player) context.getCommandSender();
        player.sendMessage(_("&eClick on a protection to remove the lock!"));

        player.onAnyInteract(new ProtectionEventNotifier() {
            @Override
            public boolean call(ProtectionEvent event) {
                Protection protection = event.getProtection();

                if (protection.getAccess(player) == ProtectionAccess.OWNER) {
                    protection.remove();
                    player.sendMessage(_("&2The protection has been removed successfully."));
                } else {
                    player.sendMessage(_("&4You do not have the required access level to do that!"));
                }

                return true;
            }
        });
    }

    @Command(
            command = "lwc info",
            permission = "lwc.info",
            aliases = {"cinfo"},
            accepts = SenderType.PLAYER
    )
    public void info(CommandContext context) {
        final Player player = (Player) context.getCommandSender();
        player.sendMessage(_("&eClick on a protection to view info on it."));

        player.onAnyInteract(new ProtectionEventNotifier() {
            @Override
            public boolean call(ProtectionEvent event) {
                Protection protection = event.getProtection();

                String roles = "";
                for (Role role : protection.getRoles()) {
                    roles += _("&7{0}&f: Role is for \"&7{1}&f\" with the access &7{2}&f\n", role.getClass().getSimpleName(), role.getRoleName(), role.getRoleAccess());
                }

                player.sendMessage(_("Location: &7[{0} {1} {2}]&f in the world \"&7{3}&f\"\n" +
                        "Created on: &7{4}\n" +
                        "Last updated on: &7{5}\n" +
                        "Last accessed on: &7{6}\n" +
                        "&eRoles(size={7}):\n" +
                        "{8}", protection.getX(), protection.getY(), protection.getZ(), protection.getWorld().getName(),
                        TimeUtil.timeToString(System.currentTimeMillis()/1000L - protection.getCreated()), TimeUtil.timeToString(System.currentTimeMillis()/1000L - protection.getUpdated()),
                        TimeUtil.timeToString(System.currentTimeMillis()/1000L - protection.getAccessed()), protection.getRoles().size(), roles));

                return true;
            }
        });
    }

    @Command(
            command = "lwc admin purge",
            description = "Removes all of the player's protections from the world",
            permission = "lwc.admin.purge",
            usage = "<player>",
            aliases = {"purge"},
            min = 1,
            max = 1
    )
    public void lwcAdminPurge(CommandContext context) {
        context.getCommandSender().sendMessage(_("Purging player?: {0}", context.getArgument(1)));
    }

}
