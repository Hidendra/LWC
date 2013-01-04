package com.griefcraft.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class ItemConfig {

    /**
     * The file item definitions are stored in
     */
    public static final String ITEMS_FILE = "/items.csv";

    /**
     * A map of the id, name configs
     */
    private static final Map<Integer, String> items = new HashMap<Integer, String>();

    static {
        loadItems();
    }

    /**
     * Get the name of an item or block
     *
     * @param id
     * @return the name of the item or block. Returns NULL if it does not exist
     */
    public static String getName(int id) {
        return items.get(id);
    }

    private static void loadItems() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(ItemConfig.class.getResourceAsStream(ITEMS_FILE)));
            String line;

            while ((line = reader.readLine()) != null) {
                String[] split = line.split(",");
                String name = split[0];
                int id = Integer.parseInt(split[1]);
                // System.out.println(id + " => " + name);
                items.put(id, name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
