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

package com.griefcraft.api;

import com.griefcraft.api.world.World;

public interface Protection {

    /**
     * The protection's type
     */
    public enum Type {

        /**
         * The protection is private and only the player or those the player allows can access it
         */
        PRIVATE,

        /**
         * Anyone can access and use the protection but not remove it
         */
        PUBLIC,

        /**
         * The protection requires a password from anyone to enter it
         */
        PASSWORD

    }

    /**
     * Get the protection's internal database id
     *
     * @return
     */
    public int getId();

    /**
     * Get the protection's x coordinate
     * 
     * @return
     */
    public int getX();

    /**
     * Get the protection's y coordinate
     * 
     * @return
     */
    public int getY();

    /**
     * Get the protection's z coordinate
     * 
     * @return
     */
    public int getZ();

    /**
     * Get the world the protection is in
     *
     * @return
     */
    public World getWorld();

    /**
     * Save the protection to the database
     */
    public void save();

    /**
     * Remove the protection from the database
     */
    public void remove();

}
