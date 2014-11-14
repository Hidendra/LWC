package org.getlwc.canary;

import org.getlwc.command.ConsoleCommandSender;
import org.getlwc.lang.Locale;

public class CanaryConsoleCommandSender extends ConsoleCommandSender {

    public void sendMessage(String message) {
        System.out.println("[LWC] " + message);
    }

    public boolean hasPermission(String node) {
        return true;
    }

    public Locale getLocale() {
        return null;
    }

    public void setLocale(Locale locale) {
        throw new UnsupportedOperationException("setLocale is unsupported for console senders");
    }

}