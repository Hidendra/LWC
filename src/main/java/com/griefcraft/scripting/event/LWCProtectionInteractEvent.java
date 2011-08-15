package com.griefcraft.scripting.event;

import com.griefcraft.model.Protection;
import com.griefcraft.scripting.ModuleLoader;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import java.util.List;

public class LWCProtectionInteractEvent extends LWCProtectionEvent implements Cancellable {

    private List<String> actions;
    private boolean cancelled;

    public LWCProtectionInteractEvent(Player player, Protection protection, List<String> actions, boolean canAccess, boolean canAdmin) {
        super(ModuleLoader.Event.INTERACT_PROTECTION, player, protection, canAccess, canAdmin);

        this.actions = actions;
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
