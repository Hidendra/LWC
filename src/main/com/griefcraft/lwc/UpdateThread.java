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

package com.griefcraft.lwc;

import java.sql.Connection;
import java.util.Iterator;

import org.bukkit.inventory.Inventory;

import com.griefcraft.logging.Logger;
import com.griefcraft.util.ConfigValues;

public class UpdateThread implements Runnable {

	private Logger logger = Logger.getLogger("Cache");
	
	/**
	 * The LWC object
	 */
	private LWC lwc;
	
	/**
	 * If the update thread is running
	 */
	private boolean running = false;
	
	/**
	 * The last update
	 */
	private long lastUpdate = -1L;
	
	/**
	 * Thread being used
	 */
	private Thread thread;
	
	public UpdateThread(LWC lwc) {
		this.lwc = lwc;
		
		running = true;
		lastUpdate = System.currentTimeMillis();
		
		thread = new Thread(this);
		thread.start();
	}
	
	/**
	 * Flush any caches to the database
	 * TODO
	 */
	public void flush() {
		// logger.info("Now checking caches to flush");
		
		if(lwc.getInventoryCache().size() > 0) {
			logger.info("Flushing " + lwc.getInventoryCache().size() + " inventories");
			
			Iterator<Inventory> iterator = lwc.getInventoryCache().getAll().iterator();
			Connection connection = lwc.getPhysicalDatabase().getConnection();
			
			
			
			
		}
		
		
	}
	
	/**
	 * Stop the update thread. 
	 * Also flush the remaining updates since we're stopping anyway
	 */
	public void stop() {
		running = false;
		thread.interrupt();
	}
	
	@Override
	public void run() {
		while(running) {
			long curr = System.currentTimeMillis();
			long interval = ConfigValues.FLUSH_DB_INTERVAL.getInt() * 1000L;
			
			if(curr - lastUpdate > interval) {
				flush();
				lastUpdate = System.currentTimeMillis();
			}
			
			try {
				Thread.sleep(1000L);
			}
			catch(Exception e) { }
		}
	}
	
}
