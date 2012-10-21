package com.griefcraft;

import com.griefcraft.model.RoleDefinition;

import java.util.HashMap;
import java.util.Map;

public class SimpleRoleManager implements RoleManager {

    /**
     * The definitions that have been registered
     */
    private Map<Integer, RoleDefinition> definitions = new HashMap<Integer, RoleDefinition>();

    public void registerDefinition(RoleDefinition definition) {
        if (definitions.containsKey(definition.getId())) {
            // TODO our own exception
            throw new UnsupportedOperationException("Role definition already exists for the id " + definition.getId());
        }

        definitions.put(definition.getId(), definition);
    }

    public RoleDefinition getDefinition(int id) {
        return definitions.get(id);
    }
}
