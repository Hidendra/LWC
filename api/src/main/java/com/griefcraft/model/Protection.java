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

import com.griefcraft.Engine;
import com.griefcraft.ProtectionAccess;
import com.griefcraft.entity.Player;
import com.griefcraft.world.World;

import java.util.HashSet;
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
     * If the protection has been modified
     */
    private boolean modified = false;

    /**
     * A set of roles this protection contains
     */
    private final Set<Role> roles = new HashSet<Role>();

    @Override
    public String toString() {
        // TODO add in updated, created
        return String.format("Protection(id=%d, world=\"%s\", location=[%d, %d, %d])", id, world, x, y, z);
    }

    public Protection(Engine engine, int id) {
        this.engine = engine;
        this.id = id;
    }

    /**
     * Get the access level a player has to this protection
     *
     * @param player
     * @return
     */
    public ProtectionAccess getAccess(Player player) {
        ProtectionAccess access = ProtectionAccess.NONE;

        for (Role role : roles) {
            ProtectionAccess roleAccess = role.getAccess(this, player);

            if (roleAccess == null) {
                continue;
            }

            // check for immediate deny
            if (roleAccess == ProtectionAccess.EXPLICIT_DENY) {
                return ProtectionAccess.NONE;
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
    }

    /**
     * Remove a role from the protection, which will prevent it from governing access on the protection
     *
     * @param role
     */
    public void removeRole(Role role) {
        roles.remove(role);
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public void setX(int x) {
        this.x = x;
        modified = true;
    }

    public void setY(int y) {
        this.y = y;
        modified = true;
    }

    public void setZ(int z) {
        this.z = z;
        modified = true;
    }

    public void setUpdated(int updated) {
        this.updated = updated;
        modified = true;
    }

    public void setCreated(int created) {
        this.created = created;
        modified = true;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
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
        modified = true;
    }

    @Override
    public void saveImmediately() {
        engine.getDatabase().saveProtection(this);
        modified = false;
    }

    @Override
    public boolean isSaveNeeded() {
        return modified;
    }

    @Override
    public void remove() {
        // remove all roles for the protection
        for (Role role : roles) {
            role.remove();
        }

        // now remove the protection
        engine.getDatabase().removeProtection(this);
        modified = false;
    }

}
