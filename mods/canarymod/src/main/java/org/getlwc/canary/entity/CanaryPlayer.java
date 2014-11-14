package org.getlwc.canary.entity;

import org.getlwc.ItemStack;
import org.getlwc.Location;
import org.getlwc.canary.LWC;
import org.getlwc.entity.Player;
import org.getlwc.entity.SimplePlayer;
import org.getlwc.lang.Locale;
import org.getlwc.util.Color;

import java.util.UUID;

public class CanaryPlayer extends SimplePlayer {

    private LWC plugin;

    /**
     * native Canary handle
     */
    private net.canarymod.api.entity.living.humanoid.Player handle;

    public CanaryPlayer(LWC plugin, net.canarymod.api.entity.living.humanoid.Player handle) {
        this.plugin = plugin;
        this.handle = handle;
    }

    public UUID getUUID() {
        return handle.getUUID();
    }

    public String getName() {
        return handle.getName();
    }

    public void sendMessage(String message) {
        for (String line : message.split("\n")) {
            handle.message(Color.replaceColors(line));
        }
    }

    public Location getLocation() {
        return new Location(plugin.getWorld(handle.getWorld().getName()), handle.getX(), handle.getY(), handle.getZ());
    }

    @Override
    public ItemStack getItemInHand() {
        return plugin.castItemStack(handle.getItemHeld());
    }

}
