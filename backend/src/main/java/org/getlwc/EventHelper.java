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
package org.getlwc;

import org.getlwc.entity.Entity;
import org.getlwc.entity.Player;
import org.getlwc.event.Event;
import org.getlwc.event.block.BlockInteractEvent;
import org.getlwc.event.protection.ProtectionInteractEvent;
import org.getlwc.lang.Locale;
import org.getlwc.lang.MessageStore;
import org.getlwc.model.Protection;

import java.util.List;
import java.util.Map;

public class EventHelper {

    /**
     * The {@link Engine} instance
     */
    private static SimpleEngine engine;

    static {
        engine = SimpleEngine.getInstance();
    }

    private EventHelper() { }

    /**
     * A generic method that checks if an entity is allowed to interact with the given block.
     *
     * @param entity
     * @param block
     * @return true if the given entity can access the block (i.e. no protection there OR they can access the protection)
     */
    private static boolean silentAccessCheck(Entity entity, Block block) {
        Protection protection = engine.getProtectionManager().loadProtection(block.getLocation());

        if (protection == null) {
            return true;
        }

        if (entity instanceof Player) {
            Player player = (Player) entity;

            Protection.Access access = protection.getAccess(player);

            if (access.ordinal() > Protection.Access.NONE.ordinal()) {
                return true;
            }

            return false;
        } else {
            // throw new UnsupportedOperationException("Unsupported Entity: " + entity.getClass().getSimpleName());
            return false;
        }
    }

    /**
     * Called when a player joins the server
     *
     * @param player
     */
    public static void onPlayerJoin(Player player) {
        if (player.hasPermission("lwc.admin")) { // TODO not a hardcoded permission?
            MessageStore store = I18n.getMessageStore();
            Locale defaultLocale = store.getDefaultLocale();
            Locale playerLocale = player.getLocale();

            if (!store.supports(playerLocale)) {
                if (engine.getConfiguration().getBoolean("notifications.missingTranslation", true)) {
                    /// TL: The double apostrophe is to work around a bug in a library.
                    ///     If you need to use it please leave it as a single apostrophe.
                    player.sendTranslatedMessage("Your client''s locale {0} is not supported by LWC. {1} will be used instead.\nWant to help translate LWC? Go to: &3&nhttp://translate.getlwc.org&r", playerLocale.getName(), defaultLocale.getName());
                }
            } else {
                if (engine.getConfiguration().getBoolean("notifications.recentlyTranslated", true)) {
                    int addedOn = engine.getLanguagesConfiguration().getInt("languages." + playerLocale.getName() + ".addedOn", -1);

                    int days30 = 86400 * 30;

                    if (addedOn > ((System.currentTimeMillis() / 1000) - days30)) {
                        /// TL: The double apostrophe is to work around a bug in a library.
                        ///     If you need to use it please leave it as a single apostrophe.
                        player.sendTranslatedMessage("Your client''s locale {0} was recently translated for LWC. It may not be 100% accurate. \nWant to help translate/proofread LWC? Go to: &3&nhttp://translate.getlwc.org&r", playerLocale.getName());
                    }
                }
            }
        }
    }

    /**
     * Called when a player quits the server
     *
     * @param player
     */
    public static void onPlayerQuit(Player player) {
        engine.getServerLayer().removePlayer(player.getName());
    }

    /**
     * Called when an entity interacts with another entity in the world
     *
     * @param entity
     * @param target
     * @return
     */
    public static boolean onEntityInteract(Entity entity, Entity target) {
        //
        return false;
    }

    /**
     * Called when an entity interacts with a block in the world
     *
     * @param entity
     * @param block
     * @return true if the event is to be cancelled, false otherwise
     */
    public static boolean onBlockInteract(Entity entity, Block block) {
        boolean cancel;

        // Match the block to a protection
        Protection protection = engine.getProtectionManager().loadProtection(block.getLocation());
        engine.getConsoleSender().sendMessage("Protection found: " + protection);

        if (entity instanceof Player) {
            Player player = (Player) entity;

            try {
                Event event;

                if (protection == null) {
                    event = new BlockInteractEvent(block);
                } else {
                    event = new ProtectionInteractEvent(protection);
                }

                engine.getEventBus().post(event);
                cancel = event.isCancelled();

                // default event action
                if (!cancel && protection != null) {
                    Protection.Access access = protection.getAccess(player);

                    /// TODO distinguish between left / right click.

                    // check if they can access the protection
                    if (access.ordinal() > Protection.Access.NONE.ordinal()) {
                        return false;
                    }

                    // they cannot access the protection o\
                    // so send them a kind message
                    if (access != Protection.Access.EXPLICIT_DENY) {
                        player.sendTranslatedMessage("&4This protection is locked by a magical spell.");
                    }

                    return true;
                }
            } catch (Exception e) {
                /// {0}: message from the exception/error that was thrown
                player.sendTranslatedMessage("&cA severe error occurred while processing the event: {0}\n"
                        + "&cThe stack trace has been printed out to the console.", e.getMessage());
                e.printStackTrace();
                return true; // Better safe than sorry
            }
        } else {
            // throw new UnsupportedOperationException("Unsupported Entity: " + (entity != null ? entity.getClass().getSimpleName() : "null"));
            cancel = !silentAccessCheck(entity, block);
        }

        return cancel;
    }

    /**
     * Called when an entity breaks a block in the world
     *
     * @param entity
     * @param block
     * @return
     */
    public static boolean onBlockBreak(Entity entity, Block block) {
        return !silentAccessCheck(entity, block);
    }

    /**
     * Called when an entity places a block in the world
     *
     * @param entity
     * @param block
     * @return
     */
    public static boolean onBlockPlace(Entity entity, Block block) {
        System.out.println("[internal] Block placed @ " + block);

        // Nothing yet.
        // - blacklisting blocks
        // - match neighbouring protection (e.g. double chest). if there is one there don't do anything
        // - auto protect
        return false;
    }

    /**
     * Called when an entity changes text on a sign
     *
     * @param entity
     * @param block
     * @return
     */
    public static boolean onSignChange(Entity entity, Block block) {
        System.out.println("[internal] Sign changed @ " + block);

        return !silentAccessCheck(entity, block);
    }

    /**
     * Called when an explosion occurs in the world
     *
     * @param type
     * @param blocksAffected
     * @return
     */
    public static boolean onExplosion(ExplosionType type, List<Block> blocksAffected) {
        System.out.println("[internal] Explosion occurred! type=" + type + " blocks affected=" + blocksAffected.size());
        // TODO

        return false;
    }

    /**
     * Called when the redstone level changes on a block
     *
     * @param block
     * @param oldLevel
     * @param newLevel
     * @return
     */
    public static boolean onRedstoneChange(Block block, int oldLevel, int newLevel) {
        ProtectionManager manager = engine.getProtectionManager();

        if (!manager.isBlockProtectable(block)) {
            return false;
        }

        Protection protection = manager.loadProtection(block.getLocation());

        /**
         * TODO globally disable redstone on protections for now
         */
        if (protection != null) {
            return true;
        }

        return false;
    }

    /**
     * Called when a move item event occurs at the given location
     *
     * @param location
     * @return
     */
    public static boolean onInventoryMoveItem(Location location) {
        System.out.println("[internal] onInventoryMoveItem @ " + location);
        return true;
    }

    /**
     * Called when a player clicks a slot in an inventory
     *
     * @param player
     * @param location
     * @param clicked
     * @param rightClick
     * @param shiftClick
     * @param doubleClick
     * @return
     */
    public static boolean onInventoryClickItem(Player player, Location location, ItemStack clicked, ItemStack cursor, int slot, int rawSlot, boolean rightClick, boolean shiftClick, boolean doubleClick) {
        if (!doubleClick) {
            // Nifty trick: these will different IFF they are interacting with the player's inventory or hotbar instead of the block's inventory
            if (slot != rawSlot) {
                return false;
            }

            if (clicked == null || clicked.getType() == 0) {
                return false;
            }

            // if it's not a right click or a shift click it should be a left click (no shift)
            // this is for when players are INSERTing items (i.e. item in hand and left clicking)
            if (player.getItemInHand() == null && (!rightClick && !shiftClick)) {
                return false;
            }

            // Are they inserting a stack?
            if (cursor != null && clicked.getType() == cursor.getType()) {
                boolean enchantmentsEqual = areEnchantmentsEqual(clicked, cursor);

                // If they are clicking an item of the stack type, they are inserting it into the inventory,
                // not switching it
                // As long as the item isn't a degradable item, we can explicitly allow it if they have the same durability
                if (clicked.getDurability() == cursor.getDurability() && clicked.getAmount() == cursor.getAmount() && enchantmentsEqual) {
                    return false;
                }
            }
        }

        // check for protection
        // check for DEPOSITONLY, etc
        System.out.println("[internal] onInventoryClickItem() Passed deposit checks");

        return true;
    }

    /**
     * Called when a piston extends
     *
     * @param piston
     * @param extending the location the piston will push
     * @return
     */
    public static boolean onPistonExtend(Block piston, Location extending) {
        Block block = extending.getBlock();

        if (block.hasTileEntity()) {
            return false;
        }

        Protection protection = engine.getProtectionManager().loadProtection(extending);
        return protection != null;
    }

    /**
     * Called when a piston retracts
     *
     * @param piston
     * @param retracting the location the piston will retract from (or attempt to pull if sticky)
     * @return
     */
    public static boolean onPistonRetract(Block piston, Location retracting) {
        if (piston.isOneOf("minecraft:sticky_piston")) {
            // A piston that cannot pull anything when retracting *should* be harmless so they can simply be globally allowed
            return false;
        }

        Block block = retracting.getBlock();

        if (block.hasTileEntity()) {
            return false;
        }

        Protection protection = engine.getProtectionManager().loadProtection(retracting);
        return protection != null;
    }

    /**
     * Compares the enchantments on two item stacks and checks that they are equal (identical)
     *
     * @param stack1
     * @param stack2
     * @return
     */
    private static boolean areEnchantmentsEqual(ItemStack stack1, ItemStack stack2) {
        if (stack1 == null || stack2 == null) {
            return false;
        }

        Map<Integer, Integer> enchantments1 = stack1.getEnchantments();
        Map<Integer, Integer> enchantments2 = stack2.getEnchantments();

        if (enchantments1.size() != enchantments2.size()) {
            return false;
        }

        for (Integer enchantment : enchantments1.keySet()) {
            if (!enchantments2.containsKey(enchantment)) {
                return false;
            }

            int level1 = enchantments1.get(enchantment);
            int level2 = enchantments2.get(enchantment);

            if (level1 != level2) {
                return false;
            }
        }

        return true;
    }

}
