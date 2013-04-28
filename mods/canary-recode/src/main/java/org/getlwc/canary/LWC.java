package org.getlwc.canary;

import net.canarymod.Canary;
import net.canarymod.plugin.Plugin;
import org.getlwc.Engine;
import org.getlwc.ServerLayer;
import org.getlwc.SimpleEngine;
import org.getlwc.canary.listeners.CanaryListener;

public class LWC extends Plugin {

    /**
     * Internal engine handle
     */
    private Engine engine;

    /**
     * The Canary server layer
     */
    private final ServerLayer layer = new CanaryServerLayer(this);

    @Override
    public boolean enable() {
        engine = SimpleEngine.createEngine(layer, new CanaryServerInfo(), new CanaryConsoleCommandSender());

        // Hooks
        Canary.hooks().registerListener(new CanaryListener(this), this);

        return true;
    }

    @Override
    public void disable() {
    }

    /**
     * @return the {@link Engine} object
     */
    public Engine getEngine() {
        return engine;
    }

    /**
     * Wrap a native Canary player
     *
     * @param player
     * @return
     */
    public org.getlwc.entity.Player wrapPlayer(net.canarymod.api.entity.living.humanoid.Player player) {
        return layer.getPlayer(player.getName());
    }

    /**
     * Get a World object for the native Canary world
     *
     * @param worldName
     * @return
     */
    public org.getlwc.World getWorld(String worldName) {
        return layer.getWorld(worldName);
    }

}
