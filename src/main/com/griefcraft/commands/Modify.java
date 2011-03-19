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
import com.griefcraft.util.Colors;

public class Modify implements ICommand {

	@Override
	public String getName() {
		return "modify";
	}
	
	@Override
	public boolean supportsConsole() {
		return false;
	}

	@Override
	public void execute(LWC lwc, CommandSender sender, String[] args) {
		if (args.length < 2) {
			sendHelp(sender);
			return;
		}

		String full = join(args, 1);
		Player player = (Player) sender;

		lwc.getMemoryDatabase().unregisterAllActions(player.getName());
		lwc.getMemoryDatabase().registerAction("modify", player.getName(), full);
		player.sendMessage(Colors.Green + "Please left click your Chest/Furnace to complete modifications");
	}

	@Override
	public boolean validate(LWC lwc, CommandSender player, String[] args) {
		return hasFlag(args, "m") || hasFlag(args, "modify");
	}

	private void sendHelp(CommandSender player) {
		player.sendMessage(" ");
		player.sendMessage(Colors.Green + "LWC Protection");
		player.sendMessage(" ");

		player.sendMessage("/lwc -m <users/groups> " + Colors.Gold + "Modify an existing protection, adding or");
		player.sendMessage(Colors.Gold + "removing users and/or groups");
		player.sendMessage(Colors.Green + "See: " + Colors.Gold + "/lwc -c" + Colors.Green + ", the example for private protections");
		player.sendMessage(" ");

		player.sendMessage(Colors.Blue + "Additional prefixes for users/groups:");
		player.sendMessage(Colors.Red + "-" + Colors.Blue + ": Remove a user/group from protection");
		player.sendMessage(Colors.Red + "@" + Colors.Blue + ": The user/group will be able to modify the chest");
		player.sendMessage(Colors.Gold + "note: chest admins cannot remove the owner from access");
		player.sendMessage(" ");

		player.sendMessage("Examples");
		player.sendMessage(Colors.Gold + "Remove a group from access: " + Colors.Blue + "/lwc -m -g:name");
		player.sendMessage(Colors.Gold + "Remove a user + add an admin: " + Colors.Blue + "/lwc -m -name @OtherName");
	}

}
