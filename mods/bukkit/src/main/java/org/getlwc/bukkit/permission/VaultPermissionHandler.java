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
import org.bukkit.plugin.RegisteredServiceProvider;
import org.getlwc.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class VaultPermissionHandler extends SuperPermsPermissionHandler {

    /**
     * The permission handle for Vault
     */
    private net.milkbowl.vault.permission.Permission permission;

    public VaultPermissionHandler() {
        checkVault();
    }

    @Override
    public String getName() {
        return "Vault";
    }

    @Override
    public boolean isEnabled() {
        return checkVault();
    }

    @Override
    public boolean hasPermission(Player player, String node) {
        if (isEnabled()) {
            org.bukkit.entity.Player handle = Bukkit.getPlayer(player.getName());
            return permission.has(handle, node);
        }

        return false;
    }

    @Override
    public Set<String> getGroups(Player player) {
        Set<String> groups = new HashSet<String>();

        if (isEnabled()) {
            org.bukkit.entity.Player handle = Bukkit.getPlayer(player.getName());
            try {
                String[] vaultGroups = permission.getPlayerGroups(handle);

                // fall back to superperms when in doubt
                if (vaultGroups == null || vaultGroups.length == 0) {
                    return super.getGroups(player);
                }

                Collections.addAll(groups, vaultGroups);
            } catch (Exception e) {
                // getPlayerGroups like to throw this
            }
        }

        return groups;
    }

    /**
     * Check that Vault is running properly. This is done because Vault may load after LWC initially loads
     * so we check for Vault every time we need to use it if we do not already have the EconomyHandler object.
     *
     * @return
     */
    private boolean checkVault() {
        if (permission != null) {
            return true;
        }

        RegisteredServiceProvider<net.milkbowl.vault.permission.Permission> rsp = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);

        if (rsp == null) {
            return false;
        }

        permission = rsp.getProvider();
        return permission != null;
    }

}
