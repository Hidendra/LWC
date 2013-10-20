package org.getlwc.util;

import java.lang.reflect.Field;
import java.util.Set;

public class ClassUtils {

    /**
     * Check if a class is loaded
     *
     * @param className
     * @return
     */
    public static boolean isClassLoaded(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (Exception e) {
            // Remove the class from Mojang's LaunchClassLoader cache if present
            try {
                Field invalidClasses = ClassUtils.class.getClassLoader().getClass().getDeclaredField("invalidClasses");

                if (invalidClasses != null) {
                    invalidClasses.setAccessible(true);

                    Set<String> set = (Set<String>) invalidClasses.get(ClassUtils.class.getClassLoader());

                    if (set != null) {
                        set.remove(className);
                    }
                }

                try {
                    Field negativeResourceCache = ClassUtils.class.getClassLoader().getClass().getDeclaredField("negativeResourceCache");

                    if (negativeResourceCache != null) {
                        negativeResourceCache.setAccessible(true);

                        Set<String> set = (Set<String>) negativeResourceCache.get(ClassUtils.class.getClassLoader());

                        if (set != null) {
                            set.remove(className);
                        }

                    }
                } catch (Exception ex) {
                    // negativeResourceCache is only in MC 1.6.4+ so we trap it
                }
            } catch (Exception ex) {
            }

            return false;
        }
    }

}
