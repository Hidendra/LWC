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

public class ConfigurationView extends AbstractDefaultConfiguration {

    /**
     * Prefix being viewed
     */
    private String prefix;

    /**
     * The parent configuration
     */
    private Configuration parent;

    public ConfigurationView(String prefix, Configuration parent) {
        this.prefix = prefix;
        this.parent = parent;
    }

    @Override
    public boolean containsPath(String path) {
        path = prefix + path;

        if (parent.containsPath(path)) {
            return true;
        } else {
            return super.containsPath(path);
        }
    }

    @Override
    public void set(String path, Object value) {
        path = prefix + path;
        parent.set(path, value);
    }

    @Override
    public Object get(String path) {
        path = prefix + path;

        if (parent.containsPath(path)) {
            return parent.get(path);
        } else {
            return super.get(path);
        }
    }

    @Override
    public void setDefault(String path, Object value) {
        path = prefix + path;
        super.setDefault(path, value);
    }

    @Override
    public String getString(String path) {
        Object value = get(path);

        if (value == null) {
            return null;
        }

        return value.toString();
    }

    @Override
    public boolean getBoolean(String path) {
        return castBoolean(get(path));
    }

    @Override
    public int getInt(String path) {
        return castInt(get(path));
    }

    @Override
    public double getDouble(String path) {
        return castDouble(get(path));
    }

    @Override
    public void save() throws IOException {
        parent.save();
    }

    @Override
    public Configuration viewFor(String viewPrefix) {
        if (!viewPrefix.endsWith(".")) {
            viewPrefix += ".";
        }

        return new ConfigurationView(viewPrefix, this);
    }

}
