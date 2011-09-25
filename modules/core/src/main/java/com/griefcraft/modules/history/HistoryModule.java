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

package com.griefcraft.modules.history;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.History;
import com.griefcraft.model.LWCPlayer;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.scripting.event.LWCProtectionRemovePostEvent;
import com.griefcraft.util.Colors;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class HistoryModule extends JavaModule {

    /**
     * Amount of history items to show per page
     */
    public static final int ITEMS_PER_PAGE = 15;

    @Override
    public void onCommand(LWCCommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!event.hasFlag("h", "history")) {
            return;
        }

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
    public void onPostRemoval(LWCProtectionRemovePostEvent event) {
        Protection protection = event.getProtection();
        Player player = event.getPlayer();

        boolean isOwner = protection.isOwner(player);

        if (isOwner) {
            // bind the player of destroyed the protection
            // We don't need to save the history we modify because it will be saved anyway immediately after this
            for(History history : protection.getRelatedHistory(History.Type.TRANSACTION)) {
                if(history.getStatus() != History.Status.ACTIVE) {
                    continue;
                }

                history.addMetaData("destroyer=" + player.getName());
            }
        }
    }
    
}
