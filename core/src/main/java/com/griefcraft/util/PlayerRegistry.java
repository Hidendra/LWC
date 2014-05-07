package com.griefcraft.util;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.PlayerInfo;
import com.griefcraft.sql.PhysDB;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerRegistry {

    /**
     * Temporal caches
     */
    private static final Map<String, PlayerInfo> nameToPlayerInfoCache = new HashMap<String, PlayerInfo>();
    private static final Map<UUID, PlayerInfo> uuidToPlayerInfoCache = new HashMap<UUID, PlayerInfo>();

    /**
     * Update the cache with a new UUID/name pair
     *
     * @param uuid
     * @param name
     */
    public static void updateCache(UUID uuid, String name) {
        PhysDB database = LWC.getInstance().getPhysicalDatabase();

        if (database != null) {
            PlayerInfo playerInfo = database.getOrCreatePlayerInfo(uuid, name);
            updateCache(playerInfo);
        }
    }

    /**
     * Update the cache for the given player info object
     *
     * @param playerInfo
     */
    private static void updateCache(PlayerInfo playerInfo) {
        if (playerInfo != null) {
            if (playerInfo.getName() != null && !playerInfo.getName().isEmpty()) {
                nameToPlayerInfoCache.put(playerInfo.getName().toLowerCase(), playerInfo);
            }

            uuidToPlayerInfoCache.put(playerInfo.getUUID(), playerInfo);
        }
    }

    /**
     * Check if a uuid in string form is a valid Java UUID
     *
     * @param uuid
     * @return true if the string is a valid UUID
     */
    public static boolean isValidUUID(String uuid) {
        return uuid.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    /**
     * Get a player's info by their internal id
     *
     * @param id
     * @return
     */
    public static PlayerInfo getPlayerInfo(int id) {
        PlayerInfo found = LWC.getInstance().getPhysicalDatabase().getPlayerInfo(id);
        updateCache(found);
        return found;
    }

    /**
     * Get a player by their identifier; the identifier can be either their name or their UUID
     *
     * @param ident
     * @return
     */
    public static PlayerInfo getPlayerInfo(String ident) {
        try {
            int id = Integer.parseInt(ident);
            return getPlayerInfo(id);
        } catch (NumberFormatException e) {
            if (isValidUUID(ident)) {
                return getPlayerInfo(UUID.fromString(ident));
            } else {
                return getPlayerInfoByName(ident);
            }
        }
    }

    /**
     * Get a player by their UUID
     *
     * @param uuid
     * @return
     */
    public static PlayerInfo getPlayerInfo(UUID uuid) {
        if (uuid == null) {
            return null;
        }

        if (uuidToPlayerInfoCache.containsKey(uuid)) {
            return uuidToPlayerInfoCache.get(uuid);
        }

        PhysDB database = LWC.getInstance().getPhysicalDatabase();

        // First way: if they're on the server already
        Player player = Bukkit.getPlayer(uuid);

        if (player != null && database != null) {
            PlayerInfo found = database.getOrCreatePlayerInfo(uuid, player.getName());
            updateCache(found);
            return found;
        }

        // Second way: if they have been on the server before
        OfflinePlayer offlinePlayer = null;

        try {
            offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        } catch(Exception e) {
            // this seems to have thrown an exception before in CraftBukkit code ... :-(
        }

        if (offlinePlayer != null && offlinePlayer.getName() != null && database != null) {
            PlayerInfo found = database.getOrCreatePlayerInfo(uuid, offlinePlayer.getName());
            updateCache(found);
            return found;
        }

        // Third way: database

        if (database != null) {
            PlayerInfo playerInfo = database.getPlayerInfo(uuid);

            if (playerInfo != null) {
                updateCache(playerInfo);
                return playerInfo;
            }
        }

        // Fourth way: use the web API
        try {
            MojangProfile profile = MojangAccountTools.fetchProfile(uuid);

            if (profile != null && database != null) {
                PlayerInfo found = database.getOrCreatePlayerInfo(profile.getUUID(), profile.getName());
                updateCache(found);
                return found;
            }
        } catch (Exception e) {
            return null;
        }

        // last resort: save the uuid w/ blank name and check another time
        PlayerInfo playerInfo = database.getOrCreatePlayerInfo(uuid, null);
        updateCache(playerInfo);
        return playerInfo;
    }

    /**
     * Get a player by their name. It might match more than one player, so in that case only one will be returned.
     *
     * @param name
     * @return
     */
    public static PlayerInfo getPlayerInfoByName(String name) {
        String nameLower = name.toLowerCase();

        try {
            if (nameToPlayerInfoCache.containsKey(nameLower)) {
                return nameToPlayerInfoCache.get(nameLower);
            }

            // Main way of translating UUIDs: using the database
            PhysDB database = LWC.getInstance().getPhysicalDatabase();

            if (database != null) {
                List<PlayerInfo> matches = database.getPlayerInfo(name);

                // TODO check if online mode / online-mode? Online mode seems to use v4 UUID, while
                // offline mode seems to use v3 UUID
                if (matches.size() > 0) {
                    PlayerInfo found = matches.get(0);
                    updateCache(found);
                    return found;
                }
            }

            if (Bukkit.getOnlineMode()) {
                MojangProfile profile = MojangAccountTools.fetchProfile(nameLower);

                // The returned name is the exact casing; so we need to look for it
                // in the case-insensitive version
                if (profile != null && database != null) {
                    PlayerInfo found = database.getOrCreatePlayerInfo(profile.getUUID(), profile.getName());
                    updateCache(found);
                    return found;
                }
            } else {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);

                if (offlinePlayer != null && offlinePlayer.getUniqueId() != null) {
                    if (offlinePlayer.getName() != null) {
                        name = offlinePlayer.getName();
                    }

                    PlayerInfo found = database.getOrCreatePlayerInfo(offlinePlayer.getUniqueId(), name);
                    updateCache(found);
                    return found;
                }
            }
        } catch (Exception e) {
        }

        // last resort: save the uuid w/ blank name and check another time
        PlayerInfo playerInfo = LWC.getInstance().getPhysicalDatabase().getOrCreatePlayerInfo(null, name);
        updateCache(playerInfo);
        return playerInfo;
    }

    /**
     * Get the name for the given UUID. If it is not already known, it will be retrieved from the account servers.
     *
     * @param uuid
     * @return
     */
    public static String getName(UUID uuid) {
        PlayerInfo found = getPlayerInfo(uuid);
        return found != null ? found.getName() : null;
    }

    /**
     * Get the UUID for the given name. If it is not already known, it will be retrieved from the account servers.
     *
     * @param name
     * @return
     * @throws Exception
     */
    public static UUID getUUID(String name) {
        PlayerInfo found = getPlayerInfoByName(name);
        return found != null ? found.getUUID() : null;
    }

    /**
     * Attempts to format a player's name, which can be a name or a UUID. If the owner is a UUID and then
     * UUID is unknown, then "Unknown (uuid)" will be returned.
     *
     * @param name
     * @return
     */
    public static String formatPlayerName(String name) {
        PlayerInfo playerInfo = getPlayerInfo(name);

        if (playerInfo == null) {
            return "Unknown (" + name + ")";
        } else {
            return playerInfo.getName();
        }
    }

}
