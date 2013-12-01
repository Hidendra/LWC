package org.getlwc.attribute;

import org.getlwc.factory.AbstractFactoryRegistry;

public class AttributeFactoryRegistry extends AbstractFactoryRegistry<ProtectionAttributeFactory> {

    @Override
    public ProtectionAttributeFactory find(String name) {
        return get(name);
    }

}
