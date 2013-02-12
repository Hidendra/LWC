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

package com.griefcraft.lwc;

import com.griefcraft.bukkit.LWCEconomyPlugin;
import com.griefcraft.integration.ICurrency;
import com.griefcraft.model.History;
import com.griefcraft.model.LWCPlayer;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCProtectionDestroyEvent;
import com.griefcraft.scripting.event.LWCProtectionInteractEvent;
import com.griefcraft.scripting.event.LWCProtectionRegisterEvent;
import com.griefcraft.scripting.event.LWCProtectionRegistrationPostEvent;
import com.griefcraft.scripting.event.LWCProtectionRemovePostEvent;
import com.griefcraft.util.Colors;
import com.griefcraft.util.config.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.InventoryHolder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class EconomyModule extends JavaModule {

    public enum DiscountType {

        /**
         * The discount will apply while the player has under X protections total.
         */
        TOTAL,

        /**
         * The discount will only give the player X discounted protections - no more.
         */
        EXACT,

        /**
         * The discount will apply while the player has under X DISCOUNTED protections total.
         * This is different from TOTAL because if they have 5 free protections, they get 5
         * for free, and if they remove one of the free ones, they will get 1 protection
         * for free.
         */
        IN_USE

    }

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
     * The discount type to use
     */
    private DiscountType discountType;

    /**
     * A cache of prices. When a value is inputted, it stays in memory for milliseconds at best.
     * The best way to do this? ha, probably not
     */
    private Map<Location, String> priceCache = Collections.synchronizedMap(new HashMap<Location, String>());

    /**
     * Our cache
     */
    private final Map<String, String> cache = new HashMap<String, String>();

    public EconomyModule(LWCEconomyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onProtectionInteract(LWCProtectionInteractEvent event) {
        if (event.getResult() != Result.DEFAULT || !event.canAccess()) {
            return;
        }

        if (!configuration.getBoolean("iConomy.enabled", true)) {
            return;
        }

        LWC lwc = event.getLWC();
        Protection protection = event.getProtection();
        Player player = event.getPlayer();

        // first, do we still have a currency processor?
        if (!lwc.getCurrency().isActive()) {
            return;
        }

        // is it actually a container? :p
        if (!(protection.getBlock().getState() instanceof InventoryHolder)) {
            return;
        }

        // Are they right clicking the chest (aka open) ?
        if (event.getEvent().getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // Usage fee
        double usageFee = resolveDouble(player, "usageFee", false);

        // No fee! :D
        if (usageFee <= 0) {
            return;
        }

        // Can they afford it?
        if (!lwc.getCurrency().canAfford(player, usageFee)) {
            // Nope!
            player.sendMessage(Colors.Red + "You need " + lwc.getCurrency().format(usageFee) + " to open your protection!");
            event.setResult(Result.CANCEL);
            return;
        }

        // Charge them!
        lwc.getCurrency().removeMoney(player, usageFee);
        player.sendMessage(Colors.Green + "You have been charged " + lwc.getCurrency().format(usageFee) + " to open your protection.");
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

        if (!LWC.getInstance().isHistoryEnabled()) {
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

        if (!LWC.getInstance().isHistoryEnabled()) {
            return;
        }

        Protection protection = event.getProtection();

        // we need to inject the iconomy price into the transaction!
        Block block = protection.getBlock();

        // Uh-oh! This REALLY should never happen ... !
        if (block == null || !priceCache.containsKey(block.getLocation())) {
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
        if (usedDiscount) {
            history.addMetaData("discount=true");

            // Was the discount's id non-null?
            String discountId = resolveValue(protection.getBukkitOwner(), "discount.id");

            if (!discountId.isEmpty()) {
                history.addMetaData("discountId=" + discountId);
            }
        }

        // save it immediately
        history.saveNow();

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

        if (!LWC.getInstance().isHistoryEnabled()) {
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

            // we can't pay them ..
            if (owner == null) {
                continue;
            }

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

        String value;
        // Check for a block override charge
        if ((value = lwc.resolveProtectionConfiguration(block.getType(), "charge")) != null) {
            try {
                charge = Double.parseDouble(value);
            } catch (NumberFormatException e) {
            }
        } else {
            charge = resolveDouble(player, "charge", false);
        }

        // check if they have a discount available
        try {
            boolean isDiscountActive = Boolean.parseBoolean(resolveValue(player, "discount.active"));

            if (isDiscountActive) {
                int discountedProtections = resolveInt(player, "discount.amount", true);
                double discountPrice = resolveDouble(player, "discount.newCharge", false);

                if (discountedProtections > 0) {
                    int currentProtections = 0;

                    // Match the discount type
                    DiscountType discountType = DiscountType.valueOf(resolveValue(player, "discount.type").toUpperCase());

                    // The unique id of the discount, by default they are shared between discounts if not set
                    String discountId = resolveValue(player, "discount.id");

                    // Count the protections
                    switch (discountType) {
                        case EXACT:
                            currentProtections = countDiscountedProtections(lwc, player, discountPrice, discountId.isEmpty() ? null : discountId, false);
                            break;

                        case TOTAL:
                            currentProtections = lwc.getPhysicalDatabase().getProtectionCount(player.getName());
                            break;

                        case IN_USE:
                            currentProtections = countDiscountedProtections(lwc, player, discountPrice, discountId.isEmpty() ? null : discountId, true);
                            break;
                    }

                    if (discountedProtections > currentProtections) {
                        charge = discountPrice;
                        usedDiscount = true;
                    }
                }
            }
        } catch (NumberFormatException e) {
        }

        // used for price cache
        Location location = block.getLocation();

        // cache the charge momentarily
        if (lwc.isHistoryEnabled()) {
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
     * @param sortHighest
     * @return
     */
    private int resolveInt(Player player, String node, boolean sortHighest) {
        return (int) resolveDouble(player, node, sortHighest);
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
     * @param sortHighest sort values by highest value
     * @return
     */
    private double resolveDouble(Player player, String node, boolean sortHighest) {
        LWC lwc = LWC.getInstance();
        double value = -1;
        
        String cacheKey = "resolve-" + player.getName() + node;
        if (cache.containsKey(cacheKey)) {
            return Double.parseDouble(cache.get(cacheKey));
        }

        // try the player
        try {
            value = Double.parseDouble(configuration.getString("players." + player.getName() + "." + node, "-1"));
        } catch (NumberFormatException e) { } // May I be forgiven in hell for that

        // try their groups
        if (value == -1) {
            for (String groupName : lwc.getPermissions().getGroups(player)) {
                if (groupName != null && !groupName.isEmpty()) {
                    try {
                        double v = Double.parseDouble(map("groups." + groupName + "." + node, "-1"));
                        
                        if (v == -1) {
                            continue;
                        }

                        // check the value
                        if (sortHighest && (v > value || value == -1)) {
                            value = v;
                        } else if (!sortHighest && (v < value || value == -1)) {
                            value = v;
                        }
                    } catch (NumberFormatException e) { }
                }
            }
        }

        // if all else fails, use master
        if (value == -1) {
            try {
                value = Double.parseDouble(map("iConomy." + node, "-1"));
            } catch (NumberFormatException e) { }
        }
        
        cache.put(cacheKey, Double.toString(value));
        return value;
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

        // resolve the limits type
        String value;

        // try the player
        value = configuration.getString("players." + player.getName() + "." + node, null);

        // try permissions
        if (value == null) {
            for (String groupName : lwc.getPermissions().getGroups(player)) {
                if (groupName != null && !groupName.isEmpty() && value == null) {
                    value = map("groups." + groupName + "." + node, null);
                }
            }
        }

        // if all else fails, use master
        if (value == null) {
            value = map("iConomy." + node, null);
        }

        return value != null && !value.isEmpty() ? value : "";
    }

    /**
     * Get the value from either the path or the default value if it's null
     *
     * @param path
     * @return
     */
    private String map(String path, String defaultValue) {
        String value = configuration.getString(path, defaultValue);

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
     * @param discountId
     * @param onlyCountActiveTransactions
     * @return
     */
    private int countDiscountedProtections(LWC lwc, Player player, double discountPrice, String discountId, boolean onlyCountActiveTransactions) {
        LWCPlayer lwcPlayer = lwc.wrapPlayer(player);
        List<History> related = lwcPlayer.getRelatedHistory(History.Type.TRANSACTION);

        int amount = 0;

        for (History history : related) {
            if (!history.getBoolean("discount")) {
                continue;
            }

            // Check the other discount id
            if (discountId != null) {
                if (!history.hasKey("discountId") || !history.getString("discountId").equals(discountId)) {
                    continue;
                }
            }

            // Are we only looking for valid transactions?
            if (onlyCountActiveTransactions && history.getStatus() == History.Status.INACTIVE) {
                continue;
            }

            // obtain the charge
            double charge = history.getDouble("charge");

            if (charge == discountPrice) {
                amount++;
            }
        }

        return amount;
    }

}
