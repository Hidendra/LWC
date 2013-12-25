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

package com.griefcraft.modules.destroy;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.History;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCProtectionDestroyEvent;
import org.bukkit.entity.Player;

public class DestroyModule extends JavaModule {

    @Override
    public void onDestroyProtection(LWCProtectionDestroyEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (event.getMethod() != LWCProtectionDestroyEvent.Method.BLOCK_DESTRUCTION) {
            return;
        }

        LWC lwc = event.getLWC();
        Protection protection = event.getProtection();
        Player player = event.getPlayer();

        boolean isOwner = protection.isOwner(player);

        if (isOwner) {
            if (!lwc.isAdmin(player) && Boolean.parseBoolean(lwc.resolveProtectionConfiguration(protection.getBlock(), "readonly-remove"))) {
                lwc.sendLocale(player, "protection.accessdenied");
                event.setCancelled(true);
                return;
            }

            // bind the player of destroyed the protection
            // We don't need to save the history we modify because it will be saved anyway immediately after this
            for (History history : protection.getRelatedHistory(History.Type.TRANSACTION)) {
                if (history.getStatus() != History.Status.ACTIVE) {
                    continue;
                }

                history.addMetaData("destroyer=" + player.getName());
                history.addMetaData("destroyerTime=" + System.currentTimeMillis() / 1000L);
            }

            protection.remove();

            if (!Boolean.parseBoolean(lwc.resolveProtectionConfiguration(protection.getBlock(), "quiet"))) {
                lwc.sendLocale(player, "protection.unregistered", "block", LWC.materialToString(protection.getBlockId()));
            }
            return;
        }

        event.setCancelled(true);
    }

}
