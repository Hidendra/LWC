package org.getlwc.provider;

@Deprecated
public interface Providable {

    /**
     * Load data for the providable via the passed data string
     *
     * @param data
     */
    public void loadData(String data);

}
