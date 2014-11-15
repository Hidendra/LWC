package org.getlwc.sponge.world;

import org.getlwc.Block;
import org.getlwc.World;

public class SpongeBlock extends Block {

    private SpongeWorld world;
    private org.spongepowered.api.block.Block handle;

    public SpongeBlock(SpongeWorld world, org.spongepowered.api.block.Block handle) {
        this.world = world;
        this.handle = handle;
    }

    @Override
    public int getType() {
        // no getter for BlockType (yet?)
        throw new UnsupportedOperationException("getType() is not yet supported");
    }

    @Override
    public byte getData() {
        throw new UnsupportedOperationException("getData() is not yet supported");
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
        throw new UnsupportedOperationException("setType() is not yet supported");
    }

    @Override
    public void setData(byte data) {
        throw new UnsupportedOperationException("setData() is not yet supported");
    }

    @Override
    public boolean hasTileEntity() {
        // TODO Sponge will hopefully expose this in some way; otherwise may have to do something similar to what Bukkit does
        throw new UnsupportedOperationException("hasTileEntity() is not yet supported");
    }

}
