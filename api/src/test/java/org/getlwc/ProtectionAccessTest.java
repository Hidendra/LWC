package org.getlwc;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ProtectionAccessTest {

    @Test
    public void testMatch() {
        for (ProtectionAccess access : ProtectionAccess.values()) {
            assertEquals(access, ProtectionAccess.match(access.toString()));
        }
    }

    @Test
    public void testMatchLower() {
        for (ProtectionAccess access : ProtectionAccess.values()) {
            assertEquals(access, ProtectionAccess.match(access.toString().toLowerCase()));
        }
    }

}
