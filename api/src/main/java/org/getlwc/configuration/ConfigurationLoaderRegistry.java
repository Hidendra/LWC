package org.getlwc.configuration;

import javax.inject.Provider;
import java.io.File;
import java.io.InputStream;

/**
 * A loader for configuration files. It permits multiple implementations
 * of different configuration types to coexist. This is done by either
 * being explicitly given the type or inferring the type from a file,
 * e.g. the file extension.
 *
 * If a file's configuration type cannot be inferred, it should use the
 * binding with type "default".
 */
public interface ConfigurationLoaderRegistry {

    /**
     * The key used for the default filetype bind
     */
    public static final String DEFAULT_KEY = "_default";

    /**
     * Loads a config from a file. The implementation it is mapped to is inferred
     * from the file type or default-assigned impl.
     *
     * @param file
     * @return
     * @throws org.getlwc.configuration.UnknownConfigurationTypeException Thrown if the implementation type cannot be inferred from the file and there is no default loader.
     */
    public Configuration load(File file);

    /**
     * Loads a config from an input stream. It will be read-only and cannot be saved.
     * The type is given.
     *
     * @param type
     * @param stream
     * @return
     * @throws org.getlwc.configuration.UnknownConfigurationTypeException Thrown if the implementation type given is unknown and there is no default loader.
     */
    public Configuration load(String type, InputStream stream);

    /**
     * Binds the provider to the given configuration type.
     *
     * @param type
     * @param loader
     */
    public void bind(String type, ConfigurationLoader loader);

}
