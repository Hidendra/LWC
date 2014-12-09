package org.getlwc.component;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BasicComponentHolder<T extends Component> implements ComponentHolder<T> {

    /**
     * Components being held
     */
    private Map<Class<T>, T> components = new HashMap<>();

    @Override
    public void addComponent(T component) {
        components.put((Class<T>) component.getClass(), component);
    }

    @Override
    public <K extends T> K getComponent(Class<K> clazz) {
        return (K) components.get(clazz);
    }

    @Override
    public boolean hasComponent(Class<? extends T> clazz) {
        return components.containsKey(clazz);
    }

    @Override
    public T removeComponent(Class<? extends T> clazz) {
        return components.remove(clazz);
    }

    @Override
    public Collection<T> getComponents() {
        return Collections.unmodifiableCollection(components.values());
    }

}
