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
 * THIS SOFTWARE IS PROVIDED BY TYLER BLAIR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL TYLER BLAIR OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of Tyler Blair.
 */

package com.griefcraft.modules.history;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.History;
import com.griefcraft.model.LWCPlayer;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.scripting.event.LWCProtectionInteractEvent;
import com.griefcraft.util.Colors;
import com.griefcraft.util.TimeUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Date;
import java.util.List;

public class HistoryModule extends JavaModule {

    /**
     * Amount of history items to show per page
     */
    public static final int ITEMS_PER_PAGE = 15;

    /**
     * History tool
     */
    @Override
    public void onProtectionInteract(LWCProtectionInteractEvent event) {
        if (event.getResult() != DEFAULT) {
            return;
        }

        LWC lwc = event.getLWC();
        LWCPlayer lwcPlayer = lwc.wrapPlayer(event.getPlayer());
        PlayerInteractEvent bukkitEvent = event.getEvent(); // the bukkit event

        // are they already checking history?
    }

    /**
     * Send the history list to a player
     *
     * @param sender
     * @param relatedHistory
     * @param page
     * @param maxPages
     * @param historyCount
     */
    public void sendHistoryList(CommandSender sender, List<History> relatedHistory, int page, int maxPages, int historyCount) {
        String format = "%4s%12s%12s%12s";

        // Header
        LWC.getInstance().sendLocale(sender, "lwc.history.list", "header", String.format(format, "Id", "Player", "Type", "Status"), "size", relatedHistory.size(),
                "page", page, "totalpages", maxPages, "totalhistory", historyCount);

        // Send all that is found to them
        for (History history : relatedHistory) {
            sender.sendMessage(String.format(format, ("" + history.getId()), history.getPlayer(), history.getType(), history.getStatus()));
        }
    }

    /**
     * Send history details to a player
     *
     * @param sender
     * @param history
     */
    private void sendDetails(CommandSender sender, History history) {
        LWC lwc = LWC.getInstance();
        Protection protection = history.getProtection(); // If the protection still exists, that is

        // if it was removed, hide the protection (if they're using SQLite, primary keys are reused)
        if (history.hasKey("destroyer")) {
            protection = null;
        }

        lwc.sendLocale(sender, "lwc.history.details.header", "id", history.getId(), "player", history.getPlayer(),
                "location", String.format("[%d %d %d]", history.getX(), history.getY(), history.getZ()),
                "status", history.getStatus(), "type", history.getType(),
                "protection", (protection == null ? "n/a" : protection),
                "creator", history.getString("creator"),
                "currencyname", lwc.getCurrency().getMoneyName());
        if (history.hasKey("destroyer")) {
            lwc.sendLocale(sender, "lwc.history.details.destroyer", "player", history.getString("destroyer"));
        }

        // New line!
        sender.sendMessage(" ");

        // If it had an economy charge, show it
        if (history.hasKey("charge")) {
            lwc.sendLocale(sender, "lwc.history.details.econcharge", "charge", history.getDouble("charge"), "moneyname", lwc.getCurrency().getMoneyName(),
                    "discount",
                    // Rest your eyes and avoid the line two lines below
                    // Its format: Yes|No (ID)  -- (ID) is only shown if Yes is shown.
                    ((history.hasKey("discount") ? (Colors.Red + "Yes") : (Colors.Yellow + "No")) + Colors.Yellow + " " + /* Discount id */ (history.hasKey("discountId") ? ("(" + history.getString("discountId") + ")") : "")));
        }

        // Show the creation date
        String creation = null;

        if (history.getTimestamp() > 0) {
            creation = new Date(history.getTimestamp() * 1000L).toString();
        }

        lwc.sendLocale(sender, "lwc.history.details.dates", "date", (creation == null ? "Unknown" : creation));

        // Only show how long ago it was if we know when it was created
        if (creation != null) {
            lwc.sendLocale(sender, "lwc.history.details.timeago", "time", TimeUtil.timeToString((System.currentTimeMillis() / 1000L) - history.getTimestamp()));
        }

        // if its been removed, it most likely will have this key
        if (history.hasKey("destroyer")) {
            // When it was removed
            String destroyerTime = history.getString("destroyerTime");
            long destroyed = 0L;

            // parse it if it's found
            if (!destroyerTime.isEmpty()) {
                destroyed = Long.parseLong(destroyerTime);
            }

            // Parse a date to show
            String absoluteDestroyDate = destroyed > 0 ? new Date(destroyed * 1000L).toString() : "Unknown";
            String relativeDestroyDate = destroyed > 0 ? TimeUtil.timeToString((System.currentTimeMillis() / 1000L) - destroyed) : "Unknown";

            // Send the exact time if known
            lwc.sendLocale(sender, "lwc.history.details.destroyed", "date", absoluteDestroyDate);

            // If there's a relative date available, display it :D
            if (destroyed > 0) {
                lwc.sendLocale(sender, "lwc.history.details.timeago", "time", relativeDestroyDate);
            }
        }
    }

    /**
     * Called when /lwc details or /lwc d is used.
     *
     * @param event
     */
    private void doDetailsCommand(LWCCommandEvent event) {
        LWC lwc = event.getLWC();
        CommandSender sender = event.getSender();
        String[] args = event.getArgs();
        event.setCancelled(true);

        // We MUST have an argument ..!
        if (args.length < 1) {
            lwc.sendSimpleUsage(sender, "/lwc details <HistoryId>");
            return;
        }

        // Load it ..
        int historyId;

        try {
            historyId = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            lwc.sendLocale(sender, "lwc.noresults");
            return;
        }

        // Try and load the history object
        History history = lwc.getPhysicalDatabase().loadHistory(historyId);

        if (history == null) {
            lwc.sendLocale(sender, "lwc.noresults");
            return;
        }

        // Can they access it?
        if (!lwc.isAdmin(sender)) {
            if (sender instanceof Player) {
                // verify they actually OWN the history object
                if (!history.getPlayer().equalsIgnoreCase(((Player) sender).getName())) {
                    // Make them think no results were found
                    lwc.sendLocale(sender, "lwc.noresults");
                    return;
                }
            }
        }

        // Tell them about it!
        sendDetails(sender, history);
    }

    /**
     * Called when /lwc history or /lwc h is used.
     *
     * @param event
     */
    private void doHistoryCommand(LWCCommandEvent event) {
        LWC lwc = event.getLWC();
        CommandSender sender = event.getSender();
        String[] args = event.getArgs();
        event.setCancelled(true);

        // Some vars we'll use more later on
        boolean isWildcard = false;
        int page = 1;
        int historyCount = 0;
        int pageCount = 0;

        // If it's the console without arguments, lookup for every player
        if (!(sender instanceof Player) && args.length == 0) {
            isWildcard = true;
        }

        // The player name to try to parse
        String playerName = null;

        // check for page number
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);

                if (args.length > 1) {
                    playerName = args[1];
                }
            } catch (NumberFormatException e) {
                playerName = args[0];

                // look for page number
                if (args.length > 1) {
                    try {
                        page = Integer.parseInt(args[1]);
                    } catch (NumberFormatException ex) {
                    }
                }
            }
        }

        // check for player string
        if (playerName != null) {
            // Are they an admin, to be able to do that?
            if (!lwc.isAdmin(sender)) {
                // Nope, go away!
                lwc.sendLocale(sender, "protection.accessdenied");
                return;
            } else {
                // Using a wildcard?
                if (playerName.equals("*")) {
                    isWildcard = true;
                }
            }
        } else {
            // if they're a player, look themselves up
            if (sender instanceof Player) {
                playerName = ((Player) sender).getName();
            }
        }

        // Check if a player was STILL not found and just apply defaults if so
        if (playerName == null) {
            if (!(sender instanceof Player)) {
                isWildcard = true;
            } else {
                playerName = ((Player) sender).getName();
            }
        }

        // Get the first page
        List<History> relatedHistory;

        if (isWildcard) {
            relatedHistory = lwc.getPhysicalDatabase().loadHistory((page - 1) * ITEMS_PER_PAGE, ITEMS_PER_PAGE);
            historyCount = lwc.getPhysicalDatabase().getHistoryCount();
        } else {
            relatedHistory = lwc.getPhysicalDatabase().loadHistory(playerName, (page - 1) * ITEMS_PER_PAGE, ITEMS_PER_PAGE);
            historyCount = lwc.getPhysicalDatabase().getHistoryCount(playerName);
        }

        // Calculate page count
        if (historyCount > 0) {
            pageCount = (int) Math.floor(historyCount / (page * ITEMS_PER_PAGE));

            // compensate for what's left
            while ((pageCount * ITEMS_PER_PAGE) < historyCount) {
                pageCount++;
            }
        }

        // Were there any usable results?
        if (relatedHistory.size() == 0) {
            lwc.sendLocale(sender, "lwc.noresults");
            return;
        }

        // Is it a valid page? (this normally will NOT happen, with the previous statement in place!)
        if (page > pageCount) {
            lwc.sendLocale(sender, "lwc.noresults");
            return;
        }

        // Send it to them
        sendHistoryList(sender, relatedHistory, page, pageCount, historyCount);
    }

    @Override
    public void onCommand(LWCCommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (event.hasFlag("h", "history")) {
            doHistoryCommand(event);
        } else if (event.hasFlag("d", "details")) {
            doDetailsCommand(event);
        }
    }

}
