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
import com.iCo6.Constants;
import com.iCo6.iConomy;
import com.iCo6.system.Account;
import com.iCo6.system.Accounts;
import com.iCo6.system.Holdings;
import org.bukkit.entity.Player;

public class iConomy6Currency implements ICurrency {

    /**
     * The object used to obtain accounts
     */
    private final Accounts accounts = new Accounts();

    /**
     * The economy configuration
     */
    private Configuration configuration = Configuration.load("iconomy.yml");

    /**
     * The server account to use
     */
    private String serverAccount;

    public iConomy6Currency() {
        serverAccount = configuration.getString("iConomy.serverBankAccount", "");

        // create the account in iConomy if needed
        if (!serverAccount.isEmpty()) {
            accounts.get(serverAccount);
        }
    }

    public boolean isActive() {
        return true;
    }

    public boolean usingCentralBank() {
        return !serverAccount.isEmpty();
    }

    public String format(double money) {
        return iConomy.format(money);
    }

    public String getMoneyName() {
        return Constants.Nodes.Major.getStringList().get(1);
    }

    public double getBalance(Player player) {
        if (player == null) {
            return 0;
        }

        Account account = accounts.get(player.getName());

        if (account == null) {
            return 0;
        }

        return account.getHoldings().getBalance();
    }

    public boolean canAfford(Player player, double money) {
        if (player == null) {
            return false;
        }

        Account account = accounts.get(player.getName());

        return account != null && account.getHoldings().hasEnough(money);
    }

    public boolean canCentralBankAfford(double money) {
        if (!usingCentralBank()) {
            return true;
        }

        Account account = accounts.get(serverAccount);

        return account != null && account.getHoldings().hasEnough(money);
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

            Account central = accounts.get(serverAccount);

            if (central == null) {
                return 0;
            }

            central.getHoldings().subtract(money);
        }

        Account account = accounts.get(player.getName());

        if (account == null) {
            return 0;
        }

        Holdings holdings = account.getHoldings();
        holdings.add(money);

        return holdings.getBalance();
    }

    public double removeMoney(Player player, double money) {
        if (player == null) {
            return 0;
        }

        // we're removing money, so it should be positive
        if (money < 0) {
            money = -money;
        }

        // add the money to the central bank if applicable
        if (usingCentralBank()) {
            Account central = accounts.get(serverAccount);

            if (central == null) {
                return 0;
            }

            central.getHoldings().add(money);
        }

        Account account = accounts.get(player.getName());

        if (account == null) {
            return 0;
        }

        Holdings holdings = account.getHoldings();

        // this SHOULD be a transaction, ensure they have enough
        if (!holdings.hasEnough(money)) {
            return holdings.getBalance();
        }

        holdings.subtract(money);

        return holdings.getBalance();
    }

}