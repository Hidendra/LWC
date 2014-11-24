package org.getlwc.sponge;

import com.google.common.base.Optional;
import org.getlwc.Engine;
import org.getlwc.command.Command;
import org.getlwc.command.CommandContext;
import org.getlwc.command.CommandSender;
import org.getlwc.command.ConsoleCommandSender;
import org.getlwc.event.Listener;
import org.getlwc.event.engine.BaseCommandRegisteredEvent;
import org.spongepowered.api.util.command.CommandCallable;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.Description;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EngineEventListener {

    private Engine engine;
    private SpongePlugin plugin;

    public EngineEventListener(Engine engine, SpongePlugin plugin) {
        this.engine = engine;
        this.plugin = plugin;
    }

    @SuppressWarnings("unused")
    @Listener
    public void onRegisterBaseCommand(BaseCommandRegisteredEvent event) {
        final String baseCommand = event.getNormalizedCommand();
        final Command command = event.getCommand();

        CommandCallable callable = new CommandCallable() {
            @Override
            public boolean call(CommandSource source, String arguments, List<String> parents) throws CommandException {
                CommandSender sender = commandSourceToSender(source);
                CommandContext.Type type = (sender instanceof ConsoleCommandSender) ? CommandContext.Type.SERVER : CommandContext.Type.PLAYER;

                try {
                    return plugin.getEngine().getCommandHandler().handleCommand(new CommandContext(type, sender, baseCommand, arguments));
                } catch (org.getlwc.command.CommandException e) {
                    plugin.getEngine().getConsoleSender().sendMessage("An error was encountered while processing a command: {0}", e.getMessage());
                    e.printStackTrace();

                    sender.sendMessage("&4[LWC] An internal error occurred while processing this command");
                    return false;
                }
            }

            @Override
            public Description getDescription() {
                return new Description() {
                    @Override
                    public Optional<String> getShortDescription() {
                        return Optional.of(command.description());
                    }

                    @Override
                    public Optional<String> getHelp() {
                        return Optional.absent();
                    }

                    @Override
                    public String getUsage() {
                        return command.usage();
                    }

                    @Override
                    public List<String> getPermissions() {
                        List<String> permissions = new ArrayList<>();
                        permissions.add(command.permission());
                        return permissions;
                    }
                };
            }

            @Override
            public boolean testPermission(CommandSource source) {
                if (command.permission() == null || command.permission().isEmpty()) {
                    return true;
                } else {
                    return commandSourceToSender(source).hasPermission(command.permission());
                }
            }

            @Override
            public List<String> getSuggestions(CommandSource commandSource, String s) throws CommandException {
                return new ArrayList<>();
            }
        };

        List<String> aliases = new ArrayList<>();
        aliases.add(baseCommand);
        aliases.addAll(Arrays.asList(command.aliases()));

        plugin.getGame().getCommandDispatcher().registerCommand(callable, plugin, aliases.toArray(new String[aliases.size()]));
    }

    /**
     * Converts a Sponge {@link org.spongepowered.api.util.command.CommandSource} to an internal {@link org.getlwc.command.CommandSender}
     * @param source
     * @return
     */
    private CommandSender commandSourceToSender(CommandSource source) {
        if (source instanceof org.spongepowered.api.entity.Player) {
            return plugin.wrapPlayer((org.spongepowered.api.entity.Player) source);
        } else {
            return  plugin.getEngine().getConsoleSender();
        }
    }

}
