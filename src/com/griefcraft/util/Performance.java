package com.griefcraft.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.griefcraft.logging.Logger;

public class Performance {

	private static Logger logger = Logger.getLogger("Performance");
	
	/**
	 * Time when LWC was started
	 */
	private static long startTime = 0L;
	
	/**
	 * Number of queries generated for phys db
	 */
	private static int physDBQueries = 0;
	
	/**
	 * Number of queries generated for mem db
	 */
	private static int memDBQueries = 0;
	
	/**
	 * Amount of players online
	 */
	private static int playersOnline = 0;
	
	/**
	 * Generated report to send to the player
	 */
	private static List<String> generatedReport = new ArrayList<String>();
	
	/**
	 * Initialize
	 */
	public static void init() {
		startTime = System.currentTimeMillis();
	}
	
	/**
	 * Set how many players are currently online
	 * 
	 * @param playersOnline
	 */
	public static void setPlayersOnline(int playersOnline) {
		Performance.playersOnline = playersOnline;
	}
	
	/**
	 * @return time in seconds it was being ran
	 */
	public static int getTimeRunningSeconds() {
		return (int) ((System.currentTimeMillis() - startTime) / 1000);
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
		if(generatedReport.size() == 0) {
			report();
		}
		
		return generatedReport;
	}
	
	/**
	 * Issue a report
	 */
	public static void report() {
		add(" ************ Start Report ************ ");
		add(" ");
		
		add(" + Date:\t" + new Date());
		add(" + Time:\t" + getTimeRunningSeconds() + " seconds");
		add(" + Players:\t" + playersOnline);
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
	 * Add a query
	 */
	public static void addPhysDBQuery() {
		physDBQueries ++;
	}
	
	/**
	 * Add a query
	 */
	public static void addMemDBQuery() {
		memDBQueries ++;
	}
	
	/**
	 * Add a line to the report and print it the the console as well
	 * 
	 * @param str
	 */
	public static void add(String str) {
		logger.info(str);
		generatedReport.add(str);
	}
	
	/**
	 * Clear the report
	 */
	public static void clear() {
		generatedReport.clear();
	}
	
}
