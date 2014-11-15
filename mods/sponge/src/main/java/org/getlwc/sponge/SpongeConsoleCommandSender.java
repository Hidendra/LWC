package org.getlwc.sponge;

import org.getlwc.command.ConsoleCommandSender;
import org.getlwc.lang.Locale;

public class SpongeConsoleCommandSender extends ConsoleCommandSender {

    @Override
    public void sendMessage(String message) {
        for (String line : message.split("\n")) {
            // TODO sponge equivalent of logger when available
            System.out.printf("[LWC]: %s\n", line);
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
