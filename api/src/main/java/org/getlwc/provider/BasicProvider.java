package org.getlwc.provider;

@Deprecated
public interface BasicProvider<T> extends Provider<T> {

    /**
     * Creates a new provider for {@link T}
     *
     * @return
     */
    public T create();

}
