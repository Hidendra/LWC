package com.griefcraft.scripting.event;

import com.griefcraft.model.Protection;
import com.griefcraft.scripting.ModuleLoader;
import org.bukkit.entity.Player;

public class LWCAccessEvent extends LWCPlayerEvent {

    /**
     * The protection they are requesting
     */
    private final Protection protection;

    /**
     * The access given
     */
    private int access;

    public LWCAccessEvent(Player player, Protection protection, int access) {
        super(ModuleLoader.Event.ACCESS_REQUEST, player);

        this.protection = protection;
        this.access = access;
    }

    public Protection getProtection() {
        return protection;
    }

    public int getAccess() {
        return access;
    }

    public void setAccess(int access) {
        this.access = access;
    }

}
