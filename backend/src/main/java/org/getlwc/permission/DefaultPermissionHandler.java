package org.getlwc.permission;

import org.getlwc.entity.Player;

import java.util.Set;

public class DefaultPermissionHandler implements PermissionHandler {

    @Override
    public String getName() {
        return "None";
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public boolean hasPermission(Player player, String node) {
        throw new UnsupportedOperationException("No PermissionHandler is installed");
    }

    @Override
    public Set<String> getGroups(Player player) {
        throw new UnsupportedOperationException("No PermissionHandler is installed");
    }

}
