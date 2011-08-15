package com.griefcraft.scripting.event;

import com.griefcraft.scripting.ModuleLoader;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.player.PlayerDropItemEvent;

public class LWCDropEvent extends LWCPlayerEvent implements Cancellable {

    private PlayerDropItemEvent dropItemEvent;
    private boolean cancelled;

    public LWCDropEvent(Player player, PlayerDropItemEvent dropItemEvent) {
        super(ModuleLoader.Event.DROP_ITEM, player);

        this.dropItemEvent = dropItemEvent;
    }

    public PlayerDropItemEvent getEvent() {
        return dropItemEvent;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
