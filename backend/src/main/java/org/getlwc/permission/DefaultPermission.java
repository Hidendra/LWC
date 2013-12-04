package org.getlwc.permission;

import org.getlwc.entity.Player;

import java.util.Set;

public class DefaultPermission implements Permission {

    public boolean isEnabled() {
        return false;
    }

    public boolean hasPermission(Player player, String permission) {
        throw new UnsupportedOperationException("No Permission is installed");
    }

    public Set<String> getGroups(Player player) {
        throw new UnsupportedOperationException("No Permission is installed");
    }

}
