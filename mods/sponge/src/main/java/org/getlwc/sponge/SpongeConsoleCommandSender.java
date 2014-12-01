package org.getlwc.sponge;

import org.getlwc.command.ConsoleCommandSender;

public class SpongeConsoleCommandSender extends ConsoleCommandSender {

    @Override
    public void sendMessage(String message) {
        for (String line : message.split("\n")) {
            // TODO sponge equivalent of logger when available
            System.out.printf("[LWC]: %s\n", line);
        }
    }

}
