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

import org.bukkit.entity.Player;

import com.griefcraft.lwc.LWC;
import com.griefcraft.util.Colors;

public class Free implements ICommand {

	@Override
	public String getName() {
		return "/lwc -free";
	}

	@Override
	public void execute(LWC lwc, Player player, String[] args) {
		if (args.length < 2) {
			lwc.sendSimpleUsage(player, "/lwc -r <protection|modes>");
			return;
		}

		String type = args[1].toLowerCase();

		if (type.equals("protection") || type.equals("chest") || type.equals("furnace") || type.equals("dispenser")) {
			if (lwc.getMemoryDatabase().hasPendingChest(player.getName())) {
				player.sendMessage(Colors.Red + "You already have a pending action.");
				return;
			}

			lwc.getMemoryDatabase().unregisterAllActions(player.getName());
			lwc.getMemoryDatabase().registerAction("free", player.getName());
			player.sendMessage(Colors.Blue + "Punch your protection to remove the lock");
		}

		else if (type.equals("modes")) {
			lwc.getMemoryDatabase().unregisterAllModes(player.getName());

			player.sendMessage(Colors.Green + "Successfully removed all set modes.");
		}

		else {
			lwc.sendSimpleUsage(player, "/lwc -r <protection|modes>");
			return;
		}
	}

	@Override
	public boolean validate(LWC lwc, Player player, String[] args) {
		return hasFlag(args, "r") || hasFlag(args, "free") || hasFlag(args, "remove");
	}

}
