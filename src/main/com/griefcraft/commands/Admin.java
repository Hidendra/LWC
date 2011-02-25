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

import org.bukkit.entity.Player;

import com.griefcraft.converters.ChastityChest;
import com.griefcraft.converters.ChestProtect;
import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCInfo;
import com.griefcraft.model.Limit;
import com.griefcraft.util.Colors;
import com.griefcraft.util.Config;
import com.griefcraft.util.Performance;
import com.griefcraft.util.StringUtils;
import com.griefcraft.util.Updater;

public class Admin implements ICommand {

	@Override
	public String getName() {
		return "/lwc -admin";
	}

	@Override
	public void execute(LWC lwc, Player player, String[] args) {
		if (args.length < 2) {
			sendHelp(player);
			return;
		}

		String action = args[1].toLowerCase();

		if (action.equals("report")) {
			Performance.setChestCount(lwc.getPhysicalDatabase().getProtectionCount());
			Performance.setPlayersOnline(lwc.getPlugin().getServer().getOnlinePlayers().length);

			for (String line : Performance.getGeneratedReport()) {
				player.sendMessage(Colors.Green + line);
			}

			Performance.clear();
		}

		else if (action.equals("purge")) {
			if (args.length < 3) {
				lwc.sendSimpleUsage(player, "/lwc -a purge <Players>");
				return;
			}

			String players = StringUtils.join(args, 2);

			for (String toRemove : players.split(" ")) {
				lwc.getPhysicalDatabase().removeProtectionByPlayer(toRemove);
				player.sendMessage(Colors.Green + "Removed all protections created by " + Colors.Blue + toRemove);
			}
		}

		else if (action.equals("reload")) {
			Config.init();
			player.sendMessage(Colors.Green + "Reloaded LWC config!");
		}

		else if (action.equals("version")) {
			player.sendMessage("");

			Updater updater = lwc.getPlugin().getUpdater();

			String pluginColor = Colors.Green;
			String updaterColor = Colors.Green;

			double currPluginVersion = LWCInfo.VERSION;
			double currSqlVersion = updater.getCurrentSQLiteVersion();

			double latestPluginVersion = updater.getLatestPluginVersion();
			double latestSqlVersion = updater.getLatestSQLiteVersion();

			if (latestPluginVersion > currPluginVersion) {
				pluginColor = Colors.Red;
			}

			if (latestSqlVersion > currSqlVersion) {
				pluginColor = Colors.Red;
			}

			player.sendMessage(Colors.Blue + "Main plugin: " + pluginColor + LWCInfo.FULL_VERSION + Colors.Yellow + "/" + Colors.Green + latestPluginVersion);
			player.sendMessage(Colors.Blue + "SQLite: " + updaterColor + currSqlVersion + Colors.Yellow + "/" + Colors.Green + latestSqlVersion);

			player.sendMessage("");
			player.sendMessage(Colors.Green + "Green: Up to date");
			player.sendMessage(Colors.Red + "Red: Out of date");
		}

		else if (action.equals("createjob")) {
			if (args.length < 5) {
				lwc.sendSimpleUsage(player, "/lwc -a createjob <type> <owner> <payload>");
				return;
			}

			try {
				// -a createjob type owner payload

				int type = Integer.parseInt(args[2]);
				String owner = args[3];
				String payload = join(args, 4);

				lwc.getPhysicalDatabase().createJob(type, owner, payload);
				player.sendMessage(Colors.Green + "Scheduled job");
			} catch (Exception e) {
				lwc.sendSimpleUsage(player, "/lwc -a createjob <type> <owner> <payload>");
			}
		}

		else if (action.equals("flush")) {
			player.sendMessage(Colors.Green + "Flushing Update Thread..");
			lwc.getUpdateThread().flush();
		}

		else if (action.equals("cleanup")) {

		}

		else if (action.equals("update")) {
			Updater updater = lwc.getPlugin().getUpdater();

			if (updater.checkDist()) {
				player.sendMessage(Colors.Green + "Updated LWC successfully to version: " + updater.getLatestPluginVersion());
				player.sendMessage(Colors.Green + "Please reload LWC to complete the update");
			} else {
				player.sendMessage(Colors.Red + "No update found.");
			}
		}

		else if (action.equalsIgnoreCase("limits")) {
			if (args.length < 3) {
				lwc.sendSimpleUsage(player, "/lwc -admin limits <count> <Groups/Users>");
				return;
			}

			final int limit = Integer.parseInt(args[2]);

			for (int i = 3; i < args.length; i++) {
				String entity = args[i];
				int type = Limit.PLAYER;
				boolean isGroup = entity.startsWith("g:");

				if (isGroup) {
					entity = entity.substring(2);
					type = Limit.GROUP;
				}

				if (entity.equalsIgnoreCase("-global")) {
					type = Limit.GLOBAL;
				}

				if (type == Limit.GLOBAL) {
					lwc.getPhysicalDatabase().registerProtectionLimit(type, limit, "");
					player.sendMessage(Colors.Green + "Registered global limit of " + Colors.Gold + limit + Colors.Green + " protections");
				} else if (limit != -2) {
					lwc.getPhysicalDatabase().registerProtectionLimit(type, limit, entity);
					player.sendMessage(Colors.Green + "Registered limit of " + Colors.Gold + limit + Colors.Green + " protections to the " + (isGroup ? "group" : "user") + " " + Colors.Gold + entity);
				} else {
					lwc.getPhysicalDatabase().unregisterProtectionLimit(type, entity);
					player.sendMessage(Colors.Green + "Unregistered limit for " + Colors.Gold + entity);
				}
			}
		}

		else if (action.equals("convert")) {
			if (args.length < 2) {
				lwc.sendSimpleUsage(player, "/lwc -admin convert chestprotect|chastity");
				return;
			}

			String pluginToConvert = args[1].toLowerCase();

			if (pluginToConvert.equals("chestprotect")) {
				new ChestProtect(player);
			} else if (pluginToConvert.equals("chastity")) {
				new ChastityChest(player);
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
				lwc.getPhysicalDatabase().unregisterProtectionRights();

				player.sendMessage(Colors.Green + "Removed all protections + rights");
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
		player.sendMessage(Colors.Red + "LWC Administration");
		player.sendMessage(" ");
		player.sendMessage("/lwc admin limits " + Colors.LightBlue + "<count> <Players/Groups/" + Colors.Yellow + "-global" + Colors.LightBlue + ">");
		player.sendMessage(Colors.Yellow + "-global " + Colors.Blue + "is optional and will be applied to anyone without a limit");
		player.sendMessage("/lwc admin purge " + Colors.LightBlue + "<Players> " + Colors.Blue + "Remove all protections by a player");
		player.sendMessage(" ");
		player.sendMessage("/lwc admin version " + Colors.Blue + "View the current/latest version of LWC");
		player.sendMessage("/lwc admin report  " + Colors.Blue + "Generate a Performance report");
		player.sendMessage("/lwc admin update  " + Colors.Blue + "Update if outdated");
		player.sendMessage("/lwc admin convert " + Colors.LightBlue + "<chestprotect|chastity> " + Colors.Blue + "- Convert X to LWC");
		player.sendMessage(" ");
		player.sendMessage("/lwc admin clear  " + Colors.LightBlue + "<protections|rights|limits> " + Colors.Blue + "- " + Colors.Red + "IRREVERSIBLE FUN");
	}

	@Override
	public boolean validate(LWC lwc, Player player, String[] args) {
		return lwc.isAdmin(player) && (hasFlag(args, "a") || hasFlag(args, "admin"));
	}

}
