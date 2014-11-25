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
