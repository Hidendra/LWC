package org.getlwc.bukkit.economy;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.getlwc.economy.EconomyHandler;
import org.getlwc.entity.Player;

public class VaultEconomyHandler implements EconomyHandler {

    /**
     * The economy instance for vault
     */
    private net.milkbowl.vault.economy.Economy economy;

    public VaultEconomyHandler() {
        checkVault();
    }

    public String getName() {
        return "Vault";
    }

    public boolean isEnabled() {
        return checkVault();
    }

    public String format(double amount) {
        if (isEnabled()) {
            return economy.format(amount);
        }

        return "n/a";
    }

    public boolean deposit(Player player, double amount) {
        if (isEnabled()) {
            EconomyResponse response = economy.depositPlayer(player.getName(), amount);
            return response.transactionSuccess();
        }

        return false;
    }

    public boolean withdraw(Player player, double amount) {
        if (isEnabled()) {
            EconomyResponse response = economy.withdrawPlayer(player.getName(), amount);
            return response.transactionSuccess();
        }

        return false;
    }

    /**
     * Check that Vault is running properly. This is done because Vault may load after LWC initially loads
     * so we check for Vault every time we need to use it if we do not already have the EconomyHandler object.
     *
     * @return
     */
    private boolean checkVault() {
        if (economy != null) {
            return true;
        }

        RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);

        if (rsp == null) {
            return false;
        }

        economy = rsp.getProvider();
        return economy != null;
    }

}
