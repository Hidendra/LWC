package org.getlwc.canary.entity;

import org.getlwc.ItemStack;
import org.getlwc.Location;
import org.getlwc.canary.CanaryPlugin;
import org.getlwc.entity.SimplePlayer;
import org.getlwc.util.Color;

import java.util.UUID;

public class CanaryPlayer extends SimplePlayer {

    private CanaryPlugin plugin;

    /**
     * native Canary handle
     */
    private net.canarymod.api.entity.living.humanoid.Player handle;

    public CanaryPlayer(CanaryPlugin plugin, net.canarymod.api.entity.living.humanoid.Player handle) {
        this.plugin = plugin;
        this.handle = handle;
    }

    @Override
    public UUID getUUID() {
        return handle.getUUID();
    }

    @Override
    public String getName() {
        return handle.getName();
    }

    @Override
    public void sendMessage(String message) {
        for (String line : message.split("\n")) {
            handle.message(Color.replaceColors(line));
        }
    }

    @Override
    public Location getLocation() {
        return new Location(plugin.getWorld(handle.getWorld().getName()), handle.getX(), handle.getY(), handle.getZ());
    }

    @Override
    public ItemStack getItemInHand() {
        return plugin.castItemStack(handle.getItemHeld());
    }

}
