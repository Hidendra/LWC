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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An implementation of {@link org.getlwc.util.registry.MinecraftRegistry} that caches
 * older retrieved block types. When the cache needs to populate an unknown value,
 * it will call <code>internalGetBlockType</code> and <code>internalGetItemType</code>
 */
public abstract class AbstractMinecraftRegistry implements MinecraftRegistry {

    /**
     * All cached block types
     */
    private final Map<String, BlockType> blockTypes = new HashMap<>();

    /**
     * All cached item types
     */
    private final Map<String, ItemType> itemTypes = new HashMap<>();

    /**
     * Gets the block type for the given id
     *
     * @param id
     * @return The block type
     */
    protected abstract BlockType internalGetBlockType(String id);

    /**
     * Gets the item type for the given id
     *
     * @param id
     * @return The item type
     */
    protected abstract ItemType internalGetItemType(String id);

    @Override
    public BlockType getLegacyBlockType(int id) {
        // implemented inefficiently on purpose -- not important and is to be removed when possible
        for (BlockType type : blockTypes.values()) {
            if (type.getLegacyId() == id) {
                return type;
            }
        }

        return null;
    }

    @Override
    public ItemType getLegacyItemType(int id) {
        // implemented inefficiently on purpose -- not important and is to be removed when possible
        for (ItemType type : itemTypes.values()) {
            if (type.getLegacyId() == id) {
                return type;
            }
        }

        return null;
    }

    @Override
    public BlockType getBlockType(String id) {
        if (!blockTypes.containsKey(id)) {
            blockTypes.put(id, internalGetBlockType(id));
        }

        return blockTypes.get(id);
    }

    @Override
    public List<BlockType> getBlocks() {
        return new ArrayList<>(blockTypes.values());
    }

    @Override
    public ItemType getItemType(String id) {
        if (!itemTypes.containsKey(id)) {
            itemTypes.put(id, internalGetItemType(id));
        }

        return itemTypes.get(id);
    }

    @Override
    public List<ItemType> getItems() {
        return new ArrayList<>(itemTypes.values());
    }

}
