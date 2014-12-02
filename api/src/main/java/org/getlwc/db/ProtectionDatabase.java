package org.getlwc.db;

import org.getlwc.Location;
import org.getlwc.meta.Meta;
import org.getlwc.model.Protection;

import java.util.Set;

public interface ProtectionDatabase {

    /**
     * Create an empty protection not attached to anything.
     *
     * @return
     */
    public Protection createProtection();

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
    public Set<Meta> loadProtectionMetadata(Protection protection);

    /**
     * Save or create an attribute in the database.
     *
     * @param meta
     * @param protection
     * @param meta
     */
    public void saveOrCreateProtectionMetadata(Protection protection, Meta meta);

    /**
     * Remove a protection's attribute from the database
     *
     * @param meta
     * @param protection
     * @param meta
     */
    public void removeProtectionMetadata(Protection protection, Meta meta);

    /**
     * Remove all protection attributes from a protection from the database
     *
     * @param protection
     */
    public void removeAllProtectionMetadata(Protection protection);

}
