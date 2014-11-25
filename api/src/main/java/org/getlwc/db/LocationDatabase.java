package org.getlwc.db;

import org.getlwc.Location;
import org.getlwc.model.Protection;

import java.util.Set;

/**
 * Locations typically represent blocks in the world for a given protection.
 * These map protected blocks to a given protection.
 */
public interface LocationDatabase {

    /**
     * Returns the locations the protection has blocks at
     *
     * @param protection
     * @return
     */
    public Set<Location> loadProtectionLocations(Protection protection);

    /**
     * Adds a location to the given protection
     *
     * @param protection
     * @param location
     */
    public void addProtectionLocation(Protection protection, Location location);

    /**
     * Removes a location from the given protection
     *
     * @param protection
     * @param location
     */
    public void removeProtectionLocation(Protection protection, Location location);

    /**
     * Removes all locations from a given protection
     *
     * @param protection
     */
    public void removeAllProtectionLocations(Protection protection);

}
