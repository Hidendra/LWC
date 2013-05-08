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
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR,
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

package org.getlwc.forge.event;

import net.minecraft.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;
import org.getlwc.Block;

import java.util.List;

public class EntityExplodeEvent extends EntityEvent {

    /**
     * The explosion x coordinate
     */
    private int explosionX;

    /**
     * The explosion y coordinate
     */
    private int explosionY;

    /**
     * The explosion z coordinate
     */
    private int explosionZ;

    /**
     * The explosion radius
     */
    private int radius;

    /**
     * A list of affected blocks
     */
    private List<Block> affectedBlocks;

    public EntityExplodeEvent(Entity entity, int explosionX, int explosionY, int explosionZ, int radius, List<Block> affectedBlocks) {
        super(entity);
        this.explosionX = explosionX;
        this.explosionY = explosionY;
        this.explosionZ = explosionZ;
        this.radius = radius;
        this.affectedBlocks = affectedBlocks;
    }

    public int getExplosionX() {
        return explosionX;
    }

    public int getExplosionY() {
        return explosionY;
    }

    public int getExplosionZ() {
        return explosionZ;
    }

    public int getRadius() {
        return radius;
    }

    public List<Block> getAffectedBlocks() {
        return affectedBlocks;
    }
}
