package org.getlwc.provider;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SimpleProviderManager<P extends Provider> implements ProviderManager<P> {

    /**
     * A map of the providers this manager managers
     */
    private final Map<String, P> providers = new HashMap<>();

    /**
     * The default provider
     */
    private P defaultProvider = null;

    public SimpleProviderManager() {

    }

    public SimpleProviderManager(P defaultProvider) {
        this.defaultProvider = defaultProvider;
    }

    /**
     * Set the default provider provided by this manager
     *
     * @param provider
     */
    public void setDefaultProvider(P provider) {
        this.defaultProvider = provider;
    }

    @Override
    public P match(String input) {
        if (providers.containsKey(input)) {
            return get(input);
        }

        for (Entry<String, P> entry : providers.entrySet()) {
            P provider = entry.getValue();

            if (provider.shouldProvide(input)) {
                return provider;
            }
        }

        return getDefault();
    }

    @Override
    public P get(Object key) {
        if (providers.containsKey(key)) {
            return providers.get(key);
        }

        return getDefault();
    }

    @Override
    public P getDefault() {
        return defaultProvider;
    }

    @Override
    public int size() {
        return providers.size();
    }

    @Override
    public boolean isEmpty() {
        return providers.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return providers.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return providers.containsValue(value);
    }

    public P put(String key, P value) {
        return providers.put(key, value);
    }

    @Override
    public P remove(Object key) {
        return providers.remove(key);
    }

    public void putAll(Map<? extends String, ? extends P> m) {
        providers.putAll(m);
    }

    @Override
    public void clear() {
        providers.clear();
    }

    @Override
    public Set<String> keySet() {
        return providers.keySet();
    }

    @Override
    public Collection<P> values() {
        return providers.values();
    }

    @Override
    public Set<Entry<String,P>> entrySet() {
        return providers.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        return providers.equals(o);
    }

    @Override
    public int hashCode() {
        return providers.hashCode();
    }

}
