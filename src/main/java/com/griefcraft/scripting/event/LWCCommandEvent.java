package com.griefcraft.scripting.event;

import com.griefcraft.scripting.ModuleLoader;
import com.griefcraft.util.StringUtils;
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
        for(String flag : flags) {
            if(StringUtils.hasFlag(command, flag)) {
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
        for(String flag : flags) {
            if(StringUtils.hasFlag(args, flag)) {
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
