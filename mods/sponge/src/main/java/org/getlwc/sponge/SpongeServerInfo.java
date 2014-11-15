package org.getlwc.sponge;

import org.getlwc.ServerInfo;
import org.spongepowered.api.Game;

public class SpongeServerInfo implements ServerInfo {

    private Game game;

    public SpongeServerInfo(Game game) {
        this.game = game;
    }

    @Override
    public String getServerImplementationTitle() {
        return "Sponge";
    }

    @Override
    public String getServerImplementationVersion() {
        return game.getImplementationVersion();
    }

}
