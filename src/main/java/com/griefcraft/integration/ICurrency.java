package com.griefcraft.integration;

import org.bukkit.entity.Player;

public interface ICurrency {

    /**
     * @return true if a currency is active
     */
    public boolean isActive();

    /**
     * @return true if the Economy plugin can support the server account feature
     */
    public boolean usingCentralBank();

    /**
     * Format money
     *
     * @param money
     * @return
     */
    public String format(double money);

    /**
     * Get the money name (e.g dollars)
     *
     * @return
     */
    public String getMoneyName();

    /**
     * Get the current balance for a player
     *
     * @param player
     * @return
     */
    public double getBalance(Player player);

    /**
     * Check the player's money to see if they can afford that
     * amount of money <b>without</b> going negative.
     *
     * @param player
     * @param money
     * @return
     */
    public boolean canAfford(Player player, double money);

    /**
     * Check if the server account can afford the amount of money given
     *
     * @param money
     * @return true if the server account has a balance equal or greater to the money given
     */
    public boolean canCentralBankAfford(double money);

    /**
     * Add money to a player's bank account
     * If server account banking is enabled, the money is automatically withdrawn from the configured bank!
     *
     * @param player
     * @param money
     * @return the balance after modifying the player's account
     */
    public double addMoney(Player player, double money);

    /**
     * Remove money from a player's bank account
     * If server account banking is enabled, the money is automatically added to the configured bank!
     *
     * @param player
     * @param money
     * @return the balance after modifying the player's account
     */
    public double removeMoney(Player player, double money);

}
