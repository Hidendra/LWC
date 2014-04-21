package com.griefcraft.util;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UUIDRegistry {

    /**
     * Temporal caches
     */
    private static final Map<String, PlayerInfo> nameToUUIDCache = new HashMap<String, PlayerInfo>();
    private static final Map<UUID, PlayerInfo> UUIDToNameCache = new HashMap<UUID, PlayerInfo>();

    static class PlayerInfo {

        private UUID uuid;
        private String name;

        public PlayerInfo(UUID uuid, String name) {
            this.uuid = uuid;
            this.name = name;
        }

        public UUID getUUID() {
            return uuid;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * Update the cache with a new UUID/name pair
     *
     * @param uuid
     * @param name
     */
    public static void updateCache(UUID uuid, String name) {
        PlayerInfo playerInfo = new PlayerInfo(uuid, name);
        nameToUUIDCache.put(name.toLowerCase(), playerInfo);
        UUIDToNameCache.put(uuid, playerInfo);
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
     * Get the name for the given UUID. If it is not already known, it will be retrieved from the account servers.
     *
     * @param uuid
     * @return
     */
    public static String getName(UUID uuid) {
        if (uuid == null) {
            return null;
        }

        if (UUIDToNameCache.containsKey(uuid)) {
            return UUIDToNameCache.get(uuid).getName();
        }

        // First way: if they're on the server already
        Player player = Bukkit.getPlayer(uuid);

        if (player != null) {
            updateCache(uuid, player.getName());
            return player.getName();
        }

        // Second way: if they have been on the server before
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);

        if (offlinePlayer != null && offlinePlayer.getName() != null) {
            updateCache(uuid, offlinePlayer.getName());
            return offlinePlayer.getName();
        }

        // Third way: use the web API
        try {
            Map<UUID, String> results = new NameFetcher(Arrays.asList(uuid)).call();

            if (results.containsKey(uuid)) {
                return results.get(uuid);
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get the UUID for the given name. If it is not already known, it will be retrieved from the account servers.
     *
     * @param name
     * @return
     * @throws Exception
     */
    public static UUID getUUID(String name) {
        String nameLower = name.toLowerCase();

        try {
            if (nameToUUIDCache.containsKey(nameLower)) {
                return nameToUUIDCache.get(nameLower).getUUID();
            }

            if (isValidUUID(name)) {
                return UUID.fromString(name);
            }

            if (Bukkit.getOnlineMode()) {
                Map<String, UUID> results = new UUIDFetcher(Arrays.asList(nameLower)).call();

                // The returned name is the exact casing; so we need to look for it
                // in the case-insensitive version
                for (String key : results.keySet()) {
                    if (key.equalsIgnoreCase(name)) {
                        UUID uuid = results.get(key);
                        updateCache(uuid, key);
                        return uuid;
                    }
                }
            } else {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);

                if (offlinePlayer != null && offlinePlayer.getUniqueId() != null) {
                    if (offlinePlayer.getName() != null) {
                        name = offlinePlayer.getName();
                    }

                    updateCache(offlinePlayer.getUniqueId(), name);
                    return offlinePlayer.getUniqueId();
                }
            }

            nameToUUIDCache.put(nameLower, null);
            return null;
        } catch (Exception e) {
            nameToUUIDCache.put(nameLower, null);
            return null;
        }
    }

    /**
     * Attempts to format a player's name, which can be a name or a UUID. If the owner is a UUID and then
     * UUID is unknown, then "Unknown (uuid)" will be returned.
     *
     * @param name
     * @return
     */
    public static String formatPlayerName(String name) {
        if (isValidUUID(name)) {
            String formattedName = getName(UUID.fromString(name));

            if (formattedName == null) {
                return "Unknown (" + name + ")";
            } else {
                return formattedName + " (" + name + ")";
            }
        } else {
            return name;
        }
    }

}
