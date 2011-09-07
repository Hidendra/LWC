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

import com.griefcraft.bukkit.LWCEconomyPlugin;
import com.griefcraft.integration.ICurrency;
import com.griefcraft.model.History;
import com.griefcraft.model.LWCPlayer;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCProtectionDestroyEvent;
import com.griefcraft.scripting.event.LWCProtectionRegisterEvent;
import com.griefcraft.scripting.event.LWCProtectionRegistrationPostEvent;
import com.griefcraft.scripting.event.LWCProtectionRemovePostEvent;
import com.griefcraft.util.Colors;
import com.griefcraft.util.config.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class EconomyModule extends JavaModule {

    private Logger logger = Logger.getLogger("LWC");

    /**
     * The iConomy module configuration
     */
    private Configuration configuration = Configuration.load("iconomy.yml");

    /**
     * The bukkit plugin
     */
    private LWCEconomyPlugin plugin;

    /**
     * A cache of prices. When a value is inputted, it stays in memory for milliseconds at best.
     * The best way to do this? ha, probably not
     */
    private Map<Location, String> priceCache = Collections.synchronizedMap(new HashMap<Location, String>());

    public EconomyModule(LWCEconomyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onDestroyProtection(LWCProtectionDestroyEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!configuration.getBoolean("iConomy.enabled", true)) {
            return;
        }

        // is refunding enabled?
        if (!configuration.getBoolean("iConomy.refunds", true)) {
            return;
        }

        if(!LWC.getInstance().isHistoryEnabled()) {
            return;
        }

        LWC lwc = event.getLWC();
        Protection protection = event.getProtection();
        Player player = event.getPlayer();

        // first, do we still have a currency processor?
        if (!lwc.getCurrency().isActive()) {
            return;
        }

        // Does it support the server bank feature
        if (!lwc.getCurrency().usingCentralBank()) {
            return;
        }

        // load the transactions so we can check the server bank
        List<History> transactions = protection.getRelatedHistory(History.Type.TRANSACTION);

        for (History history : transactions) {
            if (history.getStatus() == History.Status.INACTIVE) {
                continue;
            }

            // obtain the charge
            double charge = history.getDouble("charge");

            // No need to refund if it's negative or 0
            if (charge <= 0) {
                continue;
            }

            // check the server bank
            if (!lwc.getCurrency().canCentralBankAfford(charge)) {
                player.sendMessage(Colors.Red + "The Server's Bank does not contain enough funds to remove that protection!");
                event.setCancelled(true);
                return;
            }
        }
    }

    @Override
    public void onPostRegistration(LWCProtectionRegistrationPostEvent event) {
        if (!configuration.getBoolean("iConomy.enabled", true)) {
            return;
        }

        if(!LWC.getInstance().isHistoryEnabled()) {
            return;
        }

        Protection protection = event.getProtection();

        // we need to inject the iconomy price into the transaction!
        Block block = protection.getBlock();

        // Uh-oh! This REALLY should never happen ... !
        if (block == null || !priceCache.containsKey(block.getLocation())) {
            logger.severe("LWC-iConomy POST_REGISTRATION has encountered a severe problem!");
            return;
        }

        Location location = block.getLocation();

        // okey, get how much they were charged
        String cachedPrice = priceCache.get(location);

        boolean usedDiscount = cachedPrice.startsWith("d");
        double charge = Double.parseDouble(usedDiscount ? cachedPrice.substring(1) : cachedPrice);

        // get related transactions..
        List<History> transactions = protection.getRelatedHistory(History.Type.TRANSACTION);

        // this really should not happen either (never!)
        if (transactions.size() == 0) {
            logger.severe("LWC-iConomy POST_REGISTRATION encountered a severen problem!: transactions.size() == 0");
        }

        // get the last entry
        History history = transactions.get(transactions.size() - 1);

        // add the price
        history.addMetaData("charge=" + charge);

        // was it a discount?
        if(usedDiscount) {
            history.addMetaData("discount=true");
        }

        // save it
        history.sync();

        // we no longer need the value in the price cache :)
        priceCache.remove(location);
    }

    @Override
    public void onPostRemoval(LWCProtectionRemovePostEvent event) {
        if (!configuration.getBoolean("iConomy.enabled", true)) {
            return;
        }

        // is refunding enabled?
        if (!configuration.getBoolean("iConomy.refunds", true)) {
            return;
        }

        if(!LWC.getInstance().isHistoryEnabled()) {
            return;
        }

        LWC lwc = event.getLWC();
        Protection protection = event.getProtection();

        // first, do we still have a currency processor?
        if (!lwc.getCurrency().isActive()) {
            return;
        }

        // we need to refund them, load up transactions
        List<History> transactions = protection.getRelatedHistory(History.Type.TRANSACTION);

        for (History history : transactions) {
            if (history.getStatus() == History.Status.INACTIVE) {
                continue;
            }

            // obtain the charge
            double charge = history.getDouble("charge");

            // No need to refund if it's negative or 0
            if (charge <= 0) {
                continue;
            }

            // refund them :)
            Player owner = Bukkit.getServer().getPlayer(history.getPlayer());

            // the currency to use
            ICurrency currency = lwc.getCurrency();

            currency.addMoney(owner, charge);
            owner.sendMessage(Colors.Green + "You have been refunded " + currency.format(charge) + " because an LWC protection of yours was removed!");
        }
    }

    @Override
    public void onRegisterProtection(LWCProtectionRegisterEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!configuration.getBoolean("iConomy.enabled", true)) {
            return;
        }

        LWC lwc = event.getLWC();
        Block block = event.getBlock();
        Player player = event.getPlayer();

        // currency handler to use
        ICurrency currency = lwc.getCurrency();

        if (!currency.isActive()) {
            return;
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
                double wouldCharge = Double.parseDouble(resolveValue(player, "discount.newCharge"));

                if (discountedProtections > 0) {
                    String discountType = resolveValue(player, "discount.type");
                    int currentProtections = 0;
                    boolean isExactDiscountType = true;

                    if(discountType.equalsIgnoreCase("TOTAL")) {
                        isExactDiscountType = false;
                    }

                    if(isExactDiscountType) {
                        currentProtections = getDiscountedProtections(lwc, player, wouldCharge);
                    } else {
                        currentProtections = lwc.getPhysicalDatabase().getProtectionCount(player.getName());
                    }

                    if (discountedProtections > currentProtections) {
                        charge = wouldCharge;
                        usedDiscount = true;
                    }
                }
            }
        } catch (NumberFormatException e) {
        }

        // used for price cache
        Location location = block.getLocation();

        // cache the charge momentarily
        if(lwc.isHistoryEnabled()) {
            priceCache.put(location, (usedDiscount ? "d" : "") + charge);
        }
        
        // It's free!
        if (charge == 0) {
            player.sendMessage(Colors.Green + "This one's on us!");
            return;
        }

        // charge them
        if (charge != 0) {
            if (!currency.canAfford(player, charge)) {
                player.sendMessage(Colors.Red + "You do not have enough " + currency.getMoneyName() + " to buy an LWC protection.");
                player.sendMessage(Colors.Red + "The balance required for an LWC protection is: " + currency.format(charge));

                // remove from cache
                priceCache.remove(location);
                event.setCancelled(true);
                return;
            }

            // remove the money from their account
            currency.removeMoney(player, charge);
            player.sendMessage(Colors.Green + "Charged " + currency.format(charge) + (usedDiscount ? (Colors.Red + " (Discount)" + Colors.Green) : "") + " for an LWC protection. Thank you.");
            return;
        }

        return;
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

        // check if we have a permissions plugin
        boolean hasPermissions = lwc.getPermissions().isActive();

        // resolve the limits type
        String value;

        // try the player
        value = configuration.getString("players." + player.getName() + "." + node);

        // try permissions
        if (value == null && hasPermissions) {
            for (String groupName : lwc.getPermissions().getGroups(player)) {
                if (groupName != null && !groupName.isEmpty() && value == null) {
                    value = map("groups." + groupName + "." + node);
                }
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

    /**
     * Get the amount of protections the player purchased for  the given discount price
     *
     * @param lwc
     * @param player
     * @param discountPrice
     * @return
     */
    private int getDiscountedProtections(LWC lwc, Player player, double discountPrice) {
        LWCPlayer lwcPlayer = lwc.wrapPlayer(player);
        List<History> related = lwcPlayer.getRelatedHistory(History.Type.TRANSACTION);

        if(related.size() == 0) {
            return 0;
        }

        int amount = 0;

        for(History history : related) {
            if(!history.getBoolean("discount")) {
                continue;
            }

            // obtain the charge
            double charge = history.getDouble("charge");

            if(charge == discountPrice) {
                amount ++;
            }
        }

        return amount;
    }

}
