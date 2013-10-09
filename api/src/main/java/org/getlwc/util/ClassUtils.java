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
                Field negativeResourceCache = ClassUtils.class.getClassLoader().getClass().getDeclaredField("negativeResourceCache");

                if (invalidClasses != null) {
                    invalidClasses.setAccessible(true);

                    Set<String> set = (Set<String>) invalidClasses.get(ClassUtils.class.getClassLoader());

                    if (set != null) {
                        set.remove(className);
                    }
                }

                if (negativeResourceCache != null) {
                    negativeResourceCache.setAccessible(true);

                    Set<String> set = (Set<String>) negativeResourceCache.get(ClassUtils.class.getClassLoader());

                    if (set != null) {
                        set.remove(className);
                    }

                }
            } catch (Exception ex) {
                // This is ok. It just means we are not being loaded by Mojang's LaunchClassLoader
            }

            return false;
        }
    }

}
