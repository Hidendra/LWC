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

package com.griefcraft.integration.permissions;

import com.griefcraft.integration.IPermissions;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NijiPermissions implements IPermissions {

    /**
     * The Permissions handler
     */
    private PermissionHandler handler = null;

    public NijiPermissions() {
        Plugin permissionsPlugin = Bukkit.getServer().getPluginManager().getPlugin("Permissions");

        if (permissionsPlugin == null) {
            return;
        }

        handler = ((Permissions) permissionsPlugin).getHandler();
    }

    public boolean permission(Player player, String node) {
        return handler.has(player, node);
    }

    @SuppressWarnings("deprecation")
    public List<String> getGroups(Player player) {
        if (handler == null) {
            return null;
        }

        List<String> groups = new ArrayList<String>();

        // try Permissions 3 method
        try {
            groups.addAll(Arrays.asList(handler.getGroups(player.getWorld().getName(), player.getName())));
        } catch (NoSuchMethodError e) {
            // Fallback to Permissions 2
            groups.add(handler.getGroup(player.getWorld().getName(), player.getName()));
        }

        return groups;
    }

}
