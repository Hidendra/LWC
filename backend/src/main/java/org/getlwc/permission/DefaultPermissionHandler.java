package org.getlwc.permission;

import org.getlwc.entity.Player;

import java.util.Set;

public class DefaultPermissionHandler implements PermissionHandler {

    public String getName() {
        return "None";
    }

    public boolean isEnabled() {
        return false;
    }

    public boolean hasPermission(Player player, String node) {
        throw new UnsupportedOperationException("No PermissionHandler is installed");
    }

    public Set<String> getGroups(Player player) {
        throw new UnsupportedOperationException("No PermissionHandler is installed");
    }

}
