/*
 * Copyright (c) 2011, 2012, Tyler Blair
 * All rights reserved.
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

package com.griefcraft;

import com.griefcraft.model.AbstractSavable;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SavableSaveQueue {

    /**
     * The queue of savables
     */
    private final BlockingQueue<AbstractSavable> queue = new LinkedBlockingQueue<AbstractSavable>();

    /**
     * The queue's thread
     */
    private Thread thread = null;

    /**
     * If the thread is running or not
     */
    private boolean running = false;

    public SavableSaveQueue() {
        // TODO limit queries/sec
        // TODO properly destroy thread, etc, or use server-specific implementations (e.g Bukkit's scheduler)
        thread = new Thread(new Worker(), "LWC Savable Thread");
        thread.start();
    }

    /**
     * Push a savable object into the queue to be saved
     *
     * @param savable
     */
    public void push(AbstractSavable savable) {
        queue.offer(savable);
    }

    /**
     * Flush the queue and then close it
     */
    public void flushAndClose() {
        running = false;
        thread.interrupt();
        AbstractSavable savable;
        while ((savable = queue.poll()) != null) {
            savable.saveImmediately();
        }
    }

    private class Worker implements Runnable {

        public void run() {
            while (true) {
                if (!running) {
                    break;
                }

                try {
                    AbstractSavable savable = queue.take();
                    savable.saveImmediately();
                    System.out.println("Saving: " + savable.toString());
                } catch (InterruptedException e) {
                    continue;
                }
            }
        }

    }

}
