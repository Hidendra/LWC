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

import com.griefcraft.util.StringUtils;
import org.json.simple.JSONObject;

public class Flag {

    /**
     * The ordering of this enum <b>MUST NOT</b> change. The ordinal value is stored internally
     * in LWC. However, the name of a flag may freely change at any time.
     */
    public enum Type {

        /**
         * Redstone use will be disabled on the protection if protections.denyRedstone = false;
         * however if denyRedstone = true, this flag will instead enable redstone on the protection!
         */
        REDSTONE,

        /**
         * Attracts dropped items within a certain radius into the protection's inventory
         */
        MAGNET,

        /**
         * Protection is exempt from being auto removed from LWC - e.g /lwc admin expire -remove 2 weeks
         */
        EXEMPTION(true);

        Type() {
            this(false);
        }

        Type(boolean restricted) {
            this.restricted = restricted;
        }

        /**
         * If the flag is restricted to only LWC admins
         */
        private boolean restricted;

        /**
         * @return true if the flag should only be usable by LWC admins
         */
        public boolean isRestricted() {
            return restricted;
        }

    }

    /**
     * The flag type
     */
    private Type type;

    /**
     * Flag data
     */
    private final JSONObject data = new JSONObject();

    public Flag(Type type) {
        this.type = type;
        data.put("id", type.ordinal());
    }

    /**
     * Decode JSON data for a flag
     *
     * @param node
     * @return
     */
    public static Flag decodeJSON(JSONObject node) {
        if (node == null) {
            return null;
        }

        // decode the type
        int ordinal = -1;

        try {
            ordinal = Integer.parseInt(node.get("id").toString());
        } catch(NumberFormatException e) {
            return null;
        }

        // Still not valid..
        if (ordinal == -1) {
            return null;
        }

        // let's do a range check
        Type[] values = Type.values();

        if (ordinal > values.length) {
            return null;
        }

        // good good
        Type type = values[ordinal];

        // create the Flag and hand over the data we have
        Flag flag = new Flag(type);
        flag.getData().putAll(node);

        return flag;
    }

    @Override
    public String toString() {
        return StringUtils.capitalizeFirstLetter(type.toString());
    }

    /**
     * @return
     */
    public Type getType() {
        return type;
    }

    /**
     * @return
     */
    public JSONObject getData() {
        return data;
    }

}
