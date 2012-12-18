package com.griefcraft;

import com.griefcraft.model.Protection;

public interface RoleDefinition {

    /**
     * Get the unique role id to be associated with this role
     *
     * @return
     */
    public int getId();

    /**
     * Create a {@link Role}
     *
     * @param protection
     * @param roleName
     * @param roleAccess
     * @return
     */
    public Role createRole(Protection protection, String roleName, ProtectionAccess roleAccess);

}
