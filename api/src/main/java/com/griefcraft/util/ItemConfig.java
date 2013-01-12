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
