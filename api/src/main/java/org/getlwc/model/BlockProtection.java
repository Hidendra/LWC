package org.getlwc.model;

import org.getlwc.Engine;
import org.getlwc.Location;

/**
 * TODO support changing the location of the protection
 */
public class BlockProtection extends Protection {

    /**
     * The location being protected by the protection
     */
    private Location location;

    public BlockProtection(Engine engine, int id, Location location) {
        super(engine, id);
        this.location = location;
    }

    @Override
    public String toString() {
        // TODO add in updated, created
        return String.format("BlockProtection(id=%d, location=%s)", getId(), location.toString());
    }

    /**
     * Get the location this protection is currently centred at
     *
     * @return
     */
    public Location getLocation() {
        return location;
    }

}
