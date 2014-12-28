/**
 * Copyright (c) 2011-2014 Tyler Blair
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
package org.getlwc.sponge;

import org.getlwc.BlockType;
import org.getlwc.ItemType;
import org.getlwc.util.registry.AbstractMinecraftRegistry;
import org.spongepowered.api.Game;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SpongeMinecraftRegistry extends AbstractMinecraftRegistry {

    private Game game;

    @Inject
    public SpongeMinecraftRegistry(Game game) {
        this.game = game;
    }

    @Override
    protected BlockType internalGetBlockType(String id) {
        final org.spongepowered.api.block.BlockType type = game.getRegistry().getBlock(id).orNull();

        if (type == null) {
            return null;
        } else {
            return new BlockType() {
                @Override
                public String getId() {
                    return type.getId();
                }

                @Override
                public int getLegacyId() {
                    throw new UnsupportedOperationException("Integer block ids are not supported.");
                }

                @Override
                public String getName() {
                    return type.getTranslation().get();
                }
            };
        }
    }

    @Override
    protected ItemType internalGetItemType(String id) {
        final org.spongepowered.api.item.ItemType type = game.getRegistry().getItem(id).orNull();

        if (type == null) {
            return null;
        } else {
            return new ItemType() {
                @Override
                public String getId() {
                    return type.getId();
                }

                @Override
                public int getLegacyId() {
                    throw new UnsupportedOperationException("Integer block ids are not supported.");
                }

                @Override
                public int getMaxStackQuantity() {
                    return type.getMaxStackQuantity();
                }

                @Override
                public String getName() {
                    return type.getTranslation().get();
                }
            };
        }
    }

    @Override
    public BlockType getLegacyBlockType(int id) {
        throw new UnsupportedOperationException("Integer block ids are not supported.");
    }

    @Override
    public ItemType getLegacyItemType(int id) {
        throw new UnsupportedOperationException("Integer block ids are not supported.");
    }

}
