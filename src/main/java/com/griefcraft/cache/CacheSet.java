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

package com.griefcraft.cache;


import java.util.logging.Logger;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;

public class CacheSet {

    /**
     * Logging instance
     */
    private Logger logger = Logger.getLogger("Cache");

    /**
     * Caches protections to prevent abusing the database
     */
    private LRUCache<String, Protection> protectionCache;

    public CacheSet() {
        int maxCapacity = LWC.getInstance().getConfiguration().getInt("core.cacheSize", 10000);

        protectionCache = new LRUCache<String, Protection>(maxCapacity);
        logger.info("Protection cache: 0/" + maxCapacity);
    }

    /**
     * get the cache representing protections
     *
     * @return
     */
    public LRUCache<String, Protection> getProtections() {
        return protectionCache;
    }

}
