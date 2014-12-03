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

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.getlwc.Block;
import org.getlwc.EventHelper;
import org.getlwc.ExplosionType;
import org.getlwc.World;
import org.getlwc.command.CommandContext;
import org.getlwc.command.CommandException;
import org.getlwc.command.CommandSender;
import org.getlwc.entity.Entity;
import org.getlwc.entity.Player;
import org.getlwc.forge.ForgeMod;
import org.getlwc.forge.event.EntityExplodeEvent;
import org.getlwc.forge.event.PlayerBreakBlockEvent;
import org.getlwc.forge.event.PlayerPlaceBlockEvent;
import org.getlwc.forge.event.PlayerUpdateSignEvent;
import org.getlwc.util.StringUtils;

public class ForgeListener {

    /**
     * The mod object
     */
    private ForgeMod mod;

    public ForgeListener(ForgeMod mod) {
        this.mod = mod;
    }

    @SubscribeEvent
    public void playerQuit(PlayerEvent.PlayerLoggedOutEvent event) {
        EventHelper.onPlayerQuit(mod.wrapPlayer(event.player));
    }

    @SubscribeEvent
    public void commandEvent(CommandEvent event) {
        String commandName = event.command.getCommandName();
        String message = "/" + commandName + " " + StringUtils.join(event.parameters);
        boolean result = false;

        ICommandSender sender = event.sender;

        // Console
        if (sender instanceof MinecraftServer) {
            result = _onCommand(CommandContext.Type.SERVER, mod.getEngine().getConsoleSender(), message);
        }

        // Player
        else if (sender instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) sender;
            result = _onCommand(CommandContext.Type.PLAYER, mod.wrapPlayer(player), message);
        }

        if (result) {
            event.setCanceled(true);
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public void entityInteract(EntityInteractEvent event) {
        Player player = mod.wrapPlayer(event.entityPlayer);
        Entity target = mod.wrapEntity(event.target);

        if (EventHelper.onEntityInteract(player, target)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void playerInteract(PlayerInteractEvent event) {
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        org.getlwc.entity.Player player = mod.wrapPlayer((EntityPlayer) event.entityPlayer);
        World world = player.getLocation().getWorld();
        Block block = world.getBlockAt(event.x, event.y, event.z);

        if (EventHelper.onBlockInteract(player, block)) {
            event.setCanceled(true);
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public void playerBlockBreak(PlayerBreakBlockEvent event) {
        org.getlwc.entity.Player player = mod.wrapPlayer(event.entityPlayer);
        World world = player.getLocation().getWorld();
        Block block = world.getBlockAt(event.blockX, event.blockY, event.blockZ);

        // XXX Forge doesn't send an interact event when digging a block so we check the block so messages are sent
        if (EventHelper.onBlockInteract(player, block) || EventHelper.onBlockBreak(player, block)) {
            event.setCanceled(true);
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public void playerBlockPlace(PlayerPlaceBlockEvent event) {
        org.getlwc.entity.Player player = mod.wrapPlayer(event.entityPlayer);
        World world = player.getLocation().getWorld();
        Block block = world.getBlockAt(event.blockX, event.blockY, event.blockZ);

        if (EventHelper.onBlockPlace(player, block)) {
            event.setCanceled(true);
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public void playerUpdateSign(PlayerUpdateSignEvent event) {
        org.getlwc.entity.Player player = mod.wrapPlayer(event.entityPlayer);
        World world = player.getLocation().getWorld();
        Block block = world.getBlockAt(event.packet.func_149588_c(), event.packet.func_149586_d(), event.packet.func_149585_e()); // Natives: x/y/z

        if (EventHelper.onSignChange(player, block)) {
            event.setCanceled(true);
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public void onExplosion(EntityExplodeEvent event) {
        org.getlwc.World world = mod.getWorld(event.entity.worldObj.getWorldInfo().getWorldName());

        ExplosionType type = null;

        if (event.entity.getCommandSenderName().contains("TNT")) {
            type = ExplosionType.TNT;
        } else if (event.entity.getCommandSenderName().equalsIgnoreCase("Creeper")) {
            type = ExplosionType.CREEPER;
        }

        if (type == null) {
            throw new UnsupportedOperationException("Unsupported explosion entity: " + event.entity);
        }

        if (EventHelper.onExplosion(type, event.getAffectedBlocks())) {
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
            mod.getEngine().getConsoleSender().sendMessage("An error was encountered while processing a command: {0}", e.getMessage());
            e.printStackTrace();

            // Notify the player / console
            sender.sendMessage("&4[LWC] An internal error occurred while processing this command");

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
