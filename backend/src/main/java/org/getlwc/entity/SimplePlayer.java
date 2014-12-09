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

import org.getlwc.SimpleEngine;
import org.getlwc.event.CompositeEventFuture;
import org.getlwc.event.EventConsumer;
import org.getlwc.event.EventFuture;
import org.getlwc.event.block.BlockInteractEvent;
import org.getlwc.event.protection.ProtectionInteractEvent;

import java.util.ArrayList;
import java.util.List;

public abstract class SimplePlayer extends Player {

    /**
     * A list of all long-running futures
     */
    private final List<EventFuture> eventFutures = new ArrayList<>();

    /**
     * The future that was requested to be ran next
     */
    private EventFuture nextFuture = null;

    @Override
    public boolean hasPermission(String node) {
        return SimpleEngine.getInstance().getPermissionHandler().hasPermission(this, node);
    }

    @Override
    public EventFuture onNextProtectionInteract(final EventConsumer<ProtectionInteractEvent> consumer) {
        if (nextFuture != null) {
            nextFuture.cancel();
        }

        EventFuture mainFuture = SimpleEngine.getInstance().getEventBus().subscribe(ProtectionInteractEvent.class, new EventConsumer<ProtectionInteractEvent>() {
            @Override
            public void accept(ProtectionInteractEvent event) {
                consumer.accept(event);
                nextFuture.cancel();
            }
        });

        EventFuture secondaryFuture = SimpleEngine.getInstance().getEventBus().subscribe(BlockInteractEvent.class, new EventConsumer<BlockInteractEvent>() {
            @Override
            public void accept(BlockInteractEvent event) {
                sendTranslatedMessage("&4That block is not protected. Please interact with one that is.");
                event.markCancelled();
            }
        });

        EventFuture future = new CompositeEventFuture(mainFuture, secondaryFuture);

        nextFuture = future;
        return future;
    }

    @Override
    public EventFuture onEveryProtectionInteract(final EventConsumer<ProtectionInteractEvent> consumer) {
        EventFuture future = SimpleEngine.getInstance().getEventBus().subscribe(ProtectionInteractEvent.class, new EventConsumer<ProtectionInteractEvent>() {
            @Override
            public void accept(ProtectionInteractEvent event) {
                consumer.accept(event);
            }
        });

        eventFutures.add(future);
        return future;
    }

    @Override
    public EventFuture onEveryBlockInteract(final EventConsumer<BlockInteractEvent> consumer) {
        EventFuture future = SimpleEngine.getInstance().getEventBus().subscribe(BlockInteractEvent.class, new EventConsumer<BlockInteractEvent>() {
            @Override
            public void accept(BlockInteractEvent event) {
                consumer.accept(event);
            }
        });

        eventFutures.add(future);
        return future;
    }

    @Override
    public EventFuture onNextBlockInteract(final EventConsumer<BlockInteractEvent> consumer) {
        if (nextFuture != null) {
            nextFuture.cancel();
        }

        EventFuture mainFuture = SimpleEngine.getInstance().getEventBus().subscribe(BlockInteractEvent.class, new EventConsumer<BlockInteractEvent>() {
            @Override
            public void accept(BlockInteractEvent event) {
                consumer.accept(event);
                nextFuture.cancel();
            }
        });

        EventFuture secondaryFuture = SimpleEngine.getInstance().getEventBus().subscribe(ProtectionInteractEvent.class, new EventConsumer<ProtectionInteractEvent>() {
            @Override
            public void accept(ProtectionInteractEvent event) {
                sendTranslatedMessage("&4That block is protected. Please interact with one that isn't.");
                event.markCancelled();
            }
        });

        EventFuture future = new CompositeEventFuture(mainFuture, secondaryFuture);

        nextFuture = future;
        return future;
    }
}
