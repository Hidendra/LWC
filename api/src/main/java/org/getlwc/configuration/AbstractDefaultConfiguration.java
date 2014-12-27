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
import java.util.HashMap;
import java.util.Map;

/**
 * An abstract implementation of {@link org.getlwc.configuration.Configuration} that
 * only implements the defaults methods, which normally always function the same.
 *
 * Defaults are applied lazily; all defaults will not be forcibly applied to the
 * configuration unless it is saved. Otherwise, the implementation is expected
 * to gracefully fallback on contains(), and get(), and any other getters such as
 * <code>getInt()</code> are expected to rely on get().
 */
public abstract class AbstractDefaultConfiguration implements Configuration {

    /**
     * All defaults, keyed by the path -> value
     */
    private final Map<String, Object> defaults = new HashMap<>();

    @Override
    public boolean contains(String path) {
        return defaults.containsKey(path);
    }

    @Override
    public Object get(String path) {
        return defaults.get(path);
    }

    @Override
    public void setDefault(String path, Object value) {
        defaults.put(path, value);
    }

    @Override
    public void save() throws IOException {
        bindDefaults();
    }

    /**
     * Gets the path to a given node.
     * e.g. key = String[0], some.sub.key = { "some", "sub" }
     *
     * @param path
     * @return
     */
    protected String[] getNodePath(String path) {
        String[] split = path.split("\\.");

        if (split.length == 1) {
            return new String[0];
        } else {
            String[] result = new String[split.length - 1];
            System.arraycopy(split, 0, result, 0, result.length);
            return result;
        }
    }

    /**
     * Gets the key of final node that is accessed with.
     * e.g. key = key, some.sub.key = key
     *
     * @param path
     * @return
     */
    protected String getNodeKey(String path) {
        String[] split = path.split("\\.");

        if (split.length == 1) {
            return split[0];
        } else {
            return split[split.length - 1];
        }
    }

    /**
     * Binds all defaults to the configuration if they aren't set.
     *
     * @return
     */
    private void bindDefaults() {
        for (Map.Entry<String, Object> entry : defaults.entrySet()) {
            String path = entry.getKey();
            Object value = entry.getValue();

            if (!contains(path)) {
                set(path, value);
            }
        }
    }

}
