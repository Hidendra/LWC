package org.getlwc.granite;

import org.getlwc.Engine;
import org.getlwc.command.Command;
import org.getlwc.event.Listener;
import org.getlwc.event.engine.BaseCommandRegisteredEvent;

public class EngineEventListener {

    private Engine engine;

    public EngineEventListener(Engine engine) {
        this.engine = engine;
    }

    @SuppressWarnings("unused")
    @Listener
    public void onRegisterBaseCommand(BaseCommandRegisteredEvent event) {
        final String baseCommand = event.getNormalizedCommand();
        final Command command = event.getCommand();

    }

}
