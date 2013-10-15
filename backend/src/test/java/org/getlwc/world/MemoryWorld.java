package org.getlwc.world;

import org.getlwc.Block;
import org.getlwc.World;

import java.util.HashMap;
import java.util.Map;

public class MemoryWorld implements World {

    /**
     * The blocks in the world
     */
    private Map<WorldPoint, Block> blocks = new HashMap<WorldPoint, Block>();

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
        WorldPoint point = new WorldPoint(x, y, z);
        Block block = blocks.get(point);

        if (block == null) {
            block = new MemoryBlock(this, x, y, z);
            blocks.put(point, block);
        }

        return block;
    }

    private class WorldPoint {

        int x, y, z;

        public WorldPoint(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash += hash * 17 + x;
            hash += hash * 17 + y;
            hash += hash * 17 + z;
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof WorldPoint)) {
                return false;
            }

            WorldPoint op = (WorldPoint) o;
            return x == op.x && y == op.y && z == op.z;
        }

    }

}
