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

package com.griefcraft.modules.setup;

import com.griefcraft.lwc.LWC;
import com.griefcraft.migration.DatabaseMigrator;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.sql.Database;
import com.griefcraft.sql.PhysDB;
import org.bukkit.command.CommandSender;

public class DatabaseSetupModule extends JavaModule {

    @Override
    public void onCommand(LWCCommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!event.hasFlag("s", "setup")) {
            return;
        }

        LWC lwc = event.getLWC();
        CommandSender sender = event.getSender();
        String[] args = event.getArgs();

        if (!args[0].equals("database")) {
            return;
        }

        // we have the right command
        event.setCancelled(true);

        if (args.length < 2) {
            lwc.sendSimpleUsage(sender, "/lwc setup database <DatabaseType> [args]");
            return;
        }

        // attempt to match the database
        Database.Type databaseType = Database.Type.matchType(args[1]);
        Database.DefaultType = databaseType;

        if (databaseType == null) {
            lwc.sendLocale(sender, "lwc.setup.database.invalid");
            return;
        }

        // Immediately convert
        DatabaseMigrator migrator = new DatabaseMigrator();
        PhysDB fromDatabase = lwc.getPhysicalDatabase();
        lwc.reloadDatabase();

        if (migrator.migrate(fromDatabase, lwc.getPhysicalDatabase())) {
            lwc.sendLocale(sender, "lwc.setup.database.success", "type", databaseType.toString());
        } else {
            lwc.sendLocale(sender, "lwc.setup.database.failure", "type", databaseType.toString());
            return;
        }

        // Should have succeeded - set the database type to the new one
        lwc.getConfiguration().setProperty("database.adapter", databaseType.toString().toLowerCase());
        lwc.getConfiguration().save();

        // immediately move to the new database
        lwc.reloadDatabase();
    }

}
