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

import java.sql.PreparedStatement;

import org.bukkit.command.CommandSender;

import com.griefcraft.lwc.LWC;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.sql.PhysDB;
import com.griefcraft.util.StringUtils;

public class AdminExpire extends JavaModule {

    @Override
    public Result onCommand(LWC lwc, CommandSender sender, String command, String[] args) {
        if (!StringUtils.hasFlag(command, "a") && !StringUtils.hasFlag(command, "admin")) {
            return DEFAULT;
        }

        if (!args[0].equals("expire")) {
            return DEFAULT;
        }
        
        String toParse = StringUtils.join(args, 1);
        long time = StringUtils.parseTime(toParse);
        
        if(time == 0L) {
        	lwc.sendLocale(sender, "protection.admin.expire.invalidtime");
        	return CANCEL;
        }

        int threshold = (int) ((System.currentTimeMillis() / 1000L) - time);
        int count = 0;
        
        // raw SQL
        PhysDB database = lwc.getPhysicalDatabase();
        PreparedStatement statement = database.prepare("DELETE FROM " + database.getPrefix() + "protections WHERE last_accessed <= ? AND last_accessed > 0");
        
        try {
        	statement.setInt(1, threshold);
        	
        	statement.executeUpdate();
        	count = statement.getUpdateCount();
        } catch(Exception e) {
        	lwc.sendLocale(sender, "protection.internalerror", "id", "expire");
        	return CANCEL;
        }
        
        lwc.sendLocale(sender, "protection.admin.expire.removed", "count", count);
        
        // reset the cache
        if(count > 0) {
        	LWC.getInstance().getCaches().getProtections().clear();
        	database.precache();
        }
        
        return CANCEL;
    }

}