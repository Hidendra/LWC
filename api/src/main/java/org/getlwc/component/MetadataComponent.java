package org.getlwc.component;

import org.getlwc.meta.Meta;
import org.getlwc.meta.MetaKey;

public class MetadataComponent extends AbstractObservedMapComponent<MetaKey, Meta> {

    @Override
    public String toString() {
        return String.format("MetadataComponent(%s)", values().toString());
    }

}
