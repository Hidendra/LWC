package com.griefcraft.internal.protections;

import com.griefcraft.ProtectionAccess;
import com.griefcraft.ProtectionType;
import com.griefcraft.entity.Player;
import com.griefcraft.model.Protection;

public class PublicProtectionType implements ProtectionType {

    public int getId() {
        return 0; // adapted from LWC v4
    }

    public String getName() {
        return "Public";
    }

    public ProtectionAccess getAccess(Protection protection, Player player) {
        if (protection.isOwner(player)) {
            return ProtectionAccess.ALLOW; // TODO will be broken up into OWNER / MANAGER / USER / DENY access levels
        }

        return ProtectionAccess.ALLOW;
    }

}
