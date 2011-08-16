package com.griefcraft.scripting.event;

import com.griefcraft.lwc.LWC;
import com.griefcraft.scripting.ModuleLoader;

public class LWCEvent {

    private ModuleLoader.Event eventType;

    public LWCEvent(ModuleLoader.Event event) {
        this.eventType = event;
    }

    public ModuleLoader.Event getEventType() {
        return eventType;
    }

    public LWC getLWC() {
        return LWC.getInstance();
    }

}
