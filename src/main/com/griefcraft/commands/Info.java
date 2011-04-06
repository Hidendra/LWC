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

package com.griefcraft.commands;

import static com.griefcraft.util.StringUtils.hasFlag;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.griefcraft.lwc.LWC;

public class Info implements ICommand {

	@Override
	public void execute(LWC lwc, CommandSender sender, String[] args) {
		Player player = (Player) sender;
		String type = "info";
		
		if(args.length > 1) {
			type = args[1].toLowerCase();
		}

		if(type.equals("limits")) {
			int used = lwc.getPhysicalDatabase().getProtectionCount(player.getName());
			int quota = lwc.getProtectionLimits(player.getWorld().getName(), player.getName());
			String displayQuota = "Unlimited";
			
			if(quota != -1) {
				displayQuota = quota + "";
			}
			
			lwc.sendLocale(sender, "protection.info.limits", "used", used, "quota", displayQuota);
		} else if(type.equals("info")) {
			lwc.getMemoryDatabase().unregisterAllActions(player.getName());
			lwc.getMemoryDatabase().registerAction("info", player.getName());
			lwc.sendLocale(sender, "protection.info.finalize");
		}
	}

	@Override
	public String getName() {
		return "info";
	}

	@Override
	public boolean supportsConsole() {
		return false;
	}

	@Override
	public boolean validate(LWC lwc, CommandSender player, String[] args) {
		return hasFlag(args, "i") || hasFlag(args, "info");
	}

}
