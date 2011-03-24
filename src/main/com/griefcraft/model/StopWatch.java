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

package com.griefcraft.model;

public class StopWatch {

	private boolean running = false;
	private long startTime = 0;
	private long stopTime = 0;

	public long getElapsedTime() {
		long elapsed;
		if (running) {
			elapsed = System.currentTimeMillis() - startTime;
		} else {
			elapsed = stopTime - startTime;
		}
		return elapsed;
	}

	public long getElapsedTimeSecs() {
		long elapsed;
		if (running) {
			elapsed = (System.currentTimeMillis() - startTime) / 1000;
		} else {
			elapsed = (stopTime - startTime) / 1000;
		}
		return elapsed;
	}

	public void start() {
		startTime = System.currentTimeMillis();
		running = true;
	}

	public void stop() {
		stopTime = System.currentTimeMillis();
		running = false;
	}
}