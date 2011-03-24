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

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.griefcraft.lwc.LWC;

public class Modify implements ICommand {

	@Override
	public void execute(LWC lwc, CommandSender sender, String[] args) {
		if (args.length < 2) {
			lwc.sendLocale(sender, "help.modify");
			return;
		}

		String full = join(args, 1);
		Player player = (Player) sender;

		lwc.getMemoryDatabase().unregisterAllActions(player.getName());
		lwc.getMemoryDatabase().registerAction("modify", player.getName(), full);
		lwc.sendLocale(sender, "protection.modify.finalize");
	}

	@Override
	public String getName() {
		return "modify";
	}

	@Override
	public boolean supportsConsole() {
		return false;
	}

	@Override
	public boolean validate(LWC lwc, CommandSender player, String[] args) {
		return hasFlag(args, "m") || hasFlag(args, "modify");
	}

}
