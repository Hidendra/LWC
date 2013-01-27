/*
 * Copyright (c) 2011-2013 Tyler Blair
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and contributors and should not be interpreted as representing official policies,
 * either expressed or implied, of anybody else.
 */

package org.getlwc;

import java.util.EnumSet;

/**
 * Access levels for protections. ordinal values are used here meaning they must remain in a constant order. As well,
 * the enum values are ranked in power of ascending order meaning ProtectionAccess(4) has more power than
 * ProtectionAccess(1) will. This also implies that the initial implementation is complete and that adding
 * any more access levels would be a pain.
 * <p/>
 * As well, the only exception to these rules is EXPLICIT_DENY which will immediately deny access to the
 * protection. This will not always be used but may be useful in some cases.
 */
public enum ProtectionAccess {

    /**
     * Immediately reject access to the protection.
     */
    EXPLICIT_DENY,

    /**
     * User has NO access to the protection
     */
    NONE,

    /**
     * The user can view the protection but not modify it in any way. The implementation of this depends
     * on the mod and if the mod does not support preventing the inventory from being modified somehow
     * then access will just be blocked.
     */
    GUEST,

    /**
     * The user can only deposit into the protection
     */
    DEPOSITONLY,

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

    /**
     * Access levels that normal players can set
     */
    public final static EnumSet<ProtectionAccess> USABLE_ACCESS_LEVELS = EnumSet.range(NONE, OWNER);

    /**
     * Match a {@link ProtectionAccess} given a name.
     *
     * @param name
     * @return NULL if no {@link ProtectionAccess} is matched
     */
    public static ProtectionAccess match(String name) {
        for (ProtectionAccess access : values()) {
            if (access.toString().equalsIgnoreCase(name)) {
                return access;
            }
        }

        return null;
    }

}
