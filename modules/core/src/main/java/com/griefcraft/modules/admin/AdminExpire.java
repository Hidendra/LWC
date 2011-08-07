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
import com.griefcraft.sql.PhysDB;
import com.griefcraft.util.Colors;
import com.griefcraft.util.StringUtils;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.ContainerBlock;
import org.bukkit.command.CommandSender;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class AdminExpire extends JavaModule {

    @Override
    public Result onCommand(LWC lwc, CommandSender sender, String command, String[] args) {
        if (!StringUtils.hasFlag(command, "a") && !StringUtils.hasFlag(command, "admin")) {
            return DEFAULT;
        }

        if (!args[0].equals("expire")) {
            return DEFAULT;
        }

        if (args.length < 2) {
            lwc.sendSimpleUsage(sender, "/lwc admin expire <time>");
            return CANCEL;
        }

        boolean shouldRemoveBlocks = args[1].endsWith("remove");
        String toParse = StringUtils.join(args, shouldRemoveBlocks ? 2 : 1);
        long time = StringUtils.parseTime(toParse);

        if (time == 0L) {
            lwc.sendLocale(sender, "protection.admin.expire.invalidtime");
            return CANCEL;
        }

        int threshold = (int) ((System.currentTimeMillis() / 1000L) - time);
        int count = 0;

        // remove the protections
        int completed = lwc.fastRemoveProtections(sender, "last_accessed <= " + threshold + " AND last_accessed >= 0", shouldRemoveBlocks);

        // reset the cache
        if (completed > 0) {
            LWC.getInstance().getPhysicalDatabase().precache();
        }

        lwc.sendLocale(sender, "protection.admin.expire.removed", "count", count);

        return CANCEL;
    }

}