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

package com.griefcraft.util;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import com.griefcraft.logging.Logger;
import com.griefcraft.lwc.LWC;
import com.griefcraft.model.AccessRight;
import com.griefcraft.model.Limit;
import com.griefcraft.model.Protection;
import com.griefcraft.sql.Database.Type;
import com.griefcraft.sql.PhysDB;

// Sort of just a convenience class, so as to not make the LWC class more cluttered than it is right now
public class UpdatePatcher {

	private static Logger logger = Logger.getLogger("Patcher");

	/**
	 * Check for required SQLite->MySQL conversion
	 * 
	 * @param lwc
	 */
	public static void checkDatabaseConversion(LWC lwc) {
		PhysDB physicalDatabase = lwc.getPhysicalDatabase();

		// this patcher only does something exciting if you have mysql enabled
		// :-)
		if (physicalDatabase.getType() != Type.MySQL) {
			return;
		}

		// this patcher only does something exciting if the old SQLite database
		// still exists :-)
		String database = ConfigValues.DB_PATH.getString();

		File file = new File(database);
		if (!file.exists()) {
			return;
		}

		logger.log("######################################################");
		logger.log("######################################################");
		logger.log("SQLite to MySQL conversion required");

		logger.log("Loading SQLite");

		// rev up those sqlite databases because I sure am hungry for some
		// data...
		PhysDB sqliteDatabase = new PhysDB(Type.SQLite);

		try {
			sqliteDatabase.connect();
			sqliteDatabase.load();

			logger.log("SQLite is good to go");
			physicalDatabase.getConnection().setAutoCommit(false);

			logger.log("Preliminary scan...............");
			int startProtections = physicalDatabase.getProtectionCount();
			int startRights = physicalDatabase.getRightsCount();
			int startLimits = physicalDatabase.getLimitCount();

			int protections = sqliteDatabase.getProtectionCount();
			int rights = sqliteDatabase.getRightsCount();
			int limits = sqliteDatabase.getLimitCount();

			int expectedProtections = protections + startProtections;
			int expectedRights = rights + startRights;
			int expectedLimits = limits + startLimits;

			logger.log("TO CONVERT:");
			logger.log("Protections:\t" + protections);
			logger.log("Rights:\t\t" + rights);
			logger.log("Limits:\t\t" + limits);
			logger.log("");

			if (protections > 0) {
				logger.log("Converting: PROTECTIONS");

				List<Protection> tmp = sqliteDatabase.loadProtections();

				for (Protection protection : tmp) {
					physicalDatabase.registerProtection(protection.getBlockId(), protection.getType(), protection.getWorld(), protection.getOwner(), protection.getData(), protection.getX(), protection.getY(), protection.getZ());
				}

				logger.log("COMMITTING");
				physicalDatabase.getConnection().commit();
				logger.log("OK , expecting: " + expectedProtections);
				if (expectedProtections == (protections = physicalDatabase.getProtectionCount())) {
					logger.log("OK.");
				} else {
					logger.log("Weird, only " + protections + " protections are in the database? Continuing...");
				}
			}

			if (expectedRights > 0) {
				logger.log("Converting: RIGHTS");

				List<AccessRight> tmp = sqliteDatabase.loadRights();

				for (AccessRight right : tmp) {
					physicalDatabase.registerProtectionRights(right.getprotectionId(), right.getEntity(), right.getRights(), right.getType());
				}

				logger.log("COMMITTING");
				physicalDatabase.getConnection().commit();
				logger.log("OK , expecting: " + expectedRights);
				if (expectedRights == (rights = physicalDatabase.getRightsCount())) {
					logger.log("OK.");
				} else {
					logger.log("Weird, only " + rights + " rights are in the database? Continuing...");
				}
			}

			if (expectedRights > 0) {
				logger.log("Converting: LIMITS");

				List<Limit> tmp = sqliteDatabase.loadLimits();

				for (Limit limit : tmp) {
					physicalDatabase.registerProtectionLimit(limit.getType(), limit.getAmount(), limit.getEntity());
				}

				logger.log("COMMITTING");
				physicalDatabase.getConnection().commit();
				logger.log("OK , expecting: " + expectedLimits);
				if (expectedLimits == (limits = physicalDatabase.getLimitCount())) {
					logger.log("OK.");
				} else {
					logger.log("Weird, only " + limits + " limits are in the database? Continuing...");
				}
			}

			logger.log("Closing SQLite");
			sqliteDatabase.getConnection().close();

			logger.log("Renaming \"" + database + "\" to \"" + database + ".old\"");
			if (!file.renameTo(new File(database + ".old"))) {
				logger.log("NOTICE: FAILED TO RENAME lwc.db!! Please rename this manually!");
			}

			logger.log("SQLite to MySQL conversion is now complete!\n");
			logger.log("Thank you!");
		} catch (Exception e) {
			logger.log("#### SEVERE ERROR: Something bad happened when converting the database (Oops!)");
			e.printStackTrace();
		}

		try {
			physicalDatabase.getConnection().setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		logger.log("######################################################");
		logger.log("######################################################");
	}

}
