package org.getlwc.component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AbstractObservedMapComponent<K, V> extends AbstractMapComponent<K, V> {

    /**
     * Objects that were added to the set
     */
    private final Set<V> objectsAdded = new HashSet<>();

    /**
     * Objects that were removed from the set
     */
    private final Set<V> objectsRemoved = new HashSet<>();

    /**
     * Resets all watched state
     */
    public void resetObservedState() {
        objectsAdded.clear();
        objectsRemoved.clear();
    }

    /**
     * Returns an unmodifiable set of objects that have been added to this set
     *
     * @return
     */
    public Set<V> getObjectsAdded() {
        return Collections.unmodifiableSet(objectsAdded);
    }

    /**
     * Returns an unmodifiable set of objects that have been removed from this set
     *
     * @return
     */
    public Set<V> getObjectsRemoved() {
        return Collections.unmodifiableSet(objectsRemoved);
    }

    @Override
    public V put(K key, V value) {
        objectsAdded.add(value);
        return super.put(key, value);
    }

    @Override
    public V remove(Object key) {
        V removed = super.remove(key);

        if (removed != null) {
            objectsRemoved.add(removed);
        }

        return removed;
    }

}
