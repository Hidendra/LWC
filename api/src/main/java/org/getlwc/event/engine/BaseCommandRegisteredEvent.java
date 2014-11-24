package org.getlwc.event.engine;

import org.getlwc.command.Command;
import org.getlwc.event.Event;

public class BaseCommandRegisteredEvent extends Event {

    /**
     * The normalized base command
     */
    private String normalizedCommand;

    /**
     * The command itself that was registered
     */
    private Command command;

    public BaseCommandRegisteredEvent(String normalizedCommand, Command command) {
        this.normalizedCommand = normalizedCommand;
        this.command = command;
    }

    public String getNormalizedCommand() {
        return normalizedCommand;
    }

    public Command getCommand() {
        return command;
    }

}
