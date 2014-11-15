package org.getlwc.sponge.entity;

import org.getlwc.Location;
import org.getlwc.entity.Entity;

import java.util.UUID;

public class SpongeEntity implements Entity {

    /**
     * native Sponge handle
     */
    private org.spongepowered.api.entity.Entity handle;

    public SpongeEntity(org.spongepowered.api.entity.Entity handle) {
        this.handle = handle;
    }

    @Override
    public UUID getUUID() {
        // TODO
        throw new UnsupportedOperationException("getUUID() is not yet supported");
    }

    @Override
    public String getName() {
        // TODO
        throw new UnsupportedOperationException("getName() is not yet supported");
    }

    @Override
    public Location getLocation() {
        // TODO Entity does not even expose the world it's in yet
        return new Location(null, handle.getX(), handle.getY(), handle.getZ());
    }

}
