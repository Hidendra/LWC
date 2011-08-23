package com.griefcraft.integration.currency;

import com.griefcraft.integration.ICurrency;
import org.bukkit.entity.Player;

public class NoCurrency implements ICurrency {

    public boolean isActive() {
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

    public double addMoney(Player player, double money) {
        return 0;
    }

    public double removeMoney(Player player, double money) {
        return 0;
    }

}
