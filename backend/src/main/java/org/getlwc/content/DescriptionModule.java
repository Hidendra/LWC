/**
 * Copyright (c) 2011-2014 Tyler Blair
 * All rights reserved.
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
package org.getlwc.content;

import org.getlwc.command.Command;
import org.getlwc.command.CommandContext;
import org.getlwc.command.SenderType;
import org.getlwc.content.component.DescriptionComponent;
import org.getlwc.entity.Player;
import org.getlwc.event.EventConsumer;
import org.getlwc.event.ProtectionListener;
import org.getlwc.event.protection.ProtectionInteractEvent;
import org.getlwc.event.protection.ProtectionLoadEvent;
import org.getlwc.meta.Meta;
import org.getlwc.meta.MetaKey;
import org.getlwc.model.Protection;

public final class DescriptionModule {

    /**
     * The metadata key name used to store the description
     */
    public static final MetaKey META_KEY = MetaKey.valueOf("description");

    @ProtectionListener
    public void bootstrap(ProtectionLoadEvent event) {
        Protection protection = event.getProtection();

        if (protection.hasMeta(META_KEY)) {
            protection.addComponent(new DescriptionComponent(protection.getMeta(META_KEY).getValue()));
        }
    }

    @Command(
            command = "lwc description",
            description = "Sets or removes a description on a protection",
            aliases = { "lwc desc" },
            accepts = SenderType.PLAYER
    )
    public void setDescription(CommandContext context) {
        final Player player = (Player) context.getCommandSender();
        final String description = context.getArguments().trim();
        final boolean removingDescription = description.isEmpty() || description.equalsIgnoreCase("none");

        if (removingDescription) {
            player.sendTranslatedMessage("Click on the protection to remove its description.");
        } else {
            player.sendTranslatedMessage("Click on the protection to set the description: &e{0}", description);
        }

        player.onNextProtectionInteract(new EventConsumer<ProtectionInteractEvent>() {
            @Override
            public void accept(ProtectionInteractEvent event) {
                event.markCancelled();

                Protection protection = event.getProtection();

                if (removingDescription) {
                    protection.removeMeta(META_KEY);
                    protection.removeComponent(DescriptionComponent.class);
                    player.sendTranslatedMessage("&2Removed successfully.");
                } else {
                    protection.addMeta(new Meta(META_KEY, description));
                    protection.addComponent(new DescriptionComponent(description));
                    player.sendTranslatedMessage("&2Added successfully.");
                }

                protection.save();
            }
        });
    }

}
