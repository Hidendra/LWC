package org.getlwc.entity;

import org.getlwc.SimpleEngine;
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

        EventFuture future = SimpleEngine.getInstance().getEventBus().subscribe(ProtectionInteractEvent.class, new EventConsumer<ProtectionInteractEvent>() {
            @Override
            public void accept(ProtectionInteractEvent event) {
                consumer.accept(event);
                nextFuture.cancel();
            }
        });

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

        EventFuture future = SimpleEngine.getInstance().getEventBus().subscribe(BlockInteractEvent.class, new EventConsumer<BlockInteractEvent>() {
            @Override
            public void accept(BlockInteractEvent event) {
                consumer.accept(event);
            }
        });

        nextFuture = future;
        return future;
    }
}
