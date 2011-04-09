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
import com.griefcraft.model.Protection;
import com.griefcraft.util.ConfigValues;

public class Flag implements ICommand {

	@Override
	public void execute(LWC lwc, CommandSender sender, String[] args) {
		if(args.length < 3) {
			lwc.sendSimpleUsage(sender, "/lwc flag <flag> <on/off>");
			
			String redstone = ConfigValues.DENY_REDSTONE.getBool() ? lwc.getLocale("help.flags.redstone.allow") : lwc.getLocale("help.flags.redstone.deny");
			lwc.sendLocale(sender, "help.flags", "redstone", redstone);
			
			return;
		}
		
		Player player = (Player) sender;
		String flagName = args[1];
		String type = args[2].toLowerCase();
		String internalType = ""; // + or -

		// verify the flag name
		boolean match = false;
		for(Protection.Flag flag : Protection.Flag.values()) {
			if(flag.toString().equalsIgnoreCase(flagName)) {
				match = true;
				flagName = flag.toString(); // get the case-correct name while we're there
				break;
			}
		}
		
		if(!match) {
			lwc.sendLocale(sender, "protection.flag.invalidflag", "flag", flagName);
			return;
		}
		
		if(type.equals("on") || type.equals("true") || type.equals("yes")) {
			internalType = "+";
		} else if(type.equals("off") || type.equals("false") || type.equals("no")) {
			internalType = "-";
		} else {
			lwc.sendLocale(sender, "protection.flag.invalidtype", "type", type);
			return;
		}
		
		lwc.getMemoryDatabase().unregisterAllActions(player.getName());
		lwc.getMemoryDatabase().registerAction("flag", player.getName(), internalType + flagName);
		lwc.sendLocale(sender, "protection.flag.finalize");
	}

	@Override
	public String getName() {
		return "flags";
	}

	@Override
	public boolean supportsConsole() {
		return false;
	}

	@Override
	public boolean validate(LWC lwc, CommandSender player, String[] args) {
		return hasFlag(args, "f") || hasFlag(args, "flag");
	}

}