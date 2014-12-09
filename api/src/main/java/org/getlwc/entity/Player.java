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
package org.getlwc.entity;

import org.getlwc.ItemStack;
import org.getlwc.command.CommandSender;
import org.getlwc.component.MetadataComponent;
import org.getlwc.event.EventConsumer;
import org.getlwc.event.EventFuture;
import org.getlwc.event.block.BlockInteractEvent;
import org.getlwc.event.protection.ProtectionInteractEvent;
import org.getlwc.lang.Locale;

import java.text.MessageFormat;

import static org.getlwc.I18n._;

public abstract class Player extends Entity implements CommandSender {

    /**
     * This player's locale
     */
    private Locale locale = new Locale("en_US");

    public Player() {
        addComponent(new MetadataComponent());
    }

    /**
     * Get the item in the player's hand
     *
     * @return
     */
    public abstract ItemStack getItemInHand();

    /**
     * Calls the given consumer the next time the player interacts with a protection.
     *
     * @param consumer
     * @returns the future representing this event
     */
    public abstract EventFuture onNextProtectionInteract(EventConsumer<ProtectionInteractEvent> consumer);

    /**
     * Calls the given consumer every time the player interacts with a protection.
     * @param consumer
     * @return
     */
    public abstract EventFuture onEveryProtectionInteract(EventConsumer<ProtectionInteractEvent> consumer);

    /**
     * Calls the given consumer the next time the player interacts with a block
     *
     * @param consumer
     * @return
     */
    public abstract EventFuture onNextBlockInteract(EventConsumer<BlockInteractEvent> consumer);

    /**
     * Calls the given consumer every time the player interacts with a protection.
     *
     * @param consumer
     * @return
     */
    public abstract EventFuture onEveryBlockInteract(EventConsumer<BlockInteractEvent> consumer);

    @Override
    public void sendMessage(String message, Object... arguments) {
        if (arguments.length == 0) {
            sendMessage(message);
        } else {
            sendMessage(MessageFormat.format(message, arguments));
        }
    }

    @Override
    public void sendTranslatedMessage(String message, Object... arguments) {
        sendMessage(_(message, this, arguments));
    }

    /**
     * Get the locale for the player
     *
     * @return the player's locale
     */
    @Override
    public Locale getLocale() {
        return locale;
    }

    /**
     * Set the locale for the player
     *
     * @param locale
     */
    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

}
