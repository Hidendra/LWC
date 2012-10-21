package com.griefcraft;

import com.griefcraft.model.Role;
import com.griefcraft.model.RoleDefinition;

public interface RoleManager {

    /**
     * Register a {@link RoleDefinition} into the system to be usable by protections
     * @param definition
     */
    public void registerDefinition(RoleDefinition definition);

    /**
     * Get the {@link Role} for the given role id. If no role is found, return NULL
     * @param id
     * @return the {@link Role} object mapped to the id given. If none is found, NULL is returned
     */
    public RoleDefinition getDefinition(int id);

}
