package org.getlwc.meta;

public class Meta {

    /**
     * The metadata key
     */
    private final MetaKey key;

    /**
     * The metadata value
     */
    private final String value;

    public Meta(MetaKey key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("Meta(%s='%s')", key, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Meta meta = (Meta) o;

        if (key != null ? !key.equals(meta.key) : meta.key != null) return false;
        if (value != null ? !value.equals(meta.value) : meta.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        return result;
    }

    public MetaKey getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String asString() {
        return value;
    }

    public int asInteger() {
        return Integer.parseInt(asString());
    }

}
