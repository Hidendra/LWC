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

import com.griefcraft.bukkit.LWCiConomyPlugin;
import com.griefcraft.integration.ICurrency;
import com.griefcraft.model.History;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.util.Colors;
import com.griefcraft.util.config.Configuration;
import com.iConomy.iConomy;
import com.iConomy.util.Constants;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class iConomyModule extends JavaModule {

    private Logger logger = Logger.getLogger("LWC");

    /**
     * The iConomy module configuration
     */
    private Configuration configuration = Configuration.load("iconomy.yml");

    /**
     * The bukkit plugin
     */
    private LWCiConomyPlugin plugin;

    /**
     * A cache of prices. When a value is inputted, it stays in memory for milliseconds at best.
     * The best way to do this? ha, probably not
     */
    private Map<Location, Double> priceCache = Collections.synchronizedMap(new HashMap<Location, Double>());

    public iConomyModule(LWCiConomyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPostRegistration(LWC lwc, Protection protection) {
        if (!configuration.getBoolean("iConomy.enabled", true)) {
            return;
        }

        // we need to inject the iconomy price into the transaction!
        Block block = protection.getBlock();

        // Uh-oh! This REALLY should never happen ... !
        if(block == null || !priceCache.containsKey(block.getLocation())) {
            logger.severe("LWC-iConomy POST_REGISTRATION has encountered a severe problem!");
            return;
        }

        Location location = block.getLocation();

        // okey, get how much they were charged
        double charge = priceCache.get(location);

        // get related transactions..
        List<History> transactions = protection.getRelatedHistory(History.Type.TRANSACTION);

        // this really should not happen either (never!)
        if(transactions.size() == 0) {
            logger.severe("LWC-iConomy POST_REGISTRATION encountered a severen problem!: transactions.size() == 0");
        }

        // get the last entry
        History history = transactions.get(transactions.size() - 1);

        // add the price and save it
        history.addMetaData("iconomy=" + charge);
        history.save();

        // we no longer need the value in the price cache :)
        priceCache.remove(location);
    }

    @Override
    public void onPostRemoval(LWC lwc, Protection protection) {
        if (!configuration.getBoolean("iConomy.enabled", true)) {
            return;
        }

        // first, do we still have a currency processor?
        if(!lwc.getCurrency().isActive()) {
            return;
        }

        // we need to refund them, load up transactions
        List<History> transactions = protection.getRelatedHistory(History.Type.TRANSACTION);

        for(History history : transactions) {
            if(history.getStatus() == History.Status.INACTIVE) {
                continue;
            }

            String metadata = history.getMetaDataStartsWith("iconomy=");

            if(metadata == null) {
                continue;
            }

            // We have a match!
            String[] split = metadata.split("=");
            double charge = 0.00d;

            try {
                charge = Double.parseDouble(split[1]);
            } catch(NumberFormatException e) {
                logger.severe("uh-oh! Error parsing metadata: " + metadata + "; history=" + history.getId());
                continue;
            }

            // No need to refund if it's negative or 0
            if(charge <= 0) {
                continue;
            }

            // refund them :)
            Player owner = protection.getBukkitOwner();

            lwc.getCurrency().addMoney(owner, charge);
            owner.sendMessage(Colors.Green + "You have been refunded " + iConomy.format(charge) + " because an LWC protection of yours was removed!");
        }
    }

    @Override
    public Result onRegisterProtection(LWC lwc, Player player, Block block) {
        if (!configuration.getBoolean("iConomy.enabled", true)) {
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
        } catch (NumberFormatException e) {
        }

        // check if they have a discount available
        try {
            boolean isDiscountActive = Boolean.parseBoolean(resolveValue(player, "discount.active"));

            if (isDiscountActive) {
                int discountedProtections = Integer.parseInt(resolveValue(player, "discount.amount"));

                if (discountedProtections > 0) {
                    int currentProtections = lwc.getPhysicalDatabase().getProtectionCount(player.getName());

                    if (discountedProtections > currentProtections) {
                        charge = Double.parseDouble(resolveValue(player, "discount.newCharge"));
                        usedDiscount = true;
                    }
                }
            }
        } catch (NumberFormatException e) {
        }

        // used for price cache
        Location location = block.getLocation();

        // cache the charge momentarily
        priceCache.put(location, charge);

        // It's free!
        if (charge == 0) {
            player.sendMessage(Colors.Green + "This one's on us!");
            return ALLOW;
        }

        // charge them
        if (charge != 0) {
            // the currency handler to use
            ICurrency currency = lwc.getCurrency();
            
            if (!currency.canAfford(player, charge)) {
                player.sendMessage(Colors.Red + "You do not have enough " + Constants.Major.get(1) + " to buy an LWC protection.");
                player.sendMessage(Colors.Red + "The balance required for an LWC protection is: " + iConomy.format(charge));
                
                // remove from cache
                priceCache.remove(location);
                return CANCEL;
            }

            // remove the money from their account
            currency.removeMoney(player, charge);
            player.sendMessage(Colors.Green + "Charged " + iConomy.format(charge) + (usedDiscount ? (Colors.Red + " (Discount)" + Colors.Green) : "") + " for an LWC protection. Thank you.");
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
    private String resolveValue(Player player, String node) {
        LWC lwc = LWC.getInstance();

        // check if we have permissions
        boolean hasPermissions = lwc.getPermissions() != null;

        // resolve the limits type
        String value;

        // try the player
        value = configuration.getString("players." + player.getName() + "." + node);

        // try permissions
        if (value == null && hasPermissions) {
            String groupName = lwc.getPermissions().getGroup(player);

            if (groupName != null && !groupName.isEmpty()) {
                value = map("groups." + groupName + "." + node);
            }
        }

        // if all else fails, use master
        if (value == null) {
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

        if (value == null) {
            int lastIndex = path.lastIndexOf(".");
            String node = path.substring(lastIndex + 1);

            value = configuration.getString("iConomy." + node);
        }

        return value;
    }

}
