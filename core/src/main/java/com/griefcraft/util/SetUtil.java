package com.griefcraft.util;

import java.util.Set;

public class SetUtil {

    private SetUtil() {
    }

    /**
     * Adds the given object to the set if it's not null.
     * EnumSets error out if you add a null value, so this works around
     * it with a helper method.
     *
     * @param set
     * @param obj
     * @param <T>
     */
    public static <T> void addToSetWithoutNull(Set<T> set, T obj) {
        if (obj != null) {
            set.add(obj);
        }
    }

}
