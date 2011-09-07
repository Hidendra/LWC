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

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;
import com.griefcraft.integration.ICurrency;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class EssentialsCurrency implements ICurrency {

    /**
     * The Essentials plugin object
     */
    private Essentials essentials;

    public EssentialsCurrency() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Essentials");

        if(plugin == null) {
            return;
        }

        essentials = (Essentials) plugin;
    }

    public boolean isActive() {
        return true;
    }

    public boolean supportsServerAccount() {
        return false;
    }

    public String format(double money) {
        return Economy.format(money);
    }

    public String getMoneyName() {
        return essentials.getSettings().getCurrencySymbol();
    }

    public double getBalance(Player player) {
        if(player == null) {
            return 0;
        }

        try {
            return Economy.getMoney(player.getName());
        } catch(UserDoesNotExistException e) {
            return 0d;
        }
    }

    public boolean canAfford(Player player, double money) {
        if(player == null) {
            return false;
        }

        try {
            return Economy.hasEnough(player.getName(), money);
        } catch(UserDoesNotExistException e) {
            return false;
        }
    }

    public boolean canServerAccountAfford(double money) {
        return false;
    }

    public double addMoney(Player player, double money) {
        if(player == null) {
            return 0;
        }

        try {
            Economy.add(player.getName(), money);
        } catch(UserDoesNotExistException e) {
            return 0;
        } catch(NoLoanPermittedException e) {
            return 0;
        }

        return getBalance(player);
    }

    public double removeMoney(Player player, double money) {
        try {
            Economy.subtract(player.getName(), money);
        } catch(UserDoesNotExistException e) {
            return 0;
        } catch(NoLoanPermittedException e) {
            return 0;
        }

        return getBalance(player);
    }

}
