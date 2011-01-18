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

import static com.griefcraft.util.StringUtils.capitalizeFirstLetter;
import static com.griefcraft.util.StringUtils.hasFlag;
import static com.griefcraft.util.StringUtils.join;
import static com.griefcraft.util.StringUtils.transform;

import org.bukkit.entity.Player;

import com.griefcraft.lwc.LWC;
import com.griefcraft.util.Colors;

public class Create implements ICommand {

	@Override
	public void execute(LWC lwc, Player player, String[] args) {
		if (args.length == 1) {
			sendHelp(player);
			return;
		}

		String type = args[1].toLowerCase();
		String full = join(args, 1);

		if (type.equals("password")) {
			if (args.length < 3) {
				lwc.sendSimpleUsage(player, "/lwc -c password <Password>");
				return;
			}

			String password = join(args, 2);
			String hiddenPass = transform(password, '*');

			player.sendMessage(Colors.LightGreen + "Using password: " + Colors.Yellow + hiddenPass);
		}
		
		else if (!type.equals("public") && !type.equals("private")) {
			sendHelp(player);
			return;
		}

		lwc.getMemoryDatabase().unregisterAllActions(player.getName());
		lwc.getMemoryDatabase().registerAction("create", player.getName(), full);

		player.sendMessage(Colors.LightGreen + "Lock type: " + Colors.Green + capitalizeFirstLetter(type));
		player.sendMessage(Colors.Green + "Please left click your Chest or Furnace to lock it.");
	}

	@Override
	public boolean validate(LWC lwc, Player player, String[] args) {
		return hasFlag(args, "c") || hasFlag(args, "create");
	}

	public void sendHelp(Player player) {
		player.sendMessage(" ");
		player.sendMessage(Colors.Green + "LWC Protection");
		player.sendMessage(" ");
		
		player.sendMessage("/lwc -c public " + Colors.Gold + "Create a public protection");
		player.sendMessage(Colors.LightGreen + "Anyone can access a Public chest, but no one can protect it");
		player.sendMessage(" ");
		
		player.sendMessage("/lwc -c password <password> " + Colors.Gold + "Create a password-protected");
		player.sendMessage(Colors.Gold + "Chest or Furnace");
		player.sendMessage(Colors.LightGreen + "Each time you login you need to enter the password to access");
		player.sendMessage(Colors.LightGreen + "the chest (if someone knows the pass, they can use it too!)");
		player.sendMessage(" ");
		
		player.sendMessage("/lwc -c private " + Colors.Gold + "Create a private protection");
		player.sendMessage(Colors.LightGreen + "Private means private. You can also allow other users or");
		player.sendMessage(Colors.LightGreen + "groups to access the chest or furnace. This is done by");
		player.sendMessage(Colors.LightGreen + "adding them after \"private\".");
		player.sendMessage(" ");
		player.sendMessage("Example:");
		player.sendMessage(Colors.Blue + "/lwc -c private UserName g:GroupName OtherGuy");
		player.sendMessage(" ");
		player.sendMessage(Colors.LightGreen + "You can specify more than 1 group and/or user per command!");
	}

}
