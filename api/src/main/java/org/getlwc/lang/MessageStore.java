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

import java.util.ResourceBundle;

public interface MessageStore {

    /**
     * Gets a string using the default locale. If the default locale does not exist the given message is returned back.
     *
     * @param message
     * @return
     */
    public String getString(String message);

    /**
     * Gets a string using the given locale. If the given locale does not exist the default locale will be attempted.
     * If the default locale ALSO does not exist then the given message is returned back.
     *
     * @param message
     * @param locale
     * @return
     */
    public String getString(String message, Locale locale);

    /**
     * Get a language bundle. The bundle will be loaded if it is not already loaded. Null can be returned meaning the bundle was not found.
     *
     * @param locale
     * @return
     */
    public ResourceBundle getBundle(Locale locale);

    /**
     * Get the default locale that will be used for translating messages if a specific locale is not available
     *
     * @return
     */
    public Locale getDefaultLocale();

    /**
     * Check if the message store supports the given locale. If it is not supported then the default store will
     * be used instead when translating messages.
     *
     * @param locale
     * @return
     */
    public boolean supports(Locale locale);

}
