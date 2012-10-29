package com.griefcraft;

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
