package org.getlwc;

import org.getlwc.factory.AbstractFactoryRegistry;
import org.getlwc.model.Protection;
import org.getlwc.role.ProtectionRole;
import org.getlwc.role.Role;
import org.getlwc.role.RoleFactory;
import org.getlwc.role.RoleFactoryRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        ProtectionRole.Access access = ProtectionRole.Access.MANAGER;

        registry.register(testFactory);

        RoleFactory factory = registry.find(name);
        ProtectionRole role = factory.create(null, name, access);
        assertNotNull(role);
        assertEquals(name, role.getName());
        assertEquals(access, role.getAccess());
    }

    RoleFactory testFactory = new RoleFactory() {
        public String match(String name) {
            return name.startsWith("Test") ? name : null;
        }

        public Role create(String name) {
            Role role = mock(Role.class);
            when(role.getName()).thenReturn(name);
            return role;
        }

        public ProtectionRole create(Protection protection, String name, ProtectionRole.Access access) {
            ProtectionRole role = mock(ProtectionRole.class);
            when(role.getName()).thenReturn(name);
            when(role.getAccess()).thenReturn(access);
            return role;
        }

        public String getName() {
            return "lwc:factoryTest";
        }
    };

}
