package org.getlwc.bukkit.permission;

import org.bukkit.Bukkit;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.getlwc.entity.Player;
import org.getlwc.permission.PermissionHandler;

import java.util.HashSet;
import java.util.Set;

public class SuperPermsPermissionHandler implements PermissionHandler {

    /**
     * The prefix for groups when using permissions
     */
    private static final String GROUP_PREFIX = "group.";

    @Override
    public String getName() {
        return "Default";
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean hasPermission(Player player, String node) {
        org.bukkit.entity.Player handle = Bukkit.getPlayer(player.getName());
        return handle != null && handle.hasPermission(node);
    }

    @Override
    public Set<String> getGroups(Player player) {
        org.bukkit.entity.Player handle = Bukkit.getPlayer(player.getName());
        Set<String> groups = new HashSet<String>();

        for (PermissionAttachmentInfo pai : handle.getEffectivePermissions()) {
            if (pai.getPermission().startsWith(GROUP_PREFIX)) {
                groups.add(pai.getPermission().substring(GROUP_PREFIX.length()));
            }
        }

        return groups;
    }

}
