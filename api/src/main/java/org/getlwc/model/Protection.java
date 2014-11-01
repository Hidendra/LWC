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

import org.getlwc.AccessProvider;
import org.getlwc.Engine;
import org.getlwc.InteractProvider;
import org.getlwc.entity.Entity;
import org.getlwc.entity.Player;
import org.getlwc.role.ProtectionRole;
import org.getlwc.role.Role;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class Protection extends AbstractSavable {

    /**
     * The type of protection. The ordering of the types should never change as the ordinal
     * value is used internally.
     */
    public enum Type {

        /**
         * Protection is protecting a block
         */
        BLOCK,

        /**
         * Protection is protecting an entity
         */
        ENTITY

    }

    /**
     * The LWC engine instance
     */
    private final Engine engine;

    /**
     * The protection's internal id
     */
    private int id;

    /**
     * The protection's type
     */
    private Type type;

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
     * A set of roles this protection contains
     */
    private final Set<ProtectionRole> roles = new HashSet<ProtectionRole>();

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
        super(engine);
        this.engine = engine;
        this.id = id;

        for (AbstractAttribute<?> attribute : engine.getDatabase().loadProtectionAttributes(this)) {
            addAttribute(attribute);
        }

        for (ProtectionRole role : engine.getDatabase().loadProtectionRoles(this)) {
            addRole(role);
        }
    }

    /**
     * Called when the protection is interacted with by a player
     *
     * @param access
     * @param entity
     */
    public void interactedBy(Entity entity, ProtectionRole.Access access) {
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
    public ProtectionRole.Access getAccess(Player player) {
        ProtectionRole.Access access = ProtectionRole.Access.NONE;

        for (AccessProvider provider : accessProviders) {
            ProtectionRole.Access roleAccess = provider.getAccess(this, player);

            if (roleAccess == null) {
                continue;
            }

            // check for immediate deny
            if (roleAccess == ProtectionRole.Access.EXPLICIT_DENY) {
                return roleAccess;
            }

            // compare the access -- higher access has higher precedence.
            if (roleAccess.ordinal() > access.ordinal()) {
                access = roleAccess;
            }
        }

        return access;
    }

    /**
     * Add a role to a protection. It will be allowed to govern access to this protection immediately
     * the next time the protection is used.
     *
     * @param role
     */
    public void addRole(ProtectionRole role) {
        roles.add(role);
        accessProviders.add(role);

        if (role instanceof InteractProvider) {
            interactProviders.add((InteractProvider) role);
        }
    }

    /**
     * Check if a role exists with the same name in this protection
     *
     * @param type
     * @param name
     * @return
     */
    public ProtectionRole getRole(String type, String name) {
        for (ProtectionRole role : roles) {
            if (role.getType().equals(type) && role.getName().equalsIgnoreCase(name)) {
                return role;
            }
        }

        return null;
    }

    /**
     * Remove a role from the protection, which will prevent it from governing access on the protection
     *
     * @param role
     */
    public void removeRole(ProtectionRole role) {
        roles.remove(role);
        accessProviders.remove(role);
        engine.getDatabase().removeRole(role);

        if (role instanceof InteractProvider) {
            interactProviders.remove(role);
        }
    }

    /**
     * Add an attribute to the protection
     *
     * @param attribute
     */
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
    public AbstractAttribute getAttribute(String name) {
        return attributes.get(name);
    }

    /**
     * Remove an attribute from the protection
     *
     * @param name
     */
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

    /**
     * Returns an unmodifiable {@link Set} of the roles this protection contains
     *
     * @return
     */
    public Set<ProtectionRole> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public void setType(Type type) {
        this.type = type;
        state = State.MODIFIED;
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

    public Type getType() {
        return type;
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
        for (Role role : roles) {
            if (role.isSaveNeeded()) {
                role.saveImmediately();
            }
        }
    }

    @Override
    public boolean isSaveNeeded() {
        return state == State.MODIFIED;
    }

    @Override
    public void remove() {
        // remove all roles for the protection
        for (Role role : roles) {
            role.remove();
        }

        // now remove the protection
        engine.getDatabase().removeProtection(this);
        state = State.REMOVED;
    }

}
