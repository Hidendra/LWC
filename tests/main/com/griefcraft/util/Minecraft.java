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

package com.griefcraft.util;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.MinecraftServer;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;

import com.griefcraft.bukkit.TestPlayer;
import com.griefcraft.tests.TestSuite;

/**
 * Utility class to do direct Minecraft related things, such as creating a player (for testing purposes!)
 * Most of these methods use code that can potentially break on future releases!
 */
public class Minecraft {
	
	/**
	 * The test suite this class this associated with
	 */
	private TestSuite testSuite;
	
	public Minecraft(TestSuite testSuite) {
		this.testSuite = testSuite;
	}
	
	/**
	 * Create a player using the given name on the default world
	 * 
	 * @param name
	 * @return
	 */
	public Player createPlayer(String name) {
		return createPlayer(name, Bukkit.getServer().getWorlds().get(0));
	}
	
	/**
	 * Create a player using the given name and world
	 * 
	 * @param name
	 * @param world
	 * @return
	 */
	public Player createPlayer(String name, World world) {
		net.minecraft.server.World worldHandle = ((CraftWorld) world).getHandle(); // raw mc world handle
		ItemInWorldManager itemInWorldManager = new ItemInWorldManager(worldHandle); // item manager
		
		EntityPlayer entityPlayer = new EntityPlayer(getMinecraftServer(), worldHandle, name, itemInWorldManager); // create the raw player
		
		TestPlayer testPlayer = new TestPlayer((CraftServer) Bukkit.getServer(), entityPlayer); // create our test player
		testPlayer.setTestSuite(testSuite); // hand over our test suite object
		
		return testPlayer;
	}
	
	/**
	 * @return the MinecraftServer handle
	 */
	public static MinecraftServer getMinecraftServer() {
		return ((CraftServer) Bukkit.getServer()).getHandle().c;
	}

}
