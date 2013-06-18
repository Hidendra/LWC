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

}
