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
     * @return
     * @return null if the item is not in the registry otherwise the item
     */
    public static Material getItem(String name) {
        return items.get(name);
    }

    /**
     *
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
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(MaterialRegistry.class.getResourceAsStream(ITEMS_FILE)));
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
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(MaterialRegistry.class.getResourceAsStream(BLOCKS_FILE)));
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
