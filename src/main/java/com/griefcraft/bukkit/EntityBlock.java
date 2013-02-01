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

package com.griefcraft.bukkit;

import org.bukkit.World;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.entity.Entity;

public class EntityBlock extends CraftBlock {

    /**
     * The entity block id
     */
    public static final int ENTITY_BLOCK_ID = 5000;

    /**
     * The position offset to use in the database
     */
    public static final int POSITION_OFFSET = 50000;

    /**
     * The entity this protection is for
     */
    private Entity entity;

    public EntityBlock(Entity entity) {
        super(null, 0, 0, 0);
        this.entity = entity;
    }

    @Override
    public int getX() {
        return POSITION_OFFSET + entity.getUniqueId().hashCode();
    }

    @Override
    public int getY() {
        return POSITION_OFFSET + entity.getUniqueId().hashCode();
    }

    @Override
    public int getZ() {
        return POSITION_OFFSET + entity.getUniqueId().hashCode();
    }

    @Override
    public int getTypeId() {
        return 5000;
    }

    @Override
    public World getWorld() {
        return entity.getWorld();
    }

    public Entity getEntity() {
        return entity;
    }

}
