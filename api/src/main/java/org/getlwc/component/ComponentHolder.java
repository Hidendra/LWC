package org.getlwc.component;

import java.util.Collection;

public interface ComponentHolder<T extends Component> {

    /**
     * Adds a component to the holder
     *
     * @param component
     */
    public void addComponent(T component);

    /**
     * Gets the component with the given class from the holder
     *
     * @param clazz
     * @return
     */
    public <K extends T> K getComponent(Class<K> clazz);

    /**
     * Checks if the holder has the given component
     *
     * @param clazz
     * @return
     */
    public boolean hasComponent(Class<? extends T> clazz);

    /**
     * Removes the component with the given class from the holder
     *
     * @param clazz
     * @returns removed component
     */
    public T removeComponent(Class<? extends T> clazz);

    /**
     * Returns a list of all components being held
     *
     * @return
     */
    public Collection<T> getComponents();

}
