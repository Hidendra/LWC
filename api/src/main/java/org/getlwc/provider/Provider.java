package org.getlwc.provider;

public interface Provider<T> {

    /**
     * Check if the passed input (typically from a user) should be provided
     * by this provider. For example, if this provider provides an attribute
     * for passwords on a protection, then I should return true for inputs
     * such as "pass" and "password"
     *
     * @param input
     * @return
     */
    public boolean shouldProvide(String input);

}
