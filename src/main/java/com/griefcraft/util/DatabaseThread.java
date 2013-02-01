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
import org.bukkit.scheduler.BukkitTask;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DatabaseThread implements Runnable {

    /**
     * The maximum number of milliseconds work is done for
     */
    private static final long MAX_WORK_TIME = 100L;

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
    private final BukkitTask task;

    public DatabaseThread(LWC lwc) {
        this.lwc = lwc;
        task = lwc.getPlugin().getServer().getScheduler().runTaskTimerAsynchronously(lwc.getPlugin(), this, 20, 20);
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
        task.cancel();

        // Flush the rest of the entries
        flushDatabase(true);
    }

    /**
     * Recommend a flush as soon as possible. This does not guarantee the database will be flushed immediately.
     */
    public void flush() {
        flushDatabase(false);
    }

    /**
     * Flush the protections to the database
     */
    private void flushDatabase(boolean flushAll) {
        long start = System.currentTimeMillis();
        if (!updateQueue.isEmpty()) {
            while (!updateQueue.isEmpty()) {
                Protection protection = updateQueue.poll();
                protection.saveNow();

                if (!flushAll && System.currentTimeMillis() - start > MAX_WORK_TIME) {
                    break;
                }
            }
        }
    }

    public void run() {
        flushDatabase(false);
    }

}
