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

import static com.griefcraft.util.StringUtils.capitalizeFirstLetter;
import static com.griefcraft.util.StringUtils.hasFlag;
import static com.griefcraft.util.StringUtils.join;
import static com.griefcraft.util.StringUtils.transform;

public class Command_Create implements Command {

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

	private void sendHelp(Player player) {
		player.sendMessage("create_help");
	}

}
