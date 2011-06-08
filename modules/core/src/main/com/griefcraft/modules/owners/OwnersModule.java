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
import com.griefcraft.util.Colors;
import com.griefcraft.util.StringUtils;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class OwnersModule extends JavaModule {

    @Override
    public Result onProtectionInteract(LWC lwc, Player player, Protection protection, List<String> actions, boolean canAccess, boolean canAdmin) {
        if (!actions.contains("owners")) {
            return DEFAULT;
        }

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

        return DEFAULT;
    }

    @Override
    public Result onBlockInteract(LWC lwc, Player player, Block block, List<String> actions) {
        if (!actions.contains("owners")) {
            return DEFAULT;
        }

        lwc.sendLocale(player, "protection.interact.error.notregistered", "block", LWC.materialToString(block));
        lwc.removeModes(player);
        return CANCEL;
    }

    @Override
    public Result onCommand(LWC lwc, CommandSender sender, String command, String[] args) {
        if (!StringUtils.hasFlag(command, "o") && !StringUtils.hasFlag(command, "owner") && !StringUtils.hasFlag(command, "owners")) {
            return DEFAULT;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(Colors.Red + "Console not supported.");
            return CANCEL;
        }

        Player player = (Player) sender;
        int page = 1;

        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (Exception e) {
                lwc.sendSimpleUsage(sender, "/lwc -owners [page]");
                return CANCEL;
            }
        }

        lwc.getMemoryDatabase().unregisterAllActions(player.getName());
        lwc.getMemoryDatabase().registerAction("owners", player.getName(), page + "");
        lwc.sendLocale(sender, "protection.owners.finalize");
        return CANCEL;
    }

}
