package com.griefcraft.logging;

import com.griefcraft.LWCInfo;

public class Logger {

	/**
	 * Create a new logger
	 * 
	 * @param name
	 * @return
	 */
	public static Logger getLogger(String name) {
		return new Logger(name);
	}

	/**
	 * Logger name
	 */
	private String name;

	private Logger(String name) {
		this.name = name;
	}

	/**
	 * Log a str
	 * 
	 * @param str
	 */
	public void info(String str) {
		log(str);
	}

	/**
	 * Log a string
	 * 
	 * @param str
	 */
	public void log(String str) {
		System.out.println(format(str));
	}

	/**
	 * Format a string for a message
	 * 
	 * @param str
	 * @return
	 */
	private String format(String msg) {
		return String.format("%s\t[v%.2f]\t%s", name, LWCInfo.VERSION, msg);
	}

}
