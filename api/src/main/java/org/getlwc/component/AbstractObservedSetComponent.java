package org.getlwc.component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AbstractObservedSetComponent<K> extends AbstractSetComponent<K> {

    /**
     * Objects that were added to the set
     */
    private final Set<K> objectsAdded = new HashSet<>();

    /**
     * Objects that were removed from the set
     */
    private final Set<K> objectsRemoved = new HashSet<>();

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
    public Set<K> getObjectsAdded() {
        return Collections.unmodifiableSet(objectsAdded);
    }

    /**
     * Returns an unmodifiable set of objects that have been removed from this set
     *
     * @return
     */
    public Set<K> getObjectsRemoved() {
        return Collections.unmodifiableSet(objectsRemoved);
    }

    @Override
    public boolean add(K object) {
        boolean result = super.add(object);

        objectsAdded.add(object);

        return result;
    }

    @Override
    public boolean remove(K object) {
        boolean result = super.remove(object);

        objectsRemoved.add(object);

        return result;
    }

}
