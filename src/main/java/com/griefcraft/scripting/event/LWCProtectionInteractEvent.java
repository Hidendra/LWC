package com.griefcraft.scripting.event;

import com.griefcraft.model.Protection;
import com.griefcraft.scripting.ModuleLoader;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

public class LWCProtectionInteractEvent extends LWCProtectionEvent implements Cancellable {

    private PlayerInteractEvent event;
    private List<String> actions;
    private boolean cancelled;

    public LWCProtectionInteractEvent(PlayerInteractEvent event, Protection protection, List<String> actions, boolean canAccess, boolean canAdmin) {
        super(ModuleLoader.Event.INTERACT_PROTECTION, event.getPlayer(), protection, canAccess, canAdmin);

        this.event = event;
        this.actions = actions;
    }

    public PlayerInteractEvent getEvent() {
        return event;
    }

    public List<String> getActions() {
        return actions;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
