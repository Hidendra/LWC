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

package com.griefcraft.bukkit;

import com.griefcraft.LWC;
import com.griefcraft.SimpleLWC;
import com.griefcraft.bukkit.command.BukkitConsoleCommandSender;
import com.griefcraft.bukkit.configuration.BukkitConfiguration;
import com.griefcraft.bukkit.player.BukkitPlayer;
import com.griefcraft.command.Command;
import com.griefcraft.command.CommandSender;
import com.griefcraft.player.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitPlugin extends JavaPlugin implements Listener {

    /**
     * The LWC object
     */
    private LWC lwc;

    /**
     * Called when a player uses a command
     * @param event
     */
    @EventHandler( ignoreCancelled = true )
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        // Create the internal object
        Player player = new BukkitPlayer(event.getPlayer());
        String message = event.getMessage();

        // Should we cancel the object?
        if (_onCommand(Command.Type.PLAYER, player, message)) {
            event.setCancelled(true);
        }
    }

    /**
     * Called when the console uses a command.
     * Using LOWEST because of kTriggers
     * @param event
     */
    @EventHandler( priority = EventPriority.LOWEST )
    public void onServerCommand(ServerCommandEvent event) {
        if (_onCommand(Command.Type.SERVER, lwc.getConsoleSender(), event.getCommand())) {
            // TODO how to cancel? just change the command to something else?
        }
    }

    @Override
    public void onEnable() {

        // Create a new lwc object
        lwc = SimpleLWC.createLWC(new BukkitConsoleCommandSender(getServer().getConsoleSender()), new BukkitConfiguration(this));

        // Register events
        getServer().getPluginManager().registerEvents(this, this);

    }

    @Override
    public void onDisable() {
        lwc = null;
    }

    /**
     * Command processor
     *
     * @param sender
     * @param message the name of the command followed by any arguments.
     * @return true if the command event should be cancelled
     */
    private boolean _onCommand(Command.Type type, CommandSender sender, String message) {
        // Normalize the command, removing any prepended /, etc
        message = normalizeCommand(message);

        // Separate the command and arguments
        int indexOfSpace = message.indexOf(' ');

        if (indexOfSpace != -1) {
            String command = message.substring(0, indexOfSpace);
            String arguments = message.substring(indexOfSpace + 1);

            return lwc.getCommandHandler().handleCommand(new Command(type, sender, command, arguments));
        } else { // No arguments
            return lwc.getCommandHandler().handleCommand(new Command(type, sender, message));
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
                message = ""; // avoid a out of bounds exception
            } else {
                message = message.substring(1);
            }
        }
        
        return message.trim();
    }

}