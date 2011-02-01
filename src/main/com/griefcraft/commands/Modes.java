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

import org.bukkit.entity.Player;

import com.griefcraft.lwc.LWC;
import com.griefcraft.util.Colors;

public class Modes implements ICommand {

	@Override
	public String getName() {
		return "/lwc -p";
	}

	@Override
	public void execute(LWC lwc, Player player, String[] args) {
		if (args.length < 2) {
			lwc.sendSimpleUsage(player, "/lwc -p <persist|droptransfer>");
			return;
		}

		String mode = args[1].toLowerCase();

		if (mode.equals("persist")) {
			if (!lwc.isAdmin(player) && lwc.isModeBlacklisted(mode)) {
				player.sendMessage(Colors.Red + "That mode is currently disabled");
				return;
			}

			lwc.getMemoryDatabase().registerMode(player.getName(), mode);
			player.sendMessage(Colors.Green + "Persistance mode activated");
			player.sendMessage(Colors.Green + "Type " + Colors.Gold + "/lwc -r modes" + Colors.Green + " to undo (or logout)");
		}

		else if (mode.equals("droptransfer")) {
			mode = "dropTransfer";

			if (!lwc.isAdmin(player) && lwc.isModeBlacklisted(mode)) {
				player.sendMessage(Colors.Red + "That mode is currently disabled");
				return;
			}

			if (args.length < 3) {
				player.sendMessage(Colors.Green + "LWC Drop Transfer");
				player.sendMessage("");
				player.sendMessage(Colors.Blue + "/lwc -p droptransfer select - Select a chest to drop transfer to");
				player.sendMessage(Colors.Blue + "/lwc -p droptransfer on - Turn on drop transferring");
				player.sendMessage(Colors.Blue + "/lwc -p droptransfer off - Turn off drop transferring");
				player.sendMessage(Colors.Blue + "/lwc -p droptransfer status - Check the status of drop transferring");
				return;
			}

			String action = args[2].toLowerCase();
			String playerName = player.getName();

			if (action.equals("select")) {
				if (lwc.isPlayerDropTransferring(playerName)) {
					player.sendMessage(Colors.Red + "Please turn off drop transfer before reselecting a chest.");
					return;
				}

				lwc.getMemoryDatabase().unregisterMode(playerName, mode);
				lwc.getMemoryDatabase().registerAction("dropTransferSelect", playerName, "");

				player.sendMessage(Colors.Green + "Please left-click a registered chest to set as your transfer target.");
			} else if (action.equals("on")) {
				int target = lwc.getPlayerDropTransferTarget(playerName);

				if (target == -1) {
					player.sendMessage(Colors.Red + "Please register a chest before turning drop transfer on.");
					return;
				}

				lwc.getMemoryDatabase().unregisterMode(playerName, "dropTransfer");
				lwc.getMemoryDatabase().registerMode(playerName, "dropTransfer", "t" + target);
				player.sendMessage(Colors.Green + "Drop transfer is now on.");
				player.sendMessage(Colors.Green + "Any items dropped will be transferred to your chest.");
			} else if (action.equals("off")) {
				int target = lwc.getPlayerDropTransferTarget(playerName);

				if (target == -1) {
					player.sendMessage(Colors.Red + "Please register a chest before turning drop transfer off.");
					return;
				}

				lwc.getMemoryDatabase().unregisterMode(playerName, "dropTransfer");
				lwc.getMemoryDatabase().registerMode(playerName, "dropTransfer", "f" + target);

				player.sendMessage(Colors.Green + "Drop transfer is now off.");
			} else if (action.equals("status")) {
				if (lwc.getPlayerDropTransferTarget(playerName) == -1) {
					player.sendMessage(Colors.Green + "You have not registered a drop transfer target.");
				} else {
					if (lwc.isPlayerDropTransferring(playerName)) {
						player.sendMessage(Colors.Green + "Drop transfer is currently active.");
					} else {
						player.sendMessage(Colors.Green + "Drop transfer is currently inactive.");
					}
				}
			}
		}

	}

	@Override
	public boolean validate(LWC lwc, Player player, String[] args) {
		return hasFlag(args, "p");
	}

}
