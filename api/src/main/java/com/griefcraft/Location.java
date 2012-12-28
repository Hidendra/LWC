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

public final class Location {

    /**
     * The world this location is situated in
     */
    private final World world;

    /**
     * The x coordinate
     */
    private final double x;

    /**
     * The y coordinate
     */
    private final double y;

    /**
     * The z coordinate
     */
    private final double z;

    public Location(World world, double x, double y, double z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Location)) {
            return false;
        }

        Location l = (Location) o;
        return l.getWorld().getName().equals(world.getName()) && x == l.x && y == l.y && z == l.z;
    }

    @Override
    public int hashCode() {
        int hash = world.getName().hashCode();
        hash *= 17 + x;
        hash *= 17 + y;
        hash *= 17 + z;
        return hash;
    }

    /**
     * Get the block x coordinate
     *
     * @return
     */
    public int getBlockX() {
        return (int) x;
    }

    /**
     * Get the block y coordinate
     *
     * @return
     */
    public int getBlockY() {
        return (int) y;
    }

    /**
     * Get the block z coordinate
     *
     * @return
     */
    public int getBlockZ() {
        return (int) z;
    }

    /**
     * Get the world the location is in
     *
     * @return
     */
    public World getWorld() {
        return world;
    }

    /**
     * Get the x coordinate
     *
     * @return
     */
    public double getX() {
        return x;
    }

    /**
     * Get the y coordinate
     *
     * @return
     */
    public double getY() {
        return y;
    }

    /**
     * Get the z coordinate
     *
     * @return
     */
    public double getZ() {
        return z;
    }

}
