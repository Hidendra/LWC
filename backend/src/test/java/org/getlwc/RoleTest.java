package org.getlwc;

import org.getlwc.factory.AbstractFactoryRegistry;
import org.getlwc.model.Protection;
import org.getlwc.role.Role;
import org.getlwc.role.RoleFactory;
import org.getlwc.role.RoleFactoryRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class RoleTest {

    private AbstractFactoryRegistry<RoleFactory> registry = new RoleFactoryRegistry();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRegisterDefinition() {
        registry.register(testFactory);
        assertEquals(testFactory, registry.get("lwc:factoryTest"));
    }

    @Test
    public void testMatch() {
        String name = "TestingIsGreat";
        Role.Access access = Role.Access.MANAGER;

        registry.register(testFactory);

        RoleFactory factory = registry.find(name);
        Role role = factory.create(null, name, access);
        assertNotNull(role);
        assertEquals(name, role.getName());
        assertEquals(access, role.getAccess());
    }

    RoleFactory testFactory = new RoleFactory() {
        public String match(String name) {
            return name.startsWith("Test") ? name : null;
        }

        public Role create(Protection protection, String name, Role.Access access) {
            Role role = mock(Role.class);
            when(role.getName()).thenReturn(name);
            when(role.getAccess()).thenReturn(access);
            return role;
        }

        public String getName() {
            return "lwc:factoryTest";
        }
    };

}
