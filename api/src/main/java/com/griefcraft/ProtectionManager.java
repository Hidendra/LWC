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

package com.griefcraft;

import com.griefcraft.attribute.ProtectionAttributeFactory;
import com.griefcraft.entity.Player;
import com.griefcraft.model.AbstractAttribute;
import com.griefcraft.model.Protection;
import com.griefcraft.world.Block;
import com.griefcraft.world.Location;

public interface ProtectionManager {

    /**
     * Check if the given block can be protected
     *
     * @param block
     * @return true if the block can be protected
     */
    public boolean isBlockProtectable(Block block);

    /**
     * Find a protection at the given location
     *
     * @param location
     * @return
     */
    public Protection findProtection(Location location);

    /**
     * Create a protection in the world
     *
     * @param owner
     * @param location
     * @return
     */
    public Protection createProtection(String owner, Location location);

    /**
     * The method that is called in the event no events cancel a player interact call.
     *
     * @param protection
     * @param player
     * @return
     */
    public boolean defaultPlayerInteractAction(Protection protection, Player player);

    /**
     * Register an attribute factory that is used to create {@link com.griefcraft.model.AbstractAttribute} objects
     * for protections when they are loaded.
     *
     * @param factory
     */
    public void registerAttributeFactory(ProtectionAttributeFactory factory);

    /**
     * Create a protection attribute for the given name
     *
     * @param name
     * @return
     */
    public AbstractAttribute createProtectionAttribute(String name);

}
