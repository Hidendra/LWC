package org.getlwc.granite.world;

import org.getlwc.Block;
import org.getlwc.World;

public class GraniteWorld implements World {

    private org.granitemc.granite.api.world.World handle;

    public GraniteWorld(org.granitemc.granite.api.world.World handle) {
        this.handle = handle;
    }

    @Override
    public String getName() {
        // TODO getLevelName() returns null
        String name = handle.getLevelName();

        if (name == null) {
            name = "";
        }

        return name;
    }

    @Override
    public Block getBlockAt(int x, int y, int z) {
        return new GraniteBlock(handle.getBlock(x, y, z));
    }

}
