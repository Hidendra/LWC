package org.getlwc.canary.entity;

import org.getlwc.ItemStack;
import org.getlwc.Location;
import org.getlwc.canary.LWC;
import org.getlwc.entity.Player;
import org.getlwc.lang.Locale;
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
        loadLocale();
    }

    /**
     * Load the player's locale
     */
    private void loadLocale() {
        setLocale(new Locale(handle.getLocale()));
        plugin.getEngine().getConsoleSender().sendMessage("Player " + getName() + " loaded using locale: " + getLocale());
    }

    /**
     * {@inheritDoc}
     */
    public String getUUID() {
        // TODO: convert to unique id upon public availability of 1.7
        return handle.getName();
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return handle.getName();
    }


    /**
     * {@inheritDoc}
     */
    public void sendMessage(String message) {
        for (String line : message.split("\n")) {
            handle.message(Color.replaceColors(line));
        }
    }


    /**
     * {@inheritDoc}
     */
    public boolean hasPermission(String node) {
        return handle.hasPermission(node);
    }


    /**
     * {@inheritDoc}
     */
    public Location getLocation() {
        return new Location(plugin.getWorld(handle.getWorld().getName()), handle.getX(), handle.getY(), handle.getZ());
    }

    @Override
    public ItemStack getItemInHand() {
        return plugin.castItemStack(handle.getItemHeld());
    }

}
