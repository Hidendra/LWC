/*
 * Copyright (c) 2011, 2012, Tyler Blair
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

package com.griefcraft.bukkit.configuration;

import com.griefcraft.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class BukkitConfiguration implements Configuration {

    /**
     * The plugin this configuration file is for
     */
    private final Plugin plugin;

    /**
     * The configuration instance we read from
     */
    private final FileConfiguration configuration;

    public BukkitConfiguration(Plugin plugin) {
        this.plugin = plugin;
        this.configuration = plugin.getConfig();

        // Saves the default config if it's not there
        if (!new File("plugins/LWC/config.yml").exists()) {
            plugin.saveDefaultConfig();
        }
    }

    public void set(String key, Object value) {
        configuration.set(key, value);
    }

    public Object get(String key) {
        return configuration.get(key);
    }

    public String getString(String key) {
        return configuration.getString(key);
    }

    public String getString(String key, String defaultValue) {
        return configuration.getString(key, defaultValue);
    }

    public boolean getBoolean(String key) {
        return configuration.getBoolean(key);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return configuration.getBoolean(key, defaultValue);
    }

    public int getInt(String key) {
        return configuration.getInt(key);
    }

    public int getInt(String key, int defaultValue) {
        return configuration.getInt(key, defaultValue);
    }

    public double getDouble(String key) {
        return configuration.getDouble(key);
    }

    public double getDouble(String key, double defaultValue) {
        return configuration.getDouble(key, defaultValue);
    }

    public void save() throws IOException {
        plugin.saveConfig();
    }


}
