package org.getlwc.role;

import org.getlwc.factory.AbstractFactoryRegistry;

public class RoleFactoryRegistry extends AbstractFactoryRegistry<RoleFactory> {

    @Override
    public RoleFactory find(String name) {
        for (RoleFactory factory : factories.values()) {
            String realName = factory.match(name);

            if (realName != null) {
                return factory;
            }
        }

        return null;
    }

}
