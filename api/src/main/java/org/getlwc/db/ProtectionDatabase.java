package org.getlwc.db;

import org.getlwc.Location;
import org.getlwc.model.Protection;

public interface ProtectionDatabase {

    /**
     * Create a protection in the world.
     *
     * @param location
     * @return
     */
    public Protection createProtection(Location location);

    /**
     * Load a protection from the database at the given location
     *
     * @return
     */
    public Protection loadProtection(Location location);

    /**
     * Load a protection from the database for the given id
     *
     * @param id
     * @return
     */
    public Protection loadProtection(int id);

    /**
     * Save a protection to the database
     *
     * @param protection
     */
    public void saveProtection(Protection protection);

    /**
     * Remove a protection and all associated data about it from the database
     *
     * @param protection
     */
    public void removeProtection(Protection protection);

}
