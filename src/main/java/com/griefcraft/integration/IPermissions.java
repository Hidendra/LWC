package com.griefcraft.integration;

import org.bukkit.entity.Player;

import java.util.List;

public interface IPermissions {

    /**
     * Get the groups a player belongs to
     *
     * @param player
     * @return
     */
    public List<String> getGroups(Player player);

}
