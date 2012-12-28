package com.griefcraft.entity;

import com.griefcraft.Engine;
import com.griefcraft.Location;

public class WorkbenchPlayer extends Player {

    /**
     * The LWC engine instance
     */
    private Engine engine;

    /**
     * Player handle
     */
    private net.minecraft.workbench.server.players.Player handle;

    public WorkbenchPlayer(Engine engine, net.minecraft.workbench.server.players.Player handle) {
        this.engine = engine;
        this.handle = handle;
    }

    @Override
    public String getName() {
        return handle.getUsername();
    }

    public Location getLocation() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void sendMessage(String message) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean hasPermission(String node) {
        throw new UnsupportedOperationException("Not implemented");
    }

}
