package org.getlwc.factory;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractFactoryRegistry<T extends AbstractFactory> {

    /**
     * A map of all of the factories
     */
    protected Map<String, T> factories = new HashMap<String, T>();

    /**
     * Find a factory for the given name
     *
     * @param name
     * @return
     */
    public abstract T find(String name);

    /**
     * Register a factory
     *
     * @param factory
     */
    public T register(T factory) {
        return factories.put(factory.getName().toLowerCase(), factory);
    }

    /**
     * Remove a factory from the registry
     *
     * @param factory
     * @return
     */
    public T unregister(T factory) {
        return factories.remove(factory.getName().toLowerCase());
    }

    /**
     * Get a factory from the registry
     *
     * @param name
     * @return
     */
    public T get(String name) {
        return factories.get(name.toLowerCase());
    }

    /**
     * Clear the registry
     */
    public void clear() {
        factories.clear();
    }

}
