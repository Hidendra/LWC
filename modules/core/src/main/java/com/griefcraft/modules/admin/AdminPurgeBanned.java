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

package com.griefcraft.modules.admin;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCCommandEvent;
import org.bukkit.command.CommandSender;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AdminPurgeBanned extends JavaModule {

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

        if (!args[0].equals("purgebanned")) {
            return;
        }

        // we have the right command
        event.setCancelled(true);

        List<String> players = loadBannedPlayers();

        for (String toRemove : players) {
            // load all of their protections
            for (Protection protection : lwc.getPhysicalDatabase().loadProtectionsByPlayer(toRemove, 0, 100000)) {
                protection.remove();
            }

            lwc.sendLocale(sender, "protection.admin.purge.finalize", "player", toRemove);
        }

        return;
    }

    /**
     * Load the list of currently banned players
     *
     * @return
     */
    private List<String> loadBannedPlayers() {
        List<String> banned = new ArrayList<String>();

        File file = new File("banned-players.txt");

        if (!file.exists()) {
            return banned;
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;

            while ((line = reader.readLine()) != null) {
                banned.add(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return banned;
    }

}