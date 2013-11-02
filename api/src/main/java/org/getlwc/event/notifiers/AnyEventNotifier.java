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

package org.getlwc.event.notifiers;

import org.getlwc.entity.Player;
import org.getlwc.event.EventNotifier;

public class AnyEventNotifier extends EventNotifier {

    /**
     * The slave event notifier if we are acting as the parent
     */
    private EventNotifier slave = null;

    /**
     * The player to send the message to if we are acting as a slave
     */
    private Player player = null;

    /**
     * The message to send to the player if we are acting as a slave
     */
    private String message = null;

    /**
     * Create an {@link AnyEventNotifier} as a parent. Once this notifier is called, it will kill the slave
     * notifier that notifies of the inverse
     *
     * @param slave
     */
    public AnyEventNotifier(EventNotifier<?> slave) {
        this.slave = slave;
    }

    /**
     * Create an {@link AnyEventNotifier} as a slave. It will simply send the message to the player if
     * this notifier is triggered
     *
     * @param message
     */
    public AnyEventNotifier(Player player, String message) {
        super(true);
        this.player = player;
        this.message = message;
    }

    @Override
    public boolean call(Object event) {
        if (slave != null) {
            return slave.call(event);
        } else if (message != null) {
            player.sendMessage(message);
            return true;
        }

        throw new UnsupportedOperationException("AnyEventNotifier is in an illegal state: no actions");
    }

}
