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


import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;

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

    public void run() {
        while (running) {
            if (flush) {
                _flush();
                continue;
            }

            int flushInterval = lwc.getConfiguration().getInt("core.flushInterval", 5);
            long curr = System.currentTimeMillis();
            long interval = flushInterval * 1000L;

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

        flush = false;
        lastUpdate = System.currentTimeMillis();
    }

}
