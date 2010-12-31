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

import static com.griefcraft.util.StringUtils.hasFlag;
import static com.griefcraft.util.StringUtils.join;

public class Command_Modify implements Command {

	@Override
	public void execute(LWC lwc, Player player, String[] args) {
		if (args.length < 2) {
			sendHelp(player);
			return;
		}

		String full = join(args, 1);

		lwc.getMemoryDatabase().unregisterAllActions(player.getName());
		lwc.getMemoryDatabase().registerAction("modify", player.getName(), full);
		player.sendMessage(Colors.Green + "Please left click your Chest/Furnace to complete modifications");
	}

	@Override
	public boolean validate(LWC lwc, Player player, String[] args) {
		return hasFlag(args, "m") || hasFlag(args, "modify");
	}

	private void sendHelp(Player player) {
		player.sendMessage("modify_help");
	}

}
