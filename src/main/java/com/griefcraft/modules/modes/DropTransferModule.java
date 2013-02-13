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

package com.griefcraft.modules.modes;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Action;
import com.griefcraft.model.LWCPlayer;
import com.griefcraft.model.Mode;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCBlockInteractEvent;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.scripting.event.LWCDropItemEvent;
import com.griefcraft.scripting.event.LWCProtectionInteractEvent;
import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Set;

public class DropTransferModule extends JavaModule {

    private LWC lwc;

    @Override
    public void load(LWC lwc) {
        this.lwc = lwc;
    }

    /**
     * Check if the player is currently drop transferring
     *
     * @param player
     * @return
     */
    private boolean isPlayerDropTransferring(LWCPlayer player) {
        return player.hasMode("+dropTransfer");
    }

    /**
     * Get the drop transfer target for a player
     *
     * @param player
     * @return
     */
    private int getPlayerDropTransferTarget(LWCPlayer player) {
        Mode mode = player.getMode("dropTransfer");

        if (mode == null) {
            return -1;
        }

        String target = mode.getData();

        try {
            return Integer.parseInt(target);
        } catch (NumberFormatException e) {
        }

        return -1;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onDropItem(LWCDropItemEvent event) {
        Player bPlayer = event.getPlayer();
        Item item = event.getEvent().getItemDrop();
        ItemStack itemStack = item.getItemStack();

        // Heroes
        try {
            Plugin heroesPlugin = lwc.getPlugin().getServer().getPluginManager().getPlugin("Heroes");

            if (heroesPlugin != null) {
                Heroes heroes = (Heroes) heroesPlugin;
                Hero hero = heroes.getCharacterManager().getHero(bPlayer);

                if (hero != null && hero.isInCombat()) {
                    return;
                }
            }
        } catch (Exception e) { }

        // MobArena
        try {
            Plugin mobarenaPlugin = lwc.getPlugin().getServer().getPluginManager().getPlugin("MobArena");

            if (mobarenaPlugin != null) {
                com.garbagemule.MobArena.MobArena mobarena = (com.garbagemule.MobArena.MobArena) mobarenaPlugin;

                if (mobarena.getArenaMaster().getArenaAtLocation(bPlayer.getLocation()) != null) {
                    event.setCancelled(true);
                }
            }
        } catch (Exception e) { }

        LWCPlayer player = lwc.wrapPlayer(bPlayer);
        int protectionId = getPlayerDropTransferTarget(player);

        if (protectionId == -1) {
            return;
        }

        if (!isPlayerDropTransferring(player)) {
            return;
        }

        Protection protection = lwc.getPhysicalDatabase().loadProtection(protectionId);

        if (protection == null) {
            lwc.sendLocale(player, "lwc.nolongerexists");
            player.disableMode(player.getMode("dropTransfer"));
            return;
        }

        // load the world and the inventory
        World world = player.getServer().getWorld(protection.getWorld());

        if (world == null) {
            lwc.sendLocale(player, "lwc.invalidworld");
            player.disableMode(player.getMode("dropTransfer"));
            return;
        }

        // Don't allow them to transfer items across worlds
        if (bPlayer.getWorld() != world && !lwc.getConfiguration().getBoolean("modes.droptransfer.crossWorld", false)) {
            lwc.sendLocale(player, "lwc.dropxfer.acrossworlds");
            player.disableMode(player.getMode("dropTransfer"));
            return;
        }

        Block block = world.getBlockAt(protection.getX(), protection.getY(), protection.getZ());
        Map<Integer, ItemStack> remaining = lwc.depositItems(block, itemStack);

        if (remaining.size() > 0) {
            lwc.sendLocale(player, "lwc.dropxfer.chestfull");

            for (ItemStack temp : remaining.values()) {
                bPlayer.getInventory().addItem(temp);
            }
        }

        bPlayer.updateInventory(); // if they're in the chest and dropping items, this is required
        item.remove();
    }

    @Override
    public void onProtectionInteract(LWCProtectionInteractEvent event) {
        LWC lwc = event.getLWC();
        Protection protection = event.getProtection();
        Set<String> actions = event.getActions();
        boolean canAccess = event.canAccess();

        Player bPlayer = event.getPlayer();
        LWCPlayer player = lwc.wrapPlayer(bPlayer);

        if (!actions.contains("dropTransferSelect")) {
            return;
        }

        if (!canAccess) {
            lwc.sendLocale(player, "protection.interact.dropxfer.noaccess");
        } else {
            if (protection.getBlockId() != Material.CHEST.getId()) {
                lwc.sendLocale(player, "protection.interact.dropxfer.notchest");
                player.removeAllActions();
                event.setResult(Result.CANCEL);

                return;
            }

            Mode mode = new Mode();
            mode.setName("dropTransfer");
            mode.setData(protection.getId() + "");
            mode.setPlayer(bPlayer);
            player.enableMode(mode);
            mode = new Mode();
            mode.setName("+dropTransfer");
            mode.setPlayer(bPlayer);
            player.enableMode(mode);

            lwc.sendLocale(player, "protection.interact.dropxfer.finalize");
        }

        player.removeAllActions(); // ignore the persist mode
    }

    @Override
    public void onBlockInteract(LWCBlockInteractEvent event) {
        Player player = event.getPlayer();
        Set<String> actions = event.getActions();

        if (!actions.contains("dropTransferSelect")) {
            return;
        }

        lwc.sendLocale(player, "protection.interact.dropxfer.notprotected");
        lwc.removeModes(player);
    }

    @Override
    public void onCommand(LWCCommandEvent event) {
        if (!event.hasFlag("p", "mode")) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        LWC lwc = event.getLWC();
        CommandSender sender = event.getSender();
        String[] args = event.getArgs();

        LWCPlayer player = lwc.wrapPlayer(sender);
        String mode = args[0].toLowerCase();

        if (!mode.equals("droptransfer")) {
            return;
        }

        event.setCancelled(true);

        // internal name
        mode = "dropTransfer";

        if (args.length < 2) {
            lwc.sendLocale(player, "protection.modes.dropxfer.help");
            return;
        }

        String action = args[1].toLowerCase();
        String playerName = player.getName();

        if (action.equals("select")) {
            if (isPlayerDropTransferring(player)) {
                lwc.sendLocale(player, "protection.modes.dropxfer.select.error");
                return;
            }

            player.disableMode(player.getMode(mode));

            Action temp = new Action();
            temp.setName("dropTransferSelect");
            temp.setPlayer(player);

            player.addAction(temp);
            lwc.sendLocale(player, "protection.modes.dropxfer.select.finalize");
        } else if (action.equals("on")) {
            int target = getPlayerDropTransferTarget(player);

            if (target == -1) {
                lwc.sendLocale(player, "protection.modes.dropxfer.selectchest");
                return;
            }

            Mode temp = new Mode();
            temp.setName("+dropTransfer");
            temp.setPlayer(player.getBukkitPlayer());

            player.enableMode(temp);
            lwc.sendLocale(player, "protection.modes.dropxfer.on.finalize");
        } else if (action.equals("off")) {
            int target = getPlayerDropTransferTarget(player);

            if (target == -1) {
                lwc.sendLocale(player, "protection.modes.dropxfer.selectchest");
                return;
            }

            player.disableMode(player.getMode("+dropTransfer"));
            lwc.sendLocale(player, "protection.modes.dropxfer.off.finalize");
        } else if (action.equals("status")) {
            if (getPlayerDropTransferTarget(player) == -1) {
                lwc.sendLocale(player, "protection.modes.dropxfer.status.off");
            } else {
                if (isPlayerDropTransferring(player)) {
                    lwc.sendLocale(player, "protection.modes.dropxfer.status.active");
                } else {
                    lwc.sendLocale(player, "protection.modes.dropxfer.status.inactive");
                }
            }
        }

    }

}
