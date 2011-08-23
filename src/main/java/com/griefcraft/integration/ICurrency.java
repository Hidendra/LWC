package com.griefcraft.integration;

import org.bukkit.entity.Player;

public interface ICurrency {

    /**
     * @return true if a currency is active
     */
    public boolean isActive();

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
     * Add money to a player's bank account
     *
     * @param player
     * @param money
     * @return the balance after modifying the player's account
     */
    public double addMoney(Player player, double money);

    /**
     * Remove money from a player's bank account
     *
     * @param player
     * @param money
     * @return the balance after modifying the player's account
     */
    public double removeMoney(Player player, double money);

}
