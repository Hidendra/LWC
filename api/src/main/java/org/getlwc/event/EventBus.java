package org.getlwc.event;

public interface EventBus {

    /**
     * Posts an event onto the event bus
     *
     * @param event
     */
    public boolean post(Event event);

    /**
     * Subscribes to the given event. It will continue calling every time the event is thrown
     * until it is cancelled (by returning {@link org.getlwc.event.EventConsumer.Result#CANCEL} from the consumer)
     *
     * @param clazz
     * @param consumer
     * @param <T>
     * @return The future object for the subscribed listener.
     */
    public <T extends Event> EventFuture subscribe(Class<T> clazz, EventConsumer<T> consumer);

    /**
     * Registers all listeners for the given object
     *
     * @param object
     */
    public void subscribe(Object object);

    /**
     * Unregisters all listeners for the given object
     *
     * @param object
     */
    public void unsubscribe(Object object);

}
