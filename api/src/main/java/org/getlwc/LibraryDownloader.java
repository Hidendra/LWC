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

package org.getlwc;

import org.getlwc.configuration.Configuration;
import org.getlwc.configuration.YamlConfiguration;
import org.getlwc.util.LibraryFile;
import org.getlwc.util.MD5Checksum;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LibraryDownloader {

    /**
     * URL to the base update site
     */
    public final static String DEFAULT_UPDATE_SITE = "http://update.getlwc.org/shared/lib/";

    /**
     * The folder where libraries are stored
     */
    public static String DEST_LIBRARY_FOLDER = "";

    /**
     * The maximum number of times a download will be tried (e.g. connection was closed, checksum failed) before it is given up
     */
    public static final int MAX_DOWNLOAD_TRIES = 5;

    /**
     * The Engine instance
     */
    private Engine engine;

    /**
     * The queue of files that need to be downloaded
     */
    private final Queue<LibraryFile> fileQueue = new ConcurrentLinkedQueue<LibraryFile>();

    /**
     * The configuration file for resources.yml
     */
    private Configuration resourceConfiguration = null;

    public LibraryDownloader(Engine engine) {
        this.engine = engine;
    }

    /**
     * Initialize the downloader. This is to be called after a {@link Engine} has been initialized
     */
    protected void init() {
        DEST_LIBRARY_FOLDER = new File(engine.getServerLayer().getEngineHomeFolder(), "lib").getPath() + File.separator;
        DEST_LIBRARY_FOLDER = DEST_LIBRARY_FOLDER.replaceAll("\\\\", "/");

        // resource libraries
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/resourcelibs.txt")));
            String file;

            while ((file = reader.readLine()) != null) {
                file = file.trim();

                if (file.startsWith("#") || file.isEmpty()) {
                    continue;
                }

                String[] split = file.split(":");
                String fileName = split[0];

                if (split.length > 1) {
                    String testClass = split[1];

                    if (isClassLoaded(testClass)) {
                        continue;
                    }
                }

                verifyFile(new LibraryFile(DEST_LIBRARY_FOLDER + fileName, DEFAULT_UPDATE_SITE + fileName));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        downloadFiles();
    }

    /**
     * Check if a class is loaded
     *
     * @param className
     * @return
     */
    public boolean isClassLoaded(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (Exception e) {
            // Remove the class from Mojang's LaunchClassLoader cache
            try {
                Field invalidClasses = getClass().getClassLoader().getClass().getDeclaredField("invalidClasses");

                if (invalidClasses != null) {
                    invalidClasses.setAccessible(true);

                    Set<String> set = (Set<String>) invalidClasses.get(getClass().getClassLoader());

                    if (set != null) {
                        set.remove(className);
                    }
                }
            } catch (Exception ex) {
                // This is ok. It just means we are not being loaded by Mojang's LaunchClassLoader
            }

            return false;
        }
    }

    /**
     * Ensure the specified resource is installed and loaded. If it is not installed it will be downloaded.
     *
     * @param resource
     */
    public void ensureResourceInstalled(String resource) {
        if (resourceConfiguration == null) {
            resourceConfiguration = new YamlConfiguration(getClass().getResourceAsStream("/resources.yml"));
        }

        // base url for the update site
        String baseurl = resourceConfiguration.getString("baseUrl", DEFAULT_UPDATE_SITE);

        Map<Object, Object> res = (Map<Object, Object>) resourceConfiguration.get("resources." + resource);

        if (res == null) {
            return;
        }

        String testClass = (String) res.get("testClass");

        if (testClass == null) {
            return;
        }

        // Check to see if the class is already loaded (not needed if so)
        if (isClassLoaded(testClass)) {
            return;
        }

        List<Object> files = (List<Object>) res.get("files");

        for (Object o : files) {
            String file = (String) o;
            verifyFile(new LibraryFile(DEST_LIBRARY_FOLDER + file, baseurl + file));
        }

        // SQLite native library
        if (resource.equals("databases.sqlite")) {
            verifyFile(new LibraryFile(getFullNativeLibraryPath(), baseurl + getFullNativeLibraryPath().replaceAll(DEST_LIBRARY_FOLDER, "")));
        }

        downloadFiles();
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
            return DEST_LIBRARY_FOLDER + "native/Windows/" + arch + "/";
        } else if (osname.contains("mac")) {
            return DEST_LIBRARY_FOLDER + "native/Mac/" + arch + "/";
        } else { /* We assume linux/unix */
            return DEST_LIBRARY_FOLDER + "native/Linux/" + arch + "/";
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
     * Verify a file and if it does not exist, download it
     *
     * @param updaterFile
     * @return true if the file was queued to be downloaded
     */
    private boolean verifyFile(LibraryFile updaterFile) {
        if (updaterFile == null) {
            return false;
        }

        File file = new File(updaterFile.getLocalLocation());

        // Does it exist on the FS?
        if (file.exists()) {
            if (file.getName().endsWith(".jar")) {
                ensureLoaded(file);
            }

            // So it does!
            return false;
        }

        // It does not exist ..
        fileQueue.offer(updaterFile);
        return true;
    }

    /**
     * Ensure the given file is in the class path. It must be a jar file.
     *
     * @param file
     */
    private void ensureLoaded(File file) {
        ClassLoader classLoader = getClass().getClassLoader();
        ensureLoaded(file, classLoader.getClass(),classLoader);
    }

    /**
     * Ensure the given file is in the class path. It must be the jar file.
     *
     * @param file
     * @param clazz
     */
    private void ensureLoaded(File file, Class<?> clazz, ClassLoader classLoader) {
        try {
            Method method = clazz.getDeclaredMethod("addURL", new Class[] { URL.class });
            method.setAccessible(true);
            method.invoke(classLoader, new Object[] { file.toURI().toURL() });
        } catch (Exception e) {
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
    public void downloadFiles() {
        synchronized (fileQueue) {
            LibraryFile libraryFile;
            int size = fileQueue.size();

            if (size == 0) {
                return;
            }

            int current = 1;
            while ((libraryFile = fileQueue.poll()) != null) {
                try {
                    File local = new File(libraryFile.getLocalLocation());
                    String remote = libraryFile.getRemoteLocation();

                    engine.getConsoleSender().sendTranslatedMessage("[{0}/{1}] Downloading file {2} => {3}", current, size, local.getName(), local.getParent());

                    // check native folders
                    File folder = new File(getNativeLibraryFolder());
                    if (!folder.exists()) {
                        folder.mkdirs();
                    }

                    if (local.exists()) {
                        local.delete();
                    }

                    // create the local file
                    local.createNewFile();

                    URL url = new URL(remote);
                    URL md5Url = new URL(remote + ".md5");

                    int tries = 1;

                    do {
                        downloadLibrary(url, local);

                        // Try md5 if it is available
                        String md5 = readURLFully(md5Url);

                        if (md5 == null) {
                            engine.getConsoleSender().sendTranslatedMessage("  .. md5 {0} WARN: no checksum available from remote host", local.getName());
                            break;
                        } else {
                            String realMd5 = MD5Checksum.calculateHumanChecksum(local);

                            if (md5.equals(realMd5)) {
                                engine.getConsoleSender().sendTranslatedMessage("  .. md5 {0} OK", local.getName());
                                break;
                            } else {
                                engine.getConsoleSender().sendTranslatedMessage("  .. md5 {0} FAIL {1} (expected: {2})", local.getName(), md5, realMd5);
                                engine.getConsoleSender().sendTranslatedMessage("Will redownload {0} ({1}/{2} tries)", local.getName(), tries, MAX_DOWNLOAD_TRIES);
                            }
                        }
                    } while (tries++ <= MAX_DOWNLOAD_TRIES);

                    if (local.getName().endsWith(".jar")) {
                        ensureLoaded(local);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                current ++;
            }
        }
    }

    /**
     * Downloads a file into the given {@link OutputStream}. The {@link OutputStream} will not be closed automatically.
     *
     * @param url
     * @param downloadTo
     */
    private void downloadLibrary(URL url, File downloadTo) throws IOException {
        // open the output
        OutputStream outputStream = new FileOutputStream(downloadTo);

        URLConnection connection = url.openConnection();

        InputStream inputStream = connection.getInputStream();

        // hopefully, the content length provided isn't -1
        int contentLength = connection.getContentLength();

        // Keep a running tally
        int bytesTransfered = 0;
        long lastUpdate = 0L;

        // begin transferring
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
                        engine.getConsoleSender().sendTranslatedMessage("  >> {0}: {1}%", downloadTo.getName(), percentTransferred);
                    }
                }
            }
        }

        // ok!
        outputStream.close();
        inputStream.close();
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

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = "";
            String line;

            while ((line = reader.readLine()) != null) {
                response += line;
            }

            reader.close();

            return response;
        } catch (IOException e) {
            return null;
        }
    }

}
