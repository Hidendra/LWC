package org.getlwc.canary;

import org.getlwc.command.ConsoleCommandSender;
import org.getlwc.lang.Locale;

public class CanaryConsoleCommandSender extends ConsoleCommandSender {

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

    /**
     * {@inheritDoc}
     */
    public Locale getLocale() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void setLocale(Locale locale) {
        throw new UnsupportedOperationException("setLocale is unsupported for console senders");
    }

}