package com.griefcraft.scripting.event;

import com.griefcraft.scripting.ModuleLoader;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import java.util.List;

public class LWCBlockInteractEvent extends LWCPlayerEvent implements Cancellable {

    private Block block;
    private List<String> actions;
    private boolean cancelled;

    public LWCBlockInteractEvent(Player player, Block block, List<String> actions) {
        super(ModuleLoader.Event.INTERACT_BLOCK, player);

        this.block = block;
        this.actions = actions;
    }

    public Block getBlock() {
        return block;
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
