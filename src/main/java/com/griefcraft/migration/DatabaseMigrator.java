/*
 * Copyright 2011 Tyler Blair. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and contributors and should not be interpreted as representing official policies,
 * either expressed or implied, of anybody else.
 */

package com.griefcraft.migration;

import com.griefcraft.model.History;
import com.griefcraft.model.Protection;
import com.griefcraft.sql.PhysDB;

import java.util.List;
import java.util.logging.Logger;

public class DatabaseMigrator {
    private static Logger logger = Logger.getLogger("LWCMigrator");

    /**
     * Converts the current database to the given database type
     *
     * @param fromDatabase The database to convert from
     * @param toDatabase The database to convert to - does not need to be initialized; new PhysDB(type) is fine
     * @return true if the conversion was most likely successful
     */
    public boolean migrate(PhysDB fromDatabase, PhysDB toDatabase) {
        try {
            toDatabase.getConnection().setAutoCommit(false);

            // some prelim data
            int startProtections = toDatabase.getProtectionCount();
            int protectionCount = fromDatabase.getProtectionCount();
            int historyCount = fromDatabase.getHistoryCount();
            int expectedProtections = protectionCount + startProtections;

            if (protectionCount > 0) {
                List<Protection> tmp = fromDatabase.loadProtections();

                for (Protection protection : tmp) {
                    // sync it to the live database
                    protection.saveNow();
                }

                toDatabase.getConnection().commit();
                if (expectedProtections != (protectionCount = fromDatabase.getProtectionCount())) {
                    logger.info("Weird, only " + protectionCount + " protections are in the database? Continuing...");
                }
            }

            if (historyCount > 0) {
                List<History> tmp = fromDatabase.loadHistory();

                for (History history : tmp) {
                    // make sure it's assumed it does not exist in the database
                    history.setExists(false);

                    // sync the history object with the active database (ala MySQL)
                    history.sync();
                }
            }

            fromDatabase.getConnection().close();
            toDatabase.getConnection().setAutoCommit(true);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

}
