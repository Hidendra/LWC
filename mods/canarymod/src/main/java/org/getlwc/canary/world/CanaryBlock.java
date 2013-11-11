package org.getlwc.canary.world;

import org.getlwc.Block;
import org.getlwc.World;

public class CanaryBlock extends Block {

    /**
     * Canary block handle
     */
    private net.canarymod.api.world.blocks.Block handle;

    /**
     * The world this block belongs to
     */
    private World world;

    public CanaryBlock(World world, net.canarymod.api.world.blocks.Block handle) {
        this.world = world;
        this.handle = handle;
    }

    @Override
    public int getType() {
        return handle.getTypeId();
    }

    @Override
    public byte getData() {
        return (byte) handle.getData();
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public int getX() {
        return handle.getX();
    }

    @Override
    public int getY() {
        return handle.getY();
    }

    @Override
    public int getZ() {
        return handle.getZ();
    }

    @Override
    public void setType(int type) {
        handle.setTypeId((short) type);
    }

    @Override
    public void setData(byte data) {
        handle.setData((short) data);
    }

    @Override
    public boolean hasTileEntity() {
        return handle.getTileEntity() != null;
    }
}
