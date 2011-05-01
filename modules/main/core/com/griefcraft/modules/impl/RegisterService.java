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

package com.griefcraft.modules.impl;

import com.griefcraft.modules.create.CreateModule;
import com.griefcraft.modules.destroy.DestroyModule;
import com.griefcraft.modules.free.FreeModule;
import com.griefcraft.modules.info.InfoModule;
import com.griefcraft.modules.modadmin.ModuleAdmin;
import com.griefcraft.scripting.ModuleLoader;
import com.griefcraft.scripting.Package;

public class RegisterService extends Package {

	/**
	 * The current version
	 */
	public final static double CORE_VERSION = 1.00;
	
	@Override
	public String getName() {
		return "core";
	}
	
	@Override
	public double getVersion() {
		return CORE_VERSION;
	}
	
	@Override
	public void registerModules(ModuleLoader moduleLoader) {
		moduleLoader.registerModule(this, new CreateModule());
		moduleLoader.registerModule(this, new InfoModule());
		moduleLoader.registerModule(this, new FreeModule());
		moduleLoader.registerModule(this, new DestroyModule());
		moduleLoader.registerModule(this, new ModuleAdmin());
	}
	
}
