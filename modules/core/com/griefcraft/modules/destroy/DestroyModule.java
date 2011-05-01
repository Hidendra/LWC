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

package com.griefcraft.modules.destroy;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;

public class DestroyModule extends JavaModule {

	@Override
	public String getName() {
		return "com.griefcraft.modules.destroy";
	}
	
	@Override
	public Result onDestroyProtection(LWC lwc, Player player, Protection protection, Block block, boolean canAccess, boolean canAdmin) {
		if(canAdmin) {
			protection.remove();
            lwc.sendLocale(player, "protection.unregistered", "block", LWC.materialToString(protection.getBlockId()));
            return CANCEL;
		}
		
		if(!canAccess) {
			return CANCEL;
		}
		
		return DEFAULT;
	}

}
