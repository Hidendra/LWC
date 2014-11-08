package org.getlwc.component;

public abstract class AbstractValueComponent<V> {

    /**
     * The value stored by this component
     */
    private V value;

    /**
     * Sets the value for this component
     *
     * @param value
     */
    public void set(V value) {
        this.value = value;
    }

    /**
     * Returns the value for this component
     *
     * @return
     */
    public V get() {
        return value;
    }

}
