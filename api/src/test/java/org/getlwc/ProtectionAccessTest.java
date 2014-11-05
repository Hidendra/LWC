package org.getlwc;

import org.getlwc.model.Protection;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ProtectionAccessTest {

    @Test
    public void testMatch() {
        for (Protection.Access access : Protection.Access.values()) {
            assertEquals(access, Protection.Access.fromString(access.toString()));
        }
    }

    @Test
    public void testMatchLower() {
        for (Protection.Access access : Protection.Access.values()) {
            assertEquals(access, Protection.Access.fromString(access.toString().toLowerCase()));
        }
    }

}
