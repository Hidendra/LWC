package org.getlwc.configuration;

import javax.inject.Singleton;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class SimpleConfigurationLoaderRegistry implements ConfigurationLoaderRegistry {

    /**
     * All binded configuration types
     */
    private final Map<String, ConfigurationLoader> loaders = new HashMap<>();

    @Override
    public Configuration load(File file) {
        String type = getFileExtension(file);

        if (!loaders.containsKey(type)) {
            type = DEFAULT_KEY;
        }

        ConfigurationLoader loader = loaders.get(type);

        if (loader == null) {
            throw new UnknownConfigurationTypeException(type);
        }

        return loader.load(file);
    }

    @Override
    public Configuration load(String type, InputStream stream) {
        if (!loaders.containsKey(type)) {
            type = DEFAULT_KEY;
        }

        ConfigurationLoader loader = loaders.get(type);

        if (loader == null) {
            throw new UnknownConfigurationTypeException(type);
        }

        return loader.load(stream);
    }

    @Override
    public void bind(String type, ConfigurationLoader loader) {
        loaders.put(type, loader);
    }

    /**
     * Gets the extension of a file
     * TODO move
     *
     * @param file
     * @return
     */
    private String getFileExtension(File file) {
        String fileName = file.getName();

        int i = fileName.lastIndexOf('.');

        if (i >= 0) {
            return fileName.substring(i + 1);
        } else {
            return "";
        }
    }

}
