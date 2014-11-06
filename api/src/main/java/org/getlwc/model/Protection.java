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

import org.getlwc.*;
import org.getlwc.component.BasicComponentHolder;
import org.getlwc.component.Component;
import org.getlwc.component.RoleSetComponent;
import org.getlwc.entity.Entity;
import org.getlwc.entity.Player;
import org.getlwc.role.Role;

import java.util.*;

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
     * The map of attributes this protection contains
     */
    private final Map<String, AbstractAttribute> attributes = new HashMap<String, AbstractAttribute>();

    /**
     * The possible access providers the protection has
     */
    private final Set<AccessProvider> accessProviders = new HashSet<AccessProvider>();

    /**
     * The interact providers that want to know when this protection is interacted with
     */
    private final Set<InteractProvider> interactProviders = new HashSet<InteractProvider>();

    @Override
    public String toString() {
        // TODO add in updated, created
        return String.format("Protection(id=%d)", id);
    }

    public Protection(Engine engine, int id) {
        this.engine = engine;
        this.id = id;

        addComponent(new RoleSetComponent());

        for (AbstractAttribute<?> attribute : engine.getDatabase().loadProtectionAttributes(this)) {
            addAttribute(attribute);
        }

        for (Role role : engine.getDatabase().loadProtectionRoles(this)) {
            getComponent(RoleSetComponent.class).add(role);
        }
    }

    /**
     * Called when the protection is interacted with by a player
     *
     * @param access
     * @param entity
     */
    public void interactedBy(Entity entity, Access access) {
        for (InteractProvider provider : interactProviders) {
            provider.onInteract(this, entity, access);
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

        for (AccessProvider provider : accessProviders) {
            Access roleAccess = provider.getAccess(this, player);

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
     * Add an attribute to the protection
     *
     * @param attribute
     */
    @Deprecated
    public void addAttribute(AbstractAttribute attribute) {
        attributes.put(attribute.getName(), attribute);
        attribute.setState(State.NEW);
        state = State.MODIFIED;

        if (attribute instanceof AccessProvider) {
            accessProviders.add((AccessProvider) attribute);
        }

        if (attribute instanceof InteractProvider) {
            interactProviders.add((InteractProvider) attribute);
        }
    }

    /**
     * Get an attribute from the protection. If it does not exist, NULL will be returned.
     *
     * @param name
     * @return
     */
    @Deprecated
    public AbstractAttribute getAttribute(String name) {
        return attributes.get(name);
    }

    /**
     * Remove an attribute from the protection
     *
     * @param name
     */
    @Deprecated
    public void removeAttribute(String name) {
        if (!attributes.containsKey(name)) {
            return;
        }

        AbstractAttribute attribute = attributes.remove(name);
        engine.getDatabase().removeProtectionAttribute(this, attribute);

        if (attribute instanceof AccessProvider) {
            accessProviders.remove(attribute);
        }

        if (attribute instanceof InteractProvider) {
            interactProviders.remove(attribute);
        }
    }

    public void setUpdated(int updated) {
        this.updated = updated;
        state = State.MODIFIED;
    }

    public void setCreated(int created) {
        this.created = created;
        state = State.MODIFIED;
    }

    public void setModified(boolean modified) {
        state = State.MODIFIED;
    }

    public int getId() {
        return id;
    }

    public int getUpdated() {
        return updated;
    }

    public int getCreated() {
        return created;
    }

    public int getAccessed() {
        return accessed;
    }

    public void setAccessed(int accessed) {
        this.accessed = accessed;
        state = State.MODIFIED;
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

        // save each attribute
        for (AbstractAttribute<?> attribute : attributes.values()) {
            if (attribute.getState() == State.NEW || attribute.getState() == State.MODIFIED) {
                engine.getDatabase().saveOrCreateProtectionAttribute(this, attribute);
                attribute.setState(State.UNMODIFIED);
            }
        }

        // save each role
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

        // now remove the protection
        engine.getDatabase().removeProtection(this);
        state = State.REMOVED;
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
