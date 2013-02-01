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
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.sql.PhysDB;
import com.griefcraft.util.Colors;
import com.griefcraft.util.StringUtil;
import org.bukkit.command.CommandSender;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AdminQuery extends JavaModule {

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

        if (!args[0].equals("query") && !args[0].equals("updateprotections") && !args[0].equals("deleteprotections") && !args[0].equals("selectprotections")) {
            return;
        }

        // we have the right command
        event.setCancelled(true);

        // Raw query
        if (args[0].equals("query")) {
            String query = StringUtil.join(args, 1);

            try {
                Statement statement = lwc.getPhysicalDatabase().getConnection().createStatement();
                statement.executeUpdate(query);
                statement.close();
                sender.sendMessage(Colors.Green + "Done.");
            } catch (SQLException e) {
                sender.sendMessage(Colors.Red + "Err: " + e.getMessage());
            }
        }

        // Specific query, but the where statement is given
        else {
            String where = StringUtil.join(args, 1);

            // Ensure they don't accidentally do /lwc admin deleteprotections
            // which would delete everything..
            if (where.isEmpty()) {
                sender.sendMessage(Colors.Red + "Unsafe query detected.");
                return;
            }

            // execute the query
            try {
                PhysDB database = lwc.getPhysicalDatabase();
                Statement statement = database.getConnection().createStatement();

                // choose the statement
                if (args[0].startsWith("update")) {
                    int affected = statement.executeUpdate("UPDATE " + database.getPrefix() + "protections " + where);
                    sender.sendMessage(Colors.Green + "Affected rows: " + affected);
                } else if (args[0].startsWith("delete")) {
                    int affected = statement.executeUpdate("DELETE FROM " + database.getPrefix() + "protections WHERE " + where);
                    sender.sendMessage(Colors.Green + "Affected rows: " + affected);
                    database.precache();
                } else if (args[0].startsWith("select")) {
                    ResultSet set = statement.executeQuery("SELECT * FROM " + database.getPrefix() + "protections WHERE " + where);

                    while (set.next()) {
                        Protection protection = database.resolveProtection(set);
                        sender.sendMessage(protection.toString());
                    }

                    set.close();
                }
            } catch (SQLException e) {
                sender.sendMessage(Colors.Red + "Err: " + e.getMessage());
            }
        }

    }

}
