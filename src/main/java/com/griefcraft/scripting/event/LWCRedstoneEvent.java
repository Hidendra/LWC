package com.griefcraft.scripting.event;

import com.griefcraft.model.Protection;
import com.griefcraft.scripting.ModuleLoader;
import org.bukkit.event.Cancellable;

public class LWCRedstoneEvent extends LWCEvent implements Cancellable {

    private Protection protection;
    private int current;
    private boolean cancelled;

    public LWCRedstoneEvent(Protection protection, int current) {
        super(ModuleLoader.Event.REDSTONE);

        this.protection = protection;
        this.current = current;
    }

    public Protection getProtection() {
        return protection;
    }

    public int getCurrent() {
        return current;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
