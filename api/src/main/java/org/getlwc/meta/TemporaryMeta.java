package org.getlwc.meta;

/**
 * Represents a meta value that should not be persisted to any datastore.
 */
public class TemporaryMeta extends Meta {

    public TemporaryMeta(MetaKey key, String value) {
        super(key, value);
    }

}
