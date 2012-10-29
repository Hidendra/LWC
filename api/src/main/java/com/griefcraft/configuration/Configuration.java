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

package com.griefcraft.configuration;

import java.io.IOException;

public interface Configuration {

    /**
     * Set a configuration value
     *
     * @param key
     * @param value
     */
    public void set(String key, Object value);

    /**
     * Gets an object from the configuration
     *
     * @param key
     * @return
     */
    public Object get(String key);

    /**
     * Gets a string from the configuration
     *
     * @param key
     * @return
     */
    public String getString(String key);

    /**
     * Gets a string from the configuration using the default value if it does not exist
     *
     * @param key
     * @return
     */
    public String getString(String key, String defaultValue);

    /**
     * Gets a boolean from the configuration
     *
     * @param key
     * @return
     */
    public boolean getBoolean(String key);

    /**
     * Gets a boolean from the configuration using the default value if it does not exist
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public boolean getBoolean(String key, boolean defaultValue);

    /**
     * Gets an int from the configuration
     *
     * @param key
     * @return
     */
    public int getInt(String key);

    /**
     * Gets an int from the configuration using the default value if it does not exist
     *
     * @param key
     * @return
     */
    public int getInt(String key, int defaultValue);

    /**
     * Gets a double from the configuration
     *
     * @param key
     * @return
     */
    public double getDouble(String key);

    /**
     * Gets a double from the configuration using the default value if it does not exist
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public double getDouble(String key, double defaultValue);

    /**
     * Save the configuration file
     */
    public void save() throws IOException;


}
