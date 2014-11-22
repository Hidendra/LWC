package org.getlwc.event.handler;

import org.getlwc.component.Component;
import org.getlwc.event.Event;
import org.getlwc.event.ProtectionListener;
import org.getlwc.event.ProtectionEvent;
import org.getlwc.model.Protection;

import java.lang.reflect.Method;

public class ProtectionHandler extends BaseHandler {

    /**
     * The listener annotation attached to this handler
     */
    private ProtectionListener listener;

    public ProtectionHandler(Object parent, Method method, ProtectionListener listener) {
        super(parent, method);
        this.listener = listener;
    }

    @Override
    public void invoke(Event event) {
        if (!(event instanceof ProtectionEvent)) {
            return;
        }

        ProtectionEvent protectionEvent = (ProtectionEvent) event;
        Protection protection = protectionEvent.getProtection();

        boolean shouldCallEvent = false;

        if (listener.components().length == 0) {
            shouldCallEvent = true;
        } else {
            for (Class<? extends Component> componentClass : listener.components()) {
                if (protection.hasComponent(componentClass)) {
                    shouldCallEvent = true;
                    break;
                }
            }
        }

        if (shouldCallEvent) {
            super.invoke(event);
        }
    }

}
