/*
 * Copyright 2011 Tyler Blair. All rights reserved.
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

package com.griefcraft.lwc;

import com.griefcraft.util.Colors;
import com.griefcraft.util.StringUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class SimpleMessageParser implements MessageParser {

    /**
     * The i18n localization bundle
     */
    private final ResourceBundle locale;

    /**
     * Cached messages
     */
    private final Map<String, String> basicMessageCache = new HashMap<String, String>();

    /**
     * A heavy cache that includes binds.
     */
    private final Map<String, String> bindMessageCache = new HashMap<String, String>();

    public SimpleMessageParser(ResourceBundle locale) {
        this.locale = locale;
    }

    public String parseMessage(String key, Object... args) {
        key = StringUtil.fastReplace(key, ' ', '_');

        // For the bind cache
        String cacheKey = key;

        // add the arguments to the cache key
        if (args != null && args.length > 0) {
            for (Object argument : args) {
                cacheKey += argument.toString();
            }
        }

        if (bindMessageCache.containsKey(cacheKey)) {
            return bindMessageCache.get(cacheKey);
        }

        if (!locale.containsKey(key)) {
            return null;
        }

        Map<String, Object> bind = parseBinds(args);
        String value = basicMessageCache.get(key);

        if (value == null) {
            value = locale.getString(key);

            // apply colors
            for (String colorKey : Colors.localeColors.keySet()) {
                String color = Colors.localeColors.get(colorKey);

                if (value.contains(colorKey)) {
                    value = StringUtil.fastReplace(value, colorKey, color);
                }
            }

            // Apply aliases
            String[] aliasvars = new String[]{"cprivate", "cpublic", "cpassword", "cmodify", "cunlock", "cinfo", "cremove"};

            // apply command name modification depending on menu style
            for (String alias : aliasvars) {
                String replace = "%" + alias + "%";

                if (!value.contains(replace)) {
                    continue;
                }

                String localeName = alias + ".basic";
                value = value.replace(replace, parseMessage(localeName));
            }

            // Cache it
            basicMessageCache.put(key, value);
        }

        // apply binds
        for (String bindKey : bind.keySet()) {
            Object object = bind.get(bindKey);

            value = StringUtil.fastReplace(value, "%" + bindKey + "%", object.toString());
        }

        // include the binds
        bindMessageCache.put(cacheKey, value);
        return value;
    }

    /**
     * Convert an even-lengthed argument array to a map containing String keys i.e parseBinds("Test", null, "Test2", obj) = Map().put("test", null).put("test2", obj)
     *
     * @param args
     * @return
     */
    private Map<String, Object> parseBinds(Object... args) {
        Map<String, Object> bind = new HashMap<String, Object>();

        if (args == null || args.length < 2) {
            return bind;
        }

        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("The given arguments length must be equal");
        }

        int size = args.length;
        for (int index = 0; index < args.length; index += 2) {
            if ((index + 2) > size) {
                break;
            }

            String key = args[index].toString();
            Object object = args[index + 1];

            bind.put(key, object);
        }

        return bind;
    }

}
