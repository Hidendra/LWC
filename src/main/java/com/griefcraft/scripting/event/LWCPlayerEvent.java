package com.griefcraft.scripting.event;

import com.griefcraft.scripting.ModuleLoader;
import org.bukkit.entity.Player;

public class LWCPlayerEvent extends LWCEvent {
    
    private Player player;

    public LWCPlayerEvent(ModuleLoader.Event event, Player player) {
        super(event);

        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

}
