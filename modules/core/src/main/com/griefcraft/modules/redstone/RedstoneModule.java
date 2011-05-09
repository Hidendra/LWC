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

package com.griefcraft.modules.redstone;

import org.bukkit.block.Block;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;

public class RedstoneModule extends JavaModule {
	
	@Override
	public Result onRedstone(LWC lwc, Protection protection, Block block, int current) {
		boolean hasFlag = protection.hasFlag(Protection.Flag.REDSTONE);
		boolean denyRedstone = lwc.getConfiguration().getBoolean("protections.denyRedstone", false);
		
		if(!hasFlag && denyRedstone) {
			return CANCEL;
		} else if(hasFlag && !denyRedstone) {
			return CANCEL;
		}
		
		return DEFAULT;
	}
	
}
