package org.getlwc.event;

import org.getlwc.component.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ProtectionListener {

    /**
     * The protection must contain at least one of the given components for the event to be passed to the method.
     * An empty component list is considered a wildcard.
     *
     * @return
     */
    Class<? extends Component>[] components() default {};

}
