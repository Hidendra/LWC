package com.griefcraft.roles;

import com.griefcraft.ProtectionAccess;
import com.griefcraft.entity.Player;
import com.griefcraft.model.Protection;
import com.griefcraft.model.Role;

public class PlayerRole implements Role {

    /**
     * The protection this role is for
     */
    private Protection protection;

    /**
     * The role name for the player to grant access to
     */
    private String roleName;

    /**
     * The access to grant to players that match this role
     */
    private ProtectionAccess roleAccess;

    public PlayerRole(Protection protection, String roleName, ProtectionAccess roleAccess) {
        this.protection = protection;
        this.roleName = roleName;
        this.roleAccess = roleAccess;
    }

    public int getId() {
        return 1; // adapted from LWCv4
    }

    public Protection getProtection() {
        return protection;
    }

    public String getRoleName() {
        return roleName;
    }

    public ProtectionAccess getAccess(Player player) {
        return player.getName().equalsIgnoreCase(roleName) ? roleAccess : ProtectionAccess.NONE;
    }

}
