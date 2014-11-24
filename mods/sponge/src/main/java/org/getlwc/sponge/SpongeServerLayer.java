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

}
