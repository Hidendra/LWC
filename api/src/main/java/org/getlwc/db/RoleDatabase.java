package org.getlwc.db;

import org.getlwc.model.Protection;
import org.getlwc.role.ProtectionRole;

import java.util.Set;

public interface RoleDatabase {

    /**
     * Load all of a protection's roles
     *
     * @param protection
     * @return
     */
    public Set<ProtectionRole> loadProtectionRoles(Protection protection);

    /**
     * Save a role to the database. If the role needs to be created, this method is required
     * to create it in the database as well.
     *
     * @param role
     */
    public void saveOrCreateRole(ProtectionRole role);

    /**
     * Remove a role from the database
     *
     * @param role
     */
    public void removeRole(ProtectionRole role);

    /**
     * Remove all roles for a protection from the database
     *
     * @param protection
     */
    public void removeRoles(Protection protection);

}
