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
package org.getlwc.util.resource;

import org.getlwc.Engine;
import org.getlwc.util.ClassUtils;
import org.getlwc.util.MD5Checksum;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class SimpleResourceDownloader implements ResourceDownloader {

    /**
     * The maximum number of times a download will be tried (e.g. connection was closed, checksum failed) before it is given up
     */
    public static final int MAX_DOWNLOAD_TRIES = 5;

    /**
     * The Engine instance
     */
    private Engine engine;

    /**
     * The base url to download resources from
     */
    private String baseUrl = null;

    /**
     * A map of all available resources
     */
    private final Map<String, Resource> resources = new HashMap<>();

    @Inject
    public SimpleResourceDownloader(Engine engine) {
        this.engine = engine;
    }

    /**
     * Loads resources
     */
    public boolean loadResources() {
        JSONObject root = (JSONObject) JSONValue.parse(new InputStreamReader(getClass().getResourceAsStream("/resources.json")));

        if (root == null) {
            return false;
        }

        this.baseUrl = root.get("url").toString();

        Map<?, ?> resources = (Map<?, ?>) root.get("resources");

        for (Map.Entry<?, ?> entry : resources.entrySet()) {
            String resourceKey = entry.getKey().toString();
            Map<?, ?> resourceData = (Map<?, ?>) entry.getValue();

            Resource resource = new Resource(resourceKey);

            if (resourceData.containsKey("class")) {
                resource.setTestClass(resourceData.get("class").toString());
            }

            if (resourceData.containsKey("outputDir")) {
                resource.setOutputDir(resourceData.get("outputDir").toString());
            }

            if (resourceData.containsKey("requires")) {
                for (Object dependency : (JSONArray) resourceData.get("requires")) {
                    resource.addDependency(dependency.toString());
                }
            }

            if (resourceData.containsKey("files")) {
                for (Object fileObject : (JSONArray) resourceData.get("files")) {
                    resource.addFile(fileObject.toString());
                }
            }

            addResource(resource);
        }

        return true;
    }

    @Override
    public void ensureResourceInstalled(String resourceKey) {
        Resource resource = getResource(resourceKey);

        // ensure dependencies are installed first
        for (String dependency : resource.getDependencies()) {
            ensureResourceInstalled(dependency);
        }

        // Check to see if the class is already loaded (don't need to download if it is)
        if (resource.getTestClass() != null) {
            if (ClassUtils.isClassLoaded(resource.getTestClass())) {
                return;
            }
        }

        for (String file : resource.getFiles()) {
            String localResourceDataFolder = engine.getServerLayer().getDataPathTo(resource.getOutputDir());

            downloadFile(new ResourceFile(String.format("%s/%s", localResourceDataFolder, file), String.format("%s/%s/%s", baseUrl, resource.getOutputDir(), file)));
        }
    }

    @Override
    public void addResource(Resource resource) {
        if (!resources.containsKey(resource.getKey())) {
            // injects the SQLite native library for this platform
            // TODO do this a different way?
            if (resource.getKey().equals("databases.sqlite")) {
                resource.addFile(getFullNativeLibraryPath());
            }

            resources.put(resource.getKey(), resource);
            engine.getConsoleSender().sendMessage("Added resource {0}", resource.toString());
        }
    }

    @Override
    public Resource getResource(String key) {
        return resources.get(key);
    }

    /**
     * @return the full path to the native library for sqlite
     */
    public String getFullNativeLibraryPath() {
        return getNativeLibraryFolder() + getNativeLibraryFileName();
    }

    /**
     * @return the os/arch specific folder location for SQLite's native library
     */
    public String getNativeLibraryFolder() {
        String osname = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();

        if (osname.contains("windows")) {
            return "native/Windows/" + arch + "/";
        } else if (osname.contains("mac")) {
            return "native/Mac/" + arch + "/";
        } else { /* We assume linux/unix */
            return "native/Linux/" + arch + "/";
        }
    }

    /**
     * @return the os/arch specific file name for sqlite's native library
     */
    public String getNativeLibraryFileName() {
        String osname = System.getProperty("os.name").toLowerCase();

        if (osname.contains("windows")) {
            return "sqlitejdbc.dll";
        } else if (osname.contains("mac")) {
            return "libsqlitejdbc.jnilib";
        } else { /* We assume linux/unix */
            return "libsqlitejdbc.so";
        }
    }

    /**
     * Ensure the given file is in the class path. It must be a jar file.
     *
     * @param file
     */
    private void ensureLoaded(File file) {
        ClassLoader classLoader = getClass().getClassLoader();
        ensureLoaded(file, classLoader.getClass(), classLoader);
    }

    /**
     * Ensure the given file is in the class path. It must be the jar file.
     *
     * @param file
     * @param clazz
     */
    private void ensureLoaded(File file, Class<?> clazz, ClassLoader classLoader) {
        try {
            Method method = clazz.getDeclaredMethod("addURL", new Class[]{URL.class});
            method.setAccessible(true);
            method.invoke(classLoader, new Object[]{file.toURI().toURL()});
        } catch (Exception e) {
            // cascade up the ClassLoader tree
            if (clazz.getSuperclass() != null) {
                ensureLoaded(file, clazz.getSuperclass(), classLoader);
            } else {
                e.printStackTrace();
            }
        }
    }

    /**
     * Download all the files in the queue
     */
    public void downloadFile(ResourceFile resourceFile) {
        try {
            File local = new File(resourceFile.getLocalLocation());
            String remote = resourceFile.getRemoteLocation();

            if (!local.isDirectory()) {
                File directory = local.getParentFile();
                directory.mkdirs();
            }

            int tries = 1;

            if (local.exists()) {
                engine.getConsoleSender().sendMessage("Verifying file {0}", local.toString());
            } else {
                engine.getConsoleSender().sendMessage("Downloading file {0} to {1}", local.getName(), local.getParent());
            }

            URL fileURL = new URL(remote);
            URL md5URL = new URL(remote + ".md5");

            do {
                if (!local.exists()) {
                    downloadFully(fileURL, local);
                }

                String expectedMD5 = readURLFully(md5URL);

                if (expectedMD5 == null) {
                    engine.getConsoleSender().sendMessage("{0}: no checksum available from remote host", local.getName());
                    break;
                } else {
                    String realMD5 = MD5Checksum.calculateHumanChecksum(local);

                    if (expectedMD5.equals(realMD5)) {
                        engine.getConsoleSender().sendMessage("{0}: checksum is OK", local.getName());
                        break;
                    } else {
                        engine.getConsoleSender().sendMessage("{0}: checksum check FAILED. Found {1}, but was expecting {2}. File will be redownloaded.", local.getName(), realMD5, expectedMD5);
                        local.delete();
                    }
                }
            } while (tries++ <= MAX_DOWNLOAD_TRIES);

            if (local.getName().endsWith(".jar")) {
                ensureLoaded(local);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Downloads a file into the given {@link OutputStream}. The {@link OutputStream} will not be closed automatically.
     *
     * @param url
     * @param downloadTo
     */
    private void downloadFully(URL url, File downloadTo) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(downloadTo)) {
            URLConnection connection = url.openConnection();

            try (InputStream inputStream = connection.getInputStream()) {
                int contentLength = connection.getContentLength();

                int bytesTransfered = 0;
                long lastUpdate = 0L;

                byte[] buffer = new byte[1024];
                int read;

                while ((read = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, read);
                    bytesTransfered += read;

                    if (contentLength > 0) {
                        if (System.currentTimeMillis() - lastUpdate > 500L) {
                            int percentTransferred = (int) (((float) bytesTransfered / contentLength) * 100);
                            lastUpdate = System.currentTimeMillis();

                            // omit 0/100% ..
                            if (percentTransferred != 0 && percentTransferred != 100) {
                                engine.getConsoleSender().sendMessage("  >> {0}: {1}%", downloadTo.getName(), percentTransferred);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Attempts to read the full contents of the page at the given url. If the url returns an error (does not exist, etc)
     * null will be returned
     *
     * @param url
     * @return
     */
    private String readURLFully(URL url) {
        try {
            URLConnection connection = url.openConnection();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String response = "";
                String line;

                while ((line = reader.readLine()) != null) {
                    response += line;
                }

                return response;
            }
        } catch (IOException e) {
            return null;
        }
    }

}
