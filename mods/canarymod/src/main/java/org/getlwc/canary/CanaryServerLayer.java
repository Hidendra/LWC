package org.getlwc.canary;

import net.canarymod.Canary;
import net.canarymod.Translator;
import net.canarymod.api.Server;
import net.canarymod.chat.MessageReceiver;
import net.canarymod.commandsys.CanaryCommand;
import net.canarymod.commandsys.Command;
import org.getlwc.ServerLayer;
import org.getlwc.World;
import org.getlwc.canary.entity.CanaryPlayer;
import org.getlwc.canary.world.CanaryWorld;
import org.getlwc.command.CommandContext;
import org.getlwc.command.CommandException;
import org.getlwc.command.CommandSender;
import org.getlwc.entity.Player;
import org.getlwc.util.StringUtils;

import java.io.File;
import java.lang.annotation.Annotation;

public class CanaryServerLayer extends ServerLayer {

    /**
     * Canary plugin object
     */
    private LWC plugin;

    public CanaryServerLayer(LWC plugin) {
        this.plugin = plugin;
    }

    @Override
    public File getEngineHomeFolder() {
        File folder = new File("plugins", "LWC");

        if (!folder.exists()) {
            folder.mkdir();
        }

        return folder;
    }

    @Override
    public void onRegisterBaseCommand(final String baseCommand, final org.getlwc.command.Command command) {
        if (Canary.commands().hasCommand(baseCommand)) {
            return;
        }

        try {
            // create the annotation we will use instead
            Command commandAnnotation = new Command() {

                public String[] aliases() {
                    return new String[]{baseCommand}; // required
                }

                public String[] permissions() {
                    return new String[]{command.permission()}; // required
                }

                public String description() {
                    return command.description(); // required
                }

                public String toolTip() {
                    return ""; // required
                }

                public String parent() {
                    return "";
                }

                public String helpLookup() {
                    return "";
                }

                public String[] searchTerms() {
                    return new String[]{""};
                }

                public int min() {
                    return command.min();
                }

                public int max() {
                    return command.max();
                }

                public Class<? extends Annotation> annotationType() {
                    return Command.class;
                }

            };

            // create the command instance Canary uses
            CanaryCommand canaryCommand = new CanaryCommand(commandAnnotation, plugin, Translator.getInstance()) {
                @Override
                protected void execute(MessageReceiver caller, String[] parameters) {
                    if (caller instanceof net.canarymod.api.entity.living.humanoid.Player) {
                        _onCommand(CommandContext.Type.PLAYER, plugin.wrapPlayer((net.canarymod.api.entity.living.humanoid.Player) caller), StringUtils.join(parameters));
                    } else if (caller instanceof Server) {
                        _onCommand(CommandContext.Type.SERVER, plugin.getEngine().getConsoleSender(), StringUtils.join(parameters));
                    }
                }
            };

            Canary.commands().registerCommand(canaryCommand, plugin, false);

            /**
             * TODO I shouldn't need to reflect into Canary.
             */
        } catch (Exception e) {
            System.out.println("Error occurred while registering command: " + baseCommand);
            e.printStackTrace();
        }
    }

    @Override
    public World getDefaultWorld() {
        return internalGetWorld(Canary.getServer().getDefaultWorldName());
    }

    @Override
    protected Player internalGetPlayer(String playerName) {
        net.canarymod.api.entity.living.humanoid.Player handle = Canary.getServer().getPlayer(playerName);

        if (handle == null) {
            return null;
        }

        return new CanaryPlayer(plugin, handle);
    }

    @Override
    protected World internalGetWorld(String worldName) {
        net.canarymod.api.world.World handle = Canary.getServer().getWorld(worldName);

        if (handle == null) {
            return null;
        }

        return new CanaryWorld(handle);
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
            plugin.getEngine().getConsoleSender().sendTranslatedMessage("An error was encountered while processing a command: {0}", e.getMessage());
            e.printStackTrace();

            // Notify the player / console
            sender.sendTranslatedMessage("&4[LWC] An internal error occurred while processing this command");

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
