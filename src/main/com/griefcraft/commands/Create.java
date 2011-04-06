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
import static com.griefcraft.util.StringUtils.join;
import static com.griefcraft.util.StringUtils.transform;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.griefcraft.lwc.LWC;
import com.griefcraft.util.Colors;

public class Create implements ICommand {

	@Override
	public void execute(LWC lwc, CommandSender sender, String[] args) {
		if (args.length == 1) {
			lwc.sendLocale(sender, "help.creation");
			return;
		}

		String type = args[1].toLowerCase();
		String full = join(args, 1);
		Player player = (Player) sender;

		if (type.equals("trap")) {
			if (!lwc.isAdmin(sender)) {
				sender.sendMessage(Colors.Blue + "[lwc] " + Colors.Red + "Permission denied. ");
				return;
			}

			if (args.length < 3) {
				lwc.sendSimpleUsage(sender, "/lwc -c trap <kick/ban> [reason]");
				return;
			}
		}

		else if (type.equals("password")) {
			if (args.length < 3) {
				lwc.sendSimpleUsage(sender, "/lwc -c password <Password>");
				return;
			}

			String password = join(args, 2);
			String hiddenPass = transform(password, '*');

			lwc.sendLocale(sender, "protection.create.password", "password", hiddenPass);
		}

		else if (!type.equals("public") && !type.equals("private")) {
			lwc.sendLocale(sender, "help.creation");
			return;
		}

		lwc.getMemoryDatabase().unregisterAllActions(player.getName());
		lwc.getMemoryDatabase().registerAction("create", player.getName(), full);

		lwc.sendLocale(sender, "protection.create.finalize", "type", lwc.getLocale(type.toLowerCase()));
	}

	@Override
	public String getName() {
		return "creation";
	}

	@Override
	public boolean supportsConsole() {
		return false;
	}

	@Override
	public boolean validate(LWC lwc, CommandSender player, String[] args) {
		return hasFlag(args, "c") || hasFlag(args, "create");
	}

}
