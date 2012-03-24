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

package com.griefcraft.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PlayerEventHandler {

    private enum Type {

        /**
         * When the player interacts with a block
         */
        PLAYER_INTERACT

    }

    /**
     * The event notifiers
     */
    private final Map<Type, List<EventNotifier>> notifiers = new HashMap<Type, List<EventNotifier>>();

    /**
     * Queue the notifier to be called next time the player interacts with a protection
     *
     * @param callback
     */
    public void onPlayerInteract(Runnable callback) {
        checkCallback(callback);
        onEvent(Type.PLAYER_INTERACT, new EventNotifier(callback));
    }

    /**
     * Queue the notifier to be called next time the player interacts with a protection
     *
     * @param callback
     * @param temporary
     */
    public void onPlayerInteract(Runnable callback, boolean temporary) {
        checkCallback(callback);
        onEvent(Type.PLAYER_INTERACT, new EventNotifier(callback, temporary));
    }

    /**
     * Call an event for the given type
     *
     * @param type
     * @throws EventException
     */
    protected void callEvent(Type type) throws EventException {
        List<EventNotifier> notifiers = this.notifiers.get(type);

        if (notifiers == null) {
            return; // Nothing to call
        }

        Iterator<EventNotifier> iter = notifiers.iterator();
        while (iter.hasNext()) {
            EventNotifier notifier = iter.next();

            // First remove the event if it is temporary
            if (notifier.isTemporary()) {
                iter.remove();
            }

            // Now call the event
            // We call the event after removing it because it can throw an exception
            // So we want to make sure it is removed incase it constantly throws
            // the exception
            notifier.call();
        }
    }

    /**
     * Queue a notifier to be called the next time an event is called
     *
     * @param type
     * @param notifier
     */
    private void onEvent(Type type, EventNotifier notifier) {
        if (type == null) {
            throw new IllegalArgumentException("Event type cannot be null");
        }
        if (notifier == null) {
            throw new IllegalArgumentException("Event notifier cannot be null");
        }

        // Make sure a list is available for us to write into
        checkObserversList(type);

        // Get the list
        List<EventNotifier> notifiers = this.notifiers.get(type);

        // write into it
        notifiers.add(notifier);
    }

    /**
     * Checks a callback, making sure it is not null
     *
     * @param callback
     */
    private void checkCallback(Runnable callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }
    }

    /**
     * Check the notifiers list and ensure a valid list is available
     * @param type
     */
    private void checkObserversList(Type type) {
        List<EventNotifier> notifiers = this.notifiers.get(type);

        if (notifiers == null) {
            this.notifiers.put(type, new ArrayList<EventNotifier>());
        }
    }

}
