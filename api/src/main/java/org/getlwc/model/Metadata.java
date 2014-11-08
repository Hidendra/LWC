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

    public String getKey() {
        return key;
    }

    public String asString() {
        return value;
    }

    public int asInteger() {
        return Integer.parseInt(asString());
    }

}
