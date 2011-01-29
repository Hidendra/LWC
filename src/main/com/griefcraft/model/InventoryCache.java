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

package com.griefcraft.model;

import java.util.LinkedList;

import org.bukkit.inventory.Inventory;

public class InventoryCache {

	/**
	 * Holds the cached inventories
	 * 
	 * We will be removing/adding potentially a lot of different inventories if the cache size is relatively small
	 */
	private LinkedList<Inventory> inventories = new LinkedList<Inventory>();

	/**
	 * @return the inventories
	 */
	public LinkedList<Inventory> getAll() {
		return inventories;
	}

	/**
	 * Empty the inventory cache
	 */
	public void empty() {
		inventories.clear();
	}

	/**
	 * @return the size of the cache
	 */
	public int size() {
		return inventories.size();
	}

	/**
	 * Push an inventory to the front of the cache
	 * 
	 * @param inventory
	 */
	public void push(Inventory inventory) {
		if (inventories.contains(inventory)) {
			return;
		}

		inventories.add(inventory);
	}

}
