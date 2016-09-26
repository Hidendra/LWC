package com.griefcraft.util;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class MojangAccountToolsTest {

    @Test
    public void testFetchByName() {
        MojangProfile profile = MojangAccountTools.fetchProfile("Hidendra");
        assertNotNull(profile);
        assertEquals("Hidendra", profile.getName());
        assertEquals("86553713-3d20-4923-9fd6-587aa7ed7c16", profile.getUUID().toString());
    }

    @Test
    public void testFetchByUUID() {
        MojangProfile profile = MojangAccountTools.fetchProfile(UUID.fromString("86553713-3d20-4923-9fd6-587aa7ed7c16"));
        assertNotNull(profile);
        assertEquals("Hidendra", profile.getName());
        assertEquals("86553713-3d20-4923-9fd6-587aa7ed7c16", profile.getUUID().toString());
    }

}
