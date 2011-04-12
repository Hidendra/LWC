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

package com.griefcraft.tests.impl;

import com.griefcraft.tests.ITest;
import com.griefcraft.tests.Priority;
import com.griefcraft.tests.Result;
import com.griefcraft.tests.TestSuite;
import com.griefcraft.util.Minecraft;

public abstract class BaseTest implements ITest {

	/**
	 * The Test Suite this test is associated with
	 */
	protected TestSuite testSuite;
	
	/**
	 * The result of the test
	 */
	protected Result result = Result.NOT_STARTED;
	
	/**
	 * The minecraft utility object
	 */
	protected Minecraft minecraft;
	
	/**
	 * Abstract methods that must be inherited
	 */
	public abstract String getName();
	public abstract void execute();
    
    public void setTestSuite(TestSuite testSuite) {
    	this.testSuite = testSuite;
    	minecraft = new Minecraft(testSuite);
    }
    
    public TestSuite getTestSuite() {
    	return testSuite;
    }
    
    public Minecraft getMinecraft() {
    	return minecraft;
    }

	public String getDescription() {
		return "";
	}

	public Priority getPriority() {
		return Priority.NORMAL;
	}

	public Result getResult() {
		return result;
	}
	
	public void setResult(Result result) {
		this.result = result;
	}
	
	public String toString() {
		return getName() + (getDescription().length() > 0 ? (" - " + getDescription()) : "");
	}
	
}
