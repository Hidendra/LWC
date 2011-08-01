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
import com.griefcraft.util.StringUtils;
import org.bukkit.command.CommandSender;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AdminPurgeBanned extends JavaModule {

    @Override
    public Result onCommand(LWC lwc, CommandSender sender, String command, String[] args) {
        if (!StringUtils.hasFlag(command, "a") && !StringUtils.hasFlag(command, "admin")) {
            return DEFAULT;
        }

        if (!args[0].equals("purgebanned")) {
            return DEFAULT;
        }
        
        List<String> players = loadBannedPlayers();
        
        for (String toRemove : players) {
            // load all of their protections
            for (Protection protection : lwc.getPhysicalDatabase().loadProtectionsByPlayer(toRemove, 0, 100000)) {
                protection.remove();
            }

            lwc.sendLocale(sender, "protection.admin.purge.finalize", "player", toRemove);
        }

        return CANCEL;
    }
    
    /**
     * Load the list of currently banned players
     * 
     * @return
     */
    private List<String> loadBannedPlayers() {
    	List<String> banned = new ArrayList<String>();
    	
    	File file = new File("banned-players.txt");
    	
    	if(!file.exists()) {
    		return banned;
    	}
    	
    	try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line;
			
			while((line = reader.readLine()) != null) {
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