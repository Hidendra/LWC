/*
 * Copyright 2011 Tyler Blair. All rights reserved.
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

package com.griefcraft.model;

import com.griefcraft.util.Colors;
import com.griefcraft.util.StringUtil;
import com.griefcraft.util.UUIDRegistry;
import org.json.simple.JSONObject;

public class Permission {

    /**
     * The access level
     * The ordering of this enum <b>MUST NOT</b> change as ordinal values are used internally.
     */
    public enum Access {

        /**
         * The player has no access
         */
        NONE,

        /**
         * The player has rights that of a regular player
         */
        PLAYER,

        /**
         * The player has admin rights
         */
        ADMIN;

        @Override
        public String toString() {
            return StringUtil.capitalizeFirstLetter(super.toString());
        }
    }

    /**
     * The type this permission applies to.
     * The ordering of this enum <b>MUST NOT</b> change as ordinal values are used internally.
     */
    public enum Type {
        /**
         * Applies to a specific group of players
         */
        GROUP,

        /**
         * Applies to a specific player
         */
        PLAYER,

        /**
         * Unused / reserved, has been used before
         */
        RESERVED,

        /**
         * Applies to citizens of a Towny town
         */
        TOWN,

        /**
         * Allows a specific item (such as a key) to open the protection when interacted with in hand
         */
        ITEM,

        /**
         * Applies to members of a WorldGuard region
         */
        REGION;

        @Override
        public String toString() {
            return StringUtil.capitalizeFirstLetter(super.toString());
        }
    }

    /**
     * The entity this applies to
     */
    private String name;

    /**
     * The type of access used for the permission
     */
    private Type type;

    /**
     * The access the permission has to the protection
     */
    private Access access = Access.PLAYER;

    /**
     * If the permission is not synchronized to the database
     */
    private boolean isVolatile = false;

    public Permission() {
    }

    public Permission(String name) {
        this.name = name;
    }

    public Permission(String name, Type type) {
        this(name);
        this.type = type;
    }

    public Permission(String name, Type type, Access access) {
        this(name, type);
        this.access = access;
    }

    /**
     * Encode the Permission object to a JSONObject
     *
     * @return
     */
    public JSONObject encodeToJSON() {
        JSONObject object = new JSONObject();

        object.put("name", name);
        object.put("type", getType().ordinal());
        object.put("rights", getAccess().ordinal());

        return object;
    }

    /**
     * Decode a JSONObject into a Permission object
     *
     * @param node
     * @return
     */
    public static Permission decodeJSON(JSONObject node) {
        Permission permission = new Permission();

        Access access = Access.values()[((Long) node.get("rights")).intValue()];
        if (access.ordinal() == 0) {
            access = Access.PLAYER;
        }

        // The values are stored as longs internally, despite us passing an int
        permission.setName((String) node.get("name"));
        permission.setType(Type.values()[((Long) node.get("type")).intValue()]);
        permission.setAccess(access);

        return permission;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(Colors.Yellow);
        if (type == Type.PLAYER) {
            builder.append(UUIDRegistry.formatPlayerName(getName()));
        } else {
            builder.append(getName());
        }
        builder.append(Colors.White);
        builder.append(" (");
        builder.append(Colors.Green);
        builder.append(getType());
        builder.append(Colors.White);
        builder.append(") ");

        if (getAccess() == Access.ADMIN) {
        builder.append(Colors.White);
            builder.append("(");
            builder.append(Colors.Red);
            builder.append("ADMIN");
        builder.append(Colors.White);
            builder.append(")");
        }
        return builder.toString();
        // return String.format("Permission = { protection=%d name=%s rights=%d type=%s }", protectionId, name, rights, typeToString(rights));
    }

    public String getName() {
        return name;
    }

    public Access getAccess() {
        return access;
    }

    public Type getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAccess(Access access) {
        this.access = access;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setVolatile(boolean isVolatile) {
        this.isVolatile = isVolatile;
    }

    public boolean isVolatile() {
        return isVolatile;
    }

}
