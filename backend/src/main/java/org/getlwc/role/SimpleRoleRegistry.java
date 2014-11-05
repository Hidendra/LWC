package org.getlwc.role;

import java.util.HashMap;
import java.util.Map;

public class SimpleRoleRegistry implements RoleRegistry {

    /**
     * Factories for creating or loading roles
     */
    private Map<String, RoleFactory> factories = new HashMap<>();

    @Override
    public void registerRoleType(String type, Class<? extends Role> roleClass) {
        if (roleClass != null) {
            factories.put(type.toLowerCase(), new SimpleRoleFactory(roleClass));
        }
    }

    @Override
    public <V extends Role> V createRole(String type) {
        String typeLower = type.toLowerCase();

        if (factories.containsKey(typeLower)) {
            return (V) factories.get(typeLower).createRole();
        } else {
            return null;
        }
    }

    @Override
    public <V extends Role> V loadRole(String type, String value) {
        String typeLower = type.toLowerCase();

        if (factories.containsKey(typeLower)) {
            return (V) factories.get(typeLower).loadRole(value);
        } else {
            return null;
        }
    }

}
