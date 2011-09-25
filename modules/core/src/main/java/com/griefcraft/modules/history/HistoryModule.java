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

package com.griefcraft.modules.history;

import com.griefcraft.model.History;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCProtectionRemovePostEvent;
import org.bukkit.entity.Player;

public class HistoryModule extends JavaModule {

    @Override
    public void onPostRemoval(LWCProtectionRemovePostEvent event) {
        Protection protection = event.getProtection();
        Player player = event.getPlayer();

        boolean isOwner = protection.isOwner(player);

        if (isOwner) {
            // bind the player of destroyed the protection
            // We don't need to save the history we modify because it will be saved anyway immediately after this
            for(History history : protection.getRelatedHistory(History.Type.TRANSACTION)) {
                if(history.getStatus() != History.Status.ACTIVE) {
                    continue;
                }

                history.addMetaData("destroyer=" + player.getName());
            }
        }
    }
    
}
