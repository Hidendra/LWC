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

package com.griefcraft.lwc;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.griefcraft.bukkit.BukkitPlugin;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.util.Colors;
import com.griefcraft.util.config.Configuration;
import com.iConomy.iConomy;
import com.iConomy.system.Account;
import com.iConomy.system.BankAccount;
import com.iConomy.system.Holdings;
import com.iConomy.util.Constants;

public class iConomyModule extends JavaModule {

	/**
	 * The iConomy module configuration
	 */
	private Configuration configuration = Configuration.load("iconomy.yml");
	
	/**
	 * The bukkit plugin
	 */
	private BukkitPlugin plugin;
	
	public iConomyModule(BukkitPlugin plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public Result onRegisterProtection(LWC lwc, Player player, Block block) {
		if(!configuration.getBoolean("iConomy.enabled", true)) {
			return DEFAULT;
		}

        // if a discount was used
        boolean usedDiscount = false;
		
		// how much to charge the player
		double charge = 0D;
		
		// attempt to resolve the new charge
		try {
			String value = resolveValue(player, "charge");
			charge = Double.parseDouble(value);
		} catch(NumberFormatException e) { }

        // check if they have a discount available
        try {
            boolean isDiscountActive = Boolean.parseBoolean(resolveValue(player, "discount.active"));

            if(isDiscountActive) {
                int discountedProtections = Integer.parseInt(resolveValue(player, "discount.amount"));

                if(discountedProtections > 0) {
                    int currentProtections = lwc.getPhysicalDatabase().getProtectionCount(player.getName());

                    if(discountedProtections > currentProtections) {
                        charge = Double.parseDouble(resolveValue(player, "discount.newCharge"));
                        usedDiscount = true;
                    }
                }
            }
        } catch(NumberFormatException e) { }

        System.out.println("charge=" + charge);

        // It's free!
        if(charge == 0) {
            player.sendMessage(Colors.Green + "This one's on us!");
            return ALLOW;
        }

		// charge them
		if(charge != 0) {
			// get the player's account
			String playerName = player.getName();
			
			if(!iConomy.hasAccount(playerName)) {
				player.sendMessage(Colors.Red + "You do not have an iConomy Bank Account!");
				plugin.info(":: WARNING :: Player " + playerName + " does not have an iConomy Bank Account (TRIED TO CREATE LWC PROTECTION)");
				return CANCEL;
			}
			
			Account account = iConomy.getAccount(playerName);
			
			// attempt to withdrawl from their holdings
			Holdings holdings = account.getHoldings();
			
			if(!holdings.hasEnough(charge)) {
				player.sendMessage(Colors.Red + "You do not have enough " + Constants.Major.get(1) + " to buy an LWC protection.");
				player.sendMessage(Colors.Red + "The balance required for an LWC protection is: " + iConomy.format(charge));
				return CANCEL;
			}
			
			// remove the money from their account
			holdings.subtract(charge);
			player.sendMessage(Colors.Green + "Charged " + iConomy.format(charge) + (usedDiscount ? (Colors.Red + " (Discount) " + Colors.Green) : "") + "for an LWC protection. Thank you.");
			return ALLOW;
		}
		
		return DEFAULT;
	}
	
	/**
	 * Resolve a configuration node for a player. Tries nodes in this order:
	 * <pre>
	 * players.PLAYERNAME.node
	 * groups.GROUPNAME.node
	 * iConomy.node
	 * </pre>
	 * 
	 * @param player
	 * @param node
	 * @return
	 */
	public String resolveValue(Player player, String node) {
		LWC lwc = LWC.getInstance();
		
		// check if we have permissions
		boolean hasPermissions = lwc.getPermissions() != null;
		
		// resolve the limits type
		String value = null;
		
		// try the player
		value = configuration.getString("players." + player.getName() + "." + node);
		
		// try permissions
		if(value == null && hasPermissions) {
			String groupName = lwc.getPermissions().getGroup(player.getWorld().getName(), player.getName());
			
			if(groupName != null && !groupName.isEmpty()) {
				value = map("groups." + groupName + "." + node);
			}
		}
		
		// if all else fails, use master
		if(value == null) {
			value = map("iConomy." + node);
		}
		
		return value != null && !value.isEmpty() ? value : "";
	}
	
	/**
	 * Get the value from either the path or the default value if it's null
	 * 
	 * @param path
	 * @return
	 */
	private String map(String path) {
		String value = configuration.getString(path);
		
		if(value == null) { 
			int lastIndex = path.lastIndexOf(".");
			String node = path.substring(lastIndex + 1);
			
			value = configuration.getString("iConomy." + node);
		}
		
		return value;
	}

}
