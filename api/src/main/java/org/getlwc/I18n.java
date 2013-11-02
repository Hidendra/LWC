/*
 * Copyright (c) 2011-2013 Tyler Blair
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

package org.getlwc;

import org.getlwc.command.CommandSender;
import org.getlwc.lang.DefaultMessageStore;
import org.getlwc.lang.Locale;
import org.getlwc.lang.MessageStore;
import org.getlwc.util.StringUtils;

import java.text.MessageFormat;

public class I18n {

    /**
     * The private localization instance
     */
    private static I18n instance = new I18n();

    /**
     * The storage medium for messages
     */
    private DefaultMessageStore store = new DefaultMessageStore();

    private I18n() {
    }

    /**
     * Initialize the i18n context
     *
     * @param engine
     */
    public static void init(Engine engine) {
        instance.store.init(engine);
    }

    /**
     * Get the {@link MessageStore} that is being used to translate
     *
     * @return
     */
    public static MessageStore getMessageStore() {
        return instance.store;
    }

    /**
     * Dummy method to mark a string as translatable internally. Used to tag it in gettext and is then
     * later called via _ / translate. This is done in the event of e.g. enums so that enum names
     * can be translated without a bunch more code to manually add them in.
     *
     * @param message
     * @return the message you passed it
     */
    public static String markAsTranslatable(String message) {
        return message;
    }

    /**
     * Translate a message to the currently enabled message lang.
     *
     * @param message the message to translate
     * @return The translated message
     */
    public static String _(String message) {
        return instance.translate(message);
    }

    /**
     * Translate a message to the currently enabled message lang.
     *
     * @param message   the message to translate
     * @param arguments the arguments to bind to any parameters in the message
     * @return The translated message
     */
    public static String _(String message, Object... arguments) {
        return instance.translate(message, arguments);
    }

    /**
     * Translate a message to the currently enabled message lang.
     *
     * @param message   the message to translate
     * @param sender
     * @param arguments the arguments to bind to any parameters in the message
     * @return The translated message
     */
    public static String _(String message, CommandSender sender, Object... arguments) {
        return instance.translate(message, sender, arguments);
    }

    /**
     * Translate a message with the given arguments
     *
     * @param message the message to translate
     * @return The translated message
     */
    private String translate(String message) {
        return store.getString(message);
    }

    /**
     * Translate a message with the given arguments
     *
     * @param message   the message to translate
     * @param arguments the arguments to bind to any parameters in the message
     * @return The translated message
     */
    private String translate(String message, Object... arguments) {
        String translated = store.getString(message);

        if (arguments.length == 0) {
            return translated;
        } else {
            return MessageFormat.format(StringUtils.escapeMessageFormat(translated), arguments);
        }
    }

    /**
     * Translate a message with the given arguments
     *
     * @param message   the message to translate
     * @param sender
     * @param arguments the arguments to bind to any parameters in the message
     * @return The translated message
     */
    private String translate(String message, CommandSender sender, Object... arguments) {
        Locale locale = sender.getLocale();

        if (locale == null) {
            return translate(message, arguments);
        } else {
            String translated = store.getString(message, locale);

            if (arguments.length == 0) {
                return translated;
            } else {
                return MessageFormat.format(StringUtils.escapeMessageFormat(translated), arguments);
            }
        }
    }

}
