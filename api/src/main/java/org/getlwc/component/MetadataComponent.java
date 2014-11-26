package org.getlwc.component;

import org.getlwc.model.Metadata;

public class MetadataComponent extends AbstractObservedMapComponent<String, Metadata> {

    @Override
    public String toString() {
        return String.format("MetadataComponent(%s)", values().toString());
    }

}
