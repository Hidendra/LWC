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

public interface ITest {
	
	/**
	 * The canonical name. Unique, and short.
	 * 
	 * @return
	 */
	public String getName();
	
	/**
	 * Description of the test.
	 * 
	 * @return
	 */
	public String getDescription();
	
	/**
	 * Priority level for the test.
	 * 
	 * @return
	 */
	public Priority getPriority();
	
	/**
	 * Execute the test.
	 * 
	 * @param lwc
	 */
	public void execute();
	
	/**
	 * The result of the executed test
	 * 
	 * @return
	 */
	public Result getResult();
	
	/**
	 * Allows the defining test suite to override the test's result
	 * 
	 * @param result
	 */
	public void setResult(Result result);
	
}
