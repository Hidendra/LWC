/**
 * This file is part of LWC (https://github.com/Hidendra/LWC)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.griefcraft.integration.currency;

import com.griefcraft.integration.ICurrency;
<<<<<<< HEAD
import com.iConomy.iConomy;
import com.iConomy.system.Account;
import com.iConomy.system.Holdings;
import com.iConomy.util.Constants;
=======
import com.griefcraft.util.config.Configuration;
import com.iCo6.Constants;
import com.iCo6.iConomy;
import com.iCo6.system.Account;
import com.iCo6.system.Accounts;
import com.iCo6.system.Holdings;
>>>>>>> c15b234... Add iConomy6 support
import org.bukkit.entity.Player;

public class iConomy6Currency implements ICurrency {

    /**
     * The object used to obtain accounts
     */
    private final Accounts accounts = new Accounts();

<<<<<<< HEAD
=======
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
        if(!serverAccount.isEmpty()) {
            accounts.get(serverAccount);
        }
    }

>>>>>>> c15b234... Add iConomy6 support
    public boolean isActive() {
        return true;
    }

    public String format(double money) {
        return iConomy.format(money);
    }

    public String getMoneyName() {
        return Constants.Nodes.Major.getStringList().get(1);
    }

    public double getBalance(Player player) {
<<<<<<< HEAD
        Account account = iConomy.getAccount(player.getName());
=======
        if(player == null) {
            return 0;
        }

        Account account = accounts.get(player.getName());
>>>>>>> c15b234... Add iConomy6 support

        if (account == null) {
            return 0;
        }

        return account.getHoldings().getBalance();
    }

    public boolean canAfford(Player player, double money) {
<<<<<<< HEAD
        Account account = iConomy.getAccount(player.getName());

        return account != null && account.getHoldings().hasEnough(money);
=======
        if(player == null) {
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
>>>>>>> c15b234... Add iConomy6 support

    }

    public double addMoney(Player player, double money) {
<<<<<<< HEAD
        Account account = iConomy.getAccount(player.getName());
=======
        if(player == null) {
            return 0;
        }

        // remove the money from the central bank if applicable
        if(usingCentralBank()) {
            if (!canCentralBankAfford(money)) {
                return 0;
            }

            Account central = accounts.get(serverAccount);

            if(central == null) {
                return 0;
            }

            central.getHoldings().subtract(money);
        }

        Account account = accounts.get(player.getName());
>>>>>>> c15b234... Add iConomy6 support

        if (account == null) {
            return 0;
        }

        Holdings holdings = account.getHoldings();
        holdings.add(money);

        return holdings.getBalance();
    }

    public double removeMoney(Player player, double money) {
        // we're removing money, so it should be positive
        if (money < 0) {
            money = -money;
        }

<<<<<<< HEAD
        Account account = iConomy.getAccount(player.getName());
=======
        // add the money to the central bank if applicable
        if(usingCentralBank()) {
            Account central = accounts.get(serverAccount);

            if(central == null) {
                return 0;
            }

            central.getHoldings().add(money);
        }

        Account account = accounts.get(player.getName());
>>>>>>> c15b234... Add iConomy6 support

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
