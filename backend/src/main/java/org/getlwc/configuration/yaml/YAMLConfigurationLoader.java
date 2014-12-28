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
package org.getlwc.configuration.yaml;

import com.google.inject.Inject;
import org.getlwc.configuration.Configuration;
import org.getlwc.configuration.ConfigurationLoader;
import org.getlwc.util.resource.ResourceDownloader;
import org.yaml.snakeyaml.Yaml;

import javax.inject.Singleton;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class YAMLConfigurationLoader implements ConfigurationLoader {

    private ResourceDownloader resourceDownloader;

    @Inject
    public YAMLConfigurationLoader(ResourceDownloader resourceDownloader) {
        this.resourceDownloader = resourceDownloader;
    }

    @Override
    public Configuration load(File file) {
        resourceDownloader.ensureResourceInstalled("snakeyaml");

        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                return new YAMLConfiguration((Map<String, Object>) new Yaml().load(reader), file);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            try {
                file.createNewFile();
                Configuration config = new YAMLConfiguration(new HashMap<String, Object>(), file);
                config.save();
                return config;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    @Override
    public Configuration load(InputStream stream) {
        resourceDownloader.ensureResourceInstalled("snakeyaml");

        try (Reader reader = new InputStreamReader(stream)) {
            return new YAMLConfiguration((Map<String, Object>) new Yaml().load(reader));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
