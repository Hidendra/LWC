package com.griefcraft.model;

import com.griefcraft.AccessProvider;

public interface Role extends AccessProvider {

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

}
