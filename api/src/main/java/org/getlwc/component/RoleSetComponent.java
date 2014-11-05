package org.getlwc.component;

import org.getlwc.role.Role;

public class RoleSetComponent extends AbstractSetComponent<Role> {

    /**
     * Returns a role of the same type and value from the protection
     *
     * @param matchRole
     * @return
     */
    public Role getSimilar(Role matchRole) {
        for (Role role : getAll()) {
            if (matchRole.getClass() == role.getClass() && role.serialize().equals(matchRole.serialize())) {
                return role;
            }
        }

        return null;
    }

}
