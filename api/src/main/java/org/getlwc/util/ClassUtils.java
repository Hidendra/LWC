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
