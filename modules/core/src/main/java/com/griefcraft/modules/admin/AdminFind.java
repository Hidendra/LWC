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

package com.griefcraft.modules.admin;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.util.StringUtils;
import org.bukkit.command.CommandSender;

import java.util.List;

public class AdminFind extends JavaModule {

    @Override
    public void onCommand(LWCCommandEvent event) {
        if(event.isCancelled()) {
            return;
        }

        if(!event.hasFlag("a", "admin")) {
            return;
        }

        LWC lwc = event.getLWC();
        CommandSender sender = event.getSender();
        String[] args = event.getArgs();

        if(!args[0].equals("find")) {
            return;
        }

        // we have the right command
        event.setCancelled(true);

        if (args.length < 2) {
            lwc.sendSimpleUsage(sender, "/lwc admin find <player> [page]");
            return;
        }

        final int perPage = 7; // listings per page

        String player = args[1];
        int page = 1;

        if (args.length > 2) {
            try {
                page = Integer.parseInt(args[2]);
            } catch (Exception e) {
                lwc.sendLocale(sender, "protection.find.invalidpage");
                return;
            }
        }

        int start = (page - 1) * perPage;

        List<Protection> protections = lwc.getPhysicalDatabase().loadProtectionsByPlayer(player, start, perPage);
        int results = lwc.getPhysicalDatabase().getProtectionCount(player);
        int max = protections.size(); // may not be the full perPage
        int ceil = start + max;

        lwc.sendLocale(sender, "protection.find.currentpage", "page", page);

        if (results != max) {
            lwc.sendLocale(sender, "protection.find.nextpage", "player", player, "page", page + 1);
        }

        lwc.sendLocale(sender, "protection.find.showing", "start", start, "ceil", ceil, "results", results);

        for (Protection protection : protections) {
            sender.sendMessage(protection.toString());
        }

        return;
    }

}