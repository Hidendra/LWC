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
		return "admin";
	}
	
	@Override
	public boolean supportsConsole() {
		return true;
	}

	@Override
	public void execute(LWC lwc, CommandSender sender, String[] args) {
		if (args.length < 2) {
			sendHelp(sender);
			return;
		}

		String action = args[1].toLowerCase();

		if (action.equals("report")) {
			Performance.setChestCount(lwc.getPhysicalDatabase().getProtectionCount());
			Performance.setPlayersOnline(lwc.getPlugin().getServer().getOnlinePlayers().length);

			for (String line : Performance.getGeneratedReport()) {
				sender.sendMessage(Colors.Green + line);
			}

			Performance.clear();
		}

		else if (action.equals("locale")) {
			if (args.length < 3) {
				lwc.sendSimpleUsage(sender, "/lwc -a locale <key> [args]");
				return;
			}

			String locale = args[2];
			String[] localeArgs = new String[0];

			if (args.length > 3) {
				localeArgs = StringUtils.join(args, 3).split(" ");
			}

			if (localeArgs.length > 0) {
				lwc.sendLocale(sender, locale, (Object[]) localeArgs);
			} else {
				lwc.sendLocale(sender, locale);
			}
		}

		else if (action.equals("purge")) {
			if (args.length < 3) {
				lwc.sendSimpleUsage(sender, "/lwc -a purge <Players>");
				return;
			}

			String players = StringUtils.join(args, 2);

			for (String toRemove : players.split(" ")) {
				lwc.getPhysicalDatabase().removeProtectionByPlayer(toRemove);
				sender.sendMessage(Colors.Green + "Removed all protections created by " + Colors.Blue + toRemove);
			}
		}

		else if (action.equals("reload")) {
			Config.init();
			sender.sendMessage(Colors.Green + "Reloaded LWC config!");
		}

		else if (action.equals("version")) {
			sender.sendMessage("");

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

			sender.sendMessage(Colors.Blue + "Main plugin: " + pluginColor + LWCInfo.FULL_VERSION + Colors.Yellow + "/" + Colors.Green + latestPluginVersion);
			sender.sendMessage(Colors.Blue + "SQLite: " + updaterColor + currSqlVersion + Colors.Yellow + "/" + Colors.Green + latestSqlVersion);

			sender.sendMessage("");
			sender.sendMessage(Colors.Green + "Green: Up to date");
			sender.sendMessage(Colors.Red + "Red: Out of date");
		}

		else if (action.equals("createjob")) {
			if (args.length < 5) {
				lwc.sendSimpleUsage(sender, "/lwc -a createjob <type> <owner> <payload>");
				return;
			}

			try {
				// -a createjob type owner payload

				int type = Integer.parseInt(args[2]);
				String owner = args[3];
				String payload = join(args, 4);

				lwc.getPhysicalDatabase().createJob(type, owner, payload);
				sender.sendMessage(Colors.Green + "Scheduled job");
			} catch (Exception e) {
				lwc.sendSimpleUsage(sender, "/lwc -a createjob <type> <owner> <payload>");
			}
		}

		else if (action.equals("flush")) {
			sender.sendMessage(Colors.Green + "Flushing Update Thread..");
			lwc.getUpdateThread().flush();
			sender.sendMessage(Colors.Green + "Done.");
		}

		else if (action.equals("cleanup")) {

		}

		else if (action.equals("update")) {
			Updater updater = lwc.getPlugin().getUpdater();

			if (updater.checkDist()) {
				sender.sendMessage(Colors.Green + "Updated LWC successfully to version: " + updater.getLatestPluginVersion());
				sender.sendMessage(Colors.Green + "Please reload LWC to complete the update");
			} else {
				sender.sendMessage(Colors.Red + "No update found.");
			}
		}

		else if (action.equalsIgnoreCase("limits")) {
			if (args.length < 3) {
				lwc.sendSimpleUsage(sender, "/lwc -admin limits <count> <Groups/Users>");
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
					sender.sendMessage(Colors.Green + "Registered global limit of " + Colors.Gold + limit + Colors.Green + " protections");
				} else if (limit != -2) {
					lwc.getPhysicalDatabase().registerProtectionLimit(type, limit, entity);
					sender.sendMessage(Colors.Green + "Registered limit of " + Colors.Gold + limit + Colors.Green + " protections to the " + (isGroup ? "group" : "user") + " " + Colors.Gold + entity);
				} else {
					lwc.getPhysicalDatabase().unregisterProtectionLimit(type, entity);
					sender.sendMessage(Colors.Green + "Unregistered limit for " + Colors.Gold + entity);
				}
			}
		}

		else if (action.equals("convert")) {
			if (args.length < 2) {
				lwc.sendSimpleUsage(sender, "/lwc -admin convert chestprotect|chastity");
				return;
			}

			String pluginToConvert = args[1].toLowerCase();

			if (pluginToConvert.equals("chestprotect")) {
				new ChestProtect(sender);
			} else if (pluginToConvert.equals("chastity")) {
				new ChastityChest(sender);
			}
		}

		else if (action.equals("clear")) {
			if (args.length < 2) {
				lwc.sendSimpleUsage(sender, "/lwc -admin clear chests|limits|rights");
				return;
			}

			String toClear = args[2].toLowerCase();

			if (toClear.equals("protections")) {
				lwc.getPhysicalDatabase().unregisterProtectionEntities();
				lwc.getPhysicalDatabase().unregisterProtectionRights();

				sender.sendMessage(Colors.Green + "Removed all protections + rights");
			} else if (toClear.equals("rights")) {
				lwc.getPhysicalDatabase().unregisterProtectionRights();

				sender.sendMessage(Colors.Green + "Removed all protection rights");
			} else if (toClear.equals("limits")) {
				lwc.getPhysicalDatabase().unregisterProtectionLimits();

				sender.sendMessage(Colors.Green + "Removed all protection limits");
			}
		}
	}

	public void sendHelp(CommandSender player) {
		player.sendMessage(" ");
		player.sendMessage(Colors.Red + "LWC Administration");
		player.sendMessage(" ");
		player.sendMessage("/lwc admin reload " + Colors.Blue + "Reload LWC configuration");
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
	public boolean validate(LWC lwc, CommandSender player, String[] args) {
		return lwc.isAdmin(player) && (hasFlag(args, "a") || hasFlag(args, "admin"));
	}

}
