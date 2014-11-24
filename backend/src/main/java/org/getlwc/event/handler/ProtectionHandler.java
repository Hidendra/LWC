package org.getlwc.event.handler;

import org.getlwc.component.Component;
import org.getlwc.event.Event;
import org.getlwc.event.Listener;
import org.getlwc.event.ProtectionListener;
import org.getlwc.event.protection.ProtectionEvent;
import org.getlwc.model.Protection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class ProtectionHandler extends BaseHandler {

    /**
     * The listener annotation attached to this handler
     */
    private ProtectionListener listener;

    public ProtectionHandler(final ProtectionListener listener, Object parent, Method method) {
        super(new Listener() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Listener.class;
            }

            @Override
            public boolean ignoreCancelled() {
                return listener.ignoreCancelled();
            }
        }, parent, method);
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
