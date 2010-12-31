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

import com.griefcraft.util.Performance;

public class Command_Admin implements Command {

	@Override
	public void execute(LWC lwc, Player player, String[] args) {
		if (args.length < 1) {
			sendHelp(player);
			return;
		}

		String action = args[0].toLowerCase();

		if (action.equals("report")) {
			Performance.setChestCount(lwc.getPhysicalDatabase().entityCount());
			Performance.setPlayersOnline(etc.getServer().getPlayerList().size());

			for (String line : Performance.getGeneratedReport()) {
				player.sendMessage(Colors.Green + line);
			}

			Performance.clear();
		}

		else if (action.equals("convert")) {
			if (args.length < 2) {
				lwc.sendSimpleUsage(player, "/lwc -admin convert chestprotect");
				return;
			}

			String pluginToConvert = args[1].toLowerCase();

			if (pluginToConvert.equals("chestprotect")) {
				new CPConverter(player);
			}
		}

		else if (action.equals("clear")) {
			if (args.length < 2) {
				lwc.sendSimpleUsage(player, "/lwc -admin clear chests|limits|rights");
				return;
			}

			String toClear = args[1].toLowerCase();

			if (toClear.equals("chests")) {
				lwc.getPhysicalDatabase().unregisterProtectionEntities();

				player.sendMessage(Colors.Green + "Removed all protected chests and furnaces");
			} else if (toClear.equals("rights")) {
				lwc.getPhysicalDatabase().unregisterProtectionRights();

				player.sendMessage(Colors.Green + "Removed all protection rights");
			} else if (toClear.equals("limits")) {
				lwc.getPhysicalDatabase().unregisterProtectionLimits();

				player.sendMessage(Colors.Green + "Removed all protection limits");
			}
		}
	}

	public void sendHelp(Player player) {
		player.sendMessage("admin_help");
	}

	@Override
	public boolean validate(LWC lwc, Player player, String[] args) {
		return lwc.isAdmin(player) && hasFlag(args, "admin");
	}

}
