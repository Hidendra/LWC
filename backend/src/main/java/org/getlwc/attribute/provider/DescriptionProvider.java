package org.getlwc.attribute.provider;

import org.getlwc.Engine;
import org.getlwc.attribute.StringAttribute;
import org.getlwc.model.AbstractAttribute;
import org.getlwc.provider.BasicProvider;

public class DescriptionProvider implements BasicProvider<AbstractAttribute> {

    public static final String NAME = "description";

    private final Engine engine;

    public DescriptionProvider(Engine engine) {
        this.engine = engine;
    }

    @Override
    public AbstractAttribute create() {
        return new StringAttribute(engine, NAME);
    }

    @Override
    public boolean shouldProvide(String input) {
        return input.toLowerCase().equals("description");
    }

}
