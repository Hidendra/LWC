package com.griefcraft.jobs.impl;

import com.griefcraft.jobs.IJobHandler;
import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Job;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

public class ExpireJobHandler implements IJobHandler {

    public String getName() {
        return "expire";
    }

    public String[] getRequiredKeys() {
        return new String[0];
    }

    public int getType() {
        return 2;
    }

    public void run(LWC lwc, Job job) {
        // create a console sender
        ConsoleCommandSender sender = new ConsoleCommandSender(Bukkit.getServer());

        String arguments = "";

        // check for arguments
        if (job.getData().containsKey("arguments")) {
            arguments = (String) job.getData().get("arguments");
        }

        // call the cleanup command
        lwc.getPlugin().onCommand(sender, lwc.getPlugin().getCommand("lwc"), "lwc", ("admin expire " + arguments).split(" "));
    }

}
