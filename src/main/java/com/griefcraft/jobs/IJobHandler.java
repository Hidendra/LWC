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

import com.griefcraft.model.Job;
import com.griefcraft.lwc.LWC;

public interface IJobHandler {

    /**
     * @return the unique name which is used to create the job (e.g /lwc schedule cleanup, where getName() = cleanup). Cannot contain spaces!
     */
    public String getName();

    /**
     * @return the keys that must be set in the data object when creating the job for it to run properly
     */
    public String[] getRequiredKeys();

    /**
     * @return the unique type this job assigns to jobs
     */
    public int getType();

    /**
     * Run the given job
     *
     * @param lwc
     * @param job
     */
    public void run(LWC lwc, Job job);

}
