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

import com.griefcraft.util.StringUtil;
import org.json.simple.JSONObject;

public class Flag {

    /**
     * The ordering of this enum <b>MUST NOT</b> change. The ordinal value is stored internally.
     * However, the name of a flag may freely change at any time.
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
        EXEMPTION(true),

        /**
         * The door will automatically close after the time configured in plugins/LWC/doors.yml
         */
        AUTOCLOSE,

        /**
         * Allows explosions to blow a protection up
         */
        ALLOWEXPLOSIONS,

        /**
         * Controls whether or not hoppers can be used on a protection
         */
        HOPPER;

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
        } catch (NumberFormatException e) {
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
        return StringUtil.capitalizeFirstLetter(type.toString());
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
