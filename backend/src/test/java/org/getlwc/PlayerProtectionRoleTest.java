package org.getlwc;

import org.getlwc.entity.Player;
import org.getlwc.model.Protection;
import org.getlwc.roles.PlayerProtectionRole;
import org.getlwc.roles.PlayerRoleDefinition;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class PlayerProtectionRoleTest {

    @Mock
    private Engine engine;

    private ProtectionRoleManager manager = new SimpleProtectionRoleManager();
    private PlayerRoleDefinition definition;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        definition = new PlayerRoleDefinition(engine);
        manager.registerDefinition(definition);
    }

    @Test
    public void testType() {
        assertEquals(definition, manager.getDefinition(definition.getId()));
    }

    @Test
    public void testMatchName() {
        assertEquals("Hidendra", definition.matchRoleName("Hidendra"));
        assertEquals(null, definition.matchRoleName("g:none"));
        assertEquals(null, definition.matchRoleName("prefix:Hidendra"));
        assertEquals("qWerty1234567890", definition.matchRoleName("qWerty1234567890"));
    }

    @Test
    public void testGetAccess() {
        Protection protection = mock(Protection.class);

        Player player = mock(Player.class);
        when(player.getName()).thenReturn("Hidendra");

        ProtectionRole roleHidendraOwner = manager.matchAndCreateRoleByName(null, "Hidendra", ProtectionRole.Access.OWNER);
        ProtectionRole roleHidendraMember = manager.matchAndCreateRoleByName(null, "Hidendra", ProtectionRole.Access.MEMBER);
        ProtectionRole roleNotch = manager.matchAndCreateRoleByName(null, "Notch", ProtectionRole.Access.OWNER);

        assertEquals(ProtectionRole.Access.OWNER, roleHidendraOwner.getAccess(protection, player));
        verify(player, times(1)).getName();
        assertEquals(ProtectionRole.Access.MEMBER, roleHidendraMember.getAccess(protection, player));
        verify(player, times(2)).getName();
        assertEquals(ProtectionRole.Access.NONE, roleNotch.getAccess(protection, player));
        verify(player, times(3)).getName();
    }

    @Test
    public void testMatch() {
        String name = "TestingIsGreat";
        ProtectionRole.Access access = ProtectionRole.Access.MANAGER;

        ProtectionRole role = manager.matchAndCreateRoleByName(null, name, access);
        assertNotNull(role);
        assertEquals(definition.getId(), role.getType());
        assertEquals(name, role.getName());
        assertEquals(access, role.getAccess());
    }

}
