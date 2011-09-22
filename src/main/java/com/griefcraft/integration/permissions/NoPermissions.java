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
import org.bukkit.entity.Player;

import java.util.List;

public class NoPermissions implements IPermissions {

    public boolean isActive() {
        return false;
    }

    public boolean permission(Player player, String node) {
        throw new UnsupportedOperationException("No active permissions system");
    }

    public List<String> getGroups(Player player) {
        throw new UnsupportedOperationException("No active permissions system");
    }

}
