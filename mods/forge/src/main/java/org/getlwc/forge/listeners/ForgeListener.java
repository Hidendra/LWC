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
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR,
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

package org.getlwc.forge.listeners;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.Event;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.getlwc.Block;
import org.getlwc.ExplosionType;
import org.getlwc.World;
import org.getlwc.command.CommandContext;
import org.getlwc.command.CommandException;
import org.getlwc.command.CommandSender;
import org.getlwc.forge.LWC;
import org.getlwc.forge.event.EntityExplodeEvent;
import org.getlwc.forge.event.PlayerBreakBlockEvent;
import org.getlwc.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ForgeListener {

    /**
     * The mod object
     */
    private LWC mod;

    public ForgeListener(LWC mod) {
        this.mod = mod;
    }

    @ForgeSubscribe(receiveCanceled = true)
    public void commandEvent(CommandEvent event) {
        String commandName = event.command.getCommandName();
        String message = "/" + commandName + " " + StringUtils.join(event.parameters);
        boolean result = false;

        // UNSAFE
        ICommandSender sender = event.sender;

        // Console
        if (sender instanceof MinecraftServer) {
            result = _onCommand(CommandContext.Type.SERVER, mod.getEngine().getConsoleSender(), message);
        }

        // UNSAFE
        // grep 'implements aa'
        else if (sender instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) sender;
            result = _onCommand(CommandContext.Type.PLAYER, mod.wrapPlayer(player), message);
        }

        if (result) {
            event.setCanceled(true);
            event.setResult(Event.Result.DENY);
        }
    }

    @ForgeSubscribe
    public void playerInteract(PlayerInteractEvent event) {
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        org.getlwc.entity.Player player = mod.wrapPlayer((EntityPlayer) event.entityPlayer);
        World world = player.getLocation().getWorld();
        Block block = world.getBlockAt(event.x, event.y, event.z);

        // send the event for the player around the plugin (and maybe other plugins, too.)
        if (mod.getEngine().getEventHelper().onBlockInteract(player, block)) {
            event.setCanceled(true);
            event.setResult(Event.Result.DENY);
        }
    }

    @ForgeSubscribe
    public void playerBlockBreak(PlayerBreakBlockEvent event) {
        org.getlwc.entity.Player player = mod.wrapPlayer(event.entityPlayer);
        World world = player.getLocation().getWorld();
        Block block = world.getBlockAt(event.blockX, event.blockY, event.blockZ);

        // XXX Forge doesn't send an interact event when digging a block so we check the block so messages are sent
        if (mod.getEngine().getEventHelper().onBlockInteract(player, block) || mod.getEngine().getEventHelper().onBlockBreak(player, block)) {
            event.setCanceled(true);
            event.setResult(Event.Result.DENY);
        }
    }

    @ForgeSubscribe
    public void onExplosion(EntityExplodeEvent event) {
        org.getlwc.World world = mod.getWorld(event.entity.worldObj.getWorldInfo().getWorldName());

        ExplosionType type = null;

        if (event.entity.getEntityName().contains("TNT")) {
            type = ExplosionType.TNT;
        } else if (event.entity.getEntityName().equalsIgnoreCase("Creeper")) {
            type = ExplosionType.CREEPER;
        }

        if (type == null) {
            throw new UnsupportedOperationException("Unsupported explosion entity: " + event.entity);
        }

        List<Block> affected = new ArrayList<Block>();

        int radius = 5;
        for (int x = event.getExplosionX() - radius; x < event.getExplosionX() + radius; x++) {
            for (int y = event.getExplosionY() - radius; y < event.getExplosionY() + radius; y++) {
                for (int z = event.getExplosionZ() - radius; z < event.getExplosionZ() + radius; z++) {
                    affected.add(world.getBlockAt(x, y, z));
                }
            }
        }

        if (mod.getEngine().getEventHelper().onExplosion(type, affected)) {
            event.setCanceled(true);
            event.setResult(Event.Result.DENY);
        }
    }

    /**
     * Command processor
     *
     * @param sender
     * @param message the name of the command followed by any arguments.
     * @return true if the command event should be cancelled
     */
    private boolean _onCommand(CommandContext.Type type, CommandSender sender, String message) {
        // Normalize the command, removing any prepended /, etc
        message = normalizeCommand(message);

        // Separate the command and arguments
        int indexOfSpace = message.indexOf(' ');

        try {
            if (indexOfSpace != -1) {
                String command = message.substring(0, indexOfSpace);
                String arguments = message.substring(indexOfSpace + 1);

                return mod.getEngine().getCommandHandler().handleCommand(new CommandContext(type, sender, command, arguments));
            } else { // No arguments
                return mod.getEngine().getCommandHandler().handleCommand(new CommandContext(type, sender, message));
            }
        } catch (CommandException e) {
            // Notify the console
            mod.getEngine().getConsoleSender().sendMessage("An error was encountered while processing a command: " + e.getMessage());
            e.printStackTrace();

            // Notify the player / console
            // TODO red this bitch up
            sender.sendMessage("[LWC] An internal error occurred while processing this command");

            // We failed.. oh we failed
            return false;
        }
    }

    /**
     * Normalize a command, making player and console commands appear to be the same format
     *
     * @param message
     * @return
     */
    private String normalizeCommand(String message) {
        // Remove a prepended /
        if (message.startsWith("/")) {
            if (message.length() == 1) {
                return "";
            } else {
                message = message.substring(1);
            }
        }

        return message.trim();
    }

}
