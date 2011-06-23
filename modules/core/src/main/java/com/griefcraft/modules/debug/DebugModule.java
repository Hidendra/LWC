package com.griefcraft.modules.debug;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCInfo;
import com.griefcraft.model.Protection;
import com.griefcraft.model.Protection.Flag;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.util.Colors;
import com.griefcraft.util.StringUtils;

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

public class DebugModule extends JavaModule {

	/**
	 * The value for yes in the locale
	 */
	private String yes = null;

	/**
	 * The value for no in the locale
	 */
	private String no = null;

	@Override
	public void load(LWC lwc) {
		yes = lwc.getLocale("yes");
		no = lwc.getLocale("no");
	}

	@Override
	public Result onCommand(LWC lwc, CommandSender sender, String command, String[] args) {
		if(!StringUtils.hasFlag(command, "debug")) {
			return DEFAULT;
		}

		if(!(sender instanceof Player)) {
			sender.sendMessage("This command is only usable by real players :-)");
			return CANCEL;
		}

		Player player = (Player) sender;

		player.sendMessage(" ");
		player.sendMessage(Colors.Gray + "LWC: " + LWCInfo.FULL_VERSION);
		player.sendMessage(" ");
		player.sendMessage(Colors.Green + "Standard LWC permissions");
		doPermission(player, "lwc.protect");

		doPlayerPermission(player, "lwc.create.public");
		doPlayerPermission(player, "lwc.create.password");
		doPlayerPermission(player, "lwc.create.private");
		doPlayerPermission(player, "lwc.info");
		doPlayerPermission(player, "lwc.remove");
		doPlayerPermission(player, "lwc.modify");
		doPlayerPermission(player, "lwc.unlock");

		for(Flag flag : Protection.Flag.values()) {
			doPlayerPermission(player, "lwc.flag." + flag.toString().toLowerCase());
		}

		player.sendMessage(" ");
		player.sendMessage(Colors.Yellow + "Mod permissions");
		doPermission(player, "lwc.mod");

		player.sendMessage(" ");
		player.sendMessage(Colors.Red + "Admin permissions");
		doPermission(player, "lwc.admin");


		return CANCEL;
	}

	/**
	 * @param player
	 * @param node
	 */
	private void doPermission(Player player, String node) {
		player.sendMessage(node + ": " + strval(LWC.getInstance().hasPermission(player, node)));
	}

	/**
	 * @param player
	 * @param node
	 */
	private void doPlayerPermission(Player player, String node) {
		player.sendMessage(node + ": " + strval(LWC.getInstance().hasPlayerPermission(player, node)));
	}

	/**
	 * @param player
	 * @param node
	 */
	private void doAdminPermission(Player player, String node) {
		player.sendMessage(node + ": " + strval(LWC.getInstance().hasAdminPermission(player, node)));
	}

	/**
	 * Convert a boolean to its yes/no equivilent
	 * 
	 * @param bool
	 * @return
	 */
	private String strval(boolean bool) {
		return bool ? yes : no;
	}

}
