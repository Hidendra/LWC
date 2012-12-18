/*
 * Copyright (c) 2011, 2012, Tyler Blair
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

package com.griefcraft.model;

import com.griefcraft.AccessProvider;
import com.griefcraft.Engine;
import com.griefcraft.ProtectionAccess;
import com.griefcraft.Role;
import com.griefcraft.World;
import com.griefcraft.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Protection extends AbstractSavable {

    /**
     * The LWC engine instance
     */
    private final Engine engine;

    /**
     * The protection's internal id
     */
    private int id;

    /**
     * The world this protection is in
     */
    private World world;

    /**
     * The x coordinate of the protection
     */
    private int x;

    /**
     * The y coordinate of the protection
     */
    private int y;

    /**
     * The z coordinate of the protection
     */
    private int z;

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
    private final Set<Role> roles = new HashSet<Role>();

    /**
     * The map of attributes this protection contains
     */
    private final Map<String, AbstractAttribute> attributes = new HashMap<String, AbstractAttribute>();

    /**
     * The possible access providers the protection has
     */
    private final Set<AccessProvider> accessProviders = new HashSet<AccessProvider>();

    @Override
    public String toString() {
        // TODO add in updated, created
        return String.format("Protection(id=%d, world=\"%s\", location=[%d, %d, %d])", id, world, x, y, z);
    }

    public Protection(Engine engine, int id) {
        super(engine);
        this.engine = engine;
        this.id = id;

        for (AbstractAttribute<?> attribute : engine.getDatabase().loadProtectionAttributes(this)) {
            addAttribute(attribute);
        }

        for (Role role : engine.getDatabase().loadProtectionRoles(this)) {
            addRole(role);
        }
    }

    /**
     * Get the access level a player has to this protection
     *
     * @param player
     * @return
     */
    public ProtectionAccess getAccess(Player player) {
        ProtectionAccess access = ProtectionAccess.NONE;

        for (AccessProvider provider : accessProviders) {
            ProtectionAccess roleAccess = provider.getAccess(this, player);

            if (roleAccess == null) {
                continue;
            }

            // check for immediate deny
            if (roleAccess == ProtectionAccess.EXPLICIT_DENY) {
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
    public void addRole(Role role) {
        roles.add(role);
        accessProviders.add(role);
    }

    /**
     * Remove a role from the protection, which will prevent it from governing access on the protection
     *
     * @param role
     */
    public void removeRole(Role role) {
        roles.remove(role);
        accessProviders.remove(role);
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
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public void setX(int x) {
        this.x = x;
        state = State.MODIFIED;
    }

    public void setY(int y) {
        this.y = y;
        state = State.MODIFIED;
    }

    public void setZ(int z) {
        this.z = z;
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

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public World getWorld() {
        return world;
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
