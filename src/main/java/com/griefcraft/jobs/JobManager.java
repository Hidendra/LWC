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

package com.griefcraft.jobs;

import com.griefcraft.jobs.impl.CleanupJobHandler;
import com.griefcraft.jobs.impl.ExpireJobHandler;
import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Job;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class JobManager {

    /**
     * The amount of server ticks to wait before polling
     */
    private final static int POLL_TICKS = 50;

    /**
     * The parent LWC instance
     */
    private LWC lwc;

    /**
     * The set of loaded Jobs
     */
    private final Set<Job> jobs = Collections.synchronizedSet(new HashSet<Job>());

    /**
     * The set of job handlers to use
     */
    private final Set<IJobHandler> handlers = new HashSet<IJobHandler>();

    /**
     * The executor that is used to poll jobs for updates
     */
    private final Runnable executor = new JobExecutor();

    /**
     * Polls for jobs and executes them
     */
    class JobExecutor implements Runnable {

        public void run() {
            synchronized (jobs) {
                Iterator<Job> iter = jobs.iterator();

                while (iter.hasNext()) {
                    Job job = iter.next();

                    // we don't want the job if we aren't running it
                    if (!job.shouldRun()) {
                        continue;
                    }

                    // TODO: store previous run times (in MS) a few back
                    // Run the job
                    execute(job);

                    // check for auto run
                    if (job.getData().containsKey("autoRun")) {
                        // we need to update the next point in time to run
                        long intervalMillis = (Long) job.getData().get("autoRun");

                        // update the job and save it
                        job.setNextRun(System.currentTimeMillis() + intervalMillis);
                    }

                    // check if it was one-time, and if so, remove it
                    if (job.getData().containsKey("one-use")) {
                        lwc.getPhysicalDatabase().removeJob(job);
                        iter.remove();
                    }
                }
            }
        }

    }

    public JobManager(LWC lwc) {
        this.lwc = lwc;

        // setup default handlers
        {
            handlers.add(new CleanupJobHandler());
            handlers.add(new ExpireJobHandler());
        }
    }

    /**
     * Load all jobs
     */
    public void load() {
        // load jobs from the database
        jobs.clear();
        jobs.addAll(lwc.getPhysicalDatabase().loadJobs());

        // create the executor
        lwc.getPlugin().getServer().getScheduler().scheduleSyncRepeatingTask(lwc.getPlugin(), executor, POLL_TICKS, POLL_TICKS);
    }

    /**
     * Get an UNMODIFIABLE Set of the jobs. If you wish to modify it, please use the provided methods.
     *
     * @return
     */
    public Set<Job> getJobs() {
        return Collections.unmodifiableSet(jobs);
    }

    /**
     * Add a job to the set
     *
     * @param job
     */
    public void addJob(Job job) {
        jobs.add(job);
    }

    /**
     * Remove a job from the set
     *
     * @param job
     */
    public void removeJob(Job job) {
        jobs.remove(job);
    }

    /**
     * Execute a job
     *
     * @param job
     */
    public void execute(Job job) {
        if (job == null) {
            return;
        }

        IJobHandler handler = getJobHandler(job.getType());

        if (handler == null) {
            return; // throw?
        }

        handler.run(lwc, job);
    }

    /**
     * Get a job
     *
     * @param name case insensitive
     * @return
     */
    public Job getJob(String name) {
        for (Job job : jobs) {
            if (job.getName().equalsIgnoreCase(name)) {
                return job;
            }
        }

        return null;
    }

    /**
     * Get a job handler by its name
     *
     * @param name case insensitive
     * @return
     */
    public IJobHandler getJobHandler(String name) {
        for (IJobHandler handler : handlers) {
            if (handler.getName().equalsIgnoreCase(name)) {
                return handler;
            }
        }

        return null;
    }

    /**
     * Get a job handler by its unique type
     *
     * @param type the job handler's unique type
     * @return
     */
    public IJobHandler getJobHandler(int type) {
        for (IJobHandler handler : handlers) {
            if (handler.getType() == type) {
                return handler;
            }
        }

        return null;
    }

    /**
     * Check if a job handler exists
     *
     * @param name case insensitive
     * @return
     */
    public boolean hasJobHandler(String name) {
        for (IJobHandler handler : handlers) {
            if (handler.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Add a job handler
     *
     * @param handler
     */
    public void addJobHandler(IJobHandler handler) {
        handlers.add(handler);
    }

    /**
     * Remove a job handler
     *
     * @param handler
     */
    public void removeJobHandler(IJobHandler handler) {
        handlers.remove(handler);
    }

}
