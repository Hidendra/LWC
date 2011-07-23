package com.griefcraft.integration.currency;

import org.bukkit.entity.Player;

import com.griefcraft.integration.ICurrency;

public class NoCurrency implements ICurrency {

	public boolean isActive() {
		return false;
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
