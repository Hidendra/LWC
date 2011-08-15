package com.griefcraft.scripting.event;

import com.griefcraft.scripting.ModuleLoader;
import org.bukkit.entity.Player;

public class LWCAccessEvent extends LWCPlayerEvent {

    /**
     * The access given
     */
    private int access;

    public LWCAccessEvent(Player player, int access) {
        super(ModuleLoader.Event.ACCESS_REQUEST, player);

        this.access = access;
    }

    public int getAccess() {
        return access;
    }

    public void setAccess(int access) {
        this.access = access;
    }

}
