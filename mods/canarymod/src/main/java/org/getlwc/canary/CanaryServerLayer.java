package org.getlwc.canary;

import net.canarymod.Canary;
import org.getlwc.ServerLayer;
import org.getlwc.World;
import org.getlwc.canary.entity.CanaryPlayer;
import org.getlwc.canary.world.CanaryWorld;
import org.getlwc.entity.Player;

import java.io.File;
import java.util.UUID;

public class CanaryServerLayer extends ServerLayer {

    /**
     * Canary plugin object
     */
    private CanaryPlugin plugin;

    public CanaryServerLayer(CanaryPlugin plugin) {
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
    public UUID getOfflinePlayer(String ident) {
        Player player = getPlayer(ident);

        if (player != null) {
            return player.getUUID();
        }

        net.canarymod.api.OfflinePlayer offlinePlayer = Canary.getServer().getOfflinePlayer(ident);

        if (offlinePlayer != null) {
            return offlinePlayer.getUUID();
        } else {
            return null;
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

}
