package com.griefcraft;

/**
 * Access levels for protections. ordinal values are used here meaning they must remain in a constant order. As well,
 * the enum values are ranked in power of ascending order meaning ProtectionAccess(4) has more power than
 * ProtectionAccess(1) will. This also implies that the initial implementation is complete and that adding
 * any more access levels would be a pain.
 */
public enum ProtectionAccess {

    /**
     * User has NO access to the protection
     */
    NONE,

    /**
     * User has MEMBER access to the protection meaning they can open it and access it, but not admin
     * it in any way (add others, etc)
     */
    MEMBER,

    /**
     * User can modify the protection (add and remove members) but not add or remove other managers.
     */
    MANAGER,

    /**
     * User has the same access as the user who created the protection. They can remove the protection,
     * add or remove ANY level to the protection (i.e other owners) but they cannot remove themself.
     */
    OWNER;

}
