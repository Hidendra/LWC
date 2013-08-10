package org.getlwc.canary.entity;

import org.getlwc.ItemStack;
import org.getlwc.Location;
import org.getlwc.canary.LWC;
import org.getlwc.entity.Player;
import org.getlwc.util.Color;

public class CanaryPlayer extends Player {

    /**
     * The plugin object
     */
    private LWC plugin;

    /**
     * Canary player handle
     */
    private net.canarymod.api.entity.living.humanoid.Player handle;

    public CanaryPlayer(LWC plugin, net.canarymod.api.entity.living.humanoid.Player handle) {
        this.plugin = plugin;
        this.handle = handle;
    }

    public String getName() {
        return handle.getName();
    }

    public void sendMessage(String message) {
        for (String line : message.split("\n")) {
            handle.message(Color.replaceColors(line));
        }
    }

    public boolean hasPermission(String node) {
        return handle.hasPermission(node);
    }

    public Location getLocation() {
        return new Location(null, handle.getX(), handle.getY(), handle.getZ());
    }

    @Override
    public ItemStack getItemInHand() {
        return plugin.castItemStack(handle.getItemHeld());
    }

}
