package org.getlwc;

import org.getlwc.entity.Player;
import org.getlwc.factory.AbstractFactoryRegistry;
import org.getlwc.model.Protection;
import org.getlwc.role.PlayerRoleFactory;
import org.getlwc.role.Role;
import org.getlwc.role.RoleFactory;
import org.getlwc.role.RoleFactoryRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class RoleFactoryRegistryTest {

    @Mock
    private Engine engine;

    @Mock
    private ServerLayer layer;

    private AbstractFactoryRegistry<RoleFactory> registry = new RoleFactoryRegistry();
    private RoleFactory playerFactory;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        playerFactory = new PlayerRoleFactory(engine);
        registry.register(playerFactory);
        when(engine.getServerLayer()).thenReturn(layer);
    }

    @Test
    public void testType() {
        assertEquals(playerFactory, registry.get(playerFactory.getName()));
        assertEquals(playerFactory, registry.get(playerFactory.getName().toUpperCase()));
        assertEquals(playerFactory, registry.get(playerFactory.getName().toLowerCase()));
    }

    @Test
    public void testMatchName() {
        assertEquals("Hidendra", playerFactory.match("Hidendra"));
        assertEquals(null, playerFactory.match("g:none"));
        assertEquals(null, playerFactory.match("prefix:Hidendra"));
        assertEquals("qWerty1234567890", playerFactory.match("qWerty1234567890"));
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

        RoleFactory factoryHidendra = registry.find("Hidendra");
        RoleFactory factoryNotch = registry.find("Notch");

        Role roleHidendraOwner = factoryHidendra.create(null, "Hidendra", Role.Access.OWNER);
        Role roleHidendraMember = factoryHidendra.create(null, "Hidendra", Role.Access.MEMBER);
        Role roleNotch = factoryNotch.create(null, "Notch", Role.Access.OWNER);

        assertEquals(Role.Access.OWNER, roleHidendraOwner.getAccess(protection, player));
        verify(player, times(3)).getUUID();
        assertEquals(Role.Access.MEMBER, roleHidendraMember.getAccess(protection, player));
        verify(player, times(4)).getUUID();
        assertEquals(Role.Access.NONE, roleNotch.getAccess(protection, player));
        verify(player, times(5)).getUUID();
    }

    @Test
    public void testMatch() {
        String name = "TestingIsGreat";
        Role.Access access = Role.Access.MANAGER;

        RoleFactory factory = registry.find(name);
        Role role = factory.create(null, name, access);
        assertNotNull(role);
        assertEquals(name, role.getName());
        assertEquals(access, role.getAccess());
    }

}
