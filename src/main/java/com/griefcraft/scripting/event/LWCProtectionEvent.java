package com.griefcraft.scripting.event;

import com.griefcraft.model.Protection;
import com.griefcraft.scripting.ModuleLoader;
import org.bukkit.entity.Player;

public class LWCProtectionEvent extends LWCPlayerEvent {

    private Protection protection;
    private boolean canAccess;
    private boolean canAdmin;

    public LWCProtectionEvent(ModuleLoader.Event event, Player player, Protection protection, boolean canAccess, boolean canAdmin) {
        super(event, player);

        this.protection = protection;
        this.canAccess = canAccess;
        this.canAdmin = canAdmin;
    }

    public Protection getProtection() {
        return protection;
    }

    public boolean canAccess() {
        return canAccess;
    }

    public boolean canAdmin() {
        return canAdmin;
    }

}
