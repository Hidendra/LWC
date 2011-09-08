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
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class NijiPermissions implements IPermissions {

    /**
     * The permissions plugin instance
     */
    private Permissions plugin;

    /**
     * The Permissions handler
     */
    private PermissionHandler handler = null;

    public NijiPermissions() {
        Plugin permissionsPlugin = Bukkit.getServer().getPluginManager().getPlugin("Permissions");

        if (permissionsPlugin == null) {
            return;
        }

        plugin = (Permissions) permissionsPlugin;
        handler = plugin.getHandler();
    }

    public boolean isActive() {
        return handler != null;
    }

    public boolean permission(Player player, String node) {
        return handler != null && handler.permission(player, node);
    }

    public List<String> getGroups(Player player) {
        if (handler == null) {
            return null;
        }

        List<String> groups = new ArrayList<String>();
        groups.add(handler.getGroup(player.getWorld().getName(), player.getName()));

        // TODO: when semver comparator is done, check for >= 3.0.0 (if so, get all the player's group, otherwise
        //       fallback onto single group methods)

        // return handler.getPrimaryGroup(player.getWorld().getName(), player.getName()); // perm 3.0+
        return groups;
    }

}
