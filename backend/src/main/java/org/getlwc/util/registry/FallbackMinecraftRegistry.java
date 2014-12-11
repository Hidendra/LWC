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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A fallback registry that loads item and block names from text files.
 * Legacy integer ids are supported.
 */
public class FallbackMinecraftRegistry implements MinecraftRegistry {

    public static final String BLOCK_REGISTRY_RESOURCE = "/registry/blocks.csv";
    public static final String ITEM_REGISTRY_RESOURCE = "/registry/items.csv";

    /**
     * All cached block types their by string id
     */
    private final Map<String, BlockType> blockTypesById = new HashMap<>();

    /**
     * All cached block types by their integer id
     */
    private final Map<Integer, BlockType> blockTypesByLegacyId = new HashMap<>();

    /**
     * All cached item types by their integer id
     */
    private final Map<String, ItemType> itemTypesById = new HashMap<>();

    /**
     * All cached item types by their integer id
     */
    private final Map<Integer, ItemType> itemTypesByLegacyId = new HashMap<>();


    public FallbackMinecraftRegistry() {
        loadRegistry();
    }

    /**
     * Loads the registry
     */
    private void loadRegistry() {
        loadBlockRegistry();
        loadItemRegistry();
    }

    /**
     * Loads the block register
     */
    private void loadBlockRegistry() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(BLOCK_REGISTRY_RESOURCE)))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] split = line.split(",");

                if (split.length == 2) {
                    final String stringId = split[0];
                    final int intId = Integer.parseInt(split[1]);

                    BlockType type = new BlockType() {
                        @Override
                        public String getId() {
                            return stringId;
                        }

                        @Override
                        public int getLegacyId() {
                            return intId;
                        }

                        @Override
                        public String getName() {
                            return stringId;
                        }
                    };

                    blockTypesById.put(stringId, type);
                    blockTypesByLegacyId.put(intId, type);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the item registry
     */
    private void loadItemRegistry() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(ITEM_REGISTRY_RESOURCE)))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] split = line.split(",");

                if (split.length == 2) {
                    final String stringId = split[0];
                    final int intId = Integer.parseInt(split[1]);

                    ItemType type = new ItemType() {
                        @Override
                        public String getId() {
                            return stringId;
                        }

                        @Override
                        public int getLegacyId() {
                            return intId;
                        }

                        @Override
                        public int getMaxStackQuantity() {
                            return 64; // TODO?
                        }

                        @Override
                        public String getName() {
                            return stringId;
                        }
                    };

                    itemTypesById.put(stringId, type);
                    itemTypesByLegacyId.put(intId, type);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets a block type by its integer id
     *
     * @param id
     * @return block type
     */
    public BlockType getBlockTypeByIntegerId(int id) {
        return blockTypesByLegacyId.get(id);
    }

    @Override
    public BlockType getBlockType(String id) {
        for (BlockType type : blockTypesByLegacyId.values()) {
            if (type.getName().equals(id)) {
                return type;
            }
        }

        return null;
    }

    @Override
    public BlockType getLegacyBlockType(int id) {
        return blockTypesByLegacyId.get(id);
    }

    @Override
    public List<BlockType> getBlocks() {
        return new ArrayList<>(blockTypesByLegacyId.values());
    }

    @Override
    public ItemType getItemType(String id) {
        for (ItemType type : itemTypesByLegacyId.values()) {
            if (type.getName().equals(id)) {
                return type;
            }
        }

        return null;
    }

    @Override
    public ItemType getLegacyItemType(int id) {
        return itemTypesByLegacyId.get(id);
    }

    @Override
    public List<ItemType> getItems() {
        return new ArrayList<>(itemTypesByLegacyId.values());
    }

}
