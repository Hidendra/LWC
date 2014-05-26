package org.getlwc.provider;

import org.getlwc.model.Protection;

public interface ProtectionProvider<T> extends Provider<T> {

    /**
     * Creates a new provider for {@link T}
     *
     * @param protection
     * @return
     */
    public T create(Protection protection);

}
