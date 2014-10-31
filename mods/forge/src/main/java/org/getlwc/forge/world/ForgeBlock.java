/*
 * Copyright (c) 2011-2013 Tyler Blair
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
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR,
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

package org.getlwc.forge.world;

import cpw.mods.fml.common.registry.GameData;
import org.getlwc.Block;
import org.getlwc.World;
import org.getlwc.forge.asm.AbstractMultiClassTransformer;
import org.getlwc.forge.asm.CompilationType;
import org.getlwc.util.Tuple;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

    /**
     * Map of the cached block names
     */
    private static Map<Tuple<Integer, Byte>, String> cachedModBlockNames = new HashMap<Tuple<Integer, Byte>, String>();

    public ForgeBlock(ForgeWorld world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public String getName() {
        Tuple<Integer, Byte> cacheKey = new Tuple<Integer, Byte>(getType(), getData());

        if (cachedModBlockNames.containsKey(cacheKey)) {
            return cachedModBlockNames.get(cacheKey);
        }

        String name = null;

        try {
            Iterator iter = GameData.blockRegistry.iterator();

            while (iter.hasNext()) {
                net.minecraft.block.Block block = (net.minecraft.block.Block) iter.next();

                int blockID = GameData.blockRegistry.getId(block);

                if (block != null && blockID > 0) {
                    List<net.minecraft.item.ItemStack> blocks = new ArrayList<net.minecraft.item.ItemStack>();

                    for (Method method : block.getClass().getDeclaredMethods()) {
                        if (method.getName().equals(AbstractMultiClassTransformer.getMethodName("Block", "getSubBlocks", CompilationType.SRG))) {
                            method.invoke(block, blockID, null, blocks);
                            break;
                        }
                    }

                    if (blocks.size() == 0) { // no sub blocks; can ignore the data value
                        if (getType() == blockID) {
                            name = block.getUnlocalizedName();
                            break;
                        }
                    } else { // has sub blocks so the data value uniquely identifies the block
                        for (net.minecraft.item.ItemStack stack : blocks) {
                            if (stack != null) {
                                if (getType() == blockID && stack.getItemDamage() == getData()) {
                                    name = stack.getUnlocalizedName();
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (name == null) {
            name = super.getName();
        }

        if (name != null) {
            cachedModBlockNames.put(cacheKey, name);
        }

        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getType() {
        return GameData.blockRegistry.getId(world.getHandle().getBlock(x, y, z));
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
        throw new UnsupportedOperationException("block.setType() is unsupported");
        // world.getHandle().setBlock(x, y, z, type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setData(byte data) {
        throw new UnsupportedOperationException("block.setData() is unsupported");
        // world.getHandle().setBlockMetadata(x, y, z, data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasTileEntity() {
        return world.getHandle().getTileEntity(x, y, z) != null;
    }

}
