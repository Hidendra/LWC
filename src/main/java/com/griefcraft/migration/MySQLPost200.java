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
import com.griefcraft.model.AccessRight;
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
	 * 
	 * @param lwc
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

		if(database == null || database.equals("")) {
			return;
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

			int protections = sqliteDatabase.getProtectionCount();
			int rights = sqliteDatabase.getRightsCount();

			int expectedProtections = protections + startProtections;

			logger.info("TO CONVERT:");
			logger.info("Protections:\t" + protections);
			logger.info("Rights:\t\t" + rights);
			logger.info("");

			if (protections > 0) {
				logger.info("Converting: PROTECTIONS");

				List<Protection> tmp = sqliteDatabase.loadProtections();

				for (Protection protection : tmp) {
					int x = protection.getX();
					int y = protection.getY();
					int z = protection.getZ();
					
					// register it
					physicalDatabase.registerProtection(protection.getBlockId(), protection.getType(), protection.getWorld(), protection.getOwner(), protection.getData(), x, y, z);
				
					// get the new protection, to retrieve the id
					Protection registered = physicalDatabase.loadProtection(protection.getWorld(), x, y, z);
					
					// get the rights in the world
					List<AccessRight> tmpRights = sqliteDatabase.loadRights(protection.getId());

					// register the new rights using the newly registered protection
					for (AccessRight right : tmpRights) {
						physicalDatabase.registerProtectionRights(registered.getId(), right.getName(), right.getRights(), right.getType());
					}
				
				}

				logger.info("COMMITTING");
				physicalDatabase.getConnection().commit();
				logger.info("OK , expecting: " + expectedProtections);
				if (expectedProtections == (protections = physicalDatabase.getProtectionCount())) {
					logger.info("OK.");
				} else {
					logger.info("Weird, only " + protections + " protections are in the database? Continuing...");
				}
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
