package org.getlwc.canary;

import org.getlwc.command.ConsoleCommandSender;

public class CanaryConsoleCommandSender implements ConsoleCommandSender {

    /**
     * {@inheritDoc}
     */
    public void sendMessage(String message) {
        System.out.println("[LWC] " + message);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasPermission(String node) {
        return true;
    }

}