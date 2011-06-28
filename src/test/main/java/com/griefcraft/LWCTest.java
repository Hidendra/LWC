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

package com.griefcraft;

import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;
import org.junit.Test;

import com.griefcraft.bukkit.MockServer;
import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCPlugin;

import static org.junit.Assert.*;

public class LWCTest {

	/**
	 * mock Bukkit server
	 */
	private static MockServer server = null;
	private static Logger logger = Logger.getLogger("TestSuite");
	
	public LWCTest() {
		if(LWCTest.server == null) {
			LWCTest.server = new MockServer();
		}
		
		System.out.println("cnstrct");
	}
	
	@Test
	public void CheckLWC() {
		assertNotNull(getLWC());
	}
	
	@Test
	public void Nothing() {
		
	}
	
	/**
	 * @return the LWC object
	 */
	private LWC getLWC() {
		Plugin plugin = server.getPluginManager().getPlugin("LWC");
		
		if(plugin == null) {
			return null;
		}
		
		return ((LWCPlugin) plugin).getLWC();
	}
	
}
