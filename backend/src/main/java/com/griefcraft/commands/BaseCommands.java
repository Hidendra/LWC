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

package com.griefcraft.commands;

import com.griefcraft.LWC;
import com.griefcraft.command.Command;
import com.griefcraft.command.CommandContext;
import com.griefcraft.command.CommandSender;
import com.griefcraft.event.events.BlockEvent;
import com.griefcraft.event.notifiers.BlockEventNotifier;
import com.griefcraft.model.Protection;
import com.griefcraft.player.Player;
import com.griefcraft.world.Location;

import java.util.Random;

/**
 * A test of commands used mainly for testing
 */
public class BaseCommands {

    /**
     * The lwc object
     */
    private LWC lwc;

    public BaseCommands(LWC lwc) {
        this.lwc = lwc;
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
            command = "lwc test insert"
    )
    public void insertTest(CommandContext context) {
        lwc.getConsoleSender().sendMessage("Inserting 10,000 random protections");
        Random random = new Random();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            lwc.getDatabase().createProtection(Protection.Type.PRIVATE, "virulent",
                    new Location(lwc.getServerLayer().getDefaultWorld(), random.nextDouble() * 100000, random.nextDouble() * 100000, random.nextDouble() * 100000));
        }
        long time = System.currentTimeMillis() - start;
        lwc.getConsoleSender().sendMessage(String.format("Done. %d ms total, %.2f ms per protection", time, time / 10000D));
    }

    @Command(
            command = "lwc test select"
    )
    public void selectTest(CommandContext context) {
        lwc.getConsoleSender().sendMessage("Selecting 10,000 random protections");
        Random random = new Random();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            lwc.getDatabase().loadProtection(new Location(lwc.getServerLayer().getDefaultWorld(), random.nextDouble() * 100000, random.nextDouble() * 100000, random.nextDouble() * 100000));
        }
        long time = System.currentTimeMillis() - start;
        lwc.getConsoleSender().sendMessage(String.format("Done. %d ms total, %.2f ms per protection", time, time / 10000D));
    }

    @Command(
            command = "lwc create private",
            permission = "lwc.create.private",
            aliases = { "cprivate" }
    )
    public void createPrivateProtection(CommandContext context) {
        CommandSender sender = context.getCommandSender();

        if (!(sender instanceof Player)) {
            return;
        }

        final Player player = (Player) sender;
        player.sendMessage("Click on a block to protect it!");

        player.onBlockInteract(new BlockEventNotifier() {
            @Override
            public boolean call(BlockEvent event) {
                Protection protection = lwc.getDatabase().createProtection(Protection.Type.PRIVATE, player.getName(), event.getBlock().getLocation());
                if (protection != null) {
                    player.sendMessage("Protected~");
                } else {
                    player.sendMessage("Failed to protect for some reason ?");
                }
                return true;
            }
        });
    }

    @Command(
            command = "lwc test",
            permission = "lwc.test",
            aliases = { "ctest" }
    )
    public void lwcTest(CommandContext context) {
        CommandSender sender = context.getCommandSender();

        if (!(sender instanceof Player)) {
            return;
        }

        final Player player = (Player) sender;
        player.sendMessage("Click on a block, please!~");

        // Wait for them to click on the block
        player.onBlockInteract(new BlockEventNotifier() {
            @Override
            public boolean call(BlockEvent event) {
                player.sendMessage("You clicked on a block with the id " + event.getBlock().getType() + "!");
                return true;
            }
        });
    }

    @Command(
            command = "lwc admin purge",
            description = "Removes all of the player's protections from the world",
            permission = "lwc.admin.purge",
            usage = "<player>",
            aliases = { "purge" },
            min = 1,
            max = 1
    )
    public void lwcAdminPurge(CommandContext context) {
        context.getCommandSender().sendMessage("Purging player?: " + context.getArgument(1));
    }

}
