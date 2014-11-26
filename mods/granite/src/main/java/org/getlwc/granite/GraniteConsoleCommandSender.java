package org.getlwc.granite;

import org.getlwc.command.ConsoleCommandSender;
import org.granitemc.granite.api.Granite;

public class GraniteConsoleCommandSender extends ConsoleCommandSender {

    @Override
    public void sendMessage(String message) {
        Granite.getLogger().info("[LWC] " + message);
    }

}
