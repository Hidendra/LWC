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

package com.griefcraft.jobs;

import com.griefcraft.jobs.impl.CleanupJobHandler;
import com.griefcraft.lwc.LWC;

import java.util.HashSet;
import java.util.Set;

public class JobManager {

    /**
     * The parent LWC instance
     */
    private LWC lwc;

    /**
     * The list of job handlers to use
     */
    private final Set<IJobHandler> handlers = new HashSet<IJobHandler>();

    public JobManager(LWC lwc) {
        this.lwc = lwc;

        // setup default handlers
        {
            handlers.add(new CleanupJobHandler());
        }
    }

    /**
     * Check if a job handler exists
     *
     * @param name
     * @return
     */
    public boolean hasJobHandler(String name) {
        for (IJobHandler handler : handlers) {
            if(handler.getName().equals(name)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Add a job handler
     *
     * @param handler
     */
    public void addJobHandler(IJobHandler handler) {
        handlers.add(handler);
    }

    /**
     * Remove a job handler
     *
     * @param handler
     */
    public void removeJobHandler(IJobHandler handler) {
        handlers.remove(handler);
    }

}
