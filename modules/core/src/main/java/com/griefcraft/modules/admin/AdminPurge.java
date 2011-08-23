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
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.util.StringUtils;
import org.bukkit.command.CommandSender;

public class AdminPurge extends JavaModule {

    @Override
    public void onCommand(LWCCommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!event.hasFlag("a", "admin")) {
            return;
        }

        LWC lwc = event.getLWC();
        CommandSender sender = event.getSender();
        String[] args = event.getArgs();

        if (!args[0].equals("purge")) {
            return;
        }

        // we have the right command
        event.setCancelled(true);

        if (args.length < 2) {
            lwc.sendSimpleUsage(sender, "/lwc admin purge <Players>");
            return;
        }

        boolean shouldRemoveBlocks = args[1].endsWith("remove");
        String players = StringUtils.join(args, shouldRemoveBlocks ? 2 : 1);

        for (String toRemove : players.split(" ")) {
            if (toRemove.contains("'")) continue; // bad me

            // Remove all of them
            lwc.fastRemoveProtections(sender, "owner = '" + toRemove + "'", shouldRemoveBlocks);

            lwc.sendLocale(sender, "protection.admin.purge.finalize", "player", toRemove);
        }

        // reload the cache!
        LWC.getInstance().getPhysicalDatabase().precache();

        return;
    }

}