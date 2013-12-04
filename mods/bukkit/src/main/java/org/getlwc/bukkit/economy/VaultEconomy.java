package org.getlwc.bukkit.economy;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.getlwc.economy.Economy;
import org.getlwc.entity.Player;

public class VaultEconomy implements Economy {

    /**
     * The economy instance for vault
     */
    private net.milkbowl.vault.economy.Economy economy;

    public VaultEconomy() {
        checkVault();
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return "Vault";
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEnabled() {
        return checkVault();
    }

    /**
     * {@inheritDoc}
     */
    public String format(double amount) {
        if (isEnabled()) {
            return economy.format(amount);
        }

        return "n/a";
    }

    /**
     * {@inheritDoc}
     */
    public boolean deposit(Player player, double amount) {
        if (isEnabled()) {
            EconomyResponse response = economy.depositPlayer(player.getName(), amount);
            return response.transactionSuccess();
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean withdraw(Player player, double amount) {
        if (isEnabled()) {
            EconomyResponse response = economy.withdrawPlayer(player.getName(), amount);
            return response.transactionSuccess();
        }

        return false;
    }

    /**
     * Check that Vault is running properly. This is done because Vault may load after LWC initially loads
     * so we check for Vault every time we need to use it if we do not already have the Economy object.
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
