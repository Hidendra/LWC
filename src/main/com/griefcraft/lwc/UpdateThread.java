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
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import com.griefcraft.logging.Logger;
import com.griefcraft.model.Job;
import com.griefcraft.model.Protection;
import com.griefcraft.util.ConfigValues;
import com.griefcraft.util.StringUtils;

public class UpdateThread implements Runnable {

	/**
	 * Queue that protections can be added to to update them in the database periodically in a seperate thread
	 */
	private ConcurrentLinkedQueue<Protection> protectionUpdateQueue = new ConcurrentLinkedQueue<Protection>();

	/**
	 * True begins the flush
	 */
	private boolean flush = false;

	/**
	 * The last update
	 */
	private long lastUpdate = -1L;

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
	 * Activate flushing
	 */
	public void flush() {
		flush = true;
	}

	/**
	 * Add a protection to be updated to the top of the queue (JUST block ids!!)
	 * 
	 * @param protection
	 */
	public void queueProtectionUpdate(Protection protection) {
		protectionUpdateQueue.offer(protection);
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

	/**
	 * Stop the update thread. Also flush the remaining updates since we're stopping anyway
	 */
	public void stop() {
		running = false;

		if (thread != null && !thread.isInterrupted()) {
			thread.interrupt();
		}
	}

	/**
	 * Flush any caches to the database TODO
	 */
	private void _flush() {
		// periodically update protections in the database if a non-critical change was made
		if (protectionUpdateQueue.size() > 0) {
			Connection connection = lwc.getPhysicalDatabase().getConnection();
			Protection protection = null;

			try {
				connection.setAutoCommit(false);
			} catch (SQLException e) {
				e.printStackTrace();
			}

			/*
			 * Loop through
			 */
			while ((protection = protectionUpdateQueue.poll()) != null) {
				protection.saveNow();
			}

			/*
			 * Commit
			 */
			try {
				connection.commit();
				connection.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		if(LWCInfo.DEVELOPMENT) {
			doJobs();
		}

		flush = false;
		lastUpdate = System.currentTimeMillis();
	}

	/**
	 * Just keep this seperate
	 */
	private void doJobs() {
		List<Job> jobs = lwc.getPhysicalDatabase().getJobQueue(ConfigValues.MAX_JOBS.getInt());

		if (jobs.size() > 0) {
			logger.log("Loaded " + jobs.size() + " Jobs");

			for (Job job : jobs) {
				logger.log("Executing job id #" + job.getId());

				int type = job.getType();

				switch (type) {
				/*
				 * Jobs that start with "x y z". Optional payload can be passed after Z following a space: "x y z extra"
				 */

				case Job.REMOVE_BLOCK:
				case Job.OPEN_DOOR:
				case Job.DISPENSE_DISPENSER:
				case Job.UPDATE_SIGN:
					/* Expected initial payload: "x y z" */
					String[] coordinates = job.getPayload().split(" ");

					if (coordinates.length < 3) {
						logger.log("Unexpected payload in job " + job.getId() + ": " + job.getPayload());
						continue;
					}

					try {
						int x = Integer.parseInt(coordinates[0]);
						int y = Integer.parseInt(coordinates[1]);
						int z = Integer.parseInt(coordinates[2]);
						String extra = "";

						if (coordinates.length > 3) {
							extra = StringUtils.join(coordinates, 3).trim();
						}

						/* TODO: ?? */
						World world = lwc.getPlugin().getServer().getWorlds().get(0);

						/* Get the current block from the world */
						Block block = world.getBlockAt(x, y, z);

						/* Now find out what the job specifically does */

						if (type == Job.REMOVE_BLOCK) {
							block.setData((byte) 0);
							block.setType(Material.AIR);
						} else if (type == Job.OPEN_DOOR) {
							if (block.getType() == Material.WOODEN_DOOR || block.getType() == Material.IRON_DOOR_BLOCK) {
								Block[] doors = getDoors(world, block);

								byte bottomDoor = doors[0].getData();
								byte topDoor = doors[1].getData();

								if ((block.getData() & 0x4) == 0x4) {
									bottomDoor -= 4;
									topDoor -= 4;
								} else {
									bottomDoor |= 0x4;
									topDoor |= 0x4;
								}

								doors[0].setData(bottomDoor);
								doors[1].setData(topDoor);
							}
						} else if (type == Job.DISPENSE_DISPENSER) {
							if (block.getType() == Material.DISPENSER) {
								Dispenser dispenser = (Dispenser) block.getState();
								dispenser.dispense();
							}
						} else if (type == Job.UPDATE_SIGN) {
							if (block.getType() == Material.SIGN || block.getType() == Material.SIGN_POST) {
								Sign sign = (Sign) block.getState();

								String[] lines = extra.split("==X8LarE=="); // split
																			// by
																			// how
																			// lines
																			// are
																			// split
																			// internally

								for (int line = 0; line < lines.length; line++) {
									if (line > 3) {
										break;
									}
									logger.log(line + ":" + lines[line]);
									sign.setLine(line, lines[line]);
								}

								sign.update();
							}
						}

						/* Remove the job, we assume it's done.. */
						lwc.getPhysicalDatabase().removeJob(job.getId());
						// lwc.dev("Job completed: #" + job.getId());
					} catch (Exception e) {
						e.printStackTrace();
						logger.log("Unexpected payload in job " + job.getId() + ": " + job.getPayload());
					}

					break;

				case Job.SEND_MESSAGE:
					/* Expected payload: "Name:Message" */
					int index = job.getPayload().indexOf(":");

					if (index == -1) {
						logger.log("Unexpected payload in job " + job.getId() + ": " + job.getPayload());
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
						// lwc.dev("Job completed: #" + job.getId());
					}

					break;
				}
			}
		}

	}

	/**
	 * Get the two Block objects for a door doors[0] = Bottom half of door doors[1] = Top half of door
	 * 
	 * @param world
	 * @param block
	 * @return
	 */
	private Block[] getDoors(World world, Block block) {
		Block[] doors = new Block[2];

		Block temp;

		if ((temp = block.getFace(BlockFace.UP)) != null && temp.getType() == block.getType()) {
			doors[0] = block;
			doors[1] = temp;
		} else if ((temp = block.getFace(BlockFace.DOWN)) != null && temp.getType() == block.getType()) {
			doors[0] = temp;
			doors[1] = block;
		}

		return doors;
	}

}
