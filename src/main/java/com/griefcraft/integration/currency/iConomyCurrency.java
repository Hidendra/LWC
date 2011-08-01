package com.griefcraft.integration.currency;

import com.griefcraft.integration.ICurrency;
import com.iConomy.iConomy;
import com.iConomy.system.Account;
import com.iConomy.system.Holdings;
import org.bukkit.entity.Player;

public class iConomyCurrency implements ICurrency {
	
	public boolean isActive() {
		return true;
	}
	
	public double getBalance(Player player) {
		Account account = iConomy.getAccount(player.getName());
		
		if(account == null) {
			return 0;
		}
		
		return account.getHoldings().balance();
	}

	public boolean canAfford(Player player, double money) {
		Account account = iConomy.getAccount(player.getName());

        return account != null && account.getHoldings().hasEnough(money);

    }

	public double addMoney(Player player, double money) {
		Account account = iConomy.getAccount(player.getName());
		
		if(account == null) {
			return 0;
		}
		
		Holdings holdings = account.getHoldings();
		holdings.add(money);
		
		return holdings.balance();
	}

	public double removeMoney(Player player, double money) {
		// we're removing money, so it should be positive
		if(money < 0) {
			money = -money;
		}
		
		Account account = iConomy.getAccount(player.getName());
		
		if(account == null) {
			return 0;
		}
		
		Holdings holdings = account.getHoldings();
		
		// this SHOULD be a transaction, ensure they have enough
		if(!holdings.hasEnough(money)) {
			return holdings.balance();
		}
		
		holdings.subtract(money);
		
		return holdings.balance();
	}
	
}
