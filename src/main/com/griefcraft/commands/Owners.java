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

public class Owners implements ICommand {

	@Override
	public String getName() {
		return "/lwc -owners";
	}

	@Override
	public void execute(LWC lwc, Player player, String[] args) {
		int page = 1;
		
		if(args.length > 1) {
			try {
				page = Integer.parseInt(args[1]);
			} catch(Exception e) {
				lwc.sendSimpleUsage(player, "/lwc -owners [page]");
				return;
			}
		}
		
		lwc.getMemoryDatabase().unregisterAllActions(player.getName());
		lwc.getMemoryDatabase().registerAction("owners:" + page, player.getName());
		player.sendMessage(Colors.Blue + "Punch a protection to view who has access");
	}

	@Override
	public boolean validate(LWC lwc, Player player, String[] args) {
		return hasFlag(args, "o") || hasFlag(args, "owner") || hasFlag(args, "owners");
	}

}
