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
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.ModuleLoader;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.util.Colors;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AdminDump extends JavaModule {

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

        if (!args[0].equals("dump")) {
            return;
        }

        // we have the right command
        event.setCancelled(true);

        // get what should be dumped
        if (args.length < 2) {
            lwc.sendSimpleUsage(sender, "/lwc admin dump <ItemToDump>");
            return;
        }

        String toDump = args[1].toLowerCase();

        if (toDump.equals("locale")) {
            // check if the file already exists
            File localeFile = new File(ModuleLoader.ROOT_PATH + "locale/lwc.properties");

            if (localeFile.exists()) {
                sender.sendMessage(Colors.Red + "The file " + localeFile.getPath() + " already exists. Please delete or move this file!");
                return;
            }

            // let's create the locale folder if needed
            new File(ModuleLoader.ROOT_PATH + "locale/").mkdir();

            try {
                localeFile.createNewFile();

                // output stream
                OutputStream outputStream = new FileOutputStream(localeFile);

                // input stream
                InputStream inputStream = getClass().getResourceAsStream("/lang/lwc_" + lwc.getPlugin().getCurrentLocale() + ".properties");

                if (inputStream == null) {
                    sender.sendMessage(Colors.Red + "Failed to find locale inside the jar file.");
                    return;
                }

                // begin copying
                byte[] buffer = new byte[4096];
                int length;

                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                // cool beans!
                outputStream.close();
                inputStream.close();

                sender.sendMessage(Colors.Green + "Dumped locale file to: " + localeFile.getAbsolutePath());
            } catch (IOException e) {
                sender.sendMessage("Error: " + e.getMessage());
            }
        }
    }

}
