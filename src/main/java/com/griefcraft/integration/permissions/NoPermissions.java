package com.griefcraft.integration.permissions;

import com.griefcraft.integration.IPermissions;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class NoPermissions implements IPermissions {

    public boolean isActive() {
        return false;
    }

    public boolean permission(Player player, String node) {
        return false;
    }

    public List<String> getGroups(Player player) {
        return new ArrayList<String>();
    }

}
