package com.griefcraft.scripting.event;

import com.griefcraft.model.Protection;
import com.griefcraft.scripting.ModuleLoader;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockRedstoneEvent;

public class LWCRedstoneEvent extends LWCEvent implements Cancellable {

    private BlockRedstoneEvent event;
    private Protection protection;
    private boolean cancelled;

    public LWCRedstoneEvent(BlockRedstoneEvent event, Protection protection) {
        super(ModuleLoader.Event.REDSTONE);

        this.event = event;
        this.protection = protection;
    }

    public BlockRedstoneEvent getEvent() {
        return event;
    }

    public Protection getProtection() {
        return protection;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
