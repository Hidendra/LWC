package com.griefcraft.roles;

import com.griefcraft.Engine;
import com.griefcraft.ProtectionAccess;
import com.griefcraft.Role;
import com.griefcraft.entity.Player;
import com.griefcraft.model.Protection;

public class PlayerRole extends Role {

    public PlayerRole(Engine engine, Protection protection, String roleName, ProtectionAccess roleAccess) {
        super(engine, protection, roleName, roleAccess);
    }

    @Override
    public int getType() {
        return 1; // adapted from LWCv4
    }

    public ProtectionAccess getAccess(Protection protection, Player player) {
        return player.getName().equalsIgnoreCase(getRoleName()) ? getRoleAccess() : ProtectionAccess.NONE;
    }

}
