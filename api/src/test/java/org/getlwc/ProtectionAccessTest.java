package org.getlwc;

import org.getlwc.role.ProtectionRole;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ProtectionAccessTest {

    @Test
    public void testMatch() {
        for (ProtectionRole.Access access : ProtectionRole.Access.values()) {
            assertEquals(access, ProtectionRole.Access.match(access.toString()));
        }
    }

    @Test
    public void testMatchLower() {
        for (ProtectionRole.Access access : ProtectionRole.Access.values()) {
            assertEquals(access, ProtectionRole.Access.match(access.toString().toLowerCase()));
        }
    }

}
