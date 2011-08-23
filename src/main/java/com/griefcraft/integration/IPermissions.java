package com.griefcraft.integration;

import org.bukkit.entity.Player;

import java.util.List;

public interface IPermissions {

    /**
     * @return true if permission handling is supported
     */
    public boolean isActive();

    /**
     * Check if a player has the specified permission node.
     *
     * @param player
     * @param node
     * @return
     */
    public boolean permission(Player player, String node);

    /**
     * Get the groups a player belongs to
     *
     * @param player
     * @return
     */
    public List<String> getGroups(Player player);

}
