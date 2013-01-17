package com.griefcraft.world;/*
 * Copyright (c) 2011, 2012, Tyler Blair
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

import com.griefcraft.Block;
import com.griefcraft.World;

public class ForgeBlock extends Block {

    /**
     * The world handle
     */
    private final ForgeWorld world;

    /**
     * Block's x coordinate
     */
    private final int x;

    /**
     * Block's y coordinate
     */
    private final int y;

    /**
     * Block's z coordinate
     */
    private final int z;

    public ForgeBlock(ForgeWorld world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getType() {
        return world.getHandle().getBlockId(x, y, z);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte getData() {
        return (byte) world.getHandle().getBlockMetadata(x, y, z);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public World getWorld() {
        return world;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getX() {
        return x;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getY() {
        return y;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getZ() {
        return z;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setType(int type) {
        world.getHandle().setBlock(x, y, z, type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setData(byte data) {
        world.getHandle().setBlockMetadata(x, y, z, data);
    }

}
