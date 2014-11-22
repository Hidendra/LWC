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

    @Override
    public boolean hasPermission(String node) {
        return true;
    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public void setLocale(Locale locale) {
        throw new UnsupportedOperationException("setLocale is unsupported for console senders");
    }

}