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

package com.griefcraft.util.config;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Updates config files with config vars that aren't already stored in the local
 * config file (so the config file appears to always be up to date.)
 */
public class ConfigUpdater {

    /**
     * Config nodes NOT to update - this includes child nodes
     */
    private static List<String> BLACKLIST = Arrays.asList(
            "protections.blocks",
            "master",
            "groups",
            "players",
            "defaults"
    );

    /**
     * The cache for the reference config files
     */
    private final Map<String, FileConfiguration> referenceConfigFileCache = new HashMap<String, FileConfiguration>();

    /**
     * Load the reference config files in the local jar file. The key in the map is the
     * file name
     *
     * @return
     */
    private Map<String, FileConfiguration> loadReferenceConfigFiles() throws IOException {
        if (referenceConfigFileCache.size() > 0) {
            return referenceConfigFileCache;
        }

        String path = getClass().getResource("/config/config.yml").toString();
        path = path.substring(path.indexOf('/'), path.lastIndexOf('!'));

        // Load our jar file
        ZipFile jarFile = new ZipFile(URLDecoder.decode(path, "UTF-8"));

        // Begin loading the files
        Enumeration entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry file = (ZipEntry) entries.nextElement();
            String name = file.getName();

            // We only want config dir
            if (!name.startsWith("config/") || !name.endsWith(".yml")) {
                continue;
            }

            // Get just the name
            String realName = name.substring(name.indexOf('/') + 1);

            // Insert it
            FileConfiguration configuration = new FileConfiguration((File) null);
            configuration.load(jarFile.getInputStream(file));
            referenceConfigFileCache.put(realName, configuration);
        }

        return referenceConfigFileCache;
    }

    /**
     * Run the updater against a configuration file
     */
    public void update(FileConfiguration configuration) {
        try {
            Map<String, FileConfiguration> referenceFiles = loadReferenceConfigFiles();

            // Check for the file in the reference
            FileConfiguration reference = referenceFiles.get(configuration.getFile().getName());

            // Was it found?
            if (reference == null) {
                return;
            }

            // The found keys
            List<String> currentKeys = getKeysDepth2(configuration);
            List<String> referenceKeys = getKeysDepth2(reference);
            boolean modified = false;

            // Now go through and add any new keys
            for (String key : referenceKeys) {
                boolean contains = configuration.getProperty(key) != null;

                if (!contains) {
                    configuration.setProperty(key, reference.getProperty(key));
                    modified = true;
                }
            }

            // Look for any config values to remove
            for (String key : currentKeys) {
                boolean contains = reference.getProperty(key) != null;

                if (!contains) {
                    configuration.removeProperty(key);
                    modified = true;
                }
            }

            // Save the file if it was modified
            if (modified) {
                configuration.save();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get all of the nodes in a configuration file up to 1 node down from the root
     * <pre>
     * root
     *    -> node
     *    -> node
     *        -> subnode
     *        -> subnode
     * </pre>
     * In this case, the subnodes are not returned (the root and nodes however, are)
     *
     * @param configuration
     * @return
     */
    private List<String> getKeysDepth2(FileConfiguration configuration) {
        List<String> keys = new ArrayList<String>();

        // go through the root
        for (String key : configuration.getKeys(null)) {
            // Is it blacklisted?
            if (BLACKLIST.contains(key)) {
                continue;
            }

            keys.add(key);

            // Add the subnodes
            for (String subkey : configuration.getKeys(key)) {
                String fullKey = key + "." + subkey;

                // Is it blacklisted?
                if (BLACKLIST.contains(fullKey)) {
                    continue;
                }

                // good good!
                keys.add(fullKey);
            }
        }

        return keys;
    }

}
