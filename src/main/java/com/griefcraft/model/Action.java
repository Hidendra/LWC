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

public class Action {

    private String name;
    private Protection protection;
    private String data;
    private LWCPlayer player;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the Protection associated with this action
     */
    public Protection getProtection() {
        return protection;
    }

    /**
     * @return the data
     */
    public String getData() {
        return data;
    }

    /**
     * @return the player
     */
    public LWCPlayer getPlayer() {
        return player;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param protection the Protection to set
     */
    public void setProtection(Protection protection) {
        this.protection = protection;
    }

    /**
     * @param data the data to set
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * @param player the player to set
     */
    public void setPlayer(LWCPlayer player) {
        this.player = player;
    }

}
