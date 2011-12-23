/*
 * Copyright 2011 Tyler Blair. All rights reserved.
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

package com.griefcraft.migration;

import com.griefcraft.lwc.LWC;
import com.griefcraft.modules.worldguard.WorldGuardModule;
import org.bukkit.Material;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class ConfigPost300 implements MigrationUtility {
    private static Logger logger = Logger.getLogger("Patcher");

    // contains the core config equivalent key pairs
    // e.g locale->core.locale
    private static Map<String, String> mappings = null;

    public void run() {
        LWC lwc = LWC.getInstance();
        File configFile = new File("plugins/LWC/lwc.properties");

        if (!configFile.exists()) {
            return;
        }

        // delete internal.ini
        new File("plugins/LWC/internal.ini").delete();
        logger.info("Converting lwc.properties to new variants");

        // we need to convert..
        populate();

        // load lwc.properties
        Properties old = new Properties();

        try {
            InputStream inputStream = new FileInputStream(configFile);
            old.load(inputStream);
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // convert the easy to do mappings
        for (Map.Entry<String, String> entry : mappings.entrySet()) {
            String oldKey = entry.getKey();
            String newKey = entry.getValue();

            // Don't mind me, just making the converted values appear correctly!
            try {
                lwc.getConfiguration().setProperty(newKey, Integer.parseInt(old.getProperty(oldKey, "")));
            } catch (Exception e) {
                String value = old.getProperty(oldKey, "");

                if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                    lwc.getConfiguration().setProperty(newKey, Boolean.parseBoolean(old.getProperty(oldKey, "")));
                } else {
                    lwc.getConfiguration().setProperty(newKey, old.getProperty(oldKey, ""));
                }
            }

        }

        // custom mappings, can't be easily done

        // protection blacklist
        String protectionBlacklist = old.getProperty("protection-blacklist", "").trim();

        if (!protectionBlacklist.isEmpty()) {
            String[] split = protectionBlacklist.replaceAll(" ", "_").split(",");

            for (String protection : split) {
                int blockId = 0;

                try {
                    blockId = Integer.parseInt(protection);
                } catch (NumberFormatException e) {
                }

                // if it's an int, convert it
                if (blockId > 0) {
                    protection = Material.getMaterial(blockId).toString().toLowerCase().replaceAll("block", "");

                    if (protection.endsWith("_")) {
                        protection = protection.substring(0, protection.length() - 1);
                    }
                }

                lwc.getConfiguration().setProperty("protections.blocks." + protection + ".enabled", false);
            }
        }

        // WorldGuard
        String enforceWorldGuard = old.getProperty("enforce-worldguard-regions");

        if (Boolean.parseBoolean(enforceWorldGuard)) {
            WorldGuardModule worldGuard = (WorldGuardModule) lwc.getModuleLoader().getModule(WorldGuardModule.class);
            List<String> regions = null;

            String oldRegions = old.getProperty("worldguard-allowed-regions");
            regions = Arrays.asList(oldRegions.split(","));

            worldGuard.set("worldguard.enabled", true);
            worldGuard.set("worldguard.regions", regions);
            worldGuard.save();
        }

        // we're done, free up the mappings & save
        mappings = null;
        lwc.getConfiguration().save();
        configFile.delete();
    }

    // populate the mappings table with well the mappings
    private static void populate() {
        mappings = new HashMap<String, String>();

        mappings.put("allow-block-destruction", "protections.allowBlockDestruction");
        mappings.put("auto-update", "core.autoUpdate");
        mappings.put("database", "database.adapter");
        mappings.put("db-path", "database.path");
        mappings.put("default-menu-style", "core.defaultMenuStyle");
        mappings.put("deny-redstone", "protections.denyRedstone");
        mappings.put("flush-db-interval", "core.flushInterval");
        mappings.put("locale", "core.locale");
        mappings.put("mysql-database", "database.database");
        mappings.put("mysql-host", "database.host");
        mappings.put("mysql-pass", "database.password");
        mappings.put("mysql-user", "database.username");
        mappings.put("show-protection-notices", "core.showNotices");
        mappings.put("verbose", "core.verbose");
        mappings.put("op-is-lwcadmin", "core.opIsLWCAdmin");
    }

}
