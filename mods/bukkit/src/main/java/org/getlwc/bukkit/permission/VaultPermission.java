package org.getlwc.bukkit.permission;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.getlwc.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class VaultPermission extends SuperPermsPermission {

    /**
     * The permission handle for Vault
     */
    private net.milkbowl.vault.permission.Permission permission;

    public VaultPermission() {
        checkVault();
    }

    public boolean isEnabled() {
        return checkVault();
    }

    public boolean hasPermission(Player player, String node) {
        if (isEnabled()) {
            org.bukkit.entity.Player handle = Bukkit.getPlayer(player.getName());
            return permission.has(handle, node);
        }

        return false;
    }

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
     * so we check for Vault every time we need to use it if we do not already have the Economy object.
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
