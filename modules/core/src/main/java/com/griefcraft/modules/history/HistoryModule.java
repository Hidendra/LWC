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
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.util.Colors;
import com.griefcraft.util.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.List;

public class HistoryModule extends JavaModule {

    /**
     * Amount of history items to show per page
     */
    public static final int ITEMS_PER_PAGE = 15;

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
            sender.sendMessage(Colors.Red + "No results found.");
            return;
        }

        // Try and load the history object
        History history = lwc.getPhysicalDatabase().loadHistory(historyId);

        if (history == null) {
            sender.sendMessage(Colors.Red + "No results found.");
            return;
        }

        // Can they access it?
        if (!lwc.isAdmin(sender)) {
            if (sender instanceof Player) {
                // verify they actually OWN the history object
                if (!history.getPlayer().equalsIgnoreCase(((Player) sender).getName())) {
                    // Make them think no results were found
                    sender.sendMessage(Colors.Red + "No results found.");
                    return;
                }
            }
        }

        // Now we can start telling them about it! wee
        Protection protection = history.getProtection(); // If the protection still exists, that is

        sender.sendMessage(" ");
        sender.sendMessage("Created by: " + Colors.Yellow + history.getPlayer());
        sender.sendMessage("Status: " + Colors.Yellow + history.getStatus());
        sender.sendMessage("Type: " + Colors.Yellow + history.getType());
        
        sender.sendMessage(" ");
        sender.sendMessage("Protection: " + Colors.Yellow + (protection == null ? "Removed" : protection));
        sender.sendMessage("Created by: " + Colors.Yellow + history.getString("creator"));

        // if its been removed, it most likely will have this key
        if (history.hasKey("destroyer")) {
            sender.sendMessage("Removed by: " + Colors.Yellow + history.getString("destroyer"));
        }

        // If it had an economy charge, show it
        if (history.hasKey("charge")) {
            sender.sendMessage("Economy charge: " + Colors.Yellow + history.getDouble("charge") + " " + lwc.getCurrency().getMoneyName());
        }

        if (history.hasKey("discount")) {
            sender.sendMessage(Colors.Yellow + "(discounted price)");
        }

        String creation = null;

        if (history.getTimestamp() > 0) {
            creation = new Date(history.getTimestamp() * 1000L).toString();
        }

        sender.sendMessage(" ");
        sender.sendMessage("Created on: " + Colors.Yellow + (creation == null ? "Unknown" : creation));

        // Only show how long ago it was if we know when it was created
        if (creation != null) {
            sender.sendMessage(Colors.Yellow + StringUtils.timeToString((System.currentTimeMillis() / 1000L) - history.getTimestamp()) + " ago");
        }
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
                pageCount ++;
            }
        }

        // Were there any usable results?
        if (relatedHistory.size() == 0) {
            sender.sendMessage(Colors.Red + "No results found.");
            return;
        }

        // Is it a valid page? (this normally will NOT happen, with the previous statement in place!)
        if (page > pageCount) {
            sender.sendMessage(Colors.Red + "Page not found.");
            return;
        }

        // Send it to them
        String format = "%4s%12s%12s%12s";

        // Header
        sender.sendMessage(" ");
        sender.sendMessage("To view extended details on a history item, use " + Colors.Yellow + "/lwc details <HistoryId>");
        sender.sendMessage(" ");
        sender.sendMessage(Colors.Yellow + String.format(format, "Id", "Player", "Type", "Status"));
        sender.sendMessage(Colors.Yellow + "Showing " + relatedHistory.size() + " results on page: " + page + "/" + pageCount + " (" + historyCount + " total)");

        // Send all that is found to them
        for (History history : relatedHistory) {
            sender.sendMessage(String.format(format, ("" + history.getId()), history.getPlayer(), history.getType(), history.getStatus()));
        }
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
