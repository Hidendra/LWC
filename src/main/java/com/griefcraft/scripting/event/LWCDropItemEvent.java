package com.griefcraft.scripting.event;

import com.griefcraft.scripting.ModuleLoader;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.player.PlayerDropItemEvent;

public class LWCDropItemEvent extends LWCPlayerEvent implements Cancellable {

    private PlayerDropItemEvent dropItemEvent;

    public LWCDropItemEvent(Player player, PlayerDropItemEvent dropItemEvent) {
        super(ModuleLoader.Event.DROP_ITEM, player);

        this.dropItemEvent = dropItemEvent;
    }

    public PlayerDropItemEvent getEvent() {
        return dropItemEvent;
    }

    /**
     * Acts as a proxy for dropItemEvent.isCancelled()
     *
     * @return
     */
    public boolean isCancelled() {
        return dropItemEvent.isCancelled();
    }

    /**
     * Acts as a proxy for dropItemEvent.setCancelled(bool)
     *
     * @param cancelled
     */
    public void setCancelled(boolean cancelled) {
        dropItemEvent.setCancelled(cancelled);
    }

}
