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

package com.griefcraft.logging;

import java.util.logging.ConsoleHandler;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import com.griefcraft.LWCInfo;

public class ConsoleLogger extends ConsoleHandler {

	public ConsoleLogger() {
		super();

		setOutputStream(System.out);

		setFormatter(new SimpleFormatter() {

			@Override
			public String format(LogRecord record) {
				return String.format("%s\t[v%.2f]\t%s%s", record.getLoggerName(), LWCInfo.VERSION, record.getMessage(), System.getProperty("line.separator"));
			}

		});
	}

}
