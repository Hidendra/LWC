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

package com.griefcraft.modules.unlock;

import static com.griefcraft.util.StringUtils.encrypt;
import static com.griefcraft.util.StringUtils.join;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.griefcraft.model.ProtectionTypes;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.util.Colors;
import com.griefcraft.util.StringUtils;

public class UnlockModule extends JavaModule {

	@Override
	public Result onCommand(LWC lwc, CommandSender sender, String command, String[] args) {
		if(!StringUtils.hasFlag(command, "u") && !StringUtils.hasFlag(command, "unlock")) {
			return DEFAULT;
		}
		
		if(!(sender instanceof Player)) {
			sender.sendMessage(Colors.Red + "Console is not supported.");
			return CANCEL;
		}
		
		if (args.length < 1) {
			lwc.sendSimpleUsage(sender, "/lwc -u <Password>");
			return CANCEL;
		}

		Player player = (Player) sender;
		String password = join(args, 0);
		password = encrypt(password);

		if (!lwc.getMemoryDatabase().hasPendingUnlock(player.getName())) {
			player.sendMessage(Colors.Red + "Nothing selected. Open a locked protection first.");
			return CANCEL;
		} else {
			int chestID = lwc.getMemoryDatabase().getUnlockID(player.getName());

			if (chestID == -1) {
				lwc.sendLocale(player, "protection.internalerror", "id", "ulock");
				return CANCEL;
			}

			Protection entity = lwc.getPhysicalDatabase().loadProtection(chestID);

			if (entity.getType() != ProtectionTypes.PASSWORD) {
				lwc.sendLocale(player, "protection.unlock.notpassword");
				return CANCEL;
			}

			if (entity.getData().equals(password)) {
				lwc.getMemoryDatabase().unregisterUnlock(player.getName());
				lwc.getMemoryDatabase().registerPlayer(player.getName(), chestID);
				lwc.sendLocale(player, "protection.unlock.password.valid");
			} else {
				lwc.sendLocale(player, "protection.unlock.password.invalid");
			}
		}

		return CANCEL;
	}
	
}
