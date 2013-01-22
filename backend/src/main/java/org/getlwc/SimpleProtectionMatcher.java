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

package org.getlwc;

public class SimpleProtectionMatcher implements ProtectionMatcher {

    /**
     * The LWC engine instance
     */
    private Engine engine;

    public SimpleProtectionMatcher(Engine engine) {
        this.engine = engine;
    }

    /**
     * {@inheritDoc}
     */
    public ProtectionSet matchProtection(Block base) {
        ProtectionSet blocks = new ProtectionSet(engine);

        int baseType = base.getType();

        // first add the base block, as it must exist on the protection if it matches
        blocks.add(base);

        // Double chest
        if (baseType == 54) {
            Block adjacentChest = base.findBlockRelativeToXZ(54);

            if (adjacentChest != null) {
                blocks.add(adjacentChest);
            }
        }

        // Doors (not the block below the door)
        else if (base.typeMatchesOneOf(64, 71)) {
            Block otherDoor = base.findBlockRelativeToY(64, 71);

            // add the other half of the door
            if (otherDoor != null) {
                blocks.add(otherDoor);
            }
        }

        // other
        else {
            // get the block above the current block (useful)
            Block above = base.getRelative(0, 1, 0);

            // door above the current block
            if (above.typeMatchesOneOf(64, 71)) {
                blocks.add(above);
                blocks.add(above.getRelative(0, 1, 0)); // top of the door
            }

        }

        for (ProtectionSet.BlockType type : ProtectionSet.BlockType.values()) {
            for (Block block : blocks.get(type)) {
                engine.getConsoleSender().sendMessage(type.toString() + " => " + block.toString());
            }
        }

        // check for a protection and return
        blocks.checkForProtections();
        return blocks;
    }

}
