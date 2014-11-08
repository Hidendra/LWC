package org.getlwc.db;

import org.getlwc.Location;
import org.getlwc.model.Metadata;
import org.getlwc.model.Protection;

import java.util.Set;

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

    /**
     * Load all of a protection's attributes from the database
     *
     * @param protection
     * @return
     */
    public Set<Metadata> loadProtectionMetadata(Protection protection);

    /**
     * Save or create an attribute in the database.
     *
     * @param meta
     * @param protection
     * @param meta
     */
    public void saveOrCreateProtectionMetadata(Protection protection, Metadata meta);

    /**
     * Remove a protection's attribute from the database
     *
     * @param meta
     * @param protection
     * @param meta
     */
    public void removeProtectionMetadata(Protection protection, Metadata meta);

    /**
     * Remove all protection attributes from a protection from the database
     *
     * @param protection
     */
    public void removeAllProtectionMetadata(Protection protection);

}
