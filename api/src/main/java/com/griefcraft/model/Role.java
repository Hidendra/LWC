package com.griefcraft.model;

import com.griefcraft.ProtectionAccess;
import com.griefcraft.entity.Player;

public interface Role {

    /**
     * Get a unique integer that will be used to represent this role
     * @return
     */
    public int getId();

    /**
     * Get the protection that this role is for
     * @return
     */
    public Protection getProtection();

    /**
     * Get the name of the role this defines, e.g a player's name for a PlayerRole
     * @return
     */
    public String getRoleName();

    /**
     * Get the access level for a player using this role
     * @param player
     * @return
     */
    public ProtectionAccess getAccess(Player player);

}
