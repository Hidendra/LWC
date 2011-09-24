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

public class AdminExpire extends JavaModule {

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

        if (!args[0].equals("expire")) {
            return;
        }

        // we have the right command
        event.setCancelled(true);

        if (args.length < 2) {
            lwc.sendSimpleUsage(sender, "/lwc admin expire <time>");
            return;
        }

        boolean shouldRemoveBlocks = args[1].endsWith("remove");
        String toParse = StringUtils.join(args, shouldRemoveBlocks ? 2 : 1);
        long time = StringUtils.parseTime(toParse);

        if (time == 0L) {
            lwc.sendLocale(sender, "protection.admin.expire.invalidtime");
            return;
        }

        int threshold = (int) ((System.currentTimeMillis() / 1000L) - time);

        // remove the protections
        int completed = lwc.fastRemoveProtections(sender, "last_accessed <= " + threshold + " AND last_accessed >= 0", shouldRemoveBlocks);

        // reset the cache
        if (completed > 0) {
            LWC.getInstance().getPhysicalDatabase().precache();
        }

        lwc.sendLocale(sender, "protection.admin.expire.removed", "count", completed);

        return;
    }

}