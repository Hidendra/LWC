package org.getlwc;

import org.getlwc.model.Protection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class SimpleProtectionRoleManagerTest {

    private ProtectionRoleManager manager = new SimpleProtectionRoleManager();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRegisterDefinition() {
        manager.registerDefinition(testDefinition);
        assertEquals(testDefinition, manager.getDefinition(10));
    }

    @Test
    public void testMatch() {
        String name = "TestingIsGreat";
        ProtectionRole.Access access = ProtectionRole.Access.MANAGER;

        manager.registerDefinition(testDefinition);

        ProtectionRole role = manager.matchAndCreateRoleByName(null, name, access);
        assertNotNull(role);
        assertEquals(name, role.getName());
        assertEquals(access, role.getAccess());
    }

    RoleDefinition testDefinition = new RoleDefinition() {
        public int getId() {
            return 10;
        }

        public String matchRoleName(String name) {
            return name.startsWith("Test") ? name : null;
        }

        public ProtectionRole createRole(Protection protection, String roleName, ProtectionRole.Access roleAccess) {
            ProtectionRole role = mock(ProtectionRole.class);
            when(role.getName()).thenReturn(roleName);
            when(role.getAccess()).thenReturn(roleAccess);
            return role;
        }
    };

}
