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

package com.griefcraft.modules.admin;

import com.griefcraft.lwc.LWC;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.ModuleLoader;
import com.griefcraft.scripting.event.LWCCommandEvent;
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
                lwc.sendLocale(sender, "lwc.admin.dump.fileexists", "file", localeFile.getPath());
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
                    lwc.sendLocale(sender, "lwc.admin.dump.filenotfound");
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

                lwc.sendLocale(sender, "lwc.admin.dump.success", "file", localeFile.getAbsolutePath());
            } catch (IOException e) {
                sender.sendMessage("Error: " + e.getMessage());
            }
        }
    }

}
