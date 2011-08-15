package com.griefcraft.scripting.event;

import com.griefcraft.scripting.ModuleLoader;
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
