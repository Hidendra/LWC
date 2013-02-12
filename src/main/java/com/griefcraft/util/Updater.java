/*
 * Copyright 2011 Tyler Blair. All rights reserved.
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

package com.griefcraft.util;

import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCInfo;
import com.griefcraft.sql.Database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Updater {

    /**
     * URL to the base update site
     */
    public final static String UPDATE_SITE = "http://update.griefcraft.com";

    /**
     * Location of the plugin on the website
     */
    public final static String PLUGIN_LOCATION = "/lwc/";

    /**
     * The folder where libraries are stored
     */
    public final static String DEST_LIBRARY_FOLDER = "plugins/LWC/lib/";

    /**
     * The queue of files that need to be downloaded
     */
    private final Queue<UpdaterFile> fileQueue = new ConcurrentLinkedQueue<UpdaterFile>();

    public void init() {
        // verify we have local files (e.g sqlite.jar, etc)
        verifyFiles();
        downloadFiles();

        final LWC lwc = LWC.getInstance();
        if (lwc.getConfiguration().getBoolean("core.updateNotifier", true)) {
            lwc.getPlugin().getServer().getScheduler().scheduleAsyncDelayedTask(lwc.getPlugin(), new Runnable() {
                public void run() {
                    Version latest = getLatestVersion();

                    if (latest.newerThan(LWCInfo.FULL_VERSION)) {
                        lwc.log("An update is available. You are on version \"" + LWCInfo.FULL_VERSION.toString() + "\". The latest version is: \"" + latest.toString() + "\"");
                        lwc.log("LWC updates can be found on the Bukkit Dev page at: http://dev.bukkit.org/server-mods/lwc/");
                    }
                }
            });
        }
    }

    /**
     * Verify all required files exist
     */
    private void verifyFiles() {
        // SQLite libraries
        if (Database.DefaultType == Database.Type.SQLite) {
            // sqlite.jar
            this.verifyFile(new UpdaterFile(DEST_LIBRARY_FOLDER + "sqlite.jar", UPDATE_SITE + "/shared/lib/sqlite.jar"));

            // Native library
            this.verifyFile(new UpdaterFile(getFullNativeLibraryPath(), UPDATE_SITE + "/shared/lib/" + getFullNativeLibraryPath().replaceAll(DEST_LIBRARY_FOLDER, "")));
        }
    }

    /**
     * Verify a file and if it does not exist, download it
     *
     * @param updaterFile
     * @return true if the file was queued to be downloaded
     */
    private boolean verifyFile(UpdaterFile updaterFile) {
        if (updaterFile == null) {
            return false;
        }

        File file = new File(updaterFile.getLocalLocation());

        // Does it exist on the FS?
        if (file.exists()) {
            // So it does!
            return false;
        }

        // It does not exist ..
        fileQueue.offer(updaterFile);
        return true;
    }

    /**
     * @return the full path to the native library for sqlite
     */
    public String getFullNativeLibraryPath() {
        return getOSSpecificFolder() + getOSSpecificFileName();
    }

    /**
     * @return the os/arch specific file name for sqlite's native library
     */
    public String getOSSpecificFileName() {
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
     * @return the os/arch specific folder location for SQLite's native library
     */
    public String getOSSpecificFolder() {
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
     * Load the latest version
     */
    public Version getLatestVersion() {
        // by default, use the LATEST file
        try {
            URL url = new URL(UPDATE_SITE + PLUGIN_LOCATION + "branch/stable/LATEST");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

            // read in the first line
            String line = reader.readLine();
            reader.close();

            // parse it and we are done
            return new Version(line);
        } catch (MalformedURLException e) {
            exceptionCaught(e);
        } catch (IOException e) {
            exceptionCaught(e);
        }

        return null;
    }

    /**
     * Download all the files in the queue
     */
    public void downloadFiles() {
        synchronized (fileQueue) {
            UpdaterFile updaterFile = null;
            LWC lwc = LWC.getInstance();

            while ((updaterFile = fileQueue.poll()) != null) {
                try {
                    File local = new File(updaterFile.getLocalLocation());
                    String remote = updaterFile.getRemoteLocation();

                    lwc.log("Downloading file " + local.getName());

                    // check for LWC folder
                    File folder = new File("plugins/LWC/");
                    if (!folder.exists()) {
                        folder.mkdir();
                    }

                    // check native folders
                    folder = new File(getOSSpecificFolder());
                    if (!folder.exists()) {
                        folder.mkdirs();
                    }

                    if (local.exists()) {
                        local.delete();
                    }

                    // create the local file
                    local.createNewFile();

                    // open the file
                    OutputStream outputStream = new FileOutputStream(local);

                    // Connect to the server
                    URL url = new URL(remote);
                    URLConnection connection = url.openConnection();

                    InputStream inputStream = connection.getInputStream();

                    // hopefully, the content length provided isn't -1
                    int contentLength = connection.getContentLength();

                    // Keep a running tally
                    int bytesTransffered = 0;
                    long lastUpdate = 0L;

                    // begin transferring
                    byte[] buffer = new byte[1024];
                    int read;

                    while ((read = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, read);
                        bytesTransffered += read;

                        if (contentLength > 0) {
                            if (System.currentTimeMillis() - lastUpdate > 500L) {
                                int percentTransferred = (int) (((float) bytesTransffered / contentLength) * 100);
                                lastUpdate = System.currentTimeMillis();

                                // omit 100% ..
                                if (percentTransferred != 100) {
                                    lwc.log(percentTransferred + "%");
                                }
                            }
                        }
                    }

                    // ok!
                    outputStream.close();
                    inputStream.close();
                } catch (IOException e) {
                    exceptionCaught(e);
                }
            }
        }
    }

    /**
     * Called when an exception is caught
     *
     * @param e
     */
    private void exceptionCaught(Exception e) {
        LWC lwc = LWC.getInstance();
        lwc.log("[LWC] The updater ran into a minor issue: " + e.getMessage());
        lwc.log("[LWC] This can probably be ignored.");
    }

}
