package com.griefcraft.scripting.event;

import com.griefcraft.model.Protection;
import com.griefcraft.scripting.ModuleLoader;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class LWCProtectionDestroyEvent extends LWCProtectionEvent implements Cancellable {

    private boolean cancelled;

    public LWCProtectionDestroyEvent(Player player, Protection protection, boolean canAccess, boolean canAdmin) {
        super(ModuleLoader.Event.DESTROY_PROTECTION, player, protection, canAccess, canAdmin);
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
