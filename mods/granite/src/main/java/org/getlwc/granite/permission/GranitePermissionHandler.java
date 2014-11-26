package org.getlwc.granite.permission;

import org.getlwc.entity.Player;
import org.getlwc.permission.PermissionHandler;

import java.util.HashSet;
import java.util.Set;

public class GranitePermissionHandler implements PermissionHandler {

    @Override
    public String getName() {
        return "Granite (native)";
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean hasPermission(Player player, String node) {
        // GranitePlayer granitePlayer = (GranitePlayer) player;

        // TODO GraniteEntityPlayer.hasPermission throws an NPE lol
        // return granitePlayer.getHandle().hasPermission(node);

        if (!node.startsWith("lwc.mod") && !node.startsWith("lwc.admin")) {
            return true;
        } else {
            return false; // TODO true if op
        }
    }

    @Override
    public Set<String> getGroups(Player player) {
        return new HashSet<>(); // none
    }

}
