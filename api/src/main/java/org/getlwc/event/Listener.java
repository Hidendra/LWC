package org.getlwc.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Listener {

    /**
     * Returns true if the listener should ignore cancelled events. Defaults to false.
     *
     * @return
     */
    boolean ignoreCancelled() default false;

}
