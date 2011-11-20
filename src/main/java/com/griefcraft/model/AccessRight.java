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
import org.json.simple.JSONObject;

public class AccessRight {

    /**
     * The player has no access to the protection
     */
    public static final int RIGHT_NOACCESS = -1;

    /**
     * The player has player rights to the protection
     */
    public static final int RIGHT_PLAYER = 0;

    /**
     * The player has admin rights to the protection
     */
    public static final int RIGHT_ADMIN = 1;

    /**
     * Access right is for a group
     */
    public static final int GROUP = 0;

    /**
     * Access right is for a player
     */
    public static final int PLAYER = 1;

    /**
     * Used in conjuction with Lists
     */
    public static final int LIST = 2;

    /**
     * Used in conjunction with Towny
     */
    public static final int TOWN = 3;

    /**
     * Not saved to the database
     */
    public static final int TEMPORARY = 3;

    /**
     * Used in conjunction with /lwc -O
     */
    public static final int RESULTS_PER_PAGE = 15;

    private String name;

    private int protectionId;

    private int rights;
    private int type;

    /**
     * Encode the Access Right to a JSONObject
     *
     * @return
     */
    public JSONObject encodeToJSON() {
        JSONObject object = new JSONObject();

        // object.put("protection", protectionId);
        object.put("type", type);
        object.put("name", name);
        object.put("rights", rights);

        return object;
    }

    /**
     * Decode a JSONObject into an Access Right
     *
     * @param node
     * @return
     */
    public static AccessRight decodeJSON(JSONObject node) {
        AccessRight right = new AccessRight();

        // The values are stored as longs internally, despite us passing an int
        // right.setProtectionId(((Long) node.get("protection")).intValue());
        right.setType(((Long) node.get("type")).intValue());
        right.setName((String) node.get("name"));
        right.setRights(((Long) node.get("rights")).intValue());

        return right;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(Colors.LightBlue);
        builder.append(getName());
        builder.append(Colors.Blue);
        builder.append(" (");
        builder.append(AccessRight.typeToString(getType()));
        builder.append(") ");

        if (getRights() == 1) {
            builder.append(Colors.LightBlue);
            builder.append("(");
            builder.append(Colors.Red);
            builder.append("ADMIN");
            builder.append(Colors.LightBlue);
            builder.append(")");
        }
        return builder.toString();
        // return String.format("AccessRight = { protection=%d name=%s rights=%d type=%s }", protectionId, name, rights, typeToString(rights));
    }

    public String getName() {
        return name;
    }

    public int getProtectionId() {
        return protectionId;
    }

    public int getRights() {
        return rights;
    }

    public int getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProtectionId(int protectionId) {
        this.protectionId = protectionId;
    }

    public void setRights(int rights) {
        this.rights = rights;
    }

    public void setType(int type) {
        this.type = type;
    }

    public static String typeToString(int type) {
        if (type == GROUP) {
            return "Group";
        } else if (type == PLAYER) {
            return "Player";
        } else if (type == LIST) {
            return "List";
        } else if (type == TOWN) {
            return "Towny";
        } else if (type == TEMPORARY) {
            return "Temporary";
        }

        return "Unknown";
    }

}
