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

import com.griefcraft.cache.ProtectionCache;
import com.griefcraft.lwc.LWC;
import com.griefcraft.scripting.event.LWCProtectionRemovePostEvent;
import com.griefcraft.util.Colors;
import com.griefcraft.util.ProtectionFinder;
import com.griefcraft.util.StringUtil;
import com.griefcraft.util.TimeUtil;
import com.griefcraft.util.UUIDRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Protection {

    /**
     * The protection type
     *
     * <p>Ordering <b>must NOT change</b> as ordinal values are used</p>
     */
    public enum Type {

        /**
         * The protection is usable by anyone; the most common use would include community chests
         * where anyone can use the chest but no one should be able to protect as their own.
         */
        PUBLIC,

        /**
         * The owner (and anyone else) must enter a set password entered onto the chest in order
         * to be able to access it. Entering the correct password allows them to use the chest
         * until they log out or the protection is removed.
         */
        PASSWORD,

        /**
         * The protection is only usable by the player who created it. Further access can be
         * given to players, groups, and even more specific entities
         * such as Towns in the "Towny" plugin, or access lists via the "Lists" plugin
         */
        PRIVATE,

        /**
         * Reserved / unused, to keep ordinal order
         */
        RESERVED1,

        /**
         * Reserved / unused, to keep ordinal order
         */
        RESERVED2,

        /**
         * Allows players to deposit items into
         */
        DONATION;

        /**
         * Match a protection type using its string form
         *
         * @param text
         * @return
         */
        public static Type matchType(String text) {
            for (Type type : values()) {
                if (type.toString().equalsIgnoreCase(text)) {
                    return type;
                }
            }

            throw new IllegalArgumentException("No Protection Type found for given type: " + text);
        }

    }

    /**
     * All of the history items associated with this protection
     */
    private final Set<History> historyCache = new HashSet<History>();

    /**
     * List of the permissions rights for the protection
     */
    private final Set<Permission> permissions = new HashSet<Permission>();

    /**
     * List of flags enabled on the protection
     */
    private final Map<Flag.Type, Flag> flags = new HashMap<Flag.Type, Flag>();

    /**
     * The block id
     */
    private int blockId;

    /**
     * The password for the chest
     */
    private String password;

    /**
     * JSON data for the protection
     */
    private final JSONObject data = new JSONObject();

    /**
     * Unique id (in sql)
     */
    private int id;

    /**
     * The owner of the chest
     */
    private String owner;

    /**
     * The protection type
     */
    private Type type;

    /**
     * The world the protection is in
     */
    private String world;

    /**
     * The x coordinate
     */
    private int x;

    /**
     * The y coordinate
     */
    private int y;

    /**
     * The z coordinate
     */
    private int z;

    /**
     * The timestamp of when the protection was last accessed
     */
    private long lastAccessed;

    /**
     * The time the protection was created
     */
    private String creation;

    /**
     * Immutable flag for the protection. When removed, this bool is switched to true and any setters
     * will no longer work. However, everything is still intact and in memory at this point (for now.)
     */
    private boolean removed = false;

    /**
     * If the protection is pending removal. Only used internally.
     */
    private boolean removing = false;

    /**
     * True when the protection has been modified and should be saved
     */
    private boolean modified = false;

    /**
     * The protection finder used to find this protection
     */
    private ProtectionFinder finder;

    /**
     * The block the protection is at. Saves world calls and allows better concurrency
     */
    private Block cachedBlock;

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Protection)) {
            return false;
        }

        Protection other = (Protection) object;

        return id == other.id && x == other.x && y == other.y && z == other.z && (owner != null && owner.equals(other.owner)) &&
                (world != null && world.equals(other.world));
    }

    @Override
    public int hashCode() {
        int hash = 17;

        // the identifier is normally unique, but in SQLite ids may be quickly reused so we use other data
        hash *= 37 + id;

        // coordinates
        hash *= 37 + x;
        hash *= 37 + y;
        hash *= 37 + z;

        // and for good measure, to *guarantee* no collisions
        if (creation != null) {
            hash *= 37 + creation.hashCode();
        }

        return hash;
    }

    /**
     * Convert the protection to use UUIDs
     *
     * @return true if the protection required conversion and conversions were done
     */
    public boolean convertPlayerNamesToUUIDs() {
        if (!needsUUIDConversion()) {
            return false;
        }

        boolean res = false;

        if (!UUIDRegistry.isValidUUID(owner)) {
            UUID uuid = UUIDRegistry.getUUID(owner);

            if (uuid != null) {
                setOwner(uuid.toString());
                res = true;
            }
        }

        for (Permission permission : permissions) {
            if (permission.getType() == Permission.Type.PLAYER && !UUIDRegistry.isValidUUID(permission.getName())) {
                UUID uuid = UUIDRegistry.getUUID(permission.getName());

                if (uuid != null) {
                    permission.setName(uuid.toString());
                    modified = true;
                    res = true;
                }
            }
        }

        return res;
    }

    /**
     * Check if this protection requires conversion from plain player names to UUIDs
     *
     * @return true if the protection requires conversion
     */
    public boolean needsUUIDConversion() {
        if (!UUIDRegistry.isValidUUID(owner)) {
            return true;
        }

        for (Permission permission : permissions) {
            if (permission.getType() == Permission.Type.PLAYER && !UUIDRegistry.isValidUUID(permission.getName())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get a formatted version of the owner's name. If the owner is a UUID and the UUID is unknown, then
     * "Unknown (uuid)" will be returned.
     *
     * @return
     */
    public String getFormattedOwnerPlayerName() {
        return UUIDRegistry.formatPlayerName(owner);
    }

    /**
     * Encode the AccessRights to JSON
     *
     * @return
     */
    public void encodeRights() {
        // create the root
        JSONArray root = new JSONArray();

        // add all of the permissions to the root
        for (Permission permission : permissions) {
            if (permission != null) {
                root.add(permission.encodeToJSON());
            }
        }

        data.put("rights", root);
    }

    /**
     * Encode the protection flags to JSON
     */
    public void encodeFlags() {
        JSONArray root = new JSONArray();

        for (Flag flag : flags.values()) {
            if (flag != null) {
                root.add(flag.getData());
            }
        }

        data.put("flags", root);
    }

    /**
     * Ensure a history object is located in our cache
     *
     * @param history
     */
    public void checkHistory(History history) {
        if (!historyCache.contains(history)) {
            historyCache.add(history);
        }
    }

    /**
     * Check if a player has owner access to the protection
     *
     * @param player
     * @return
     */
    public boolean isOwner(Player player) {
        LWC lwc = LWC.getInstance();

        if (isRealOwner(player)) {
            return true;
        } else {
            return lwc.isAdmin(player);
        }
    }

    /**
     * Check if a player is the real owner to the protection
     *
     * @param player
     * @return
     */
    public boolean isRealOwner(Player player) {
        if (player == null) {
            return false;
        }

        if (UUIDRegistry.isValidUUID(owner)) {
            return UUID.fromString(owner).equals(player.getUniqueId());
        } else {
            return owner.equalsIgnoreCase(player.getName());
        }
    }

    /**
     * Create a History object that is attached to this protection
     *
     * @return
     */
    public History createHistoryObject() {
        History history = new History();

        history.setProtectionId(id);
        history.setProtection(this);
        history.setStatus(History.Status.INACTIVE);
        history.setX(x);
        history.setY(y);
        history.setZ(z);

        // add it to the cache
        historyCache.add(history);

        return history;
    }

    /**
     * @return the related history for this protection, which is immutable
     */
    public Set<History> getRelatedHistory() {
        // cache the database's history if we don't have any yet
        if (historyCache.size() == 0) {
            historyCache.addAll(LWC.getInstance().getPhysicalDatabase().loadHistory(this));
        }

        // now we can return an immutable cache
        return Collections.unmodifiableSet(historyCache);
    }

    /**
     * Get the related history for this protection using the given type
     *
     * @param type
     * @return
     */
    public List<History> getRelatedHistory(History.Type type) {
        List<History> matches = new ArrayList<History>();
        Set<History> relatedHistory = getRelatedHistory();

        for (History history : relatedHistory) {
            if (history.getType() == type) {
                matches.add(history);
            }
        }

        return matches;
    }

    /**
     * Check if a flag is enabled
     *
     * @param type
     * @return
     */
    public boolean hasFlag(Flag.Type type) {
        return flags.containsKey(type);
    }

    /**
     * Get the enabled flag for the corresponding type
     *
     * @param type
     * @return
     */
    public Flag getFlag(Flag.Type type) {
        return flags.get(type);
    }

    /**
     * Add a flag to the protection
     *
     * @param flag
     * @return
     */
    public boolean addFlag(Flag flag) {
        if (removed || flag == null) {
            return false;
        }

        if (!flags.containsKey(flag.getType())) {
            flags.put(flag.getType(), flag);
            modified = true;
            return true;
        }

        return false;
    }

    /**
     * Remove a flag from the protection
     *
     * @param flag
     * @return
     */
    public void removeFlag(Flag flag) {
        if (removed) {
            return;
        }

        flags.remove(flag);
        this.modified = true;
    }

    /**
     * Check if the entity + permissions type exists, and if so return the rights (-1 if it does not exist)
     *
     * @param type
     * @param name
     * @return the permissions the player has
     */
    public Permission.Access getAccess(String name, Permission.Type type) {
        for (Permission permission : permissions) {
            if (permission.getType() == type && permission.getName().equalsIgnoreCase(name)) {
                return permission.getAccess();
            }
        }

        return Permission.Access.NONE;
    }

    /**
     * @return the list of permissions
     */
    public List<Permission> getPermissions() {
        return Collections.unmodifiableList(new ArrayList<Permission>(permissions));
    }

    /**
     * Remove temporary permissions rights from the protection
     */
    public void removeTemporaryPermissions() {
        Iterator<Permission> iter = permissions.iterator();

        while (iter.hasNext()) {
            Permission permission = iter.next();

            if (permission.isVolatile()) {
                iter.remove();
            }
        }
    }

    /**
     * Add an permission to the protection
     *
     * @param permission
     */
    public void addPermission(Permission permission) {
        if (removed || permission == null) {
            return;
        }

        // remove any other rights with the same identity
        removePermissions(permission.getName(), permission.getType());

        // now we can safely add it
        permissions.add(permission);
        modified = true;
    }

    /**
     * Remove permissions from the protection that match a name AND type
     *
     * @param name
     * @param type
     */
    public void removePermissions(String name, Permission.Type type) {
        if (removed) {
            return;
        }

        Iterator<Permission> iter = permissions.iterator();

        while (iter.hasNext()) {
            Permission permission = iter.next();

            if ((permission.getName().equals(name) || name.equals("*")) && permission.getType() == type) {
                iter.remove();
                modified = true;
            }
        }
    }

    /**
     * Remove all of the permissions
     */
    public void removeAllPermissions() {
        permissions.clear();
        modified = true;
    }

    /**
     * Checks if the protection has the correct block in the world
     *
     * @return
     */
    public boolean isBlockInWorld() {
        int storedBlockId = getBlockId();
        Block block = getBlock();

        switch (block.getType()) {
            case FURNACE:
            case BURNING_FURNACE:
                return storedBlockId == Material.FURNACE.getId() || storedBlockId == Material.BURNING_FURNACE.getId();

            case STEP:
            case DOUBLE_STEP:
                    return storedBlockId == Material.STEP.getId() || storedBlockId == Material.DOUBLE_STEP.getId();

            default:
                return storedBlockId == block.getTypeId();
        }
    }

    public JSONObject getData() {
        return data;
    }

    public int getBlockId() {
        return blockId;
    }

    public String getPassword() {
        return password;
    }

    public String getCreation() {
        return creation;
    }

    public int getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public Type getType() {
        return type;
    }

    public String getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public long getLastAccessed() {
        return lastAccessed;
    }

    public void setBlockId(int blockId) {
        if (removed) {
            return;
        }

        this.blockId = blockId;
        this.modified = true;
    }

    public void setPassword(String password) {
        if (removed) {
            return;
        }

        this.password = password;
        this.modified = true;
    }

    public void setCreation(String creation) {
        if (removed) {
            return;
        }

        this.creation = creation;
        this.modified = true;
    }

    public void setId(int id) {
        if (removed) {
            return;
        }

        this.id = id;
        this.modified = true;
    }

    public void setOwner(String owner) {
        if (removed) {
            return;
        }

        this.owner = owner;
        this.modified = true;
    }

    public void setType(Type type) {
        if (removed) {
            return;
        }

        this.type = type;
        this.modified = true;
    }

    public void setWorld(String world) {
        if (removed) {
            return;
        }

        this.world = world;
        this.modified = true;
    }

    public void setX(int x) {
        if (removed) {
            return;
        }

        this.x = x;
        this.modified = true;
    }

    public void setY(int y) {
        if (removed) {
            return;
        }

        this.y = y;
        this.modified = true;
    }

    public void setZ(int z) {
        if (removed) {
            return;
        }

        this.z = z;
        this.modified = true;
    }

    public void setLastAccessed(long lastAccessed) {
        if (removed) {
            return;
        }

        this.lastAccessed = lastAccessed;
        this.modified = true;
    }

    /**
     * Sets the protection finder used to create this protection
     *
     * @param finder
     */
    public void setProtectionFinder(ProtectionFinder finder) {
        this.finder = finder;
    }

    /**
     * Gets the protection finder used the create this protection
     *
     * @return the ProtectionFinder used to create this protection
     */
    public ProtectionFinder getProtectionFinder() {
        return finder;
    }

    /**
     * Remove the protection from the database
     */
    public void remove() {
        if (removed) {
            return;
        }

        LWC lwc = LWC.getInstance();
        removeTemporaryPermissions();

        // we're removing it, so assume there are no changes
        modified = false;
        removing = true;

        // broadcast the removal event
        // we broadcast before actually removing to give them a chance to use any password that would be removed otherwise
        lwc.getModuleLoader().dispatchEvent(new LWCProtectionRemovePostEvent(this));

        // mark related transactions as inactive
        for (History history : getRelatedHistory(History.Type.TRANSACTION)) {
            if (history.getStatus() != History.Status.ACTIVE) {
                continue;
            }

            history.setStatus(History.Status.INACTIVE);
        }

        // ensure all history objects for this protection are saved
        checkAndSaveHistory();

        // make the protection immutable
        removed = true;

        // and now finally remove it from the database
        lwc.getDatabaseThread().removeProtection(this);
        lwc.getPhysicalDatabase().removeProtection(id);
        removeCache();
    }

    /**
     * Remove the protection from cache
     */
    public void removeCache() {
        LWC lwc = LWC.getInstance();
        lwc.getProtectionCache().removeProtection(this);
        radiusRemoveCache();
    }

    /**
     * Remove blocks around the protection in a radius of 3, to account for broken known / null blocks
     */
    public void radiusRemoveCache() {
        ProtectionCache cache = LWC.getInstance().getProtectionCache();

        for (int x = -3; x <= 3; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) {
                    String cacheKey = world + ":" + (this.x + x) + ":" + (this.y + y) + ":" + (this.z + z);

                    // get the protection for that entry
                    Protection protection = cache.getProtection(cacheKey);

                    // the ifnull compensates for the block being in the null cache. It will remove it from that.
                    if ((protection != null && id == protection.getId()) || protection == null) {
                        cache.remove(cacheKey);
                    }
                }
            }
        }
    }

    /**
     * Queue the protection to be saved
     */
    public void save() {
        if (removed) {
            return;
        }

        LWC.getInstance().getDatabaseThread().addProtection(this);
    }

    /**
     * Force a protection update to the live database
     */
    public void saveNow() {
        if (removed) {
            return;
        }

        // encode JSON objects
        encodeRights();
        encodeFlags();

        // only save the protection if it was modified
        if (modified && !removing) {
            LWC.getInstance().getPhysicalDatabase().saveProtection(this);
        }

        // check the cache for history updates
        checkAndSaveHistory();
    }

    /**
     * Saves any of the history items for the Protection that have been modified
     */
    public void checkAndSaveHistory() {
        if (removed) {
            return;
        }

        for (History history : getRelatedHistory()) {
            // if the history object was modified we need to save it
            if (history.wasModified()) {
                history.saveNow();
            }
        }
    }

    /**
     * @return the key used for the protection cache
     */
    public String getCacheKey() {
        return world + ":" + x + ":" + y + ":" + z;
    }

    /**
     * @return the Bukkit world the protection should be located in
     */
    public World getBukkitWorld() {
        if (world == null || world.isEmpty()) {
            return Bukkit.getServer().getWorlds().get(0);
        }

        return Bukkit.getServer().getWorld(world);
    }

    /**
     * @return the Bukkit Player object of the owner
     */
    public Player getBukkitOwner() {
        return Bukkit.getServer().getPlayer(owner);
    }

    /**
     * @return the block representing the protection in the world
     */
    public Block getBlock() {
        if (cachedBlock != null) {
            return cachedBlock;
        }

        World world = getBukkitWorld();

        if (world == null) {
            return null;
        }

        cachedBlock = world.getBlockAt(x, y, z);
        return cachedBlock;
    }

    /**
     * @return
     */
    @Override
    public String toString() {
        // format the flags prettily
        String flagStr = "";

        for (Flag flag : flags.values()) {
            flagStr += flag.toString() + ",";
        }

        if (flagStr.endsWith(",")) {
            flagStr = flagStr.substring(0, flagStr.length() - 1);
        }

        // format the last accessed time
        String lastAccessed = TimeUtil.timeToString((System.currentTimeMillis() / 1000L) - this.lastAccessed);

        if (!lastAccessed.equals("Not yet known")) {
            lastAccessed += " ago";
        }

        return String.format("%s %s" + Colors.White + " " + Colors.Green + "Id=%d Owner=%s Location=[%s %d,%d,%d] Created=%s Flags=%s LastAccessed=%s", typeToString(), (blockId > 0 ? (LWC.materialToString(blockId)) : "Not yet cached"), id, owner, world, x, y, z, creation, flagStr, lastAccessed);
    }

    /**
     * @return string representation of the protection type
     */
    public String typeToString() {
        return StringUtil.capitalizeFirstLetter(type.toString());
    }

    /**
     * Updates the protection in the protection cache
     */
    @Deprecated
    public void update() {
        throw new UnsupportedOperationException("Protection.update() is no longer necessary!");
    }

}
