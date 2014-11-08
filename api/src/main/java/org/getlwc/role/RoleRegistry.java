package org.getlwc.role;

public interface RoleRegistry {

    /**
     * Registers a new role loader for use
     *
     * @param type
     * @param factory
     */
    public void registerRoleLoader(String type, RoleFactory<?> factory);

    /**
     * Loads a role of the given type with the given value
     *
     * @param type
     * @param value
     * @return
     */
    public <V extends Role> V loadRole(String type, String value) throws RoleCreationException;

}
