package org.getlwc.configuration;

public class UnknownConfigurationTypeException extends RuntimeException {

    public UnknownConfigurationTypeException() {
    }

    public UnknownConfigurationTypeException(String message) {
        super(message);
    }

    public UnknownConfigurationTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownConfigurationTypeException(Throwable cause) {
        super(cause);
    }

}
