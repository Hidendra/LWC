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

package com.griefcraft.modules.owners;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.AccessRight;
import com.griefcraft.model.Action;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCBlockInteractEvent;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.scripting.event.LWCProtectionInteractEvent;
import com.griefcraft.util.Colors;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class OwnersModule extends JavaModule {

    @Override
    public void onProtectionInteract(LWCProtectionInteractEvent event) {
        if (event.getResult() != Result.DEFAULT) {
            return;
        }

        if (!event.hasAction("owners")) {
            return;
        }

        LWC lwc = event.getLWC();
        Protection protection = event.getProtection();
        Player player = event.getPlayer();
        event.setResult(Result.CANCEL);

        Action action = lwc.getMemoryDatabase().getAction("owners", player.getName());
        int accessPage = Integer.parseInt(action.getData());

        /*
         * Calculate range
         */
        int start = (accessPage - 1) * AccessRight.RESULTS_PER_PAGE;
        int max = start + AccessRight.RESULTS_PER_PAGE;

        List<AccessRight> accessRights = lwc.getPhysicalDatabase().loadAccessRights(protection.getId(), start, max);
        int numRights = lwc.getPhysicalDatabase().countRightsForProtection(protection.getId());

        /*
         * May have only been 2 rows left, or something. Get the real max
         */
        int realMax = start + accessRights.size();

        player.sendMessage("");
        player.sendMessage(Colors.Green + "   LWC Protection");
        player.sendMessage(Colors.Blue + "Showing results " + Colors.LightBlue + start + Colors.Blue + "-" + Colors.LightBlue + realMax + Colors.Blue + ". Total: " + Colors.LightBlue + numRights);
        player.sendMessage("");
        player.sendMessage("");

        for (AccessRight accessRight : accessRights) {
            StringBuilder builder = new StringBuilder();
            builder.append(Colors.LightBlue);
            builder.append(accessRight.getName());
            builder.append(Colors.Blue);
            builder.append(" (");
            builder.append(AccessRight.typeToString(accessRight.getType()));
            builder.append(") ");

            if (accessRight.getRights() == 1) {
                builder.append(Colors.LightBlue);
                builder.append("(");
                builder.append(Colors.Red);
                builder.append("ADMIN");
                builder.append(Colors.LightBlue);
                builder.append(")");
            }

            player.sendMessage(builder.toString());
        }

        lwc.removeModes(player);
        return;
    }

    @Override
    public void onBlockInteract(LWCBlockInteractEvent event) {
        if (event.getResult() != Result.DEFAULT) {
            return;
        }

        if (!event.hasAction("owners")) {
            return;
        }

        LWC lwc = event.getLWC();
        Player player = event.getPlayer();
        Block block = event.getBlock();

        lwc.sendLocale(player, "protection.interact.error.notregistered", "block", LWC.materialToString(block));
        lwc.removeModes(player);
        return;
    }

    @Override
    public void onCommand(LWCCommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!event.hasFlag("o", "owner", "owners")) {
            return;
        }

        LWC lwc = event.getLWC();
        CommandSender sender = event.getSender();
        String[] args = event.getArgs();

        event.setCancelled(true);

        if (!(sender instanceof Player)) {
            sender.sendMessage(Colors.Red + "Console not supported.");
            return;
        }

        Player player = (Player) sender;
        int page = 1;

        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (Exception e) {
                lwc.sendSimpleUsage(sender, "/lwc -owners [page]");
                return;
            }
        }

        lwc.getMemoryDatabase().unregisterAllActions(player.getName());
        lwc.getMemoryDatabase().registerAction("owners", player.getName(), page + "");
        lwc.sendLocale(sender, "protection.owners.finalize");
        return;
    }

}
