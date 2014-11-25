package org.getlwc.event;

import org.getlwc.event.filter.BaseEventFilter;
import org.getlwc.event.filter.ProtectionEventFilter;
import org.getlwc.event.handler.BaseHandler;
import org.getlwc.event.handler.EventHandler;
import org.getlwc.event.handler.FilteredHandler;
import org.getlwc.event.handler.SubscribeHandler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SimpleEventBus implements EventBus {

    /**
     * All handlers for the bus
     */
    private final Map<Class<? extends Event>, Set<EventHandler>> handlers = new HashMap<>();

    @Override
    public boolean post(Event event) {
        Set<EventHandler> handlerList = handlers.get(event.getClass());

        if (handlerList == null) {
            return false;
        }

        Iterator<EventHandler> iter = handlerList.iterator();

        while (iter.hasNext()) {
            EventHandler handler = iter.next();

            if (handler instanceof EventFuture && ((EventFuture) handler).isCancelled()) {
                iter.remove();
                continue;
            }

            handler.invoke(event);
        }

        return event.isCancelled();
    }

    @Override
    public <T extends Event> EventFuture subscribe(Class<T> clazz, EventConsumer<T> consumer) {
        SubscribeHandler handler = new SubscribeHandler<>(this, clazz, consumer);
        register(handler);
        return handler;
    }

    /**
     * Unsubscribes the subscribed handler
     *
     * @param handler
     */
    public void unsubscribe(EventHandler handler) {
        synchronized (handlers) {
            verifyHandlerList(handler.getEventType());
            handlers.get(handler.getEventType()).remove(handler);
        }
    }

    /**
     * Registers an event handler
     *
     * @param handler
     */
    public void register(EventHandler handler) {
        verifyHandlerList(handler.getEventType());
        handlers.get(handler.getEventType()).add(handler);
    }

    @Override
    public void subscribe(Object object) {
        synchronized (handlers) {
            for (EventHandler handler : findHandlers(object)) {
                register(handler);
            }
        }
    }

    @Override
    public void unsubscribe(Object object) {
        synchronized (handlers) {
            for (EventHandler handler : findHandlers(object)) {
                unsubscribe(handler);
            }
        }
    }

    /**
     * Finds all handlers on the given object
     *
     * @param object
     * @return
     */
    private List<EventHandler> findHandlers(Object object) {
        List<EventHandler> result = new ArrayList<>();

        for (Method method : object.getClass().getMethods()) {
            Class<?>[] parameters = method.getParameterTypes();

            if (parameters.length != 1 || !Event.class.isAssignableFrom(parameters[0])) {
                continue;
            }

            BaseHandler handler = null;

            if (method.isAnnotationPresent(Listener.class)) {
                Listener listener = method.getAnnotation(Listener.class);
                handler = new FilteredHandler(object, method, new BaseEventFilter(listener));
            } else if (method.isAnnotationPresent(ProtectionListener.class)) {
                ProtectionListener listener = method.getAnnotation(ProtectionListener.class);
                handler = new FilteredHandler(object, method, new ProtectionEventFilter(listener));
            }

            if (handler != null) {
                result.add(handler);
            }
        }

        return result;
    }

    /**
     * Verfies that the handler list for the given class is valid.
     * If it isn't, it is initialized.
     *
     * @param clazz
     */
    private void verifyHandlerList(Class<? extends Event> clazz) {
        if (!handlers.containsKey(clazz)) {
            handlers.put(clazz, new HashSet<EventHandler>());
        }
    }

}
