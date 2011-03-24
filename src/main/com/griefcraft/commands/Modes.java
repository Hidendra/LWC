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

		if (mode.equals("persist")) {
			if (!lwc.isAdmin(sender) && lwc.isModeBlacklisted(mode)) {
				sender.sendMessage(Colors.Red + "That mode is currently disabled");
				return;
			}

			lwc.getMemoryDatabase().registerMode(player.getName(), mode);
			sender.sendMessage(Colors.Green + "Your commands will now persist");
			sender.sendMessage(Colors.Green + "Type " + Colors.Gold + "/lwc remove modes" + Colors.Green + " to undo (or logout)");
		}

		else if (mode.equals("droptransfer")) {
			mode = "dropTransfer";

			if (!lwc.isAdmin(sender) && lwc.isModeBlacklisted(mode)) {
				sender.sendMessage(Colors.Red + "That mode is currently disabled");
				return;
			}

			if (args.length < 3) {
				sender.sendMessage(Colors.Green + "LWC Drop Transfer");
				sender.sendMessage("");
				sender.sendMessage("/lwc mode droptransfer " + Colors.Blue + "select" + Colors.White + " - Select a chest to drop transfer to");
				sender.sendMessage("/lwc mode droptransfer " + Colors.Blue + "on" + Colors.White + " - Turn on drop transferring");
				sender.sendMessage("/lwc mode droptransfer " + Colors.Blue + "off" + Colors.White + " - Turn off drop transferring");
				sender.sendMessage("/lwc mode droptransfer " + Colors.Blue + "status" + Colors.White + " - Check the status of drop transferring");
				return;
			}

			String action = args[2].toLowerCase();
			String playerName = player.getName();

			if (action.equals("select")) {
				if (lwc.isPlayerDropTransferring(playerName)) {
					sender.sendMessage(Colors.Red + "Please turn off drop transfer before reselecting a chest.");
					return;
				}

				lwc.getMemoryDatabase().unregisterMode(playerName, mode);
				lwc.getMemoryDatabase().registerAction("dropTransferSelect", playerName, "");

				sender.sendMessage(Colors.Blue + "Please left click a registered chest to set it as your transfer target.");
			} else if (action.equals("on")) {
				int target = lwc.getPlayerDropTransferTarget(playerName);

				if (target == -1) {
					sender.sendMessage(Colors.Red + "Please register a chest before turning drop transfer on.");
					return;
				}

				lwc.getMemoryDatabase().registerMode(playerName, "+dropTransfer");
				sender.sendMessage(Colors.Blue + "Drop transfer is now on.");
				sender.sendMessage(Colors.Blue + "Any items dropped will be transferred to your chest.");
			} else if (action.equals("off")) {
				int target = lwc.getPlayerDropTransferTarget(playerName);

				if (target == -1) {
					sender.sendMessage(Colors.Red + "Please register a chest before turning drop transfer off.");
					return;
				}

				lwc.getMemoryDatabase().unregisterMode(playerName, "+dropTransfer");

				sender.sendMessage(Colors.Blue + "Drop transfer is now off.");
			} else if (action.equals("status")) {
				if (lwc.getPlayerDropTransferTarget(playerName) == -1) {
					sender.sendMessage(Colors.Blue + "You have not registered a drop transfer target.");
				} else {
					if (lwc.isPlayerDropTransferring(playerName)) {
						sender.sendMessage(Colors.Blue + "Drop transfer is currently active.");
					} else {
						sender.sendMessage(Colors.Blue + "Drop transfer is currently inactive.");
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
