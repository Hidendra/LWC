/*
 * Copyright (c) 2011-2013 Tyler Blair
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

package org.getlwc.command;

import org.getlwc.Engine;
import org.getlwc.entity.Player;
import org.getlwc.event.events.ProtectionEvent;
import org.getlwc.event.notifiers.ProtectionEventNotifier;
import org.getlwc.model.AbstractAttribute;
import org.getlwc.model.Protection;
import org.getlwc.provider.BasicProvider;
import org.getlwc.provider.Provider;
import org.getlwc.role.ProtectionRole;
import org.getlwc.util.StringUtils;

public class AttributeCommands {

    /**
     * The engine object
     */
    private Engine engine;

    public AttributeCommands(Engine engine) {
        this.engine = engine;
    }

    @Command(
            command = "lwc set",
            description = "Set an attribute on a protection",
            permission = "lwc.attribute.set",
            aliases = {"cset"},
            accepts = SenderType.PLAYER,
            min = 1
    )
    public void set(CommandContext context) {
        final Player player = (Player) context.getCommandSender();
        final String attributeName = context.getArgument(1);
        String value = "";

        String[] arguments = context.getArgumentsArray();
        if (arguments.length > 1) {
            value = StringUtils.join(arguments, 1);
        }

        // verify the attribute
        final BasicProvider<AbstractAttribute> provider = engine.getProtectionManager().getAttributeManager().get(attributeName);

        if (provider == null) {
            player.sendTranslatedMessage("&4Invalid attribute name.");
            return;
        }

        final String finalAttributeValue = value;
        player.sendTranslatedMessage("&2Click on your protection to set the attribute");
        player.onAnyInteract(new ProtectionEventNotifier() {
            @Override
            public boolean call(ProtectionEvent event) {
                Protection protection = event.getProtection();

                ProtectionRole.Access currAccess = protection.getAccess(player);
                if (currAccess.ordinal() < ProtectionRole.Access.MANAGER.ordinal()) {
                    player.sendTranslatedMessage("&4Only managers and above can modify the attributes of a protection.");
                    return true;
                }

                AbstractAttribute attribute = provider.create();
                attribute.loadData(finalAttributeValue);

                protection.addAttribute(attribute);
                protection.save();
                player.sendTranslatedMessage("&2Added the attribute {0} to the protection successfully.", attribute.getName().toLowerCase());
                return true;
            }
        });
    }

    // TODO better word for unset ?
    @Command(
            command = "lwc unset",
            description = "Remove an attribute from a protection",
            permission = "lwc.attribute.delete",
            aliases = {"cunset"},
            accepts = SenderType.PLAYER,
            min = 1,
            max = 1
    )
    public void remove(CommandContext context) {
        final Player player = (Player) context.getCommandSender();
        final String attributeName = context.getArgument(1).toLowerCase();

        // verify the attribute
        final Provider<AbstractAttribute> provider = engine.getProtectionManager().getAttributeManager().get(attributeName);

        if (provider == null) {
            player.sendTranslatedMessage("&4Invalid attribute name.");
            return;
        }

        player.sendTranslatedMessage("&2Click on your protection to remove the attribute");
        player.onAnyInteract(new ProtectionEventNotifier() {
            @Override
            public boolean call(ProtectionEvent event) {
                Protection protection = event.getProtection();

                ProtectionRole.Access currAccess = protection.getAccess(player);
                if (currAccess.ordinal() < ProtectionRole.Access.MANAGER.ordinal()) {
                    player.sendTranslatedMessage("&4Only managers and above can modify the attributes of a protection.");
                    return true;
                }

                // verify the attribute
                if (protection.getAttribute(attributeName) != null) {
                    protection.removeAttribute(attributeName);
                    player.sendTranslatedMessage("&2Removed the attribute {0} from the protection successfully.", attributeName);
                } else {
                    player.sendTranslatedMessage("&4The protection does not have the attribute {0}", attributeName);
                }

                return true;
            }
        });
    }

}
