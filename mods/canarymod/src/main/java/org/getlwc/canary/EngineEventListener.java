package org.getlwc.canary;

import net.canarymod.Canary;
import net.canarymod.Translator;
import net.canarymod.api.Server;
import net.canarymod.chat.MessageReceiver;
import net.canarymod.commandsys.CanaryCommand;
import net.canarymod.commandsys.Command;
import org.getlwc.Engine;
import org.getlwc.command.CommandContext;
import org.getlwc.command.CommandException;
import org.getlwc.command.CommandSender;
import org.getlwc.event.Listener;
import org.getlwc.event.engine.BaseCommandRegisteredEvent;
import org.getlwc.util.StringUtils;

import java.lang.annotation.Annotation;

public class EngineEventListener {

    private Engine engine;
    private CanaryPlugin plugin;

    public EngineEventListener(Engine engine, CanaryPlugin plugin) {
        this.engine = engine;
        this.plugin = plugin;
    }

    @SuppressWarnings("unused")
    @Listener
    public void onRegisterCommand(BaseCommandRegisteredEvent event) {
        final String baseCommand = event.getNormalizedCommand();
        final org.getlwc.command.Command command = event.getCommand();

        if (Canary.commands().hasCommand(baseCommand)) {
            return;
        }

        try {
            // create the annotation we will use instead
            Command commandAnnotation = new Command() {

                @Override
                public String[] aliases() {
                    return new String[]{baseCommand}; // required
                }

                @Override
                public String[] permissions() {
                    return new String[]{command.permission()}; // required
                }

                @Override
                public String description() {
                    return command.description(); // required
                }

                @Override
                public String toolTip() {
                    return ""; // required
                }

                @Override
                public String parent() {
                    return "";
                }

                @Override
                public String helpLookup() {
                    return "";
                }

                @Override
                public String[] searchTerms() {
                    return new String[]{""};
                }

                @Override
                public int min() {
                    return command.min();
                }

                @Override
                public int max() {
                    return command.max();
                }

                @Override
                public int version() {
                    return 1;
                }

                @Override
                public Class<? extends Annotation> annotationType() {
                    return Command.class;
                }

            };

            // create the command instance Canary uses
            CanaryCommand canaryCommand = new CanaryCommand(commandAnnotation, plugin, Translator.getInstance()) {
                @Override
                protected void execute(MessageReceiver caller, String[] parameters) {
                    if (caller instanceof net.canarymod.api.entity.living.humanoid.Player) {
                        handleOnCommand(CommandContext.Type.PLAYER, plugin.wrapPlayer((net.canarymod.api.entity.living.humanoid.Player) caller), StringUtils.join(parameters));
                    } else if (caller instanceof Server) {
                        handleOnCommand(CommandContext.Type.SERVER, plugin.getEngine().getConsoleSender(), StringUtils.join(parameters));
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

    /**
     * Command processor
     *
     * @param sender
     * @param message the name of the command followed by any arguments.
     * @return true if the command event should be cancelled
     */
    private boolean handleOnCommand(CommandContext.Type type, CommandSender sender, String message) {
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
            plugin.getEngine().getConsoleSender().sendMessage("An error was encountered while processing a command: {0}", e.getMessage());
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
