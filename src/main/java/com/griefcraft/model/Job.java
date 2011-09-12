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

import com.griefcraft.jobs.IJobHandler;
import com.griefcraft.lwc.LWC;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Job {

    /**
     * The parser that is shared between jobs
     */
    private static final JSONParser parser = new JSONParser();

    /**
     * The job id
     */
    private int id;

    /**
     * Unique name for the job
     */
    private String name;

    /**
     * The job type - essentially, the function it will perform
     */
    private int type;

    /**
     * JSON data
     */
    private JSONObject data = new JSONObject();

    /**
     * The epoch time to next run the job
     */
    private long nextRun;

    /**
     * True if the Job exists in the database
     */
    private boolean exists = false;

    /**
     * Decode the Job data (which uses JSON) into a usable Map
     *
     * @param json
     * @return
     */
    public static JSONObject decodeJSON(String json) {
        try {
            Object parsed = parser.parse(json);

            if (parsed instanceof JSONObject) {
                return (JSONObject) parsed;
            } else {
                return null;
            }
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * @return the Job handler assigned to this job
     */
    public IJobHandler getJobHandler() {
        return LWC.getInstance().getJobManager().getJobHandler(type);
    }

    /**
     * @return the job's id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the job's name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the job type - essentially, the function it will perform
     */
    public int getType() {
        return type;
    }

    /**
     * @return the JSON object data (a Map)
     */
    public JSONObject getData() {
        return data;
    }

    /**
     * @return true if the job should automatically run when it is polled
     */
    public boolean shouldRun() {
        if (nextRun == 0) {
            return false;
        }

        return nextRun == 1 || getTimeRemaining() <= 0;
    }

    /**
     * @return get the epoch time the job will next run at
     */
    public long getNextRun() {
        return nextRun;
    }

    /**
     * @return the time in milliseconds until the job should be ran
     */
    public long getTimeRemaining() {
        return nextRun <= 0 ? 1 : nextRun - System.currentTimeMillis();
    }

    /**
     * @param id
     */
    public void setId(int id) {
        this.id = id;
        this.exists = true;
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param type
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * @param data
     */
    public void setData(JSONObject data) {
        this.data = data;
    }

    /**
     * @param nextRun
     */
    public void setNextRun(long nextRun) {
        this.nextRun = nextRun;
    }

    /**
     * Immediately save the job to the database
     */
    public void save() {
        LWC.getInstance().getPhysicalDatabase().saveJob(this);
        LWC.getInstance().getJobManager().addJob(this);
    }

    /**
     * Remove the job from the database
     */
    public void remove() {
        LWC.getInstance().getPhysicalDatabase().removeJob(this);
        LWC.getInstance().getJobManager().removeJob(this);
    }

    /**
     * @return true if the protection exists in the database
     */
    public boolean doesExist() {
        return exists;
    }

}
