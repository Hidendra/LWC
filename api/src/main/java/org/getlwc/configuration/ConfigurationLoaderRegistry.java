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
