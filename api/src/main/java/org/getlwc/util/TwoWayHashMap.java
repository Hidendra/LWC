package org.getlwc.util;

import java.util.HashMap;
import java.util.Map;

public class TwoWayHashMap<K, V> {

    /**
     * The forward hash map
     */
    private Map<K, V> forward = new HashMap<K, V>();

    /**
     * The inverse hash map
     */
    private Map<V, K> backward = new HashMap<V, K>();

    public synchronized void put(K key, V value) {
        forward.put(key, value);
        backward.put(value, key);
    }

    public synchronized V getForward(K key) {
        return forward.get(key);
    }

    public synchronized K getBackward(V key) {
        return backward.get(key);
    }

    public Map<K, V> getForward() {
        return forward;
    }

    public Map<V, K> getBackward() {
        return backward;
    }

}
