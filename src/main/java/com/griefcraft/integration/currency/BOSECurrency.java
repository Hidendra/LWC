package com.griefcraft.integration.currency;

import com.griefcraft.integration.ICurrency;
import cosine.boseconomy.BOSEconomy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Created by IntelliJ IDEA.
 * User: hidendra
 * Date: 13/08/11
 * Time: 2:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class BOSECurrency implements ICurrency {

    /**
     * The BOSEconomy plugin instance
     */
    private BOSEconomy handler;

    public BOSECurrency() {
        handler = (BOSEconomy) Bukkit.getServer().getPluginManager().getPlugin("BOSEconomy");
    }

    public boolean isActive() {
		return true;
	}

    public String format(double money) {
        return handler.getMoneyFormatted(money);
    }

    public String getMoneyName() {
        return handler.getMoneyName();
    }

	public double getBalance(Player player) {
		return handler.getPlayerMoneyDouble(player.getName());
	}

	public boolean canAfford(Player player, double money) {
        return getBalance(player) >= money;
    }

	public double addMoney(Player player, double money) {
        handler.addPlayerMoney(player.getName(), money, false);
        
        return getBalance(player);
	}

	public double removeMoney(Player player, double money) {
		// we're removing money, so it should be positive
		if(money > 0) {
			money = -money;
		}

        handler.addPlayerMoney(player.getName(), money, false);

        return getBalance(player);
	}

}
