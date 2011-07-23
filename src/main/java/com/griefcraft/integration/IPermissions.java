package com.griefcraft.integration;

import org.bukkit.entity.Player;

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
	 * Get the group the selected player is located in.
	 * 
	 * @param player
	 * @return
	 */
	public String getGroup(Player player);
	
}
