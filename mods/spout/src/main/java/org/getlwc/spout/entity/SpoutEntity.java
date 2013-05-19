package org.getlwc.spout.entity;

import org.getlwc.Location;
import org.getlwc.entity.Entity;
import org.getlwc.spout.SpoutPlugin;
import org.spout.api.geo.discrete.Point;

public class SpoutEntity implements Entity {

    /**
     * The plugin object
     */
    private final SpoutPlugin plugin;

    /**
     * The native handle
     */
    private org.spout.api.entity.Entity handle;

    public SpoutEntity(SpoutPlugin plugin, org.spout.api.entity.Entity handle) {
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
        return handle.getClass().getSimpleName();
    }

    /**
     * {@inheritDoc}
     */
    public Location getLocation() {
        Point phandle = handle.getScene().getPosition();
        return new Location(plugin.getWorld(phandle.getWorld().getName()), phandle.getX(), phandle.getY(), phandle.getZ());
    }

}
