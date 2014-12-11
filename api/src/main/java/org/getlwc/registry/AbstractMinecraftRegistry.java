package org.getlwc.registry;

import org.getlwc.BlockType;
import org.getlwc.ItemType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An implementation of {@link org.getlwc.registry.MinecraftRegistry} that caches
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
