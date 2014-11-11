package org.getlwc.content.component;

import org.getlwc.component.AbstractValueComponent;

public class DescriptionComponent extends AbstractValueComponent<String> {

    public DescriptionComponent(String description) {
        set(description);
    }

    @Override
    public String toString() {
        return String.format("DescriptionComponent(\"%s\")", get());
    }

}
