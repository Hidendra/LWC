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
