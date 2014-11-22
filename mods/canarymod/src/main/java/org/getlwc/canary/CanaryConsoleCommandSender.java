package org.getlwc.canary;

import net.canarymod.logger.Logman;
import org.getlwc.command.ConsoleCommandSender;
import org.getlwc.lang.Locale;

public class CanaryConsoleCommandSender extends ConsoleCommandSender {

    private Logman logger;

    public CanaryConsoleCommandSender(Logman logger) {
        this.logger = logger;
    }

    @Override
    public void sendMessage(String message) {
        for (String line : message.split("\n")) {
            logger.info(line);
        }
    }

}