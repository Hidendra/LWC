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

package com.griefcraft.tests;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.griefcraft.tests.impl.BaseTest;
import com.griefcraft.util.ClassFinder;
import com.griefcraft.util.Colors;
import com.griefcraft.util.StopWatch;

public abstract class TestSuite {
	
	/**
	 * The name of the suite. Defaults to getClass().getSimpleName()
	 */
	protected String name;

	/**
	 * List of tests that will be performed
	 */
	protected List<BaseTest> tests = new ArrayList<BaseTest>();
	
	/**
	 * The sender that started the suite
	 */
	protected CommandSender sender;
	
	/**
	 * Used to time how long the tests took
	 */
	private StopWatch timer = new StopWatch(getClass().getSimpleName());
	
	public TestSuite(CommandSender sender) {
		this.sender = sender;
		name = getClass().getSimpleName();
		loadTests();
	}
	
	/**
	 * Get the name of the suite.
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * The package name to look for java tests.
	 * You may wish to override loadTests() if you don't want this functionality
	 * 
	 * @return
	 */
	public String getPackage() {
		return "com.griefcraft.tests";
	}
	
	/**
	 * Default test loader -- loads all tests from Java package specified
	 * by getPackage()
	 */
	public void loadTests() {
		log("Searching: " + Colors.Yellow + getPackage());
		
		for(Class<?> clazz : ClassFinder.getClasses(getPackage())) {
			try {
				Object object = clazz.newInstance();
			
				if(object instanceof BaseTest) {
					BaseTest baseTest = (BaseTest) object;
					baseTest.setTestSuite(this);
					
					tests.add(baseTest);
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			}
		}
		
		log(Colors.White + "Loaded " + Colors.Green + tests.size() + Colors.White + " tests");
	}
	
	/**
	 * Execute every test
	 */
	public void start() {
		// execute tests in order of importance
		executeTests(Priority.CRITICAL);
		executeTests(Priority.HIGH);
		executeTests(Priority.NORMAL);
		executeTests(Priority.LOW);
		
		// print results
		printResults();
	}
	
	/**
	 * Print test results to console
	 */
	public void printResults() {
		sender.sendMessage("Total:\t\t" + tests.size());
		sender.sendMessage("Completed:\t" + count(Result.COMPLETED));
		sender.sendMessage("Failed:\t\t" + count(Result.FAILURE));
		timer.prettyPrint(sender);
	}
	
	/**
	 * Count the tests with a specific result
	 * 
	 * @param result
	 */
	public int count(Result result) {
		int tmp = 0;
		
		for(ITest test : tests) {
			if(test == null) {
				continue;
			}
			
			if(test.getResult() == result) {
				tmp ++;
			}
		}
		
		return tmp;
	}
	
	/**
	 * Execute all tests for a given priority
	 * 
	 * @param priority
	 */
	private void executeTests(Priority priority) {
		for(ITest test : tests) {
			if(test == null) {
				continue;
			}
			
			if(test.getResult() != Result.NOT_STARTED) {
				continue;
			}
			
			if(test.getPriority() == priority || test.getPriority() == Priority.WILDCARD) {
				
				log("Running test: " + Colors.Blue + test.toString());
				
				try {
					timer.start(test.toString());
					test.execute();
					timer.stop();
					
					if(test.getResult() == Result.NOT_STARTED) {
						test.setResult(Result.COMPLETED);
					}
				} catch(Throwable throwable) {
					// mark it as failed
					test.setResult(Result.FAILURE);
					
					// stop the timer
					timer.stop();
					
					log("Test " + Colors.Red + test.getName() + Colors.White + " threw error: " + Colors.Red + throwable.getMessage() + Colors.White + ". Check console for full error");
					throwable.printStackTrace();
				}
				
			}
		}
	}
	
	/**
	 * Log a message to the CommandSender
	 * 
	 * @param str
	 */
	protected void log(String str) {
		sender.sendMessage(Colors.White + "[" + Colors.Green + name + Colors.White + "]\t" + str);
	}
	
}
