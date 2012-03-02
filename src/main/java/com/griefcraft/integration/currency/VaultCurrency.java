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
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class VaultCurrency implements ICurrency {

    /**
     * The economy services handler
     */
    private Economy economy;

    public VaultCurrency() {
        economy = Bukkit.getServer().getServicesManager().getRegistration(Economy.class).getProvider();
    }

    @Override
    public boolean isActive() {
        return economy != null;
    }

    @Override
    public boolean usingCentralBank() {
        return false;
    }

    @Override
    public String format(double money) {
        return economy.format(money);
    }

    @Override
    public String getMoneyName() {
        return economy.currencyNameSingular();
    }

    @Override
    public double getBalance(Player player) {
        return economy.getBalance(player.getName());
    }

    @Override
    public boolean canAfford(Player player, double money) {
        return economy.has(player.getName(), money);
    }

    @Override
    public boolean canCentralBankAfford(double money) {
        return false;
    }

    @Override
    public double addMoney(Player player, double money) {
        economy.depositPlayer(player.getName(), money);
        return getBalance(player);
    }

    @Override
    public double removeMoney(Player player, double money) {
        economy.withdrawPlayer(player.getName(), money);
        return getBalance(player);
    }
}
