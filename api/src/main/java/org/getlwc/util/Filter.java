package org.getlwc.util;

public interface Filter<T> {

    /**
     * Returns true if the given value should be accepted into the filter.
     *
     * @param value
     * @return true if the value should be accepted
     */
    public boolean accept(T value);

}
