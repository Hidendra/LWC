package org.getlwc.role;

import org.getlwc.factory.AbstractFactory;
import org.getlwc.model.Protection;

public interface RoleFactory extends AbstractFactory {

    /**
     * Check if an input name matches this definition. e.g for a GroupRoleDefintion, it will check
     * if the name starts with "g:" and return what is after "g:"
     * If you are rolling your own role you should do something similar
     *
     * @param name
     * @return NULL if this definition does not match the role
     */
    public String match(String name);

    /**
     * Create a {@link Role}
     *
     * @param protection
     * @param name
     * @param access
     * @return
     */
    public Role create(Protection protection, String name, Role.Access access);

}
