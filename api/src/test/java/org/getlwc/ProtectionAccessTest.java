package org.getlwc;

import org.getlwc.role.Role;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ProtectionAccessTest {

    @Test
    public void testMatch() {
        for (Role.Access access : Role.Access.values()) {
            assertEquals(access, Role.Access.match(access.toString()));
        }
    }

    @Test
    public void testMatchLower() {
        for (Role.Access access : Role.Access.values()) {
            assertEquals(access, Role.Access.match(access.toString().toLowerCase()));
        }
    }

}
