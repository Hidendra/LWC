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
package org.getlwc.lang;

import org.getlwc.Engine;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.zip.GZIPInputStream;

public class DefaultMessageStore implements MessageStore {

    /**
     * The default locale to use before the configuration can be initialized.
     * For example, the downloader spits out localised output however it downloads
     * the required YAML libraries, so a default locale must be used first.
     */
    private static final Locale DEFAULT_LOCALE = new Locale("en_US");

    /**
     * A store of the loaded resource bundles. The bundle CAN be null (i.e. does not exist)
     */
    private Map<Locale, ResourceBundle> bundles = new HashMap<>();

    /**
     * The default lang
     */
    private Locale defaultLocale;

    public DefaultMessageStore() {
    }

    /**
     * Initialize the message store
     *
     * @param engine
     */
    public void init(Engine engine) {
        defaultLocale = new Locale(engine.getConfiguration().getString("core.locale", DEFAULT_LOCALE.getName()));

        if (getBundle(defaultLocale) == null) {
            engine.getConsoleSender().sendTranslatedMessage("WARNING: The default locale ({0}) has no associated language file installed!", defaultLocale.getName());
        } else {
            engine.getConsoleSender().sendTranslatedMessage("Using default locale: {0}", defaultLocale.getName());
        }
    }

    @Override
    public String getString(String message) {
        return getString(message, defaultLocale == null ? DEFAULT_LOCALE : defaultLocale);
    }

    @Override
    public String getString(String message, Locale locale) {
        if (message == null) {
            throw new UnsupportedOperationException("message cannot be null");
        }

        if (locale == null) {
            throw new UnsupportedOperationException("locale cannot be null");
        }

        // attempt the given locale first
        ResourceBundle bundle = getBundle(locale);

        if (bundle == null && !locale.equals(defaultLocale)) {
            if (defaultLocale == null) {
                return message;
            }

            bundle = getBundle(defaultLocale);
        }

        if (bundle == null && !locale.equals(DEFAULT_LOCALE)) {
            bundle = getBundle(DEFAULT_LOCALE);
        }

        if (bundle == null) {
            return message;
        }

        return bundle.getString(message);
    }

    @Override
    public ResourceBundle getBundle(Locale locale) {
        if (locale == null) {
            return null;
        }

        if (bundles.containsKey(locale)) {
            return bundles.get(locale);
        }

        ResourceBundle bundle = null;

        try {
            String filePath = "/lang/" + locale.getName() + ".lang";
            InputStream stream;

            // try compressed version first
            stream = getClass().getResourceAsStream(filePath + ".gz");

            if (stream != null) {
                bundle = new PropertyResourceBundle(new GZIPInputStream(stream));
            } else {
                stream = getClass().getResourceAsStream(filePath);

                if (stream != null) {
                    bundle = new PropertyResourceBundle(stream);
                }
            }
        } catch (IOException e) {
        }

        bundles.put(locale, bundle);
        return bundle;
    }

    @Override
    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    @Override
    public boolean supports(Locale locale) {
        return getBundle(locale) != null;
    }

}
