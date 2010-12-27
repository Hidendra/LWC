package com.griefcraft.logging;

import com.griefcraft.LWCInfo;

public class Logger {
	
	/**
	 * Logger name
	 */
	private String name;

	/**
	 * Create a new logger
	 * 
	 * @param name
	 * @return
	 */
	public static Logger getLogger(String name) {
		return new Logger(name);
	}
	
	private Logger(String name) {
		this.name = name;
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
	 * Log a str
	 * 
	 * @param str
	 */
	public void info(String str) {
		log(str);
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
