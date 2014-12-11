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
package org.getlwc.util.registry;

import org.getlwc.BlockType;
import org.getlwc.ItemType;

import java.util.List;

public interface MinecraftRegistry {

    /**
     * Gets the block type for the given id
     *
     * @param id
     * @return The {@link org.getlwc.BlockType} or null if not found
     */
    public BlockType getBlockType(String id);

    /**
     * Gets the block type for the given id.
     * This should not be used.
     *
     * @param id
     * @return
     */
    @Deprecated
    public BlockType getLegacyBlockType(int id);

    /**
     * Gets a list of all available {@link org.getlwc.BlockType}s
     *
     * @return an immutable list of all available {@link org.getlwc.BlockType}s in the registry
     */
    public List<BlockType> getBlocks();

    /**
     * Gets the item type for the given id
     *
     * @param id
     * @return The {@link org.getlwc.ItemType} or null if not found
     */
    public ItemType getItemType(String id);

    /**
     * Gets the item type for the given id.
     * This should not be used.
     *
     * @param id
     * @return
     */
    @Deprecated
    public ItemType getLegacyItemType(int id);

    /**
     * Gets a list of all available {@link org.getlwc.ItemType}s
     *
     * @return an immutable list of all available {@link org.getlwc.ItemType}s in the registry
     */
    public List<ItemType> getItems();

}
