package org.getlwc.sponge;

import org.getlwc.ServerLayer;
import org.getlwc.World;
import org.getlwc.entity.Player;
import org.getlwc.sponge.entity.SpongePlayer;
import org.getlwc.sponge.world.SpongeWorld;
import org.spongepowered.api.Game;

import java.io.File;
import java.util.UUID;

public class SpongeServerLayer extends ServerLayer {

    private SpongePlugin plugin;
    private Game game;

    public SpongeServerLayer(SpongePlugin plugin, Game game) {
        this.plugin = plugin;
        this.game = game;
    }

    @Override
    public File getDataFolder() {
        String path = SpongeServerLayer.class.getProtectionDomain().getCodeSource().getLocation().getPath();

        if (path.startsWith("file:")) {
            path = path.substring(5);
        }

        // drive letters (windows)
        if (path.charAt(0) == '\\' && path.charAt(2) == ':') {
            path = path.substring(3);
        } else if (path.charAt(1) == ':') {
            path = path.substring(2);
        }

        int index = path.indexOf(".jar!");

        if (index != -1) {
            path = path.substring(0, index + 4);
        }

        File runningFromJar = new File(path);
        return new File(new File(runningFromJar.getParentFile().getParent(), "config"), "LWC");
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
