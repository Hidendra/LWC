package com.griefcraft.scripting.event;

import com.griefcraft.scripting.ModuleLoader;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class LWCProtectionRegisterEvent extends LWCPlayerEvent implements Cancellable {

    private Block block;
    private boolean cancelled;

    public LWCProtectionRegisterEvent(Player player, Block block) {
        super(ModuleLoader.Event.REGISTER_PROTECTION, player);

        this.block = block;
    }

    public Block getBlock() {
        return block;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
