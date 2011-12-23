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

package com.griefcraft.integration.currency;

import com.griefcraft.integration.ICurrency;
import com.griefcraft.util.config.Configuration;
import cosine.boseconomy.BOSEconomy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BOSECurrency implements ICurrency {

    /**
     * The BOSEconomy plugin instance
     */
    private BOSEconomy handler;

    /**
     * The economy configuration
     */
    private Configuration configuration = Configuration.load("iconomy.yml");

    /**
     * The server account to use
     */
    private String serverAccount;

    public BOSECurrency() {
        handler = (BOSEconomy) Bukkit.getServer().getPluginManager().getPlugin("BOSEconomy");
        serverAccount = configuration.getString("iConomy.serverBankAccount", "");

        // create the bank if it does not exist
        if (!serverAccount.isEmpty() && !handler.bankExists(serverAccount)) {
            handler.createBank(serverAccount);
        }
    }

    public boolean isActive() {
        return true;
    }

    public boolean usingCentralBank() {
        return !serverAccount.isEmpty();
    }

    public String format(double money) {
        return handler.getMoneyFormatted(money);
    }

    public String getMoneyName() {
        return handler.getMoneyName();
    }

    public double getBalance(Player player) {
        if (player == null) {
            return 0;
        }

        return handler.getPlayerMoneyDouble(player.getName());
    }

    public boolean canAfford(Player player, double money) {
        return player != null && getBalance(player) >= money;

    }

    public boolean canCentralBankAfford(double money) {
        return handler.getBankMoneyDouble(serverAccount) >= money;
    }

    public double addMoney(Player player, double money) {
        if (player == null) {
            return 0;
        }

        // remove the money from the central bank if applicable
        if (usingCentralBank()) {
            if (!canCentralBankAfford(money)) {
                return 0;
            }

            handler.addBankMoney(serverAccount, -money, true);
        }

        handler.addPlayerMoney(player.getName(), money, false);

        return getBalance(player);
    }

    public double removeMoney(Player player, double money) {
        // we're removing money, so it should be positive
        if (money > 0) {
            money = -money;
        }

        // add the money to the central bank if applicable
        if (usingCentralBank()) {
            handler.addBankMoney(serverAccount, money, true);
        }

        handler.addPlayerMoney(player.getName(), money, false);

        return getBalance(player);
    }

}
