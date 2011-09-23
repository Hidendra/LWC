/**
 * This file is part of LWC (https://github.com/Hidendra/LWC)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.griefcraft.model;

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
        return String.format("AccessRight = { protection=%d name=%s rights=%d type=%s }", protectionId, name, rights, typeToString(rights));
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
