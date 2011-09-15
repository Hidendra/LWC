/**
 * This file is part of LWC (https://github.com/Hidendra/LWC)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import com.griefcraft.util.Colors;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

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
            player.sendMessage(Colors.Red + "Protection no longer exists");
            player.disableMode(player.getMode("dropTransfer"));
            return;
        }

        // load the world and the inventory
        World world = player.getServer().getWorld(protection.getWorld());

        if (world == null) {
            player.sendMessage(Colors.Red + "Invalid world!");
            player.disableMode(player.getMode("dropTransfer"));
            return;
        }

        Block block = world.getBlockAt(protection.getX(), protection.getY(), protection.getZ());
        Map<Integer, ItemStack> remaining = lwc.depositItems(block, itemStack);

        if (remaining.size() > 0) {
            player.sendMessage("Chest could not hold all the items! Have the remaining items back.");

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
        List<String> actions = event.getActions();
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
        List<String> actions = event.getActions();

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

        if(event.isCancelled()) {
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

        return;
    }

}
