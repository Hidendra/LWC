/*
 * Copyright 2011 Tyler Blair. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and contributors and should not be interpreted as representing official policies,
 * either expressed or implied, of anybody else.
 */

package com.griefcraft.util;


import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

public class UpdateThread implements Runnable {

    /**
     * Queue used to update protections on a seperate thread (the updates that aren't immediately required)
     */
    private Set<Protection> protectionUpdateQueue = Collections.synchronizedSet(new LinkedHashSet<Protection>(1000));

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
        _flush();
    }

    /**
     * @return the size of the waiting queue
     */
    public int size() {
        return protectionUpdateQueue.size();
    }

    /**
     * Add a protection to be updated to the top of the queue (JUST block ids!!)
     *
     * @param protection
     */
    public void queueProtectionUpdate(Protection protection) {
        protectionUpdateQueue.add(protection);
    }

    /**
     * Unqueue a protection to be updated to the database if it's already queued
     *
     * @param protection
     */
    public void unqueueProtectionUpdate(Protection protection) {
       protectionUpdateQueue.remove(protection);
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
        _flush();

        if (thread != null && !thread.isInterrupted()) {
            thread.interrupt();
        }
    }

    /**
     * Flush any caches to the database
     */
    private void _flush() {
        // periodically update protections in the database if a non-critical change was made
        if (protectionUpdateQueue.size() > 0) {
            synchronized (protectionUpdateQueue) {
                Connection connection = lwc.getPhysicalDatabase().getConnection();

                try {
                    connection.setAutoCommit(false);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                // save all of the protections
                Iterator<Protection> iter = protectionUpdateQueue.iterator();
                while (iter.hasNext()) {
                    Protection protection = iter.next();
                    iter.remove();

                    // Save it !
                    protection.saveNow();
                }

                // commit
                try {
                    connection.commit();
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        flush = false;
        lastUpdate = System.currentTimeMillis();
    }

}
