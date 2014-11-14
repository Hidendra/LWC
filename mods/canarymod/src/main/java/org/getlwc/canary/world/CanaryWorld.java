package org.getlwc.canary.world;

import org.getlwc.Block;
import org.getlwc.World;

public class CanaryWorld implements World {

    /**
     * Canary world handle
     */
    private net.canarymod.api.world.World handle;

    public CanaryWorld(net.canarymod.api.world.World handle) {
        this.handle = handle;
    }

    @Override
    public String getName() {
        return handle.getName();
    }

    @Override
    public Block getBlockAt(int x, int y, int z) {
        return new CanaryBlock(this, handle.getBlockAt(x, y, z));
    }
}
