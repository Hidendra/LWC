package com.griefcraft.modules.admin;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.util.UUIDRegistry;
import org.bukkit.command.CommandSender;

import java.util.Iterator;
import java.util.UUID;

public class AdminPlayer2UUID extends JavaModule {

    @Override
    public void onCommand(LWCCommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!event.hasFlag("a", "admin")) {
            return;
        }

        LWC lwc = event.getLWC();
        CommandSender sender = event.getSender();
        String[] args = event.getArgs();

        if (!args[0].equals("player2uuid") && !args[0].equals("p2uuid")) {
            return;
        }

        event.setCancelled(true);

        if (args.length < 3) {
            lwc.sendSimpleUsage(sender, "/lwc admin player2uuid <FromName> <ToName>");
            sender.sendMessage("This can be used to convert protections for players that changed their name in Minecraft but LWC did not have their UUID.");
            sender.sendMessage("The FromName is their old name that LWC knows, and the ToName is their new name, which will be used to lookup their UUID to use.");
            return;
        }

        String playerFrom = args[1];
        String playerTo = args[2];
        UUID playerToUUID = UUIDRegistry.getUUID(playerTo);

        if (playerToUUID == null) {
            sender.sendMessage("&4No UUID could be found for the player: " + playerTo);
            return;
        }

        sender.sendMessage("Renaming player from " + playerFrom + " to " + playerTo + " (" + playerToUUID.toString() + ")...");

        int changed = 0;
        int touched = 0;
        int total = lwc.getPhysicalDatabase().getProtectionCount();

        Iterator<Protection> iter = lwc.getPhysicalDatabase().protectionIterator();

        while (iter.hasNext()) {
            Protection protection = iter.next();

            if (protection.renamePlayerName(playerFrom, playerToUUID)) {
                protection.save();
                changed ++;
            }

            touched ++;

            if (touched % 1000 == 0) {
                lwc.log("\tLooked at " + touched + "/" + total + " protections.");
            }
        }

        sender.sendMessage("Successfully converted " + changed + " protections from " + playerFrom + " to " + playerTo + " (" + playerToUUID.toString() + ")");
    }

}
