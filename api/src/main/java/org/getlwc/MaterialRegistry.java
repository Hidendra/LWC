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
package org.getlwc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * The registry for materials. Presently, these are split up into items and blocks.
 * This is done because some items/blocks collide with each other; for example
 * minecraft:wooden_door is both an item and a block.
 */
public class MaterialRegistry {

    /**
     * The file item definitions are stored in
     */
    public static final String ITEMS_FILE = "/items.csv";

    /**
     * The file block definitions are stored in
     */
    public static final String BLOCKS_FILE = "/blocks.csv";

    /**
     * A map of items in the registry
     */
    private final static Map<String, Material> items = new HashMap<String, Material>();

    /**
     * A map of items in the registry (by id)
     */
    private final static Map<Integer, Material> itemsById = new HashMap<Integer, Material>();

    /**
     * A map of blocks in the registry
     */
    private final static Map<String, Material> blocks = new HashMap<String, Material>();

    /**
     * A map of blocks in the registry (by id)
     */
    private final static Map<Integer, Material> blocksById = new HashMap<Integer, Material>();

    static {
        loadItems();
        loadBlocks();
    }

    /**
     * Get an item material from the registry
     *
     * @param name
     * @return null if the item is not in the registry otherwise the item
     */
    public static Material getItem(String name) {
        return items.get(name);
    }

    /**
     * Get an item material from the registry by its id
     *
     * @param id
     * @return null if the block is not in the registry otherwise the block
     */
    public static Material getItemById(int id) {
        return itemsById.get(id);
    }

    /**
     * Get a block material from the registry
     *
     * @param name
     * @return null if the block is not in the registry otherwise the block
     */
    public static Material getBlock(String name) {
        return items.get(name);
    }

    /**
     * Get a block material from the registry by its id
     *
     * @param id
     * @return null if the block is not in the registry otherwise the block
     */
    public static Material getBlockById(int id) {
        return blocksById.get(id);
    }

    /**
     * Load all of the items
     */
    private static void loadItems() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(MaterialRegistry.class.getResourceAsStream(ITEMS_FILE)))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] split = line.split(",");
                String name = split[0];
                int id = Integer.parseInt(split[1]);

                Material material = new Material(name, id);
                // System.out.println("item: " + material);

                items.put(name, material);
                itemsById.put(id, material);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Load all of the items
     */
    private static void loadBlocks() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(MaterialRegistry.class.getResourceAsStream(BLOCKS_FILE)))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] split = line.split(",");
                String name = split[0];
                int id = Integer.parseInt(split[1]);

                Material material = new Material(name, id);
                // System.out.println("block: " + material);

                blocks.put(name, material);
                blocksById.put(id, material);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
