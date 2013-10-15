package org.getlwc.world;

import org.getlwc.Block;
import org.getlwc.World;

public class MemoryBlock extends Block {

    private World world;
    private int type = 0;
    private final int x, y, z;
    private byte data = 0;

    public MemoryBlock(World world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public byte getData() {
        return data;
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getZ() {
        return z;
    }

    @Override
    public void setType(int type) {
        this.type = type;
    }

    @Override
    public void setData(byte data) {
        this.data = data;
    }

    @Override
    public boolean hasTileEntity() {
        switch (type) {
            case 54:
                return true;
            default:
                return false;
        }
    }

}
