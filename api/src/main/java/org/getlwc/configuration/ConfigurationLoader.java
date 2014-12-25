package org.getlwc.configuration;

import java.io.File;
import java.io.InputStream;

public interface ConfigurationLoader {

    /**
     * Loads a config from a file.
     *
     * @param file
     * @return
     */
    public Configuration load(File file);

    /**
     * Loads a config from an input stream. It will be read-only and cannot be saved.
     *
     * @param stream
     * @return
     */
    public Configuration load(InputStream stream);

}
