package org.getlwc.sponge;

import com.google.common.base.Optional;
import org.getlwc.ServerLayer;
import org.getlwc.World;
import org.getlwc.command.Command;
import org.getlwc.command.CommandContext;
import org.getlwc.command.CommandSender;
import org.getlwc.command.ConsoleCommandSender;
import org.getlwc.entity.Player;
import org.getlwc.sponge.entity.SpongePlayer;
import org.getlwc.sponge.world.SpongeWorld;
import org.spongepowered.api.Game;
import org.spongepowered.api.util.command.CommandCallable;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.Description;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class SpongeServerLayer extends ServerLayer {

    private SpongePlugin plugin;
    private Game game;

    public SpongeServerLayer(SpongePlugin plugin, Game game) {
        this.plugin = plugin;
        this.game = game;
    }

    @Override
    public void onRegisterBaseCommand(final String baseCommand, final Command command) {
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

        game.getCommandDispatcher().registerCommand(callable, plugin, aliases.toArray(new String[aliases.size()]));
    }

    @Override
    public File getEngineHomeFolder() {
        // TODO better way to get data folder when it's available
        return new File("plugins/LWC/");
    }

    @Override
    public World getDefaultWorld() {
        return getWorld(game.getWorlds().iterator().next().getName());
    }

    @Override
    protected Player internalGetPlayer(String playerName) {
        return new SpongePlayer(game.getPlayer(playerName).orNull());
    }

    @Override
    protected World internalGetWorld(String worldName) {
        return new SpongeWorld(game.getWorld(worldName));
    }

    @Override
    public UUID getOfflinePlayer(String ident) {
        // TODO
        return null;
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
