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

package com.griefcraft.modules.modes;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.ContainerBlock;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;

public class MagnetModule extends JavaModule {
	
	// does all of the work
	// searches the worlds for items and magnet chests nearby
	class SearchThread implements Runnable {
		public void run() {
			Server server = Bukkit.getServer();
			LWC lwc = LWC.getInstance();
			
			for(World world : server.getWorlds()) {
				String worldName = world.getName();
				List<Entity> entities = world.getEntities();
				Iterator<Entity> iterator = entities.iterator();
				
				while(iterator.hasNext()) {
					Entity entity = iterator.next();
					if(!(entity instanceof Item)) {
						continue;
					}
					
					Item item = (Item) entity;
					ItemStack itemStack = item.getItemStack();
					Location location = item.getLocation();
					int x = location.getBlockX();
					int y = location.getBlockY();
					int z = location.getBlockZ();
					
					List<Protection> protections = lwc.getPhysicalDatabase().loadProtections(worldName, x, y, z, 3);
					Block block = null;
					Protection protection = null;
					
					for(Protection temp : protections) {
						protection = temp;
						block = world.getBlockAt(protection.getX(), protection.getY(), protection.getZ());
						
						if(!(block.getState() instanceof ContainerBlock)) {
							continue;
						}
						
						if(!protection.hasFlag(Protection.Flag.MAGNET)) {
							continue;
						}
						
						// Remove the items and suck them up :3
						Map<Integer, ItemStack> remaining = lwc.depositItems(block, itemStack);
						
						if(remaining.size() == 1) {
							ItemStack other = remaining.values().iterator().next();
							
							if(itemStack.getTypeId() == other.getTypeId() && itemStack.getAmount() == other.getAmount() && itemStack.getData() == other.getData() && itemStack.getDurability() == other.getDurability()) {
								continue;
							}
						}
						
						// remove the item on the ground
						item.remove();
						
						// if we have a remainder, we need to drop them
						if(remaining.size() > 0) {
							for(ItemStack stack : remaining.values()) {
								world.dropItemNaturally(location, stack);
							}
						}
						
						break;
					}
				}
			}
		}
	}
	
	/**
	 * The BukkitScheduler task id
	 */
	private int taskId;
	
	@Override
	public void load(LWC lwc) {
		// register our search thread schedule
		SearchThread searchThread = new SearchThread();
		taskId = lwc.getPlugin().getServer().getScheduler().scheduleSyncRepeatingTask(lwc.getPlugin(), searchThread, 50, 50);
	}
	
}
