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

public interface Configuration {

    /**
     * Checks if the configuration contains the given key
     *
     * @param path
     * @return true if the config contains the given key
     */
    public boolean contains(String path);

    /**
     * Set a configuration value
     *
     * @param path
     * @param value
     */
    public void set(String path, Object value);

    /**
     * Gets an object from the configuration
     *
     * @param path
     * @return
     */
    public Object get(String path);

    /**
     * Sets a default value for the configuration.
     *
     * @param path
     * @param value
     */
    public void setDefault(String path, Object value);

    /**
     * Gets a string from the configuration
     *
     * @param path
     * @return
     */
    public String getString(String path);

    /**
     * Gets a boolean from the configuration
     *
     * @param path
     * @return
     */
    public boolean getBoolean(String path);

    /**
     * Gets an int from the configuration
     *
     * @param path
     * @return
     */
    public int getInt(String path);

    /**
     * Gets a double from the configuration
     *
     * @param path
     * @return
     */
    public double getDouble(String path);

    /**
     * Save the configuration file
     */
    public void save() throws IOException;


}
