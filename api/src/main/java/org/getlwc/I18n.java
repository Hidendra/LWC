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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class I18n {

    /**
     * The private localization instance
     */
    private static I18n instance = new I18n();

    /**
     * The resource bundle that is to be used
     */
    private ResourceBundle bundle;

    private I18n() {
    }

    /**
     * Translate a message to the currently enabled message locale.
     *
     * @param message the message to translate
     * @return The translated message
     */
    public static String _(String message) {
        return instance.translate(message);
    }

    /**
     * Translate a message to the currently enabled message locale.
     *
     * @param message   the message to translate
     * @param arguments the arguments to bind to any parameters in the message
     * @return The translated message
     */
    public static String _(String message, Object... arguments) {
        return instance.translate(message, arguments);
    }

    /**
     * Translate a message with the given arguments
     *
     * @param message the message to translate
     * @return The translated message
     */
    private String translate(String message) {
        // load the bundle if it hasn't been loaded yet
        if (bundle == null) {
            loadLanguageBundle();
        }

        return bundle.getString(message);
    }

    /**
     * Translate a message with the given arguments
     *
     * @param message   the message to translate
     * @param arguments the arguments to bind to any parameters in the message
     * @return The translated message
     */
    private String translate(String message, Object... arguments) {
        // load the bundle if it hasn't been loaded yet
        if (bundle == null) {
            loadLanguageBundle();
        }

        return MessageFormat.format(translate(message), arguments);
    }

    /**
     * Load the language bundle
     */
    private void loadLanguageBundle() {
        try {
            bundle = new PropertyResourceBundle(getClass().getResourceAsStream("/Messages_en.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
