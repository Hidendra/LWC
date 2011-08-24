/**
 * This file is part of LWC (https://github.com/Hidendra/LWC)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.griefcraft.integration.permissions;

import com.griefcraft.integration.IPermissions;
import com.platymuus.bukkit.permissions.Group;
import com.platymuus.bukkit.permissions.PermissionsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * BukkitPermissions is supported by the CraftBukkit Recommended Build #1000+ ONLY
 */
public class BukkitPermissions implements IPermissions {

    /**
     * The PermissionsBukkit handler
     */
    private PermissionsPlugin handler = null;

    public BukkitPermissions() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("PermissionsBukkit");

        if (plugin == null) {
            return;
        }

        handler = (PermissionsPlugin) plugin;
    }

    public boolean isActive() {
        return handler != null;
    }

    public boolean permission(Player player, String node) {
        return player.hasPermission(node);
    }

    public List<String> getGroups(Player player) {
        if (handler == null) {
            return null;
        }

        List<Group> found = handler.getGroups(player.getName());
        List<String> groups = new ArrayList<String>(found.size());

        if (found.size() == 0) {
            return groups;
        }

        // add in the groups
        for (Group group : found) {
            groups.add(group.getName());
        }

        return groups;
    }

}
