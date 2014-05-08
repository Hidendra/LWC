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

import com.griefcraft.lwc.LWC;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.HashMap;
import java.util.Map;

public class History {

    /**
     * The history type defines what this History object is for, such as TRANSACTION.
     * <p/>
     * Ordering <b>must</b> remain constant as internally, the ordinal is used and
     * if ordering is changed, LWC will experience undefined behaviour.
     */
    public enum Type {

        /**
         * Designates that this history object is for a transaction - e.g when a protection is removed
         */
        TRANSACTION

    }

    /**
     * The status of this History object; most often ACTIVE or INACTIVE.
     * <p/>
     * As with {@link Type}, the ordering <b>must</b> remain constant and not change.
     */
    public enum Status {

        /**
         * This type is still active
         */
        ACTIVE,

        /**
         * For some reason the type is now inactive. Most likely the
         * protection was removed by the player
         */
        INACTIVE
    }

    /**
     * History id in the database
     */
    private int id;

    /**
     * Affected protection id
     */
    private int protectionId;

    /**
     * The protection known for this history object
     */
    private Protection protection;

    /**
     * The player that caused the history action to be created
     */
    private PlayerInfo player;

    /**
     * The x coordinate of the history item
     */
    private int x;

    /**
     * The y coordinate of the history item
     */
    private int y;

    /**
     * The z coordinate of the history item
     */
    private int z;

    /**
     * The history type, e.g TRANSACTION
     */
    private Type type = Type.TRANSACTION;

    /**
     * The status (ACTIVE or INACTIVE normally)
     */
    private Status status = Status.INACTIVE;

    /**
     * Metadata for the history record
     */
    private final Map<String, String> metadata = new HashMap<String, String>();

    /**
     * The seconds (since linux epoch) this History object was created
     */
    private long timestamp;

    /**
     * If the history val exists in the database
     */
    private boolean exists = false;

    /**
     * If the history object was modified
     */
    private boolean modified = false;

    /**
     * If the History object is waiting to be flushed to the database
     */
    private boolean saving = false;

    /**
     * Parser used to parse JSON
     */
    private static final JSONParser jsonParser = new JSONParser();

    public History() {
        // set some defaults to account for stupidness
        status = Status.INACTIVE;
    }

    /**
     * @return true if the history object should be synced to the database
     */
    public boolean wasModified() {
        return modified;
    }

    /**
     * @return the Protection this history value is associated with
     */
    public Protection getProtection() {
        // attempt to load the protection if it hasn't been loaded yet
        if (protection == null) {
            this.protection = LWC.getInstance().getPhysicalDatabase().loadProtection(protectionId);
        }

        return protection;
    }

    /**
     * Add a string of data to the stored metadata
     *
     * @param key
     * @param value
     */
    public void setMetaData(String key, String value) {
        metadata.put(key, value);
    }

    /**
     * Check if the metadata contains a given key
     *
     * @param key
     * @return
     */
    public boolean hasKey(String key) {
        return metadata.containsKey(key);
    }

    /**
     * Get a boolean value from the metadata using the key (key=value)
     *
     * @param key
     * @return
     */
    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(metadata.get(key));

    }

    /**
     * Get a String value from the metadata using the key (key=value)
     *
     * @param key
     * @return
     */
    public String getString(String key) {
        return metadata.get(key);
    }

    /**
     * Get an integer value from the metadata using the key (key=value)
     *
     * @param key
     * @return
     */
    public int getInteger(String key) {
        return Integer.parseInt(metadata.get(key));
    }

    /**
     * Get a double value from the metadata using the key
     *
     * @param key
     * @return
     */
    public double getDouble(String key) {
        return Double.parseDouble(metadata.get(key));
    }

    /**
     * Remove a string of known data from the stored metadata
     *
     * @param data
     * @return true if the given metadata was successfully removed
     */
    public boolean removeMetaData(String data) {
        return metadata.remove(data) != null;
    }

    /**
     * Set the cached protection this History object belongs to save a query or two later on
     *
     * @param protection
     */
    public void setProtection(Protection protection) {
        if (protection == null) {
            return;
        }

        this.protection = protection;
        this.protectionId = protection.getId();
    }

    /**
     * @return true if this History object exists in the database
     */
    public boolean doesExist() {
        return exists;
    }

    /**
     * Set if the History object exists in the database
     *
     * @param exists
     */
    public void setExists(boolean exists) {
        this.exists = exists;
        this.modified = true;
    }

    /**
     * @return a STRING representation of the metadata for use in the database
     */
    public String getSafeMetaData() {
        return JSONObject.toJSONString(metadata);
    }

    /**
     * Decode the metadata value
     *
     * @param safe
     */
    public void decodeMetaData(String safe) {
        try {
            Object json = jsonParser.parse(safe);

            if (json != null) {
                JSONObject object = (JSONObject) json;

                for (Object o : object.entrySet()) {
                    Map.Entry entry = (Map.Entry) o;

                    String key = entry.getKey().toString();
                    String value = entry.getValue().toString();
                    setMetaData(key, value);
                }
            }
        } catch (ParseException e) {

        }

        // Probably old format
        String[] split = safe.split(",");

        for (String str : split) {
            String[] dataValue = str.split("=");

            if (dataValue.length > 1) {
                String key = dataValue[0];
                String value = dataValue[1];
                setMetaData(key, value);
            }
        }
    }

    /**
     * Sync this history object to the database when possible
     */
    public void save() {
        // if it was not modified, no point in saving it :-)
        if (!modified || saving) {
            return;
        }

        LWC lwc = LWC.getInstance();

        // find the protection the history object is attached to
        Protection protection = getProtection();

        // no protection? weird, just sync anyway
        if (protection == null) {
            saveNow();
            return;
        }

        // wait!
        this.saving = true;

        // ensure the protection knows about us
        protection.checkHistory(this);

        // save it when possible
        protection.save();
    }

    /**
     * Force the history object to be saved immediately
     */
    public void saveNow() {
        LWC.getInstance().getPhysicalDatabase().saveHistory(this);
        this.modified = false;
        this.saving = false;
    }

    /**
     * Alias for {@see save}
     */
    public void sync() {
        save();
    }

    /**
     * Remove this history object from the database
     * TODO: broadcast an event
     */
    public void remove() {
        LWC.getInstance().getPhysicalDatabase().removeHistory(id);
        this.modified = false;
    }

    public int getId() {
        return id;
    }

    public int getProtectionId() {
        return protectionId;
    }

    public PlayerInfo getPlayer() {
        return player;
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

    public Type getType() {
        return type;
    }

    public Status getStatus() {
        return status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setId(int id) {
        this.id = id;
        this.exists = true;
        this.modified = true;
    }

    public void setProtectionId(int protectionId) {
        this.protectionId = protectionId;
        this.modified = true;
    }

    public void setPlayer(PlayerInfo player) {
        this.player = player;
        this.modified = true;
    }

    public void setX(int x) {
        this.x = x;
        this.modified = true;
    }

    public void setY(int y) {
        this.y = y;
        this.modified = true;
    }

    public void setZ(int z) {
        this.z = z;
        this.modified = true;
    }

    public void setType(Type type) {
        this.type = type;
        this.modified = true;
    }

    public void setStatus(Status status) {
        this.status = status;
        this.modified = true;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        this.modified = true;
    }

}
