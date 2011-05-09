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

import org.bukkit.command.CommandSender;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Limit;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.util.Colors;
import com.griefcraft.util.StringUtils;

public class AdminLimits extends JavaModule {

	@Override
	public Result onCommand(LWC lwc, CommandSender sender, String command, String[] args) {
		if(!StringUtils.hasFlag(command, "a") && !StringUtils.hasFlag(command, "admin")) {
			return DEFAULT;
		}
		
		if(!args[0].equals("limits")) {
			return DEFAULT;
		}
		
		if (args.length < 2) {
			lwc.sendSimpleUsage(sender, "/lwc admin limits <count> <Groups/Users>");
			sender.sendMessage(Colors.Red + "Note:" + Colors.White + " You can use " + Colors.LightBlue + "-global" + Colors.White + " in place of <Groups/Users> to allow it to apply to anyone without a protection limit.");
			return CANCEL;
		}

		final int limit = Integer.parseInt(args[1]);

		for (int i = 2; i < args.length; i++) {
			String entity = args[i];
			int type = Limit.PLAYER;
			boolean isGroup = entity.startsWith("g:");

			if (isGroup) {
				entity = entity.substring(2);
				type = Limit.GROUP;
			}

			if (entity.equalsIgnoreCase("-global")) {
				type = Limit.GLOBAL;
			}

			if (type == Limit.GLOBAL) {
				lwc.getPhysicalDatabase().registerProtectionLimit(type, limit, "");
				lwc.sendLocale(sender, "protection.admin.limit.global", "limit", limit);
				sender.sendMessage(Colors.Green + "Registered global limit of " + Colors.Gold + limit + Colors.Green + " protections");
			} else if (limit != -2) {
				String localeChild = isGroup ? "group" : "player";
				
				lwc.getPhysicalDatabase().registerProtectionLimit(type, limit, entity);
				lwc.sendLocale(sender, "protection.admin.limit." + localeChild, "limit", limit, "name", entity);
			} else {
				lwc.getPhysicalDatabase().unregisterProtectionLimit(type, entity);
				lwc.sendLocale(sender, "protection.admin.limit.remove", "name", entity);
			}
		}
		
		return CANCEL;
	}
	
}