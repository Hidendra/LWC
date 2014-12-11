package org.getlwc.sponge;

import org.getlwc.BlockType;
import org.getlwc.ItemType;
import org.getlwc.util.registry.AbstractMinecraftRegistry;
import org.spongepowered.api.Game;

public class SpongeMinecraftRegistry extends AbstractMinecraftRegistry {

    private Game game;

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
