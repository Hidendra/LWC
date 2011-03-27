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

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.griefcraft.lwc.LWC;
import com.griefcraft.util.Colors;

public class Modes implements ICommand {

	@Override
	public void execute(LWC lwc, CommandSender sender, String[] args) {
		if (args.length < 2) {
			lwc.sendSimpleUsage(sender, "/lwc mode <persist|droptransfer>");
			return;
		}

		String mode = args[1].toLowerCase();
		Player player = (Player) sender;
		
		if (!lwc.isAdmin(sender) && lwc.isModeBlacklisted(mode)) {
			lwc.sendLocale(player, "protection.modes.disabled");
			return;
		}

		if (mode.equals("persist")) {
			lwc.getMemoryDatabase().registerMode(player.getName(), mode);
			lwc.sendLocale(player, "protection.modes.persist.finalize");
		}

		else if (mode.equals("droptransfer")) {
			mode = "dropTransfer";

			if (args.length < 3) {
				lwc.sendLocale(player, "protection.modes.dropxfer.help");
				return;
			}

			String action = args[2].toLowerCase();
			String playerName = player.getName();

			if (action.equals("select")) {
				if (lwc.isPlayerDropTransferring(playerName)) {
					lwc.sendLocale(player, "protection.modes.dropxfer.select.error");
					return;
				}

				lwc.getMemoryDatabase().unregisterMode(playerName, mode);
				lwc.getMemoryDatabase().registerAction("dropTransferSelect", playerName, "");

				lwc.sendLocale(player, "protection.modes.dropxfer.select.finalize");
			} else if (action.equals("on")) {
				int target = lwc.getPlayerDropTransferTarget(playerName);

				if (target == -1) {
					lwc.sendLocale(player, "protection.modes.dropxfer.selectchest");
					return;
				}

				lwc.getMemoryDatabase().registerMode(playerName, "+dropTransfer");
				lwc.sendLocale(player, "protection.modes.dropxfer.on.finalize");
			} else if (action.equals("off")) {
				int target = lwc.getPlayerDropTransferTarget(playerName);

				if (target == -1) {
					lwc.sendLocale(player, "protection.modes.dropxfer.selectchest");
					return;
				}

				lwc.getMemoryDatabase().unregisterMode(playerName, "+dropTransfer");
				lwc.sendLocale(player, "protection.modes.dropxfer.off.finalize");
			} else if (action.equals("status")) {
				if (lwc.getPlayerDropTransferTarget(playerName) == -1) {
					lwc.sendLocale(player, "protection.modes.dropxfer.status.off");
				} else {
					if (lwc.isPlayerDropTransferring(playerName)) {
						lwc.sendLocale(player, "protection.modes.dropxfer.status.active");
					} else {
						lwc.sendLocale(player, "protection.modes.dropxfer.status.inactive");
					}
				}
			}
		}

	}

	@Override
	public String getName() {
		return "modes";
	}

	@Override
	public boolean supportsConsole() {
		return false;
	}

	@Override
	public boolean validate(LWC lwc, CommandSender player, String[] args) {
		return hasFlag(args, "p") || hasFlag(args, "mode");
	}

}
