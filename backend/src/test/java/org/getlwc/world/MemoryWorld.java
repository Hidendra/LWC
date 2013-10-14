package org.getlwc.world;

import org.getlwc.Block;
import org.getlwc.World;

public class MemoryWorld implements World {

    /**
     * The size of the world
     */
    private static final int SIZE = 16;

    private Block[][][] blocks = new Block[SIZE][SIZE][SIZE];

    public MemoryWorld() {
        fillWorld(0);
    }

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
        return blocks[x][y][z];
    }

    /**
     * Fill the world with a specific id
     *
     * @param id
     */
    private void fillWorld(int id) {
        for (int x = 0; x < SIZE; x ++) {
            for (int y = 0; y < SIZE; y ++) {
                for (int z = 0; z < SIZE; z ++) {
                    blocks[x][y][z] = new MemoryBlock(this, x, y, z);
                }
            }
        }
    }

}
