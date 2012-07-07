package com.griefcraft;

import java.text.MessageFormat;

public class I18n {

    /**
     * Translate a message to the currently enabled message locale.
     * TODO
     *
     * @param message
     * @param arguments
     * @return
     */
    public static String _(String message, Object... arguments) {
        return MessageFormat.format(message, arguments);
    }

}
