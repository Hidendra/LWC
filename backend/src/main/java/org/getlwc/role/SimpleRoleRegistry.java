package org.getlwc.role;

import java.util.HashMap;
import java.util.Map;

public class SimpleRoleRegistry implements RoleRegistry {

    /**
     * Factories for creating or loading roles
     */
    private Map<String, RoleFactory> factories = new HashMap<>();

    @Override
    public void registerRoleLoader(String type, RoleFactory<?> factory) {
        factories.put(type.toLowerCase(), factory);
    }

    @Override
    public <V extends Role> V loadRole(String type, String value) throws RoleCreationException {
        String typeLower = type.toLowerCase();

        if (factories.containsKey(typeLower)) {
            return (V) factories.get(typeLower).createFromValue(value);
        } else {
            return null;
        }
    }

}
