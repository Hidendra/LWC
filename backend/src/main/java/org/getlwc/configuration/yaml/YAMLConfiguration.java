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
package org.getlwc.configuration.yaml;

import org.getlwc.configuration.AbstractDefaultConfiguration;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class YAMLConfiguration extends AbstractDefaultConfiguration {

    private static final Yaml yaml = new Yaml();

    /**
     * The root node
     */
    private Map<String, Object> root;

    /**
     * The file to save the config to. If null, it will not be saved.
     */
    private File file = null;

    public YAMLConfiguration(Map<String, Object> root, File file) {
        this.root = root;
        this.file = file;
    }

    public YAMLConfiguration(Map<String, Object> root) {
        this.root = root;
    }

    public YAMLConfiguration() {
        this.root = new HashMap<>();
    }

    @Override
    public boolean containsPath(String path) {
        Map<String, Object> node = getNode(getNodePath(path));

        if (node != null && node.containsKey(getNodeKey(path))) {
            return true;
        } else {
            return super.containsPath(path);
        }
    }

    @Override
    public void set(String path, Object value) {
        Map<String, Object> node = getOrCreateNode(getNodePath(path));

        if (node == null) {
            throw new IllegalStateException("Tried to set() on an uninitialized path " + Arrays.toString(getNodePath(path)) + "!");
        }

        node.put(getNodeKey(path), value);
    }

    @Override
    public Object get(String path) {
        String nodeKey = getNodeKey(path);
        Map<String, Object> node = getNode(getNodePath(path));

        if (node != null && node.containsKey(nodeKey)) {
            return node.get(nodeKey);
        } else {
            return super.get(path);
        }
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
        super.save();

        if (file != null) {
            yaml.dump(root, new FileWriter(file));
        }
    }

    /**
     * Gets the node for the given path
     *
     * @param path
     * @return
     */
    private Map<String, Object> getNode(String[] path) {
        Map<String, Object> node = root;

        for (String key : path) {
            Object o = node.get(key);

            if (o == null || !(o instanceof Map)) {
                return null;
            }

            node = (Map<String, Object>) o;
        }

        return node;
    }

    /**
     * Gets the node for the given path. If nodes along the path do not exist,
     * the will be created.
     *
     * @param path
     * @return
     */
    private Map<String, Object> getOrCreateNode(String[] path) {
        Map<String, Object> node = root;

        for (String key : path) {
            Object o = node.get(key);

            if (o == null || !(o instanceof Map)) {
                o = new HashMap<>();
                node.put(key, o);
            }

            node = (Map<String, Object>) o;
        }

        return node;
    }

}
