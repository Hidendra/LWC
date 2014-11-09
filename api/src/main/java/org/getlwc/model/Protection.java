/*
 * Copyright (c) 2011-2013 Tyler Blair
 * All rights reserved.
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

package org.getlwc.model;

import org.getlwc.Engine;
import org.getlwc.I18n;
import org.getlwc.component.BasicComponentHolder;
import org.getlwc.component.Component;
import org.getlwc.component.RoleSetComponent;
import org.getlwc.entity.Player;
import org.getlwc.role.Role;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import static org.getlwc.I18n._;

public class Protection extends BasicComponentHolder<Component> implements Savable {

    /**
     * The LWC engine instance
     */
    private final Engine engine;

    /**
     * The protection's internal id
     */
    private int id;

    /**
     * The unix timestamp of when the protection was created
     */
    private int created;

    /**
     * The unix timestamp of when the protection was last updated and
     */
    private int updated;

    /**
     * The unix timestamp of when the protection was last accessed by a member (i.e not overridden)
     */
    private int accessed;

    /**
     * The protection's state
     */
    private State state = State.NEW;

    /**
     * Metadata for the protection
     */
    private final Map<String, Metadata> metadata = new HashMap<>();

    /**
     * State for local metadata
     */
    private final Map<Metadata, State> metadataState = new HashMap<>();

    @Override
    public String toString() {
        // TODO add in updated, created
        return String.format("Protection(id=%d)", id);
    }

    public Protection(Engine engine, int id) {
        this.engine = engine;
        this.id = id;

        addComponent(new RoleSetComponent());

        for (Metadata meta : engine.getDatabase().loadProtectionMetadata(this)) {
            metadata.put(meta.getKey(), meta);
        }

        for (Role role : engine.getDatabase().loadProtectionRoles(this)) {
            getComponent(RoleSetComponent.class).add(role);
        }
    }

    /**
     * Get the access level a player has to this protection
     *
     * @param player
     * @return
     */
    public Access getAccess(Player player) {
        Access access = Access.NONE;

        for (Role role : getComponent(RoleSetComponent.class).getAll()) {
            Access roleAccess = role.getAccess(this, player);

            if (roleAccess == null) {
                continue;
            }

            if (roleAccess == Access.EXPLICIT_DENY) {
                return roleAccess;
            }

            if (roleAccess.ordinal() > access.ordinal()) {
                access = roleAccess;
            }
        }

        return access;
    }

    /**
     * Adds metadata to the protection
     *
     * @param meta
     */
    public void addMeta(Metadata meta) {
        synchronized (metadata) {
            metadataState.put(meta, metadata.containsKey(meta.getKey()) ? State.MODIFIED : State.NEW);
            metadata.put(meta.getKey(), meta);
            state = State.MODIFIED;
        }
    }

    /**
     * Removes metadata from the protection
     *
     * @param key
     */
    public void removeMeta(String key) {
        synchronized (metadata) {
            if (metadata.containsKey(key)) {
                metadataState.put(metadata.get(key), State.REMOVED);
                metadata.remove(key);
                state = State.MODIFIED;
            }
        }
    }

    /**
     * Returns metadata for the protection
     *
     * @param key
     * @return
     */
    public Metadata getMeta(String key) {
        synchronized (metadata) {
            return metadata.get(key);
        }
    }

    /**
     * Sets the unix epoch when the protection was updated
     *
     * @param updated
     */
    public void setUpdated(int updated) {
        this.updated = updated;
        state = State.MODIFIED;
    }

    /**
     * Sets the unix epoch the protection was created at
     *
     * @param created
     */
    public void setCreated(int created) {
        this.created = created;
        state = State.MODIFIED;
    }

    /**
     * Sets the unix epoch the protection was last modified at
     *
     * @param accessed
     */
    public void setAccessed(int accessed) {
        this.accessed = accessed;
        state = State.MODIFIED;
    }

    /**
     * Marks the protection as modified so that it is forcibly saved to the database the next time it's saved.
     */
    public void markModified() {
        state = State.MODIFIED;
    }

    /**
     * Returns the protection's unique id
     *
     * @return the protection's unique d
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the unix epoch the protection was last updated at
     *
     * @return unix epoch the protection was last updated at
     */
    public int getUpdated() {
        return updated;
    }

    /**
     * Returns the unix epoch the protection was created at
     *
     * @return unix epoch the protection was created at
     */
    public int getCreated() {
        return created;
    }

    /**
     * Returns the unix epoch the protection was last accessed at
     *
     * @return the unix epoch the protection was last accessed at
     */
    public int getAccessed() {
        return accessed;
    }

    /**
     * Get the state this protection is in
     *
     * @return
     */
    public State getState() {
        return state;
    }

    /**
     * Change the state this protection is in
     *
     * @param state
     */
    public void setState(State state) {
        this.state = state;
    }

    @Override
    public void saveImmediately() {
        if (state == State.MODIFIED) {
            engine.getDatabase().saveProtection(this);
            state = State.UNMODIFIED;
        }

        // sync metadata
        synchronized (metadata) {
            for (Map.Entry<Metadata, State> entry : metadataState.entrySet()) {
                Metadata meta = entry.getKey();
                State state = entry.getValue();

                if (state == State.NEW || state == State.MODIFIED) {
                    engine.getDatabase().saveOrCreateProtectionMetadata(this, meta);
                } else if (state == State.REMOVED) {
                    engine.getDatabase().removeProtectionMetadata(this, meta);
                }
            }

            metadataState.clear();
        }

        // sync roles
        for (Role role : getComponent(RoleSetComponent.class).getAll()) {
            engine.getDatabase().saveOrCreateProtectionRole(this, role);
        }
    }

    @Override
    public boolean isSaveNeeded() {
        return state == State.MODIFIED;
    }

    @Override
    public void remove() {
        engine.getDatabase().removeAllProtectionRoles(this);
        engine.getDatabase().removeAllProtectionMetadata(this);
        engine.getDatabase().removeProtection(this);
        state = State.REMOVED;
        metadataState.clear();
    }

    @Override
    public void save() {
        engine.getDatabase().saveLater(this);
    }

    /**
     * Access levels for protections. ordinal values are used here meaning they must remain in a constant order. As well,
     * the enum values are ranked in power of ascending order meaning Access(4) has more power than
     * Access(1) will. This also implies that the initial implementation is complete and that adding
     * any more access levels would be a pain.
     * <p/>
     * As well, the only exception to these rules is EXPLICIT_DENY which will immediately deny access to the
     * protection. This will not always be used but may be useful in some cases.
     */
    public static enum Access {

        /**
         * Immediately reject access to the protection.
         */
        EXPLICIT_DENY(I18n.markAsTranslatable("explicit_deny")),

        /**
         * User has NO access to the protection
         */
        NONE(I18n.markAsTranslatable("none")),

        /**
         * The user can view the protection but not modify it in any way. The implementation of this depends
         * on the mod and if the mod does not support preventing the inventory from being modified somehow
         * then access will just be blocked.
         */
        GUEST(I18n.markAsTranslatable("guest")),

        /**
         * User can only deposit into the protection
         */
        DEPOSITONLY(I18n.markAsTranslatable("depositonly")),

        /**
         * User can deposit and withdraw from the protection at will but not add or remove other users to it.
         */
        MEMBER(I18n.markAsTranslatable("member")),

        /**
         * User can modify the protection (add and remove members) but not add or remove other managers.
         */
        ADMIN(I18n.markAsTranslatable("admin")),

        /**
         * User has the same access as the user who created the protection. They can remove the protection,
         * add or remove ANY level to the protection (i.e. other owners) but they cannot remove themselves
         * from the protection
         */
        OWNER(I18n.markAsTranslatable("owner"));

        /**
         * Access levels that normal players can set
         */
        public final static EnumSet<Access> USABLE_ACCESS_LEVELS = EnumSet.range(NONE, OWNER);

        /**
         * Access levels that can view or interact with the protection
         */
        public final static EnumSet<Access> CAN_ACCESS = EnumSet.range(GUEST, OWNER);

        /**
         * The translated name for the enum
         */
        private String translatedName = null;

        Access(String translatedName) {
        }

        /**
         * Get the translated name of the access level
         *
         * @return translated name
         */
        public String getTranslatedName() {
            if (translatedName == null) {
                translatedName = _(toString().toLowerCase());
            }

            return translatedName;
        }

        /**
         * Match a {@link org.getlwc.model.Protection.Access} given a name.
         *
         * @param name
         * @return NULL if no {@link org.getlwc.model.Protection.Access} is matched
         */
        public static Access fromString(String name) {
            for (Access access : Access.values()) {
                if (access.toString().equalsIgnoreCase(name)) {
                    return access;
                }
            }

            return null;
        }

    }
}
