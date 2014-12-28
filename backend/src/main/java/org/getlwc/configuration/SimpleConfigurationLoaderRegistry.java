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
package org.getlwc.configuration;

import org.getlwc.command.ConsoleCommandSender;

import javax.inject.Inject;
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

    private ConsoleCommandSender logger;

    @Inject
    public SimpleConfigurationLoaderRegistry(ConsoleCommandSender logger) {
        this.logger = logger;
    }

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
        logger.sendMessage("{0}::bind {1} -> {2}", getClass().getSimpleName(), type, loader.getClass().getCanonicalName());

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
