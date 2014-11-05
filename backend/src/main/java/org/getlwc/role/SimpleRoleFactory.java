package org.getlwc.role;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class SimpleRoleFactory implements RoleFactory {

    /**
     * The class for the role. It will be used to create
     * and load roles.
     */
    private Class<? extends Role> clazz;

    public SimpleRoleFactory(Class<? extends Role> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Role createRole() {
        return privateCreateRole();
    }

    @Override
    public Role loadRole(String value) {
        Role role = privateCreateRole();

        if (role != null) {
            role.deserialize(value);
        }

        return role;
    }

    /**
     * Creates a new role.
     *
     * @return
     */
    private Role privateCreateRole() {
        try {
            Constructor ctor = clazz.getDeclaredConstructor();

            return (Role) ctor.newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

}
