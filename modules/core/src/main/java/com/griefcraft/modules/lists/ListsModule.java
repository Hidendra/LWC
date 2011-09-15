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

package com.griefcraft.modules.lists;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.AccessRight;
import com.griefcraft.model.Protection;
import com.griefcraft.model.ProtectionTypes;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCAccessEvent;
import com.herocraftonline.dthielke.lists.Lists;
import com.herocraftonline.dthielke.lists.PrivilegedList;
import com.herocraftonline.dthielke.lists.PrivilegedList.PrivilegeLevel;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ListsModule extends JavaModule {

    /**
     * The com.griefcraft.modules.lists api
     */
    private Lists lists = null;

    @Override
    public void load(LWC lwc) {
        Plugin listsPlugin = lwc.getPlugin().getServer().getPluginManager().getPlugin("Lists");

        if (listsPlugin != null) {
            lists = (Lists) listsPlugin;
        }
    }

    @Override
    public void onAccessRequest(LWCAccessEvent event) {
        Player player = event.getPlayer();
        Protection protection = event.getProtection();
        
        if (protection.getType() != ProtectionTypes.PRIVATE) {
            return;
        }

        if (lists != null) {
            for (AccessRight right : protection.getAccessRights()) {
                if (right.getType() != AccessRight.LIST) {
                    continue;
                }

                String listName = right.getName();

                // load the list
                PrivilegedList privilegedList = lists.getList(listName);

                if (privilegedList != null) {
                    PrivilegeLevel privilegeLevel = privilegedList.get(player.getName());

                    // they have access in some way or another, let's allow them in
                    if (privilegeLevel != null) {
                        event.setAccess(AccessRight.RIGHT_PLAYER);
                    }
                }
            }
        }
    }

}
