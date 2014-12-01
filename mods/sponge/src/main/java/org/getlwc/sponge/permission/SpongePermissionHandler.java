package org.getlwc.sponge.permission;

import org.getlwc.entity.Player;
import org.getlwc.permission.PermissionHandler;

import java.util.HashSet;
import java.util.Set;

public class SpongePermissionHandler implements PermissionHandler {

    @Override
    public String getName() {
        return "Sponge (Default)";
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean hasPermission(Player player, String node) {
        // TODO use Sponge's internal permission handler when it's available

        if (!node.startsWith("lwc.mod") && !node.startsWith("lwc.admin")) {
            return true;
        } else {
            return false; // TODO true if op
        }
    }

    @Override
    public Set<String> getGroups(Player player) {
        return new HashSet<>();
    }

}
