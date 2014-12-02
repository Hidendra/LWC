package org.getlwc.meta;

public final class MetaKey {

    /**
     * The key
     */
    private final String key;

    private MetaKey(String key) {
        this.key = key;
    }

    /**
     * Creates a new {@link org.getlwc.meta.MetaKey} with the given name.
     *
     * @param name
     * @return
     */
    public static MetaKey valueOf(String name) {
        return new MetaKey(name.toLowerCase());
    }

    @Override
    public String toString() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MetaKey metaKey = (MetaKey) o;

        if (key != null ? !key.equals(metaKey.key) : metaKey.key != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }

    /**
     * Returns the key
     *
     * @return
     */
    public String getKey() {
        return key;
    }
}
