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

public class CanaryBlock extends org.getlwc.Block {

    /**
     * The block handle
     */
    private Block handle;

    /**
     * The local world handle
     */
    private org.getlwc.World world;

    public CanaryBlock(org.getlwc.World world, Block handle) {
        if (handle == null) {
            throw new IllegalArgumentException("Block handle cannot be null");
        }

        this.handle = handle;
        this.world = world;
    }

    /**
     * {@inheritDoc}
     */
    public int getType() {
        return handle.getType();
    }

    /**
     * {@inheritDoc}
     */
    public byte getData() {
        return (byte) handle.getData();
    }

    /**
     * {@inheritDoc}
     */
    public org.getlwc.World getWorld() {
        return world;
    }

    /**
     * {@inheritDoc}
     */
    public int getX() {
        return handle.getX();
    }

    /**
     * {@inheritDoc}
     */
    public int getY() {
        return handle.getY();
    }

    /**
     * {@inheritDoc}
     */
    public int getZ() {
        return handle.getZ();
    }

    /**
     * {@inheritDoc}
     */
    public void setType(int type) {
        handle.setType(type);
    }

    /**
     * {@inheritDoc}
     */
    public void setData(byte data) {
        handle.setData(data);
    }

}
