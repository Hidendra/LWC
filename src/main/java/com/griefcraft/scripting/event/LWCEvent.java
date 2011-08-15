package com.griefcraft.scripting.event;

import com.griefcraft.lwc.LWC;
import com.griefcraft.scripting.ModuleLoader;

public class LWCEvent {

    private ModuleLoader.Event event;

    public LWCEvent(ModuleLoader.Event event) {
        this.event = event;
    }

    public ModuleLoader.Event getEventType() {
        return event;
    }

    public LWC getLWC() {
        return LWC.getInstance();
    }

}
