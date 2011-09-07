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
import com.griefcraft.lwc.LWC;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.ArrayList;
import java.util.List;

public class SuperPermsPermissions implements IPermissions {

    /**
     * The group prefix to use to lookup in Permissions - can be overrided in core.yml with groupPrefix: 'new.prefix.'
     * Must include leading period (.)
     * 
     * Default: lwc.group.
     */
    private String groupPrefix;

    public SuperPermsPermissions() {
        groupPrefix = LWC.getInstance().getConfiguration().getString("core.groupPrefix", "lwc.group.");
    }

    public boolean isActive() {
        return true;
    }

    public boolean permission(Player player, String node) {
        return player.hasPermission(node);
    }

    // modified implementation by ZerothAngel ( https://github.com/Hidendra/LWC/issues/88#issuecomment-2017807 )
    public List<String> getGroups(Player player) {
        List<String> groups = new ArrayList<String>();

        for (PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
            if(pai.getPermission().startsWith(groupPrefix)) {
                groups.add(pai.getPermission().substring(groupPrefix.length()));
            }
        }

        return groups;
    }

}
