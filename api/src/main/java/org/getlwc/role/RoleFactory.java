package org.getlwc.role;

public interface RoleFactory {

    /**
     * Creates a new role
     *
     * @return
     */
    public Role createRole();

    /**
     * Loads a role from the given value
     *
     * @param value
     * @return
     */
    public Role loadRole(String value);

}
