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
package org.getlwc.configuration.json;

import org.getlwc.configuration.AbstractDefaultConfiguration;
import org.getlwc.util.JSONPrettyWriter;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

public class JSONConfiguration extends AbstractDefaultConfiguration {

    /**
     * The root node
     */
    private JSONObject root;

    /**
     * The file the configuration is being serialized to.
     * If null, the configuration won't be saved
     */
    private File file = null;

    public JSONConfiguration(JSONObject root, File file) {
        this.root = root;
        this.file = file;
    }

    public JSONConfiguration(JSONObject root) {
        this.root = root;
    }

    public JSONConfiguration() {
        this.root = new JSONObject();
    }

    @Override
    public boolean containsPath(String path) {
        JSONObject node = getNode(getNodePath(path));

        if (node != null && node.containsKey(getNodeKey(path))) {
            return true;
        } else {
            return super.containsPath(path);
        }
    }

    @Override
    public void set(String path, Object value) {
        JSONObject node = getOrCreateNode(getNodePath(path));

        if (node == null) {
            throw new IllegalStateException("Tried to set() on an unitialized path " + Arrays.toString(getNodePath(path)) + "!");
        }

        node.put(getNodeKey(path), value);
    }

    @Override
    public Object get(String path) {
        String nodeKey = getNodeKey(path);
        JSONObject node = getNode(getNodePath(path));

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
            try (Writer writer = new JSONPrettyWriter(new FileWriter(file))) {
                JSONValue.writeJSONString(root, writer);
            }
        }
    }

    /**
     * Gets the node for the given path
     *
     * @param path
     * @return
     */
    private JSONObject getNode(String[] path) {
        JSONObject node = root;

        for (String key : path) {
            Object o = node.get(key);

            if (o == null || !(o instanceof JSONObject)) {
                return null;
            }

            node = (JSONObject) o;
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
    private JSONObject getOrCreateNode(String[] path) {
        JSONObject node = root;

        for (String key : path) {
            Object o = node.get(key);

            if (o == null || !(o instanceof JSONObject)) {
                o = new JSONObject();
                node.put(key, o);
            }

            node = (JSONObject) o;
        }

        return node;
    }

}
