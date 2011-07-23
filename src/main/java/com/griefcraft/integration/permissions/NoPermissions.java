package com.griefcraft.integration.permissions;

import org.bukkit.entity.Player;

import com.griefcraft.integration.IPermissions;

public class NoPermissions implements IPermissions {

	public boolean isActive() {
		return false;
	}

	public boolean permission(Player player, String node) {
		return false;
	}

	public String getGroup(Player player) {
		return null;
	}

}
