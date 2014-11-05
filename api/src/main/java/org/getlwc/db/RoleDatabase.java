package org.getlwc.db;

import org.getlwc.model.Protection;
import org.getlwc.role.Role;

import java.util.Set;

public interface RoleDatabase {

    /**
     * Load all of a protection's roles
     *
     * @param protection
     * @return
     */
    public Set<Role> loadProtectionRoles(Protection protection);

    /**
     * Save a role to the database. If the role needs to be created, this method is required
     * to create it in the database as well.
     *
     * @param protection
     * @param role
     */
    public void saveOrCreateProtectionRole(Protection protection, Role role);

    /**
     * Remove a role from the database
     *
     * @param protection
     * @param role
     */
    public void removeProtectionRole(Protection protection, Role role);

    /**
     * Remove all roles for a protection from the database
     *
     * @param protection
     */
    public void removeAllProtectionRoles(Protection protection);

}
