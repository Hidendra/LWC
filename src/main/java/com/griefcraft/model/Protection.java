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

import com.griefcraft.cache.CacheSet;
import com.griefcraft.cache.LRUCache;
import com.griefcraft.lwc.LWC;
import com.griefcraft.scripting.event.LWCProtectionRemovePostEvent;
import com.griefcraft.util.Colors;
import com.griefcraft.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class Protection {

    // re-use LWC logger
    @SuppressWarnings("unused")
    private Logger logger = Logger.getLogger("LWC");

    /**
     * All of the history items associated with this protection
     */
    private final Set<History> historyCache = new HashSet<History>();

    /**
     * List of the accessRights rights for the protection
     */
    private final Set<AccessRight> accessRights = new HashSet<AccessRight>();

    /**
     * List of flags enabled on the protection
     */
    private final Set<Flag> flags = new HashSet<Flag>();

    /**
     * The block id
     */
    private int blockId;

    /**
     * The password for the chest
     */
    private String password;

    /**
     * The date created
     */
    private String date;

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
     * The chest type
     */
    private int type;

    /**
     * The world this protection is in
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
     * Immutable flag for the protection. When removed, this bool is switched to true and any setters
     * will no longer work. However, everything is still intact and in memory at this point (for now.)
     */
    private boolean removed = false;

    /**
     * True when the protection has been modified and should be saved
     */
    private boolean modified = false;

    /**
     * Encode the AccessRights to JSON
     *
     * @return
     */
    public void encodeRights() {
        // create the root
        JSONArray root = new JSONArray();

        // add all of the access rights to the root
        for (AccessRight right : accessRights) {
            root.add(right.encodeToJSON());
        }

        data.put("rights", root);
    }

    /**
     * Encode the protection flags to JSON
     */
    public void encodeFlags() {
        JSONArray root = new JSONArray();

        for (Flag flag : flags) {
            root.add(flag.getData());
        }

        data.put("flags", root);
    }

    /**
     * Ensure a history object is located in our cache
     * 
     * @param history
     */
    public void checkHistory(History history) {
        if(!historyCache.contains(history)) {
            historyCache.add(history);
        }
    }

    /**
     * Check if a player is the owner of the protection
     *
     * @param player
     * @return
     */
    public boolean isOwner(Player player) {
        LWC lwc = LWC.getInstance();

        return player != null && (owner.equals(player.getName()) || lwc.isAdmin(player));
    }

    /**
     * Create a History object that is attached to this protection
     *
     * @return
     */
    public History createHistoryObject() {
        History history = new History();

        history.setProtectionId(id);
        history.setStatus(History.Status.INACTIVE);

        // add it to the cache
        historyCache.add(history);

        return history;
    }

    /**
     * @return the related history for this protection, which is immutable
     */
    public Set<History> getRelatedHistory() {
        // cache the database's history if we don't have any yet
        if(historyCache.size() == 0) {
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
        for (Flag flag : flags) {
            if (flag.getType() == type) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the enabled flag for the corresponding type
     *
     * @param type
     * @return
     */
    public Flag getFlag(Flag.Type type) {
        for (Flag flag : flags) {
            if (flag.getType() == type) {
                return flag;
            }
        }

        return null;
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

        if (!flags.contains(flag)) {
            flags.add(flag);
            modified = true;
            return true;
        }

        return false;
    }

    /**
     * Remove a flag from the protection
     * TODO: redo? :s
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
     * Check if the entity + accessRights type exists, and if so return the rights (-1 if it does not exist)
     *
     * @param type
     * @param name
     * @return the accessRights the player has
     */
    public int getAccess(int type, String name) {
        for (AccessRight right : accessRights) {
            if (right.getType() == type && right.getName().equalsIgnoreCase(name)) {
                return right.getRights();
            }
        }

        return -1;
    }

    /**
     * @return the list of access rights
     */
    public List<AccessRight> getAccessRights() {
        return Collections.unmodifiableList(new ArrayList<AccessRight>(accessRights));
    }

    /**
     * Remove temporary accessRights rights from the protection
     */
    public void removeTemporaryAccessRights() {
        removeAccessRightsMatching("*", AccessRight.TEMPORARY);
    }

    /**
     * Add an accessRights right to the stored list
     *
     * @param right
     */
    public void addAccessRight(AccessRight right) {
        if (removed || right == null) {
            return;
        }

        // remove any other rights with the same identity
        removeAccessRightsMatching(right.getName(), right.getType());

        // now we can safely add it
        accessRights.add(right);
        modified = true;
    }

    /**
     * Remove access rights from the protection that match an entity AND type
     * 
     * @param entity
     * @param type
     */
    public void removeAccessRightsMatching(String entity, int type) {
        if (removed) {
            return;
        }

        Iterator<AccessRight> iter = accessRights.iterator();

        while(iter.hasNext()) {
            AccessRight right = iter.next();

            if((right.getName().equals(entity) || entity.equals("*")) && right.getType() == type) {
                iter.remove();
                modified = true;
            }
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

    public String getDate() {
        return date;
    }

    public int getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public int getType() {
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

    public void setDate(String date) {
        if (removed) {
            return;
        }

        this.date = date;
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

    public void setType(int type) {
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
     * Remove the protection from the database
     */
    public void remove() {
        if (removed) {
            return;
        }

        LWC lwc = LWC.getInstance();
        removeTemporaryAccessRights();

        // we're removing it, so assume there are no changes
        modified = false;

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

        // now perform final saving to ensure all history objects are saved immediately
        saveNow();

        // make the protection immutable
        removed = true;

        // and now finally remove it from the database
        lwc.getUpdateThread().unqueueProtectionUpdate(this);
        lwc.getPhysicalDatabase().unregisterProtection(id);
        removeCache();
    }

    /**
     * Remove the protection from cache
     */
    public void removeCache() {
        LWC lwc = LWC.getInstance();
        LRUCache<String, Protection> cache = lwc.getCaches().getProtections();

        cache.remove(getCacheKey());

        /* For Bug 656 workaround we record in-memory any double-chests/etc we find as
        * we find them, since we can't count on Bukkit to reliably return that info later.
        * As a result, when we are removing a protection (and therefore LWC calls this method
        * to remove it's cache object), we need to remove the adjacent block from memory also.
        */
        if (lwc.isBug656WorkAround()) {
            World worldObject = lwc.getPlugin().getServer().getWorld(world);
            List<Block> blocks = lwc.getRelatedBlocks(worldObject, x, y, z);
            for (Block b : blocks) {
                String cacheKey = b.getWorld().getName() + ":" + b.getX() + ":" + b.getY() + ":" + b.getZ();
                cache.remove(cacheKey);
            }
        }
    }

    /**
     * Updates the protection in the protection cache
     * Note that save() and saveNow() call this
     */
    public void update() {
        if (removed) {
            return;
        }

        CacheSet caches = LWC.getInstance().getCaches();
        removeCache();

        Protection temp = LWC.getInstance().getPhysicalDatabase().loadProtection(id);

        if (temp != null) {
            caches.getProtections().put(getCacheKey(), temp);
        }
    }

    /**
     * Queue the protection to be saved
     */
    public void save() {
        if (removed) {
            return;
        }

        LWC.getInstance().getCaches().getProtections().put(getCacheKey(), this);
        LWC.getInstance().getUpdateThread().queueProtectionUpdate(this);
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
        if(modified) {
            LWC.getInstance().getPhysicalDatabase().saveProtection(this);
            update();
        }

        // check the cache for history updates
        for(History history : historyCache) {
            // if the history object was modified we need to save it
            if(history.wasModified()) {
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
        World world = getBukkitWorld();

        if (world == null) {
            return null;
        }

        return world.getBlockAt(x, y, z);
    }

    /**
     * @return
     */
    @Override
    public String toString() {
        // format the flags prettily
        String flagStr = "";

        for (Flag flag : flags) {
            flagStr += flag.toString() + ",";
        }

        if (flagStr.endsWith(",")) {
            flagStr = flagStr.substring(0, flagStr.length() - 1);
        }

        // format the last accessed time
        String lastAccessed = StringUtils.timeToString((System.currentTimeMillis() / 1000L) - this.lastAccessed);

        if (!lastAccessed.equals("Not yet known")) {
            lastAccessed += " ago";
        }

        return String.format("%s %s" + Colors.White + " " + Colors.Green + "Id=%d Owner=%s Location=[%s %d,%d,%d] Created=%s Flags=%s LastAccessed=%s", typeToString(), (blockId > 0 ? (LWC.materialToString(blockId)) : "Not yet cached"), id, owner, world, x, y, z, date, flagStr, lastAccessed);
    }

    /**
     * @return string representation of the protection type
     */
    public String typeToString() {
        switch (type) {
            case ProtectionTypes.PRIVATE:
                return "Private";

            case ProtectionTypes.PUBLIC:
                return "Public";

            case ProtectionTypes.PASSWORD:
                return "Password";

            case ProtectionTypes.TRAP_KICK:
                return "Kick trap";

            case ProtectionTypes.TRAP_BAN:
                return "Ban trap";
        }

        return "Unknown(raw:" + type + ")";
    }

}
