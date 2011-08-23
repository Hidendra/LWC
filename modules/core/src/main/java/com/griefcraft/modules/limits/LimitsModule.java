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

package com.griefcraft.modules.limits;

import com.griefcraft.lwc.LWC;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.scripting.event.LWCProtectionRegisterEvent;
import com.griefcraft.util.Colors;
import com.griefcraft.util.StringUtils;
import com.griefcraft.util.config.Configuration;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LimitsModule extends JavaModule {

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

    private Configuration configuration = Configuration.load("limits.yml");

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
        LWC lwc = LWC.getInstance();
        int limit = mapProtectionLimit(player, block.getTypeId());

        // if they're limit is unlimited, how could they get above it? :)
        if (limit == UNLIMITED) {
            return false;
        }

        Type type = Type.resolve(resolveValue(player, "type"));
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
     * Get the protection limits for a player
     *
     * @param player
     * @param blockId
     * @return
     */
    private int mapProtectionLimit(Player player, int blockId) {
        String limit = null;
        Type type = Type.resolve(resolveValue(player, "type"));

        if (type == Type.DEFAULT) {
            limit = resolveValue(player, "limit");
        } else if (type == Type.CUSTOM) {
            // first try the block id
            limit = resolveValue(player, blockId + "");

            // and now try the name
            if (limit == null && blockId > 0) {
                String name = Material.getMaterial(blockId).toString().toLowerCase().replaceAll("block", "");

                if (name.endsWith("_")) {
                    name = name.substring(0, name.length() - 1);
                }

                limit = resolveValue(player, name);
            }

            // if it's STILL null, fall back
            if (limit == null) {
                limit = resolveValue(player, "limit");
            }
        }

        if (limit == null || limit.equalsIgnoreCase("unlimited")) {
            return UNLIMITED;
        }

        return !limit.isEmpty() ? Integer.parseInt(limit) : UNLIMITED;
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
    private String resolveValue(Player player, String node) {
        LWC lwc = LWC.getInstance();

        // check if we have permissions
        boolean hasPermissions = lwc.getPermissions().isActive();

        // resolve the limits type
        String value;

        // try the player
        value = configuration.getString("players." + player.getName() + "." + node);

        // try the player's groups
        if (value == null && hasPermissions) {
            for(String groupName : lwc.getPermissions().getGroups(player)) {
                if (groupName != null && !groupName.isEmpty()) {
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
        if(event.isCancelled()) {
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

    @Override
    public void onCommand(LWCCommandEvent event) {
        if(event.isCancelled()) {
            return;
        }

        if (!event.hasFlag("limits")) {
            return;
        }

        LWC lwc = event.getLWC();
        CommandSender sender = event.getSender();
        String[] args = event.getArgs();
        event.setCancelled(true);

        if (args.length == 0 && !(sender instanceof Player)) {
            sender.sendMessage(Colors.Red + "Unsupported");
            return;
        }

        String playerName;

        if (args.length == 0) {
            playerName = ((Player) sender).getName();
        } else {
            if (lwc.isAdmin(sender)) {
                playerName = args[0];
            } else {
                lwc.sendLocale(sender, "protection.accessdenied");
                return;
            }
        }

        Player player = lwc.getPlugin().getServer().getPlayer(playerName);

        if (player == null) {
            return;
        }

        Type type = Type.resolve(resolveValue(player, "type"));
        int limit = mapProtectionLimit(player, 0);
        String limitShow = limit + "";
        int current = lwc.getPhysicalDatabase().getProtectionCount(playerName);

        if (limit == UNLIMITED) {
            limitShow = "Unlimited";
        }

        String currColour = Colors.Green;

        if (limit == current) {
            currColour = Colors.Red;
        } else if (current > (limit / 2)) {
            currColour = Colors.Yellow;
        }

        lwc.sendLocale(sender, "protection.limits", "type", StringUtils.capitalizeFirstLetter(type.toString()), "player", playerName, "limit", limitShow, "protected", (currColour + current));
        return;
    }

}
