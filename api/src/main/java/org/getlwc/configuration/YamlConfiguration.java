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
package org.getlwc.configuration;

import java.io.IOException;
import java.io.InputStream;

@Deprecated
public class YamlConfiguration implements Configuration {

    /**
     * The configuration instance we read from
     */
    private final FileConfiguration configuration;

    /**
     * If we are using an input stream to load the config
     */
    private boolean inputStream = false;

    public YamlConfiguration(String config) {
        this.configuration = FileConfiguration.load(config);

        // Saves the default config if it's not there
        if (!configuration.getFile().exists()) {
            configuration.save();
        }
    }

    public YamlConfiguration(InputStream is) {
        this.configuration = FileConfiguration.loadViaInputStream(is);
        inputStream = true;
    }

    @Override
    public void set(String path, Object value) {
        configuration.setProperty(path, value);
    }

    @Override
    public Object get(String path) {
        return configuration.getProperty(path);
    }

    @Override
    public String getString(String path) {
        return configuration.getString(path);
    }

    @Override
    public String getString(String key, String defaultValue) {
        return configuration.getString(key, defaultValue);
    }

    @Override
    public boolean getBoolean(String path) {
        return configuration.getBoolean(path, false);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return configuration.getBoolean(key, defaultValue);
    }

    @Override
    public int getInt(String path) {
        return configuration.getInt(path, 0);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return configuration.getInt(key, defaultValue);
    }

    @Override
    public double getDouble(String path) {
        return configuration.getDouble(path, 0);
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        return configuration.getDouble(key, defaultValue);
    }

    @Override
    public void save() throws IOException {
        if (inputStream) {
            throw new UnsupportedOperationException("Configuration was loaded from an InputStream. Cannot save()");
        }

        configuration.save();
    }

}
