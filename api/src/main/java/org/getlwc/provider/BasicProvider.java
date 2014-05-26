package org.getlwc.provider;

public interface BasicProvider<T> extends Provider<T> {

    /**
     * Creates a new provider for {@link T}
     *
     * @return
     */
    public T create();

}
