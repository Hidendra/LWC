/**
 * Copyright (c) 2011-2014 Tyler Blair
 * All rights reserved.
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
        Set<String> groups = new HashSet<>();

        for (PermissionAttachmentInfo pai : handle.getEffectivePermissions()) {
            if (pai.getPermission().startsWith(GROUP_PREFIX)) {
                groups.add(pai.getPermission().substring(GROUP_PREFIX.length()));
            }
        }

        return groups;
    }

}
