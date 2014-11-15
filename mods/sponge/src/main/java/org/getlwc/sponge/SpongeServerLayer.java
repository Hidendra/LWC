package org.getlwc.sponge;

import org.getlwc.ServerLayer;
import org.getlwc.World;
import org.getlwc.command.Command;
import org.getlwc.entity.Player;
import org.getlwc.sponge.entity.SpongePlayer;
import org.getlwc.sponge.world.SpongeWorld;
import org.spongepowered.api.Game;

import java.io.File;
import java.util.UUID;

public class SpongeServerLayer extends ServerLayer {

    private Game game;

    public SpongeServerLayer(Game game) {
        this.game = game;
    }

    @Override
    public void onRegisterBaseCommand(String baseCommand, Command command) {
        // TODO sponge command will need to be registered
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
        return null;
    }

}
