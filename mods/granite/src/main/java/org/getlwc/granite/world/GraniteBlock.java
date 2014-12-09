/**
 * Copyright (c) 2011-2014 Tyler Blair
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and contributors and should not be interpreted as representing official policies,
 * either expressed or implied, of anybody else.
 */
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
        // TODO
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
