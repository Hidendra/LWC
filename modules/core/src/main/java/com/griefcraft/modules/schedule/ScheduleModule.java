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

package com.griefcraft.modules.schedule;

import com.griefcraft.jobs.IJobHandler;
import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Job;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.util.Colors;
import com.griefcraft.util.StringUtil;
import com.griefcraft.util.TimeUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.Set;

public class ScheduleModule extends JavaModule {

    @Override
    public void onCommand(LWCCommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!event.hasFlag("schedule") && !event.hasFlag("tasks") && !event.hasFlag("task")) {
            return;
        }

        LWC lwc = LWC.getInstance();
        CommandSender sender = event.getSender();
        String[] args = event.getArgs();

        // We are using the command from here on out!
        event.setCancelled(true);

        if (!lwc.isAdmin(sender)) {
            lwc.sendLocale(sender, "protection.accessdenied");
            return;
        }

        if (args.length < 1) {
            lwc.sendSimpleUsage(sender, "/lwc schedule <Action> [JobName] [...]");
            return;
        }

        String action = args[0].toLowerCase();
        String name = args.length > 1 ? args[1].toLowerCase() : "";
        String[] jobArgs = new String[0];
        String joinedArguments = ""; // equivilent to StringUtil.join(jobArgs);

        // get the job specific arguments found if they are there
        if (args.length > 2) {
            jobArgs = new String[args.length - 2];
            System.arraycopy(args, 2, jobArgs, 0, jobArgs.length);
            joinedArguments = StringUtil.join(jobArgs).trim();
        }

        // Attempt to load the job they named
        Job job = lwc.getJobManager().getJob(name);

        // Look for actions .. !
        if (action.equals("create")) {
            if (args.length < 2) {
                lwc.sendSimpleUsage(sender, "/lwc schedule <Action> [JobName] [...]");
                return;
            }

            if (job != null) {
                lwc.sendLocale(sender, "lwc.job.exists", "name", name);
                return;
            }

            if (jobArgs.length == 0) {
                lwc.sendSimpleUsage(sender, "/lwc schedule create " + name + " HANDLER_NAME");
                return;
            }

            // Check for the job handler
            IJobHandler handler = lwc.getJobManager().getJobHandler(joinedArguments);

            if (handler == null) {
                lwc.sendLocale(sender, "lwc.job.nohandler", "name", joinedArguments);
                return;
            }

            // create the job
            job = new Job();
            job.setName(name);
            job.setType(handler.getType());

            // add who created the Job
            if (sender instanceof Player) {
                job.getData().put("creator", ((Player) sender).getName());
            } else {
                job.getData().put("creator", "Console");
            }

            // save it to the database
            job.save();
            lwc.sendLocale(sender, "lwc.job.created", "name", name, "handler", handler.getName());
        } else if (action.equals("run")) {
            if (args.length < 2) {
                lwc.sendSimpleUsage(sender, "/lwc schedule <Action> [JobName] [...]");
                return;
            }

            if (job == null) {
                lwc.sendLocale(sender, "lwc.invalidjob");
                return;
            }

            lwc.sendLocale(sender, "lwc.job.run", "name", job.getName());
            long start = System.currentTimeMillis();

            // run the job!
            lwc.getJobManager().execute(job);

            // calculate how long it took
            long timeMillis = System.currentTimeMillis() - start;

            lwc.sendLocale(sender, "lwc.job.run.time", "time", timeMillis);
        } else if (action.equals("remove") || action.equals("delete")) {
            if (args.length < 2) {
                lwc.sendSimpleUsage(sender, "/lwc schedule <Action> [JobName] [...]");
                return;
            }

            if (job == null) {
                lwc.sendLocale(sender, "lwc.invalidjob");
                return;
            }

            job.remove();
            lwc.sendLocale(sender, "lwc.job.removed");
        } else if (action.equals("list")) {
            // force a reload of jobs
            lwc.getJobManager().load();

            // Get the jobs
            Set<Job> jobs = lwc.getJobManager().getJobs();

            if (jobs.size() == 0) {
                lwc.sendLocale(sender, "lwc.job.nojobs");
                return;
            }

            // No pages as of yet (TODO)

            String format = "%16s%16s%16s";
            sender.sendMessage(Colors.Blue + String.format(format, "Name", "Handler", "Creator"));
            sender.sendMessage(" ");

            for (Job foundJob : jobs) {
                IJobHandler handler = foundJob.getJobHandler();
                String lineColour = "";

                // check the time remaining, if applicable
                if (foundJob.getNextRun() <= 0) {
                    lineColour = Colors.Yellow;
                } else {
                    if (foundJob.getTimeRemaining() <= 0) {
                        lineColour = Colors.Red;
                    } else {
                        lineColour = Colors.Green;
                    }
                }

                sender.sendMessage(lineColour + String.format(format, foundJob.getName(), handler.getName(), foundJob.getData().get("creator")));
            }
        } else if (action.equals("autorun")) {
            if (args.length < 2) {
                lwc.sendSimpleUsage(sender, "/lwc schedule <Action> [JobName] [...]");
                return;
            }

            if (job == null) {
                lwc.sendLocale(sender, "lwc.invalidjob");
                return;
            }

            if (jobArgs.length == 0) {
                lwc.sendSimpleUsage(sender, "/lwc schedule autoRun " + name + " Time (e.g 1 week)");
                return;
            }

            // Attempt to parse the time ...
            long parsedTimeSeconds = TimeUtil.parseTime(joinedArguments);
            long parsedTimeMillis = parsedTimeSeconds * 1000L;

            if (parsedTimeSeconds == 0) {
                lwc.sendLocale(sender, "lwc.invalidtime", "time", joinedArguments);
                return;
            }

            // calculate the time to next run at
            job.setNextRun(System.currentTimeMillis() + parsedTimeMillis);

            // store the time interval for the next time it runs so it can recalculate
            job.getData().put("autoRun", parsedTimeMillis);

            // save it to the database and notify the player
            job.save();
            lwc.sendLocale(sender, "lwc.job.autorun", "name", job.getName(), "time", TimeUtil.timeToString(parsedTimeSeconds),
                    "date", new Date(job.getNextRun()).toString());
        } else if (action.equals("check")) {
            if (job == null) {
                lwc.sendLocale(sender, "lwc.invalidjob");
                return;
            }

            long nextRun = job.getNextRun();

            if (nextRun <= 0) {
                lwc.sendLocale(sender, "lwc.job.autorun.notset", "name", job.getName());
            } else {
                long timeRemaining = job.getTimeRemaining();

                if (job.shouldRun()) {
                    lwc.sendLocale(sender, "lwc.job.waiting", "name", job.getName());
                    return;
                }

                lwc.sendLocale(sender, "lwc.job.nextrun", "name", job.getName(), "time", TimeUtil.timeToString(timeRemaining / 1000L));
            }
        } else if (action.equals("arguments")) {
            if (job == null) {
                lwc.sendLocale(sender, "lwc.invalidjob");
                return;
            }

            if (joinedArguments.isEmpty()) {
                lwc.sendLocale(sender, "lwc.noarguments");
                return;
            }

            job.getData().put("arguments", joinedArguments);
            job.save();
            lwc.sendLocale(sender, "lwc.job.setarguments", "name", job.getName(), "arguments", joinedArguments);
        }
    }

}
