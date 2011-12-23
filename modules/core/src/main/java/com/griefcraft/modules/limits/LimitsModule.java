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

package com.griefcraft.modules.limits;

import com.griefcraft.lwc.LWC;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCProtectionRegisterEvent;
import com.griefcraft.util.config.Configuration;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class LimitsModule extends JavaModule {

    /**
     * The permission node for type: default protections
     */
    public static final String PERMISSION_NODE_GLOBAL = "lwc.limit.";

    /**
     * NODE.BLOCK.limit
     */
    public static final String PERMISSION_NODE_BLOCK = "lwc.limit.block.";

    /**
     * Limits type
     */
    public enum Type {
        /**
         * Undefined type
         */
        NULL,

        /**
         * Combines all blocks together into one
         * This type ignores chest:, furnace:, etc
         */
        DEFAULT,

        /**
         * Limits are defined per-block
         */
        CUSTOM;

        public static Type resolve(String name) {
            for (Type type : values()) {
                if (type.toString().equalsIgnoreCase(name)) {
                    return type;
                }
            }

            return NULL;
        }
    }

    private Configuration configuration = Configuration.load("limits.yml", false);

    /**
     * Integer value that represents unlimited protections
     */
    private int UNLIMITED = Integer.MAX_VALUE;

    /**
     * Set a config value in the configuration
     *
     * @param path
     * @param value
     */
    public void set(String path, Object value) {
        configuration.setProperty(path, value);
    }

    /**
     * Save the configuration
     */
    public boolean save() {
        return configuration.save();
    }

    /**
     * Check if a player has reached their protection limit for a specific block type
     *
     * @param player
     * @param block
     * @return true if the player reached their limit
     */
    public boolean hasReachedLimit(Player player, Block block) {
        if (configuration == null) {
            return false;
        }

        LWC lwc = LWC.getInstance();
        int limit = mapProtectionLimit(player, block.getTypeId());

        // if they're limit is unlimited, how could they get above it? :)
        if (limit == UNLIMITED) {
            return false;
        }

        Type type = Type.resolve(resolveString(player, "type"));
        int protections; // 0 = *

        switch (type) {
            case CUSTOM:
                protections = lwc.getPhysicalDatabase().getProtectionCount(player.getName(), block.getTypeId());
                break;

            case DEFAULT:
                protections = lwc.getPhysicalDatabase().getProtectionCount(player.getName());
                break;

            default:
                throw new UnsupportedOperationException("Limit type " + type.toString() + " is undefined in LimitsModule::hasReachedLimit");
        }

        return protections >= limit;
    }

    /**
     * Search the player's permissions for a permission and return it
     * Depending on this is used, this can become O(scary)
     *
     * @param player
     * @param prefix
     * @return
     */
    public PermissionAttachmentInfo searchPermissions(Player player, String prefix) {
        for (PermissionAttachmentInfo attachment : player.getEffectivePermissions()) {
            String permission = attachment.getPermission();

            // check for the perm node
            if (attachment.getValue() && permission.startsWith(prefix)) {
                // Bingo!
                return attachment;
            }
        }

        return null;
    }

    /**
     * Search permissions for an integer and if found, return it
     *
     * @param player
     * @param prefix
     * @return
     */
    public int searchPermissionsForInteger(Player player, String prefix) {
        PermissionAttachmentInfo attachment = searchPermissions(player, prefix);

        // Not found
        if (attachment == null) {
            return -1;
        }

        // Found
        return Integer.parseInt(attachment.getPermission().substring(prefix.length()));
    }

    /**
     * Get the protection limits for a player
     *
     * @param player
     * @param blockId
     * @return
     */
    public int mapProtectionLimit(Player player, int blockId) {
        if (configuration == null) {
            return 0;
        }

        int limit = -1;
        Type type = Type.resolve(resolveString(player, "type"));

        // Try permissions
        int globalLimit = searchPermissionsForInteger(player, PERMISSION_NODE_GLOBAL);

        // Was it found?
        if (globalLimit >= 0) {
            return globalLimit;
        }

        // Try the block id now
        int blockLimit = searchPermissionsForInteger(player, PERMISSION_NODE_BLOCK + blockId + ".");

        if (blockLimit != -1) {
            return blockLimit;
        }

        switch (type) {

            case DEFAULT:
                limit = resolveInteger(player, "limit");
                break;

            case CUSTOM:
                // first try the block id
                limit = resolveInteger(player, blockId + "");

                // and now try the name
                if (limit == -1 && blockId > 0) {
                    String name = Material.getMaterial(blockId).toString().toLowerCase().replaceAll("block", "");

                    if (name.endsWith("_")) {
                        name = name.substring(0, name.length() - 1);
                    }

                    limit = resolveInteger(player, name);
                }

                // if it's STILL null, fall back
                if (limit == -1) {
                    limit = resolveInteger(player, "limit");
                }
                break;

        }

        return limit == -1 ? UNLIMITED : limit;
    }

    /**
     * Resolve a configuration node for a player. Tries nodes in this order:
     * <pre>
     * players.PLAYERNAME.node
     * groups.GROUPNAME.node
     * master.node
     * </pre>
     *
     * @param player
     * @param node
     * @return
     */
    private String resolveString(Player player, String node) {
        LWC lwc = LWC.getInstance();

        // resolve the limits type
        String value;

        // try the player
        value = configuration.getString("players." + player.getName() + "." + node);

        // try the player's groups
        if (value == null) {
            for (String groupName : lwc.getPermissions().getGroups(player)) {
                if (groupName != null && !groupName.isEmpty() && value == null) {
                    value = map("groups." + groupName + "." + node);
                }
            }
        }

        // if all else fails, use master
        if (value == null) {
            value = map("master." + node);
        }

        return value != null && !value.isEmpty() ? value : null;
    }

    /**
     * Resolve an integer for a player
     *
     * @param player
     * @param node
     * @return
     */
    private int resolveInteger(Player player, String node) {
        LWC lwc = LWC.getInstance();

        // resolve the limits type
        int value = -1;

        // try the player
        String temp = configuration.getString("players." + player.getName() + "." + node);

        if (temp != null && !temp.isEmpty()) {
            value = parseInt(temp);
        }

        // try the player's groups
        if (value == -1) {
            for (String groupName : lwc.getPermissions().getGroups(player)) {
                if (groupName != null && !groupName.isEmpty()) {
                    temp = map("groups." + groupName + "." + node);

                    if (temp != null && !temp.isEmpty()) {
                        int resolved = parseInt(temp);

                        // Is it higher than what we already have?
                        if (resolved > value) {
                            value = resolved;
                        }
                    }
                }
            }
        }

        // if all else fails, use master
        if (value == -1) {
            temp = map("master." + node);

            if (temp != null && !temp.isEmpty()) {
                value = parseInt(temp);
            }
        }

        // Default to 0, not -1 if it is still -1
        return value;
    }

    /**
     * Parse an int
     *
     * @param input
     * @return
     */
    private int parseInt(String input) {
        if (input.equalsIgnoreCase("unlimited")) {
            return UNLIMITED;
        }

        return Integer.parseInt(input);
    }

    /**
     * Get the value from either the path or the master value if it's null
     *
     * @param path
     * @return
     */
    private String map(String path) {
        String value = configuration.getString(path);

        if (value == null) {
            int lastIndex = path.lastIndexOf(".");
            String node = path.substring(lastIndex + 1);

            value = configuration.getString("master." + node);
        }

        if (value == null) {
            value = "";
        }

        return value;
    }

    @Override
    public void onRegisterProtection(LWCProtectionRegisterEvent event) {
        if (event.isCancelled()) {
            return;
        }

        LWC lwc = event.getLWC();
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (hasReachedLimit(player, block)) {
            lwc.sendLocale(player, "protection.exceeded");
            event.setCancelled(true);
        }
    }

}
