package org.getlwc.component;

import org.getlwc.Location;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AbstractSetComponent<T> extends Component {

    /**
     * Set of objects being held
     */
    private Set<T> objects = new HashSet<>();

    /**
     * Adds a held object to this component
     *
     * @param object
     */
    public boolean add(T object) {
        return objects.add(object);
    }

    /**
     * Removes a held location from this component
     *
     * @param object
     */
    public boolean remove(T object) {
        return objects.remove(object);
    }

    /**
     * Checks if the location is held by this component
     *
     * @param object
     * @return
     */
    public boolean has(T object) {
        return objects.contains(object);
    }

    /**
     * Returns a collection of all locations being held by this component
     *
     * @return
     */
    public Collection<T> getAll() {
        return Collections.unmodifiableCollection(objects);
    }

}
