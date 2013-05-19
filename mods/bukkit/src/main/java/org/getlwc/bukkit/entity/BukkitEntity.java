package org.getlwc.bukkit.entity;

import org.getlwc.Location;
import org.getlwc.bukkit.BukkitPlugin;
import org.getlwc.entity.Entity;

public class BukkitEntity implements Entity {

    /**
     * The plugin object
     */
    private final BukkitPlugin plugin;

    /**
     * The native handle
     */
    private org.bukkit.entity.Entity handle;

    public BukkitEntity(BukkitPlugin plugin, org.bukkit.entity.Entity handle) {
        if (handle == null) {
            throw new IllegalArgumentException("Entity handle cannot be null");
        }

        this.plugin = plugin;
        this.handle = handle;
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return handle.getType().getName();
    }

    /**
     * {@inheritDoc}
     */
    public Location getLocation() {
        org.bukkit.Location lhandle = handle.getLocation();
        return new Location(plugin.getWorld(lhandle.getWorld().getName()), lhandle.getX(), lhandle.getY(), lhandle.getZ());
    }

}
