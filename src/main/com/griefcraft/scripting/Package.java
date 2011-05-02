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

package com.griefcraft.scripting;

/**
 * Used by modules to register all included modules
 */
public abstract class Package {
	
	/**
	 * Register any modules used by the package
	 * 
	 * @param moduleLoader
	 */
	public abstract void registerModules(ModuleLoader moduleLoader);
	
	/**
	 * @return the name of the package
	 */
	public abstract String getName();
	
	/**
	 * @return the package version
	 */
	public abstract double getVersion();
	
	/**
	 * Creates meta data for a standard module (usually java)
	 * 
	 * @param module
	 * @return
	 */
	public static MetaData createMetaData(Module module) {
		return new MetaData(module.getName(), module);
	}
	
}
