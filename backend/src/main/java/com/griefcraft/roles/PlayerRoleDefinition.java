package com.griefcraft.roles;

import com.griefcraft.ProtectionAccess;
import com.griefcraft.model.Protection;
import com.griefcraft.model.Role;
import com.griefcraft.model.RoleDefinition;

public class PlayerRoleDefinition implements RoleDefinition {

    public int getId() {
        return 1; // adapted from LWCv4
    }

    public Role createRole(Protection protection, String roleName, ProtectionAccess roleAccess) {
        return new PlayerRole(protection, roleName, roleAccess);
    }

}
