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
