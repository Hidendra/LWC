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

package com.griefcraft.listeners;

import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.model.Flag;
import com.griefcraft.model.LWCPlayer;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.Module;
import com.griefcraft.scripting.event.LWCBlockInteractEvent;
import com.griefcraft.scripting.event.LWCDropItemEvent;
import com.griefcraft.scripting.event.LWCProtectionInteractEvent;
import com.griefcraft.util.UUIDRegistry;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Hopper;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Set;

public class LWCPlayerListener implements Listener {

    /**
     * The plugin instance
     */
    private LWCPlugin plugin;

    public LWCPlayerListener(LWCPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUIDRegistry.updateCache(player.getUniqueId(), player.getName());
    }

    @EventHandler(ignoreCancelled = true)
    public void onMoveItem(InventoryMoveItemEvent event) {
        boolean result;

        // if the initiator is the same as the source it is a dropper i.e. depositing items
        if (event.getInitiator() == event.getSource()) {
            result = handleMoveItemEvent(event.getInitiator(), event.getDestination());
        } else {
            result = handleMoveItemEvent(event.getInitiator(), event.getSource());
        }

        if (result) {
            event.setCancelled(true);
        }
    }

    /**
     * Handle the item move event
     *
     * @param inventory
     */
    private boolean handleMoveItemEvent(Inventory initiator, Inventory inventory) {
        LWC lwc = LWC.getInstance();

        if (inventory == null) {
            return false;
        }

        Location location;
        InventoryHolder holder;
        Location hopperLocation = null;
        InventoryHolder hopperHolder;

        try {
            holder = inventory.getHolder();
            hopperHolder = initiator.getHolder();
        } catch (AbstractMethodError e) {
            return false;
        }

        try {
            if (holder instanceof BlockState) {
                location = ((BlockState) holder).getLocation();
            } else if (holder instanceof DoubleChest) {
                location = ((DoubleChest) holder).getLocation();
            } else {
                return false;
            }

            if (hopperHolder instanceof Hopper) {
                hopperLocation = ((Hopper) hopperHolder).getLocation();
            }
        } catch (Exception e) {
            return false;
        }

        // High-intensity zone: increase protection cache if it's full, otherwise
        // the database will be getting rammed
        lwc.getProtectionCache().increaseIfNecessary();

        // Attempt to load the protection at that location
        Protection protection = lwc.findProtection(location);

        // If no protection was found we can safely ignore it
        if (protection == null) {
            return false;
        }

        if (hopperLocation != null && Boolean.parseBoolean(lwc.resolveProtectionConfiguration(Material.HOPPER, "enabled"))) {
            Protection hopperProtection = lwc.findProtection(hopperLocation);

            if (hopperProtection != null) {
                // if they're owned by the same person then we can allow the move
                if (protection.getOwner().equals(hopperProtection.getOwner())) {
                    return false;
                }
            }
        }

        boolean denyHoppers = Boolean.parseBoolean(lwc.resolveProtectionConfiguration(Material.getMaterial(protection.getBlockId()), "denyHoppers"));

        // xor = (a && !b) || (!a && b)
        if (denyHoppers ^ protection.hasFlag(Flag.Type.HOPPER)) {
            return true;
        }

        return false;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (event.isCancelled() || !LWC.ENABLED) {
            return;
        }

        Player player = event.getPlayer();

        LWCDropItemEvent evt = new LWCDropItemEvent(player, event);
        plugin.getLWC().getModuleLoader().dispatchEvent(evt);

        if (evt.isCancelled()) {
            event.setCancelled(true);
        }
    }

/*
    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        if (event.isCancelled() || !LWC.ENABLED) {
            return;
        }

        LWC lwc = plugin.getLWC();
        if (!lwc.getConfiguration().getBoolean("core.filterunlock", true)) {
            return;
        }

        // We want to block messages starting with cunlock incase someone screws up /cunlock password.
        String message = event.getMessage();

        if (message.startsWith("cunlock") || message.startsWith("lcunlock") || message.startsWith(".cunlock")) {
            event.setCancelled(true);
            lwc.sendLocale(event.getPlayer(), "lwc.blockedmessage");
        }
    }
*/

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!LWC.ENABLED) {
            return;
        }

        LWC lwc = plugin.getLWC();
        Player player = event.getPlayer();
        LWCPlayer lwcPlayer = lwc.wrapPlayer(player);

        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        BlockState state;

        try {
            state = block.getState();
        } catch (NullPointerException e) {
            //
            lwc.log("Invalid Tile Entity detected at " + block.getLocation());
            lwc.log("This is either an issue with your world or a bug in Bukkit");
            return;
        }

        // Prevent players with lwc.deny from interacting with blocks that have an inventory
        if (state instanceof InventoryHolder && lwc.isProtectable(block)) {
            if (!lwc.hasPermission(player, "lwc.protect") && lwc.hasPermission(player, "lwc.deny") && !lwc.isAdmin(player) && !lwc.isMod(player)) {
                lwc.sendLocale(player, "protection.interact.error.blocked");
                event.setCancelled(true);
                return;
            }
        }

        try {
            Set<String> actions = lwcPlayer.getActionNames();
            Protection protection = lwc.findProtection(block.getLocation());
            Module.Result result;
            boolean canAccess = lwc.canAccessProtection(player, protection);

            // Calculate if the player has a pending action (i.e any action besides 'interacted')
            int actionCount = actions.size();
            boolean hasInteracted = actions.contains("interacted");
            boolean hasPendingAction = (hasInteracted && actionCount > 1) || (!hasInteracted && actionCount > 0);

            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                boolean ignoreLeftClick = Boolean.parseBoolean(lwc.resolveProtectionConfiguration(block, "ignoreLeftClick"));

                if (ignoreLeftClick) {
                    return;
                }
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                boolean ignoreRightClick = Boolean.parseBoolean(lwc.resolveProtectionConfiguration(block, "ignoreRightClick"));

                if (ignoreRightClick) {
                    return;
                }
            }

            // If the event was cancelled and they have an action, warn them
            if (event.isCancelled()) {
                // only send it if a non-"interacted" action is set which is always set on the player
                if (hasPendingAction) {
                    lwc.sendLocale(player, "lwc.pendingaction");
                }

                // it's cancelled, do not continue !
                return;
            }

            // register in an action what protection they interacted with (if applicable.)
            if (protection != null) {
                com.griefcraft.model.Action action = new com.griefcraft.model.Action();
                action.setName("interacted");
                action.setPlayer(lwcPlayer);
                action.setProtection(protection);

                lwcPlayer.addAction(action);
            }

            // events are only used when they already have an action pending
            boolean canAdmin = lwc.canAdminProtection(player, protection);

            if (protection != null) {
                LWCProtectionInteractEvent evt = new LWCProtectionInteractEvent(event, protection, actions, canAccess, canAdmin);
                lwc.getModuleLoader().dispatchEvent(evt);

                result = evt.getResult();
            } else {
                LWCBlockInteractEvent evt = new LWCBlockInteractEvent(event, block, actions);
                lwc.getModuleLoader().dispatchEvent(evt);

                result = evt.getResult();
            }

            if (result == Module.Result.ALLOW) {
                return;
            }

            // optional.onlyProtectIfOwnerIsOnline
            if (protection != null && !canAccess && lwc.getConfiguration().getBoolean("optional.onlyProtectWhenOwnerIsOnline", false)) {
                Player owner = protection.getBukkitOwner();

                // If they aren't online, allow them in :P
                if (owner == null || !owner.isOnline()) {
                    return;
                }
            }

            // optional.onlyProtectIfOwnerIsOffline
            if (protection != null && !canAccess && lwc.getConfiguration().getBoolean("optional.onlyProtectWhenOwnerIsOffline", false)) {
                Player owner = protection.getBukkitOwner();

                // If they aren't online, allow them in :P
                if (owner != null && owner.isOnline()) {
                    return;
                }
            }

            if (result == Module.Result.DEFAULT) {
                canAccess = lwc.enforceAccess(player, protection, block, canAccess);
            }

            if (!canAccess || result == Module.Result.CANCEL) {
                event.setCancelled(true);
                event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
            }
        } catch (Exception e) {
            event.setCancelled(true);
            event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
            lwc.sendLocale(player, "protection.internalerror", "id", "PLAYER_INTERACT");
            e.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!LWC.ENABLED) {
            return;
        }

        // remove the place from the player cache and reset anything they can access
        LWCPlayer.removePlayer(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        LWC lwc = LWC.getInstance();

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        // Player interacting with the inventory
        Player player = (Player) event.getWhoClicked();

        // The inventory they are using
        Inventory inventory = event.getInventory();

        if (inventory == null || event.getSlot() < 0) {
            return;
        }

        // Location of the container
        Location location;
        InventoryHolder holder = null;

        try {
            holder = event.getInventory().getHolder();
        } catch (AbstractMethodError e) {
            lwc.log("Caught issue with Bukkit's Inventory.getHolder() method! This is occuring NEAR the player: " + player.getName());
            lwc.log("This player is located at: " + player.getLocation().toString());
            lwc.log("This should be reported to the Bukkit developers.");
            e.printStackTrace();
            return;
        }

        try {
            if (holder instanceof BlockState) {
                location = ((BlockState) holder).getLocation();
            } else if (holder instanceof DoubleChest) {
                location = ((DoubleChest) holder).getLocation();
            } else {
                return;
            }
        } catch (Exception e) {
            Location ploc = player.getLocation();
            String holderName = holder != null ? holder.getClass().getSimpleName() : "Unknown Block";
            lwc.log("Exception with getting the location of a " + holderName + " has occurred NEAR the player: " + player.getName() + " [" + ploc.getBlockX() + " " + ploc.getBlockY() + " " + ploc.getBlockZ() + "]");
            lwc.log("The exact location of the block is not possible to obtain. This is caused by a Minecraft or Bukkit exception normally.");
            e.printStackTrace();
            return;
        }

        if (event.getAction() != InventoryAction.COLLECT_TO_CURSOR) {
            // If it's not a container, we don't want it
            if (event.getSlotType() != InventoryType.SlotType.CONTAINER) {
                return;
            }

            // Nifty trick: these will different IFF they are interacting with the player's inventory or hotbar instead of the block's inventory
            if (event.getSlot() != event.getRawSlot()) {
                return;
            }

            // The item they are taking/swapping with
            ItemStack item;

            try {
                item = event.getCurrentItem();
            } catch (ArrayIndexOutOfBoundsException e) {
                return;
            }

            // Item their cursor has
            ItemStack cursor = event.getCursor();

            if (item == null || item.getType() == null || item.getType() == Material.AIR) {
                return;
            }

            // if it's not a right click or a shift click it should be a left click (no shift)
            // this is for when players are INSERTing items (i.e. item in hand and left clicking)
            if (player.getItemInHand() == null && (!event.isRightClick() && !event.isShiftClick())) {
                return;
            }

            // Are they inserting a stack?
            if (cursor != null && item.getType() == cursor.getType()) {
                boolean enchantmentsEqual = areEnchantmentsEqual(item, cursor);

                // If they are clicking an item of the stack type, they are inserting it into the inventory,
                // not switching it
                // As long as the item isn't a degradable item, we can explicitly allow it if they have the same durability
                if (item.getDurability() == cursor.getDurability() && item.getAmount() == cursor.getAmount() && enchantmentsEqual) {
                    return;
                }
            }
        }

        // Attempt to load the protection at that location
        Protection protection = lwc.findProtection(location);

        // If no protection was found we can safely ignore it
        if (protection == null) {
            return;
        }

        // If it's not a donation chest, ignore if
        if (protection.getType() != Protection.Type.DONATION) {
            return;
        }

        // Can they admin it? (remove items/etc)
        boolean canAdmin = lwc.canAdminProtection(player, protection);

        // nope.avi
        if (!canAdmin) {
            event.setCancelled(true);
        }
    }

    /**
     * Compares the enchantments on two item stacks and checks that they are equal (identical)
     *
     * @param stack1
     * @param stack2
     * @return
     */
    private boolean areEnchantmentsEqual(ItemStack stack1, ItemStack stack2) {
        if (stack1 == null || stack2 == null) {
            return false;
        }

        Map<Enchantment, Integer> enchantments1 = stack1.getEnchantments();
        Map<Enchantment, Integer> enchantments2 = stack2.getEnchantments();

        if (enchantments1.size() != enchantments2.size()) {
            return false;
        }

        // Enchanted Books use ItemMeta
        if (stack1.getItemMeta() != null && stack2.getItemMeta() != null) {
            if (!stack1.getItemMeta().equals(stack2.getItemMeta())) {
                return false;
            }
        }

        for (Enchantment enchantment : enchantments1.keySet()) {
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
