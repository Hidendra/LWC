package org.getlwc.canary;

import org.getlwc.command.ConsoleCommandSender;
import org.getlwc.lang.Locale;

public class CanaryConsoleCommandSender extends ConsoleCommandSender {

    @Override
    public void sendMessage(String message) {
        System.out.println("[LWC] " + message);
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