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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.griefcraft.logging.Logger;
import com.griefcraft.sql.Database;

public class Performance {

	/**
	 * Chest count
	 */
	private static int chestCount = 0;

	/**
	 * Generated report to send to the player
	 */
	private static List<String> generatedReport = new ArrayList<String>();

	private static Logger logger = Logger.getLogger("Performance");

	/**
	 * Number of queries generated for mem db
	 */
	private static int memDBQueries = 0;

	/**
	 * Number of queries generated for phys db
	 */
	private static int physDBQueries = 0;

	/**
	 * Amount of players online
	 */
	private static int playersOnline = 0;

	/**
	 * Time when LWC was started
	 */
	private static long startTime = 0L;

	/**
	 * Add a line to the report and print it the the console as well
	 * 
	 * @param str
	 */
	public static void add(String str) {
		logger.log(str);
		generatedReport.add(str);
	}

	/**
	 * Add a query
	 */
	public static void addMemDBQuery() {
		memDBQueries++;
	}

	/**
	 * Add a query
	 */
	public static void addPhysDBQuery() {
		physDBQueries++;
	}

	/**
	 * Clear the report
	 */
	public static void clear() {
		generatedReport.clear();
	}

	/**
	 * @return the average amount of queries per second
	 */
	public static double getAverageQueriesSecond(int queries) {
		return (double) queries / getTimeRunningSeconds();
	}

	/**
	 * @return the generated report
	 */
	public static List<String> getGeneratedReport() {
		if (generatedReport.size() == 0) {
			report();
		}

		return generatedReport;
	}

	/**
	 * @return time in seconds it was being ran
	 */
	public static int getTimeRunningSeconds() {
		return (int) ((System.currentTimeMillis() - startTime) / 1000);
	}

	/**
	 * Initialize
	 */
	public static void init() {
		startTime = System.currentTimeMillis();
	}

	/**
	 * Issue a report
	 */
	public static void report() {
		add(" ************ Start Report ************ ");
		add(" ");

		add(" + Engine:\t" + Database.DefaultType);
		add(" + Date:\t" + new Date());
		add(" + Time:\t" + getTimeRunningSeconds() + " seconds");
		add(" + Players:\t" + playersOnline);
		add(" + Protections:\t" + chestCount);
		add(" ");
		add(" - Physical database");
		add("  + Queries:\t" + physDBQueries);
		add("  + Average:\t" + getAverageQueriesSecond(physDBQueries) + " /second");
		add(" ");
		add(" - Memory database");
		add("  + Queries:\t" + memDBQueries);
		add("  + Average:\t" + getAverageQueriesSecond(memDBQueries) + " /second");

		add(" ");
		add(" ************  End Report  ************ ");
	}

	/**
	 * Set the chest count
	 * 
	 * @param chestCount
	 */
	public static void setChestCount(int chestCount) {
		Performance.chestCount = chestCount;
	}

	/**
	 * Set how many players are currently online
	 * 
	 * @param playersOnline
	 */
	public static void setPlayersOnline(int playersOnline) {
		Performance.playersOnline = playersOnline;
	}

}
