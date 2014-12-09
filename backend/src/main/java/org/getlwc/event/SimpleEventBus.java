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
