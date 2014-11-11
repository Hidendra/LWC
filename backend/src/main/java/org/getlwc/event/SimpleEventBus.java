package org.getlwc.event;

import org.getlwc.event.handler.BaseHandler;
import org.getlwc.event.handler.ProtectionHandler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SimpleEventBus implements EventBus {

    /**
     * All handlers for the bus
     */
    private Map<Class<? extends Event>, Set<BaseHandler>> handlers = new HashMap<>();

    @Override
    public boolean dispatch(Event event) {
        Set<BaseHandler> handlerList = handlers.get(event.getClass());

        if (handlerList == null) {
            return false;
        }

        for (BaseHandler handler : handlerList) {
            handler.invoke(event);
        }

        return event.isCancelled();
    }

    @Override
    public void registerAll(Object object) {
        for (BaseHandler handler : findHandlers(object)) {
            Class<? extends Event> type = handler.getEventType();

            verifyHandlerList(type);
            handlers.get(type).add(handler);
        }
    }

    @Override
    public void unregisterAll(Object object) {
        for (BaseHandler handler : findHandlers(object)) {
            Class<? extends Event> type = handler.getEventType();

            verifyHandlerList(type);
            handlers.get(type).remove(handler);
        }
    }

    /**
     * Finds all handlers on the given object
     *
     * @param object
     * @return
     */
    private List<BaseHandler> findHandlers(Object object) {
        List<BaseHandler> result = new ArrayList<>();

        for (Method method : object.getClass().getMethods()) {
            Class<?>[] parameters = method.getParameterTypes();

            if (parameters.length != 1 || !Event.class.isAssignableFrom(parameters[0])) {
                continue;
            }

            BaseHandler handler = null;

            if (method.isAnnotationPresent(Listener.class)) {
                handler = new BaseHandler(object, method);
            } else if (method.isAnnotationPresent(ProtectionListener.class)) {
                handler = new ProtectionHandler(object, method, method.getAnnotation(ProtectionListener.class));
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
            handlers.put(clazz, new HashSet<BaseHandler>());
        }
    }

}
