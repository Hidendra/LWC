/**
\ * This file is part of LWC (https://github.com/Hidendra/LWC)
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.griefcraft.util.Performance;
import com.griefcraft.util.Updater;

public class Command_Admin implements Command {

	@Override
	public void execute(LWC lwc, Player player, String[] args) {
		if (args.length < 2) {
			sendHelp(player);
			return;
		}

		String action = args[1].toLowerCase();

		if (action.equals("report")) {
			Performance.setChestCount(lwc.getPhysicalDatabase().entityCount());
			Performance.setPlayersOnline(etc.getServer().getPlayerList().size());

			for (String line : Performance.getGeneratedReport()) {
				player.sendMessage(Colors.Green + line);
			}

			Performance.clear();
		}
		
		else if(action.equals("update")) {
			boolean updated = lwc.getUpdater().checkDist();
			
			if(updated) {
				etc.getLoader().reloadPlugin("LWC");
				player.sendMessage(Colors.Green + "Updated LWC successfully to version: " + lwc.getUpdater().getLatestVersion());
			} else {
				player.sendMessage(Colors.Red + "No update found.");
			}
		}
		
		else if (action.equalsIgnoreCase("limits")) {
			if (args.length < 3) {
				lwc.sendSimpleUsage(player, "/lwc -admin limits <count> <Group/User>");
				return;
			}

			final int limit = Integer.parseInt(args[2]);

			for (int i = 3; i < args.length; i++) {
				String entity = args[i]; 
				final boolean isGroup = entity.startsWith("g:");

				if (isGroup) {
					entity = entity.substring(2);
				}

				if (limit != -2) {
					lwc.getPhysicalDatabase().registerProtectionLimit(isGroup ? 0 : 1, limit, entity);
					player.sendMessage(Colors.Green + "Registered limit of " + Colors.Gold + limit + Colors.Green + " chests to the " + (isGroup ? "group" : "user") + " " + Colors.Gold + entity);
				} else {
					lwc.getPhysicalDatabase().unregisterProtectionLimit(isGroup ? 0 : 1, entity);
					player.sendMessage(Colors.Green + "Unregistered limit for " + Colors.Gold + entity);
				}
			}
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

			String toClear = args[2].toLowerCase();

			if (toClear.equals("protections")) {
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
		player.sendMessage(" ");
		player.sendMessage(Colors.Green + "LWC Administration");
		player.sendMessage(" ");
		player.sendMessage("/lwc admin report - " + Colors.Gold + "Generate a Performance report");
		player.sendMessage("/lwc admin update - " + Colors.Gold + "Check for an update (if one, update)");
		player.sendMessage("/lwc admin convert " + Colors.Gold + "<chestprotect> - Convert X to LWC");
		player.sendMessage(" ");
		player.sendMessage("/lwc admin clear - " + Colors.Red + "PERMANENTLY removes data");
		player.sendMessage("/lwc admin clear " + Colors.Gold + "<protections|rights|limits>");
	}

	@Override
	public boolean validate(LWC lwc, Player player, String[] args) {
		return lwc.isAdmin(player) && (hasFlag(args, "a") || hasFlag(args, "admin"));
	}

}
