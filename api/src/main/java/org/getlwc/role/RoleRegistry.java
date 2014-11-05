package org.getlwc.role;

public interface RoleRegistry {

    /**
     * Registers a new role type for use
     *
     * @param type
     * @param roleClass
     */
    public void registerRoleType(String type, Class<? extends Role> roleClass);

    /**
     * Creates a new role of the given type
     *
     * @param type
     * @return
     */
    public <V extends Role> V createRole(String type);

    /**
     * Loads a role of the given type with the given value
     *
     * @param type
     * @param value
     * @return
     */
    public <V extends Role> V loadRole(String type, String value);

}
