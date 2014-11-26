package org.getlwc.granite.world;

import org.getlwc.Block;
import org.getlwc.World;
import org.granitemc.granite.api.block.BlockTypes;

public class GraniteBlock extends Block {

    private org.granitemc.granite.api.block.Block handle;

    public GraniteBlock(org.granitemc.granite.api.block.Block handle) {
        this.handle = handle;
    }

    @Override
    public int getType() {
        // TOOD
        return handle.getType().getNumericId();
    }

    @Override
    public byte getData() {
        // TODO
        return 0;
    }

    @Override
    public World getWorld() {
        // TODO avoid recreating GraniteWorld everytime
        return new GraniteWorld(handle.getWorld());
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
        handle.setType(BlockTypes.getById(type));
    }

    @Override
    public void setData(byte data) {
        // TODO
    }

    @Override
    public boolean hasTileEntity() {
        // TODO Does Granite expose this? otherwise may have to do something similar to what Bukkit does
        throw new UnsupportedOperationException("hasTileEntity() is not yet supported");
    }

}
