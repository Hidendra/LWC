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

package com.griefcraft.migration;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.History;
import com.griefcraft.model.Protection;
import com.griefcraft.sql.Database.Type;
import com.griefcraft.sql.PhysDB;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

// Sort of just a convenience class, so as to not make the LWC class more cluttered than it is right now
public class MySQLPost200 implements MigrationUtility {

    private static Logger logger = Logger.getLogger("Patcher");

    /**
     * Check for required SQLite->MySQL conversion
     */
    public void run() {
        LWC lwc = LWC.getInstance();
        PhysDB physicalDatabase = lwc.getPhysicalDatabase();

        // this patcher only does something exciting if you have mysql enabled
        // :-)
        if (physicalDatabase.getType() != Type.MySQL) {
            return;
        }

        // this patcher only does something exciting if the old SQLite database
        // still exists :-)
        String database = lwc.getConfiguration().getString("database.path");

        if (database == null || database.trim().equals("")) {
            database = "plugins/LWC/lwc.db";
        }

        File file = new File(database);
        if (!file.exists()) {
            return;
        }

        logger.info("######################################################");
        logger.info("######################################################");
        logger.info("SQLite to MySQL conversion required");

        logger.info("Loading SQLite");

        // rev up those sqlite databases because I sure am hungry for some
        // data...
        PhysDB sqliteDatabase = new PhysDB(Type.SQLite);

        try {
            sqliteDatabase.connect();
            sqliteDatabase.load();

            logger.info("SQLite is good to go");
            physicalDatabase.getConnection().setAutoCommit(false);

            logger.info("Preliminary scan...............");
            int startProtections = physicalDatabase.getProtectionCount();

            int protectionCount = sqliteDatabase.getProtectionCount();
            int historyCount = sqliteDatabase.getHistoryCount();

            int expectedProtections = protectionCount + startProtections;

            logger.info("TO CONVERT:");
            logger.info("Protections:\t" + protectionCount);
            logger.info("History:\t" + historyCount);
            logger.info("");

            if (protectionCount > 0) {
                logger.info("Converting: PROTECTIONS");

                List<Protection> tmp = sqliteDatabase.loadProtections();

                for (Protection protection : tmp) {
                    // sync it to the live database
                    protection.saveNow();
                }

                logger.info("COMMITTING");
                physicalDatabase.getConnection().commit();
                logger.info("OK , expecting: " + expectedProtections);
                if (expectedProtections == (protectionCount = physicalDatabase.getProtectionCount())) {
                    logger.info("OK.");
                } else {
                    logger.info("Weird, only " + protectionCount + " protections are in the database? Continuing...");
                }
            }

            if(historyCount > 0) {
                logger.info("Converting: HISTORY");

                List<History> tmp = sqliteDatabase.loadHistory();

                for(History history : tmp) {
                    // make sure it's assumed it does not exist in the database
                    history.setExists(false);

                    // sync the history object with the active database (ala MySQL)
                    history.sync();
                }

                logger.info("OK");
            }

            logger.info("Closing SQLite");
            sqliteDatabase.getConnection().close();

            logger.info("Renaming \"" + database + "\" to \"" + database + ".old\"");
            if (!file.renameTo(new File(database + ".old"))) {
                logger.info("NOTICE: FAILED TO RENAME lwc.db!! Please rename this manually!");
            }

            logger.info("SQLite to MySQL conversion is now complete!\n");
            logger.info("Thank you!");
        } catch (Exception e) {
            logger.info("#### SEVERE ERROR: Something bad happened when converting the database (Oops!)");
            e.printStackTrace();
        }

        try {
            physicalDatabase.getConnection().setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        logger.info("######################################################");
        logger.info("######################################################");
    }

}
