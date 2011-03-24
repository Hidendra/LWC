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

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.inventory.ItemStack;

public class ProtectionInventory {

	/**
	 * The inventory
	 */
	private ItemStack[] itemStacks;

	/**
	 * The protection id this inventory is attached to
	 */
	private int protectionId;

	/**
	 * @return
	 */
	public ItemStack[] getItemStacks() {
		return itemStacks;
	}

	/**
	 * @return
	 */
	public int getProtectionId() {
		return protectionId;
	}

	@Override
	public int hashCode() {
		return 0x00;
	}

	/**
	 * Check if this inventory is in a queue
	 * 
	 * @param queue
	 * @return
	 */
	public boolean isIn(ConcurrentLinkedQueue<ProtectionInventory> queue) {
		Iterator<ProtectionInventory> iterator = queue.iterator();

		while (iterator.hasNext()) {
			ProtectionInventory pInventory = iterator.next();

			if (pInventory.getProtectionId() == protectionId) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Set the inventory
	 * 
	 * @param inventory
	 */
	public void setItemStacks(ItemStack[] itemStacks) {
		this.itemStacks = itemStacks;
	}

	/**
	 * Set the protection id
	 * 
	 * @param protectionId
	 */
	public void setProtectionId(int protectionId) {
		this.protectionId = protectionId;
	}

}
