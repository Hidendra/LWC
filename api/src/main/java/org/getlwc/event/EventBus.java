package org.getlwc.event;

public interface EventBus {

    /**
     * Dispatches an event onto the event bus
     *
     * @param event
     */
    public boolean dispatch(Event event);

    /**
     * Registers all listeners for the given object
     *
     * @param object
     */
    public void registerAll(Object object);

    /**
     * Unregisters all listeners for the given object
     *
     * @param object
     */
    public void unregisterAll(Object object);

}
