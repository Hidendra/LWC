/*
 * Copyright 2011 Tyler Blair. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and contributors and should not be interpreted as representing official policies,
 * either expressed or implied, of anybody else.
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
