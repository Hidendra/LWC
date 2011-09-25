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

        // call the expire command
        lwc.getPlugin().onCommand(sender, lwc.getPlugin().getCommand("lwc"), "lwc", ("admin expire " + arguments).split(" "));
    }

}
