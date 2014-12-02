package org.getlwc.canary.entity;

import org.getlwc.Location;
import org.getlwc.canary.CanaryPlugin;
import org.getlwc.entity.Entity;

import java.util.UUID;

public class CanaryEntity extends Entity {

    private CanaryPlugin plugin;

    /**
     * The entity's handle
     */
    private net.canarymod.api.entity.Entity handle;

    public CanaryEntity(CanaryPlugin plugin, net.canarymod.api.entity.Entity handle) {
        this.plugin = plugin;
        this.handle = handle;
    }

    @Override
    public UUID getUUID() {
        return handle.getUUID();
    }

    @Override
    public String getName() {
        return plugin.getName();
    }

    @Override
    public Location getLocation() {
        return new Location(plugin.getWorld(handle.getWorld().getName()), handle.getX(), handle.getY(), handle.getZ());
    }

}
