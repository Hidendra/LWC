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

package com.griefcraft.modules.schedule;

import com.griefcraft.jobs.IJobHandler;
import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Job;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.util.Colors;
import com.griefcraft.util.StringUtils;
import org.bukkit.command.CommandSender;

public class ScheduleModule extends JavaModule {

    @Override
    public void onCommand(LWCCommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!event.hasFlag("schedule")) {
            return;
        }

        LWC lwc = LWC.getInstance();
        CommandSender sender = event.getSender();
        String[] args = event.getArgs();

        // We are using the command from here on out!
        event.setCancelled(true);

        if (args.length < 2) {
            lwc.sendSimpleUsage(sender, "/lwc schedule <action> <name> [...]");
            return;
        }

        String action = args[0].toLowerCase();
        String name = args[1].toLowerCase();
        String[] jobArgs = new String[0];
        String joinedArguments = ""; // equivilent to StringUtils.join(jobArgs);

        // get the job specific arguments found if they are there
        if (args.length > 2) {
            jobArgs = new String[args.length - 2];
            System.arraycopy(args, 2, jobArgs, 0, jobArgs.length);
            joinedArguments = StringUtils.join(jobArgs).trim();
        }

        // Attempt to load the job they named
        Job job = lwc.getJobManager().getJob(name);

        // Look for actions .. !
        if (action.equals("create")) {
            if (job != null) {
                sender.sendMessage(Colors.Red + "Job " + name + " already exists.");
                return;
            }

            if (jobArgs.length == 0) {
                lwc.sendSimpleUsage(sender, "/lwc schedule create " + name + " HANDLER_NAME");
                return;
            }

            // Check for the job handler
            IJobHandler handler = lwc.getJobManager().getJobHandler(joinedArguments);

            if (handler == null) {
                sender.sendMessage(Colors.Red + "Job handler " + joinedArguments + " does not exist!");
                return;
            }

            // create the job
            job = new Job();
            job.setName(name);
            job.setType(handler.getType());

            // save it to the database
            job.save();
            sender.sendMessage(Colors.Green + "Created the job " + name + " successfully using the handler " + handler.getName());
        }

        else if (action.equals("run")) {
            if (job == null) {
                sender.sendMessage(Colors.Red + "Invalid job.");
                return;
            }

            sender.sendMessage("Running job " + job.getName());
            long start = System.currentTimeMillis();

            // run the job!
            lwc.getJobManager().execute(job);

            // calculate how long it took
            long timeMillis = System.currentTimeMillis() - start;

            sender.sendMessage("Successfully ran the job (" + timeMillis + " ms)");
        }
    }

}
