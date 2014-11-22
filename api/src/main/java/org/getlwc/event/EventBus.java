package org.getlwc.event;

public interface EventBus {

    /**
     * Posts an event onto the event bus
     *
     * @param event
     */
    public boolean post(Event event);

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
