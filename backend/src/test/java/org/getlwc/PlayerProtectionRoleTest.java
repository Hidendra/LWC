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

    @Mock
    private ServerLayer layer;

    private ProtectionRoleManager manager = new SimpleProtectionRoleManager();
    private PlayerRoleDefinition definition;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        definition = new PlayerRoleDefinition(engine);
        manager.registerDefinition(definition);
        when(engine.getServerLayer()).thenReturn(layer);
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
        when(player.getUUID()).thenReturn("VuXQ8cu0u4VB5Z3vg5wid9dQTLJr8XW0");
        when(player.getName()).thenReturn("Hidendra");

        when(layer.getPlayer("VuXQ8cu0u4VB5Z3vg5wid9dQTLJr8XW0")).thenReturn(player);
        when(layer.getPlayer("Hidendra")).thenReturn(player);
        when(layer.getPlayer("Notch")).thenReturn(null);

        ProtectionRole roleHidendraOwner = manager.matchAndCreateRoleByName(null, "Hidendra", ProtectionRole.Access.OWNER);
        ProtectionRole roleHidendraMember = manager.matchAndCreateRoleByName(null, "Hidendra", ProtectionRole.Access.MEMBER);
        ProtectionRole roleNotch = manager.matchAndCreateRoleByName(null, "Notch", ProtectionRole.Access.OWNER);

        assertEquals(ProtectionRole.Access.OWNER, roleHidendraOwner.getAccess(protection, player));
        verify(player, times(3)).getUUID();
        assertEquals(ProtectionRole.Access.MEMBER, roleHidendraMember.getAccess(protection, player));
        verify(player, times(4)).getUUID();
        assertEquals(ProtectionRole.Access.NONE, roleNotch.getAccess(protection, player));
        verify(player, times(5)).getUUID();
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
