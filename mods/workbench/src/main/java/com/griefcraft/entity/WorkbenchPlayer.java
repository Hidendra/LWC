package com.griefcraft.entity;

import com.griefcraft.Engine;
import com.griefcraft.Location;
import com.griefcraft.event.PlayerEventDelegate;

public class WorkbenchPlayer extends Player {

    /**
     * The LWC engine instance
     */
    private Engine engine;

    /**
     * Player handle
     */
    private net.minecraft.workbench.server.players.Player handle;

    /**
     * The player event delegate
     */
    private PlayerEventDelegate eventDelegate;

    public WorkbenchPlayer(Engine engine, net.minecraft.workbench.server.players.Player handle) {
        this.engine = engine;
        this.handle = handle;
        eventDelegate = new PlayerEventDelegate(engine, this);
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
