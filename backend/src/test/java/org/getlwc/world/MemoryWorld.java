package org.getlwc.world;

import org.getlwc.Block;
import org.getlwc.Location;
import org.getlwc.World;

import java.util.HashMap;
import java.util.Map;

public class MemoryWorld implements World {

    /**
     * The blocks in the world
     */
    private Map<Location, Block> blocks = new HashMap<>();

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return "memory";
    }

    /**
     * {@inheritDoc}
     */
    public Block getBlockAt(int x, int y, int z) {
        Location point = new Location(this, x, y, z);
        Block block = blocks.get(point);

        if (block == null) {
            block = new MemoryBlock(this, x, y, z);
            blocks.put(point, block);
        }

        return block;
    }

}
