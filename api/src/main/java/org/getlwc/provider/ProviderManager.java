package org.getlwc.provider;

import java.util.Map;

public interface ProviderManager<P extends Provider> extends Map<String, P> {

    /**
     * Attempts to match a provider to a given input. If the input dose not correlate identically
     * to an already existing type, it will check the input against all provider's
     * {@link Provider#shouldProvide(String)}
     *
     * @param input
     * @return
     */
    public P match(String input);

    /**
     * Get the default provider provided by this provider
     *
     * @return the default provider; null if none exists
     */
    public P getDefault();

}
