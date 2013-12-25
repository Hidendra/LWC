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
import com.griefcraft.sql.Database;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DatabaseThread implements Runnable {

    /**
     * The LWC object
     */
    private final LWC lwc;

    /**
     * The protections waiting to be updated in the database
     */
    private final Queue<Protection> updateQueue = new ConcurrentLinkedQueue<Protection>();

    /**
     * The thread we are running in
     */
    private final Thread thread = new Thread(this);

    /**
     * If the database thread is active and running
     */
    private boolean running = false;

    /**
     * The last time the queue was flushed to the database
     */
    private long lastFlush = -1L;

    /**
     * The time the next keepalive packet will be sent at
     */
    private long nextKeepalivePacket = 0;

    /**
     * Interval between pinging the database
     */
    private int pingInterval = 0;

    public DatabaseThread(LWC lwc) {
        this.lwc = lwc;
        this.running = true;
        this.lastFlush = System.currentTimeMillis();
        this.thread.start();
        pingInterval = lwc.getConfiguration().getInt("database.ping_interval", 300);
    }

    /**
     * Adds a protection to the update queue so that it is flushed to the database asap
     *
     * @param protection
     */
    public void addProtection(Protection protection) {
        updateQueue.offer(protection);
    }

    /**
     * Removes a protection from the update queue
     *
     * @param protection
     */
    public void removeProtection(Protection protection) {
        updateQueue.remove(protection);
    }

    /**
     * Gets the current amount of protections queued to be updated
     *
     * @return the amount of protections queued to be updated
     */
    public int size() {
        return updateQueue.size();
    }

    /**
     * Stop the database thread
     */
    public void stop() {
        // stop running and interrupt the thread
        running = false;
        if (!thread.isInterrupted()) {
            thread.interrupt();
        }

        // Flush the rest of the entries
        flushDatabase();
    }

    /**
     * Recommend a flush as soon as possible. This does not guarantee the database will be flushed immediately.
     */
    public void flush() {
        lastFlush = System.currentTimeMillis() - 9999999L;
    }

    /**
     * Flush the protections to the database
     */
    private void flushDatabase() {
        if (!updateQueue.isEmpty()) {
            Database database = lwc.getPhysicalDatabase();
            database.setAutoCommit(false);
            database.setUseStatementCache(false);

            // Begin iterating through the queue
            Iterator<Protection> iter = updateQueue.iterator();
            while (iter.hasNext()) {
                Protection protection = iter.next();
                iter.remove();
                protection.saveNow();
            }

            // Commit the changes to the database
            database.setUseStatementCache(true);
            database.setAutoCommit(true);
        }

        // update the time we last flushed at
        lastFlush = System.currentTimeMillis();

        if (System.currentTimeMillis() > nextKeepalivePacket && lwc.getPhysicalDatabase().isConnected()) {
            nextKeepalivePacket = System.currentTimeMillis() + (pingInterval * 1000);
            lwc.getPhysicalDatabase().pingDatabase();
        }
    }

    public void run() {
        while (running) {
            // how many seconds between each flush
            int interval = lwc.getConfiguration().getInt("core.flushInterval", 5);

            if (interval > 120) {
                interval = 120;
            }

            long currentTime = System.currentTimeMillis();
            long intervalMilliseconds = interval * 1000L;

            // compare the current time to the last flush
            if (currentTime - lastFlush > intervalMilliseconds) {
                flushDatabase();
            }

            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                running = false;
            }
        }
    }

}
