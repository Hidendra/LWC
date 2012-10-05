package com.griefcraft.entity;

import com.griefcraft.LWC;
import com.griefcraft.event.PlayerEventDelegate;
import com.griefcraft.world.Location;

public class WorkbenchPlayer extends Player {

    /**
     * The LWC instance
     */
    private LWC lwc;

    /**
     * Player handle
     */
    private net.minecraft.workbench.server.players.Player handle;

    /**
     * The player event delegate
     */
    private PlayerEventDelegate eventDelegate;

    public WorkbenchPlayer(LWC lwc, net.minecraft.workbench.server.players.Player handle) {
        this.lwc = lwc;
        this.handle = handle;
        eventDelegate = new PlayerEventDelegate(lwc, this);
    }

    @Override
    public String getName() {
        return handle.getUsername();
    }

    @Override
    public Location getLocation() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public PlayerEventDelegate getEventDelegate() {
        return eventDelegate;
    }

    public void sendMessage(String message) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean hasPermission(String node) {
        throw new UnsupportedOperationException("Not implemented");
    }

}
