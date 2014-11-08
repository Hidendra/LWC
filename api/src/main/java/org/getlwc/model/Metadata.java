package org.getlwc.model;

public final class Metadata {

    /**
     * The metadata key
     */
    private final String key;

    /**
     * The metadata value
     */
    private final String value;

    public Metadata(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Metadata metadata = (Metadata) o;

        if (key != null ? !key.equals(metadata.key) : metadata.key != null) return false;
        if (value != null ? !value.equals(metadata.value) : metadata.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    public String getKey() {
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
