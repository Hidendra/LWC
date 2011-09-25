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

    public boolean usingCentralBank() {
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

    public boolean canCentralBankAfford(double money) {
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
