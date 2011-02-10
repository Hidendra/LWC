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

import static com.griefcraft.util.StringUtils.encrypt;
import static com.griefcraft.util.StringUtils.hasFlag;
import static com.griefcraft.util.StringUtils.join;

import org.bukkit.entity.Player;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.griefcraft.model.ProtectionTypes;
import com.griefcraft.util.Colors;

public class Unlock implements ICommand {

	@Override
	public String getName() {
		return "/lwc -unlock";
	}

	@Override
	public void execute(LWC lwc, Player player, String[] args) {
		if (args.length < 1) {
			player.sendMessage(Colors.Red + "Usage: " + Colors.Gold + "/lwc -u <Password>");
			return;
		}

		String password = join(args, 1);
		password = encrypt(password);

		if (!lwc.getMemoryDatabase().hasPendingUnlock(player.getName())) {
			player.sendMessage(Colors.Red + "Nothing selected. Open a locked protection first.");
			return;
		}

		else {
			int chestID = lwc.getMemoryDatabase().getUnlockID(player.getName());

			if (chestID == -1) {
				player.sendMessage(Colors.Red + "[lwc] Internal error. [ulock]");
				return;
			}

			Protection entity = lwc.getPhysicalDatabase().loadProtectedEntity(chestID);

			if (entity.getType() != ProtectionTypes.PASSWORD) {
				player.sendMessage(Colors.Red + "That is not password protected!");
				return;
			}

			if (entity.getData().equals(password)) {
				player.sendMessage(Colors.Green + "Password accepted.");
				lwc.getMemoryDatabase().unregisterUnlock(player.getName());
				lwc.getMemoryDatabase().registerPlayer(player.getName(), chestID);
			} else {
				player.sendMessage(Colors.Red + "Invalid password.");
			}
		}
	}

	@Override
	public boolean validate(LWC lwc, Player player, String[] args) {
		return hasFlag(args, "u") || hasFlag(args, "unlock");
	}

}
