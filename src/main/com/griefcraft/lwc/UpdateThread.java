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
import java.util.List;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.griefcraft.logging.Logger;
import com.griefcraft.model.Job;
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

	/**
	 * True begins the flush
	 */
	private boolean flush = false;

	public UpdateThread(LWC lwc) {
		this.lwc = lwc;

		if (LWCInfo.DEVELOPMENT == false) {
			return;
		}

		running = true;
		lastUpdate = System.currentTimeMillis();

		thread = new Thread(this);
		thread.start();
	}

	/**
	 * Just keep this seperate
	 */
	private void doJobs() {
		List<Job> jobs = lwc.getPhysicalDatabase().getJobQueue(ConfigValues.MAX_JOBS.getInt());

		if (jobs.size() > 0) {
			logger.info("Loaded " + jobs.size() + " Jobs");

			for (Job job : jobs) {
				logger.info("Executing job id #" + job.getId());

				switch (job.getType()) {
				case Job.REMOVE_BLOCK:
					/* Expected payload: "x y z" */
					String[] coordinates = job.getPayload().split(" ");

					if (coordinates.length != 3) {
						logger.info("Unexpected payload: " + job.getPayload());
						continue;
					}

					try {
						int x = Integer.parseInt(coordinates[0]);
						int y = Integer.parseInt(coordinates[1]);
						int z = Integer.parseInt(coordinates[2]);

						/* TODO: ?? */
						World world = lwc.getPlugin().getServer().getWorlds()[0];

						/* Get the current block from the world */
						Block block = world.getBlockAt(x, y, z);

						/* Remove the block */
						block.setData((byte) 0);
						block.setType(Material.AIR);

						/* Remove the job, we assume it's done.. */
						lwc.getPhysicalDatabase().removeJob(job.getId());
						logger.info("Job completed: #" + job.getId());
					} catch (Exception e) {
						logger.info("Unexpected payload: " + job.getPayload());
					}

					break;

				case Job.SEND_MESSAGE:
					/* Expected payload: "Name:Message" */
					int index = job.getPayload().indexOf(":");

					if (index == -1) {
						logger.info("Unexpected payload: " + job.getPayload());
						continue;
					}

					String player = job.getPayload().substring(0, index);
					String message = job.getPayload().substring(index + 1);

					Player sendTo = lwc.getPlugin().getServer().getPlayer(player);

					/**
					 * Make sure they're online! Then remove the job
					 */
					if (sendTo != null && sendTo.isOnline()) {
						sendTo.sendMessage(message);
						lwc.getPhysicalDatabase().removeJob(job.getId());
						logger.info("Job completed: #" + job.getId());
					}

					break;
				}
			}
		}

	}

	/**
	 * Flush any caches to the database TODO
	 */
	private void _flush() {
		logger.info("Now checking caches to flush");

		if (lwc.getInventoryCache().size() > 0) {
			logger.info("Flushing " + lwc.getInventoryCache().size() + " inventories");

			Iterator<Inventory> iterator = lwc.getInventoryCache().getAll().iterator();
			Connection connection = lwc.getPhysicalDatabase().getConnection();

		}

		doJobs();

		flush = false;
		lastUpdate = System.currentTimeMillis();
	}

	/**
	 * Activate flushing
	 */
	public void flush() {
		flush = true;
	}

	/**
	 * Stop the update thread. Also flush the remaining updates since we're stopping anyway
	 */
	public void stop() {
		running = false;

		if (thread != null && !thread.isInterrupted()) {
			thread.interrupt();
		}
	}

	@Override
	public void run() {
		while (running) {
			if (flush) {
				_flush();
				continue;
			}

			long curr = System.currentTimeMillis();
			long interval = ConfigValues.FLUSH_DB_INTERVAL.getInt() * 1000L;

			if (curr - lastUpdate > interval) {
				flush = true;
			}

			try {
				Thread.sleep(1000L);
			} catch (Exception e) {
			}
		}
	}

}
