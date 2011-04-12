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

package com.griefcraft.tests.impl;

import org.bukkit.Bukkit;
import org.bukkit.Server;

import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.tests.TestSuite;

public abstract class LWCTest extends BaseTest {

	/**
	 * The LWC object, loaded when the first LWCTest object is created
	 */
	protected static LWC lwc = null;
	
	public LWCTest() {
		// load LWC if needed
		if(LWCTest.lwc == null) {
			Server server = Bukkit.getServer();
			LWCTest.lwc = ((LWCPlugin) server.getPluginManager().getPlugin("LWC")).getLWC();
		}
	}
	
	public abstract String getName();
	public abstract void execute();

}
