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
/*
 * Copyright 2002-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Note: this code is originally part of the package org.springframework.util,
 * which is a part of the Spring Framework application framework.
 */

package com.griefcraft.util;

import org.bukkit.command.CommandSender;

import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;

/**
 * Simple stop watch, allowing for timing of a number of tasks,
 * exposing total running time and running time for each named task.
 * <p/>
 * <p>Conceals use of <code>System.currentTimeMillis()</code>, improving the
 * readability of application code and reducing the likelihood of calculation errors.
 * <p/>
 * <p>Note that this object is not designed to be thread-safe and does not
 * use synchronization.
 * <p/>
 * <p>This class is normally used to verify performance during proof-of-concepts
 * and in development, rather than as part of production applications.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since May 2, 2001
 */
public class StopWatch {

    /**
     * Identifier of this stop watch.
     * Handy when we have output from multiple stop watches
     * and need to distinguish between them in log or console output.
     */
    private final String id;

    private boolean keepTaskList = true;

    /**
     * List of TaskInfo objects
     */
    private final List<TaskInfo> taskList = new LinkedList<TaskInfo>();

    /**
     * Start time of the current task
     */
    private long startTimeMillis;

    /**
     * Is the stop watch currently running?
     */
    private boolean running;

    /**
     * Name of the current task
     */
    private String currentTaskName;

    private TaskInfo lastTaskInfo;

    private int taskCount;

    /**
     * Total running time
     */
    private long totalTimeMillis;


    /**
     * Construct a new stop watch. Does not start any task.
     */
    public StopWatch() {
        this.id = "";
    }

    /**
     * Construct a new stop watch with the given id.
     * Does not start any task.
     *
     * @param id identifier for this stop watch.
     *           Handy when we have output from multiple stop watches
     *           and need to distinguish between them.
     */
    public StopWatch(String id) {
        this.id = id;
    }

    /**
     * Determine whether the TaskInfo array is built over time. Set this to
     * "false" when using a StopWatch for millions of intervals, or the task
     * info structure will consume excessive memory. Default is "true".
     */
    public void setKeepTaskList(boolean keepTaskList) {
        this.keepTaskList = keepTaskList;
    }


    /**
     * Start an unnamed task. The results are undefined if {@link #stop()}
     * or timing methods are called without invoking this method.
     *
     * @see #stop()
     */
    public void start() throws IllegalStateException {
        start("");
    }

    /**
     * Start a named task. The results are undefined if {@link #stop()}
     * or timing methods are called without invoking this method.
     *
     * @param taskName the name of the task to start
     * @see #stop()
     */
    public void start(String taskName) throws IllegalStateException {
        if (this.running) {
            throw new IllegalStateException("Can't start StopWatch: it's already running");
        }
        this.startTimeMillis = System.currentTimeMillis();
        this.running = true;
        this.currentTaskName = taskName;
    }

    /**
     * Stop the current task. The results are undefined if timing
     * methods are called without invoking at least one pair
     * {@link #start()} / {@link #stop()} methods.
     *
     * @see #start()
     */
    public void stop() throws IllegalStateException {
        if (!this.running) {
            throw new IllegalStateException("Can't stop StopWatch: it's not running");
        }
        long lastTime = System.currentTimeMillis() - this.startTimeMillis;
        this.totalTimeMillis += lastTime;
        this.lastTaskInfo = new TaskInfo(this.currentTaskName, lastTime);
        if (this.keepTaskList) {
            this.taskList.add(lastTaskInfo);
        }
        ++this.taskCount;
        this.running = false;
        this.currentTaskName = null;
    }

    /**
     * Return whether the stop watch is currently running.
     */
    public boolean isRunning() {
        return this.running;
    }


    /**
     * Return the time taken by the last task.
     */
    public long getLastTaskTimeMillis() throws IllegalStateException {
        if (this.lastTaskInfo == null) {
            throw new IllegalStateException("No tests run: can't get last interval");
        }
        return this.lastTaskInfo.getTimeMillis();
    }

    /**
     * Return the total time in milliseconds for all tasks.
     */
    public long getTotalTimeMillis() {
        return totalTimeMillis;
    }

    /**
     * Return the total time in seconds for all tasks.
     */
    public double getTotalTimeSeconds() {
        return totalTimeMillis / 1000.0;
    }

    /**
     * Return the number of tasks timed.
     */
    public int getTaskCount() {
        return taskCount;
    }

    /**
     * Return an array of the data for tasks performed.
     */
    public TaskInfo[] getTaskInfo() {
        if (!this.keepTaskList) {
            throw new UnsupportedOperationException("Task info is not being kept!");
        }
        return this.taskList.toArray(new TaskInfo[this.taskList.size()]);
    }


    /**
     * Return a short description of the total running time.
     */
    public String shortSummary() {
        return "" + this.id + ": " + Colors.Yellow + getTotalTimeMillis() + " ms";
    }

    /**
     * Return a string with a table describing all tasks performed.
     * For custom reporting, call getTaskInfo() and use the task info directly.
     */
    public String prettyPrint() {
        StringBuilder sb = new StringBuilder(shortSummary());
        sb.append('\n');
        if (!this.keepTaskList) {
            sb.append("No task info kept");
        } else {
            TaskInfo[] tasks = getTaskInfo();
            sb.append("-----------------------------------------\n");
            sb.append("ms     %     Task name\n");
            sb.append("-----------------------------------------\n");
            NumberFormat nf = NumberFormat.getNumberInstance();
            nf.setMinimumIntegerDigits(5);
            nf.setGroupingUsed(false);
            NumberFormat pf = NumberFormat.getPercentInstance();
            pf.setMinimumIntegerDigits(3);
            pf.setGroupingUsed(false);
            for (TaskInfo task : tasks) {
                sb.append(nf.format(task.getTimeMillis())).append("  ");
                sb.append(pf.format(task.getTimeSeconds() / getTotalTimeSeconds())).append("  ");
                sb.append(task.getTaskName()).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Send the pretty print table to a command sender
     */
    public void prettyPrint(CommandSender sender) {
        sender.sendMessage(shortSummary());
        if (!this.keepTaskList) {
            sender.sendMessage("No task info kept");
        } else {
            TaskInfo[] tasks = getTaskInfo();
            sender.sendMessage("-----------------------------------------");
            sender.sendMessage("ms     %     Task name");
            sender.sendMessage("-----------------------------------------");
            NumberFormat nf = NumberFormat.getNumberInstance();
            nf.setMinimumIntegerDigits(5);
            nf.setGroupingUsed(false);
            NumberFormat pf = NumberFormat.getPercentInstance();
            pf.setMinimumIntegerDigits(3);
            pf.setGroupingUsed(false);

            for (TaskInfo task : tasks) {
                String line = "";
                line += nf.format(task.getTimeMillis()) + "  ";
                line += pf.format(task.getTimeSeconds() / getTotalTimeSeconds()) + "  ";
                line += task.getTaskName();

                sender.sendMessage(line);
            }
        }
    }

    /**
     * Return an informative string describing all tasks performed
     * For custom reporting, call <code>getTaskInfo()</code> and use the task info directly.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(shortSummary());
        if (this.keepTaskList) {
            TaskInfo[] tasks = getTaskInfo();
            for (TaskInfo task : tasks) {
                sb.append("; [").append(task.getTaskName()).append("] took ").append(task.getTimeMillis());
                long percent = Math.round((100.0 * task.getTimeSeconds()) / getTotalTimeSeconds());
                sb.append(" = ").append(percent).append("%");
            }
        } else {
            sb.append("; no task info kept");
        }
        return sb.toString();
    }


    /**
     * Inner class to hold data about one task executed within the stop watch.
     */
    public static class TaskInfo {

        private final String taskName;

        private final long timeMillis;

        private TaskInfo(String taskName, long timeMillis) {
            this.taskName = taskName;
            this.timeMillis = timeMillis;
        }

        /**
         * Return the name of this task.
         */
        public String getTaskName() {
            return taskName;
        }

        /**
         * Return the time in milliseconds this task took.
         */
        public long getTimeMillis() {
            return timeMillis;
        }

        /**
         * Return the time in seconds this task took.
         */
        public double getTimeSeconds() {
            return timeMillis / 1000.0;
        }
    }

}