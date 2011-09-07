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
import org.bukkit.entity.Player;

public class NoCurrency implements ICurrency {

    public boolean isActive() {
        return false;
    }

    public boolean usingCentralBank() {
        return false;
    }

    public String format(double money) {
        return Double.toString(money);
    }

    public String getMoneyName() {
        return "";
    }

    public double getBalance(Player player) {
        return 0;
    }

    public boolean canAfford(Player player, double money) {
        return false;
    }

    public boolean canCentralBankAfford(double money) {
        return false;
    }

    public double addMoney(Player player, double money) {
        return 0;
    }

    public double removeMoney(Player player, double money) {
        return 0;
    }

}
