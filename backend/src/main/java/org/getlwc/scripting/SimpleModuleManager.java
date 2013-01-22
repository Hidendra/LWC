/*
 * Copyright (c) 2011-2013 Tyler Blair
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

package org.getlwc.scripting;

import org.getlwc.Engine;
import org.getlwc.configuration.Configuration;
import org.getlwc.configuration.YamlConfiguration;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class SimpleModuleManager implements ModuleManager {

    /**
     * The directory that modules are stored in
     */
    private static final String MODULE_DIR = "modules";

    /**
     * The engine object
     */
    private final Engine engine;

    /**
     * A list of the modules
     */
    private final Map<String, Module> modules = new HashMap<String, Module>();

    public SimpleModuleManager(Engine engine) {
        this.engine = engine;
    }

    /**
     * {@inheritDoc}
     */
    public void loadAll() {
        //
        File moduleDir = new File(engine.getServerLayer().getEngineHomeFolder(), MODULE_DIR);

        if (!moduleDir.exists()) {
            moduleDir.mkdir();
        }

        File[] files = moduleDir.listFiles(new FilenameFilter() {
            public boolean accept(File file, String name) {
                return name.endsWith(".jar");
            }
        });

        for (File file : files) {
            //
            try {
                ZipFile zip = new ZipFile(file);
                ZipEntry entry = zip.getEntry("module.yml");

                if (entry == null) {
                    throw new ModuleException("module.yml does not exist in file: " + file.getPath());
                }

                Configuration moduleConfig = new YamlConfiguration(zip.getInputStream(entry));
                String name = moduleConfig.getString("name", null);
                String description = moduleConfig.getString("description", null);
                String main = moduleConfig.getString("main", null);

                if (name == null) {
                    throw new ModuleException("Name cannot be null in file: " + file.getPath());
                }

                if (description == null) {
                    throw new ModuleException("Description cannot be null in file: " + file.getPath());
                }

                if (main == null) {
                    throw new ModuleException("Main class cannot be null in file: " + file.getPath());
                }

                ClassLoader loader = new URLClassLoader(new URL[] { file.toURI().toURL() }, getClass().getClassLoader());
                JavaModule module = (JavaModule) loader.loadClass(main).newInstance();
                module.setName(name);
                module.setDescription(description);

                enable(module);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ModuleException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void enable(Module module) {
        modules.put(module.getName(), module);
        module.initialize(engine);
        engine.getConsoleSender().sendMessage("Module.initialize() => " + module.getName());
    }

    /**
     * {@inheritDoc}
     */
    public void disable(Module module) {
        modules.remove(module.getName());
        module.terminate();
    }

    /**
     * {@inheritDoc}
     */
    public void reload() {
        // TODO replace reload() in this class with an event ?
        for (Module module : modules.values()) {
            module.reload();
        }
    }

}
