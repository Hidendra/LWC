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

import org.getlwc.command.CommandContext;
import org.getlwc.command.CommandException;
import org.getlwc.command.CommandSender;
import org.getlwc.util.StringUtils;

public class CanaryListener extends PluginListener {

    /**
     * The plugin object
     */
    private LWC plugin;

    public CanaryListener(LWC plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(Player player, String[] split) {
        return _onCommand(CommandContext.Type.PLAYER, plugin.wrapPlayer(player), StringUtils.join(split));
    }

    @Override
    public boolean onConsoleCommand(String[] split) {
        return _onCommand(CommandContext.Type.SERVER, plugin.getEngine().getConsoleSender(), StringUtils.join(split));
    }

    @Override
    public boolean onBlockRightClick(Player nativePlayer, Block blockClicked, Item itemInHand) {
        org.getlwc.entity.Player player = plugin.wrapPlayer(nativePlayer);
        org.getlwc.World world = plugin.getWorld(nativePlayer.getWorld().getName());
        org.getlwc.Block block = new CanaryBlock(world, blockClicked);

        // send the event for the player around the plugin (and maybe other plugins, too.)
        return plugin.getEngine().getEventHelper().onBlockInteract(player, block);
    }

    @Override
    public boolean onBlockBreak(Player nativePlayer, Block blockBroken) {
        org.getlwc.entity.Player player = plugin.wrapPlayer(nativePlayer);
        org.getlwc.World world = plugin.getWorld(nativePlayer.getWorld().getName());
        org.getlwc.Block block = new CanaryBlock(world, blockBroken);

        return plugin.getEngine().getEventHelper().onBlockBreak(player, block);
    }

    @Override
    public boolean onBlockPlace(Player nativePlayer, Block blockPlaced, Block blockClicked, Item itemInHand) {
        org.getlwc.entity.Player player = plugin.wrapPlayer(nativePlayer);
        org.getlwc.World world = plugin.getWorld(nativePlayer.getWorld().getName());
        org.getlwc.Block block = new CanaryBlock(world, blockPlaced);

        return plugin.getEngine().getEventHelper().onBlockPlace(player, block);
    }

    @Override
    public boolean onSignChange(Player nativePlayer, Sign sign) {
        org.getlwc.entity.Player player = plugin.wrapPlayer(nativePlayer);
        org.getlwc.World world = plugin.getWorld(nativePlayer.getWorld().getName());
        org.getlwc.Block block = new CanaryBlock(world, sign.getBlock());

        return plugin.getEngine().getEventHelper().onSignChange(player, block);
    }

    @Override
    public boolean onOpenInventory(HookParametersOpenInventory inventory) {
        return false;
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

                return plugin.getEngine().getCommandHandler().handleCommand(new CommandContext(type, sender, command, arguments));
            } else { // No arguments
                return plugin.getEngine().getCommandHandler().handleCommand(new CommandContext(type, sender, message));
            }
        } catch (CommandException e) {
            // Notify the console
            plugin.getEngine().getConsoleSender().sendMessage("An error was encountered while processing a command: " + e.getMessage());
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
