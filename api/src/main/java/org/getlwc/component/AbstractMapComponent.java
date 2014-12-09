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
package org.getlwc.component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AbstractMapComponent<K, V> extends Component {

    /**
     * Objects inside the component
     */
    private final Map<K, V> objects = new HashMap<>();

    /**
     * Removes a value from the component
     *
     * @param key
     * @return
     */
    public V remove(Object key) {
        return objects.remove(key);
    }

    /**
     * Retrieves a value from the component
     *
     * @param key
     * @return
     */
    public V get(Object key) {
        return objects.get(key);
    }

    /**
     * Returns true if the component contains the given value
     *
     * @param value
     * @return
     */
    public boolean containsValue(Object value) {
        return objects.containsValue(value);
    }

    /**
     * Returns true if the component contains the given key
     *
     * @param key
     * @return
     */
    public boolean containsKey(Object key) {
        return objects.containsKey(key);
    }

    /**
     * Puts a key, value pair into the component
     *
     * @param key
     * @param value
     * @return
     */
    public V put(K key, V value) {
        return objects.put(key, value);
    }

    /**
     * Returns a set of all keys inside the map
     *
     * @return
     */
    public Set<K> keySet() {
        return objects.keySet();
    }

    /**
     * Returns a collection of all values in the component
     * @return
     */
    public Collection<V> values() {
        return objects.values();
    }

}
