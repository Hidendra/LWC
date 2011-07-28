package com.griefcraft.integration.permissions;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.griefcraft.integration.IPermissions;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class NijiPermissions implements IPermissions {

	/**
	 * The Permissions handler
	 */
	private PermissionHandler handler = null;
	
	public NijiPermissions() {
		Plugin permissionsPlugin = Bukkit.getServer().getPluginManager().getPlugin("Permissions");
		
		if(permissionsPlugin == null) {
			return;
		}
		
		handler = ((Permissions) permissionsPlugin).getHandler();
	}
	
	public boolean isActive() {
		return handler != null;
	}
	
	public boolean permission(Player player, String node) {
		if(handler == null) {
			return false;
		}
		
		return handler.permission(player, node);
	}

	public String getGroup(Player player) {
		if(handler == null) {
			return null;
		}
		
		// return handler.getPrimaryGroup(player.getWorld().getName(), player.getName());
		return handler.getGroup(player.getWorld().getName(), player.getName());
	}

}
