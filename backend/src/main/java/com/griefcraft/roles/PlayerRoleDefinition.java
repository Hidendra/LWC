package com.griefcraft.roles;

import com.griefcraft.Engine;
import com.griefcraft.ProtectionAccess;
import com.griefcraft.Role;
import com.griefcraft.RoleDefinition;
import com.griefcraft.model.Protection;

public class PlayerRoleDefinition implements RoleDefinition {

    /**
     * The engine instance
     */
    private Engine engine;

    public PlayerRoleDefinition(Engine engine) {
        this.engine = engine;
    }

    public int getId() {
        return 1; // adapted from LWCv4
    }

    public Role createRole(Protection protection, String roleName, ProtectionAccess roleAccess) {
        return new PlayerRole(engine, protection, roleName, roleAccess);
    }

}
