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

package com.griefcraft.scripting.event;

import com.griefcraft.scripting.ModuleLoader;
import com.griefcraft.util.StringUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;

public class LWCCommandEvent extends LWCEvent implements Cancellable {

    private CommandSender sender;
    private String command;
    private String[] args;
    private boolean cancelled;

    public LWCCommandEvent(CommandSender sender, String command, String[] args) {
        super(ModuleLoader.Event.COMMAND);

        this.sender = sender;
        this.command = command;
        this.args = args;
    }

    /**
     * Checks if the command begins with the flag.
     *
     * @param flags
     * @return
     */
    public boolean hasFlag(String... flags) {
        for (String flag : flags) {
            if (StringUtil.hasFlag(command, flag)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the arguments includes the flag
     *
     * @param flags
     * @return
     */
    public boolean hasArgumentFlag(String... flags) {
        for (String flag : flags) {
            if (StringUtil.hasFlag(args, flag)) {
                return true;
            }
        }

        return false;
    }

    public CommandSender getSender() {
        return sender;
    }

    public String getCommand() {
        return command;
    }

    public String[] getArgs() {
        return args;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
