/*
 * Copyright (c) 2011, 2012, Tyler Blair
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

package com.griefcraft.event;

import com.griefcraft.event.notifiers.BlockEventNotifier;
import com.griefcraft.event.notifiers.ProtectionEventNotifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PlayerEventHandler {

    public enum Type {

        /**
         * When the player interacts with a protection
         */
        PLAYER_INTERACT_PROTECTION,

        /**
         * When a player interacts with a block that has no protection
         */
        PLAYER_INTERACT_BLOCK

    }

    /**
     * The map of non-temporary event notifiers
     */
    private final Map<Type, List<EventNotifier>> notifiers = new HashMap<Type, List<EventNotifier>>();

    /**
     * We only allow one temporary event notifier for each event type to prevent large amounts of
     * cascading event calls.
     * By design it is safe to assume that a temporary event notifier is normally for when we want
     * the player to execute an action. If it's overridden by another temporary notifier, then
     * it can be assumed that they want to do something else instead.
     */
    private final Map<Type, EventNotifier<?>> temporaryNotifiers = new HashMap<Type, EventNotifier<?>>();

    /**
     * Queue the notifier to be called next time the player interacts with a protection
     *
     * @param notifier
     */
    public void onPlayerInteract(ProtectionEventNotifier notifier) {
        checkNotifier(notifier);
        addEventNotifier(Type.PLAYER_INTERACT_PROTECTION, notifier);
    }

    /**
     * Queue the notifier to be called next time the player interacts with a block
     *
     * @param notifier
     */
    public void onBlockInteract(BlockEventNotifier notifier) {
        checkNotifier(notifier);
        addEventNotifier(Type.PLAYER_INTERACT_BLOCK, notifier);
    }

    /**
     * Call an event for the given type
     *
     * @param type
     * @param event
     * @return true if the event should be cancelled, false otherwise
     * @throws EventException
     */
    protected boolean callEvent(Type type, Event event) throws EventException {
        // Check temporary notifier
        if (temporaryNotifiers != null) {
            EventNotifier<?> notifier = temporaryNotifiers.get(type);

            if (notifier != null) {
                // remove the temporary notifier association before calling it
                temporaryNotifiers.remove(type);

                if (internalCallEvent(event, notifier)) {
                    return true;
                }
            }
        }

        List<EventNotifier> notifiers = this.notifiers.get(type);

        if (notifiers == null) {
            return false; // Nothing to call
        }

        Iterator<EventNotifier> iter = notifiers.iterator();
        while (iter.hasNext()) {
            EventNotifier<?> notifier = iter.next();

            // First remove the event if it is temporary
            if (notifier.isTemporary()) {
                iter.remove();
            }

            // Now call the event
            // We call the event after removing it because it can throw an exception
            // So we want to make sure it is removed incase it constantly throws
            // the exception
            if (internalCallEvent(event, notifier)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Call an event on the event notifier and return the result
     *
     * @param event
     * @param notifier
     * @return
     * @throws EventException
     */
    private boolean internalCallEvent(Event event, EventNotifier notifier) throws EventException {
        try {
            boolean result = notifier.unsafeCall(event);

            if (result) {
                return true;
            }
        } catch (Exception e) {
            throw new EventException("Event notifier threw an exception!", e);
        }

        return false;
    }

    /**
     * Queue a notifier to be called the next time an event is called
     *
     * @param type
     * @param notifier
     */
    private void addEventNotifier(Type type, EventNotifier notifier) {
        if (type == null) {
            throw new IllegalArgumentException("Event type cannot be null");
        }
        if (notifier == null) {
            throw new IllegalArgumentException("Event notifier cannot be null");
        }

        // handle temporary notifiers separately
        if (notifier.isTemporary()) {
            temporaryNotifiers.put(type, notifier);
            return;
        }

        // Make sure a list is available for us to write into
        checkNotifiers(type);

        // Get the list
        List<EventNotifier> notifiers = this.notifiers.get(type);

        // write into it
        notifiers.add(notifier);
    }

    /**
     * Checks a callback, making sure it is not null
     *
     * @param notifier
     */
    private void checkNotifier(EventNotifier<?> notifier) {
        if (notifier == null) {
            throw new IllegalArgumentException("Event notifier cannot be null");
        }
    }

    /**
     * Check for notifiers and ensure a valid list is available
     * @param type
     */
    private void checkNotifiers(Type type) {
        List<EventNotifier> notifiers = this.notifiers.get(type);

        if (notifiers == null) {
            this.notifiers.put(type, new ArrayList<EventNotifier>());
        }
    }

}
