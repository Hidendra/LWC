package org.getlwc.sponge.world;

import org.getlwc.Block;
import org.getlwc.World;

public class SpongeWorld implements World {

    /**
     * native Sponge handle
     */
    private org.spongepowered.api.world.World handle;

    public SpongeWorld(org.spongepowered.api.world.World handle) {
        this.handle = handle;
    }

    @Override
    public String getName() {
        return handle.getName();
    }

    @Override
    public Block getBlockAt(int x, int y, int z) {
        return new SpongeBlock(this, handle.getBlock(x, y, z));
    }

}
