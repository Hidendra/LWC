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

import com.griefcraft.ProtectionAccess;
import com.griefcraft.ProtectionType;
import com.griefcraft.entity.Player;
import com.griefcraft.world.World;

public class Protection extends AbstractSavable {

    /**
     * The protection's internal id
     */
    private int id;

    /**
     * The protection's type
     */
    private ProtectionType type;

    /**
     * The protection's owner
     */
    private String owner;

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
     * The unix timestamp of when the protection was last updated and/or accessed

     */
    private int updated;

    /**
     * The unix timestamp of when the protection was created
     */
    private int created;

    /**
     * If the protection has been modified
     */
    private boolean modified = false;

    @Override
    public String toString() {
        // TODO add in updated, created
        return String.format("Protection(id=%d, owner=\"%s\", world=\"%s\", location=[%d, %d, %d])", id, owner, world, x, y, z);
    }

    public Protection(int id) {
        this.id = id;
    }

    /**
     * Check if a player is the owner of this protection
     *
     * @param player
     * @return
     */
    public boolean isOwner(Player player) {
        return player != null && player.getName().equals(owner);
    }

    /**
     * Get the {@link ProtectionAccess} level a player has to this protection
     * @param player
     * @return
     */
    public ProtectionAccess getAccess(Player player) {
        if (type == null) {
            return ProtectionAccess.DENY;
        }

        return type.getAccess(this, player);
    }


    public void setType(ProtectionType type) {
        this.type = type;
        modified = true;
    }

    public void setOwner(String owner) {
        this.owner = owner;
        modified = true;
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

    public ProtectionType getType() {
        return type;
    }

    public String getOwner() {
        return owner;
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

    @Override
    public void saveImmediately() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isSaveNeeded() {
        return modified;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not implemented");
    }

}
