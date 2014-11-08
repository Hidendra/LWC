package org.getlwc.role;

public interface RoleFactory<T extends Role> {

    /**
     * Creates a role from the given value
     *
     * @param value
     * @return
     */
    public T createFromValue(String value) throws RoleCreationException;

}
