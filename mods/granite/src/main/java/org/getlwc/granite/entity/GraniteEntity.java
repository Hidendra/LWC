package org.getlwc.granite.entity;

import org.getlwc.Location;
import org.getlwc.entity.Entity;
import org.getlwc.granite.world.GraniteWorld;

import java.util.UUID;

public class GraniteEntity extends Entity {

    private org.granitemc.granite.api.entity.Entity handle;

    public GraniteEntity(org.granitemc.granite.api.entity.Entity handle) {
        this.handle = handle;
    }

    @Override
    public UUID getUUID() {
        return handle.getUniqueID();
    }

    @Override
    public String getName() {
        return handle.getName();
    }

    @Override
    public Location getLocation() {
        org.granitemc.granite.api.utils.Location locationHandle = handle.getLocation();

        // TODO avoid recreating GraniteWorld everytime
        return new Location(new GraniteWorld(locationHandle.getWorld()), locationHandle.getX(), locationHandle.getY(), locationHandle.getZ());
    }

}
