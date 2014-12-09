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
package org.getlwc.event.handler;

import org.getlwc.event.Event;
import org.getlwc.event.EventConsumer;
import org.getlwc.event.EventFuture;
import org.getlwc.event.SimpleEventBus;

public class SubscribeHandler<T extends Event> implements EventHandler, EventFuture {

    private SimpleEventBus eventBus;

    /**
     * The event type being subscribed to
     */
    private Class<T> eventType;

    /**
     * The consumer from the subscriber
     */
    private EventConsumer<T> consumer;

    private boolean cancelled = false;

    public SubscribeHandler(SimpleEventBus eventBus, Class<T> eventType, EventConsumer<T> consumer) {
        this.eventBus = eventBus;
        this.eventType = eventType;
        this.consumer = consumer;
    }

    @Override
    public Class<? extends Event> getEventType() {
        return eventType;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void invoke(Event event) {
        consumer.accept((T) event);
    }

    @Override
    public void cancel() {
        if (!cancelled) {
            eventBus.unsubscribe(this);
            cancelled = true;
        }
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

}
