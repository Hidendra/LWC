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
import java.util.Enumeration;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Updater {

    public enum UpdateMethod {

        /**
         * Updating will be performed automatically by LWC
         */
        AUTOMATIC,

        /**
         * Updating will be performed manually by the admin
         */
        MANUAL;

        /**
         * Match an input to an update method
         *
         * @param input
         * @return
         */
        public static UpdateMethod match(String input) {
            for (UpdateMethod method : values()) {
                if (method.toString().equalsIgnoreCase(input)) {
                    return method;
                }
            }

            return null;
        }

    }

    public enum UpdateBranch {

        /**
         * Long release schedules and is recommended on any server that wishes to have stable LWC features
         * but does not necessarily want the very latest LWC has to offer in features, bug fixes, and
         * performance.
         */
        STABLE("stable"),

        /**
         * The most volatile branch. Should not be used on production servers but offers the most up to date features
         * but testing may not have been performed all around. Performance may also not have been rounded out and
         * any features may be removed at any time. Versioning for this branch is based off of the latest build number.
         */
        BLEEDING_EDGE("bleeding");

        /**
         * The branch name to use on the update site
         */
        private String branch;

        UpdateBranch(String branch) {
            this.branch = branch;
        }

        /**
         * @return
         */
        public String getBranch() {
            return branch;
        }

        /**
         * Match an input to a branch
         *
         * @param input
         * @return
         */
        public static UpdateBranch match(String input) {
            for (UpdateBranch branch : values()) {
                if (branch.toString().equalsIgnoreCase(input)) {
                    return branch;
                }
            }

            return null;
        }

    }

    /**
     * URL to the base update site
     */
    public final static String UPDATE_SITE = "http://update.griefcraft.com";

    /**
     * Location of the plugin on the website
     */
    public final static String PLUGIN_LOCATION = "/lwc/";

    /**
     * URL to the Jenkins job for LWC
     */
    public final static String JENKINS = "http://ci.griefcraft.com/job/LWC/";

    /**
     * The folder where libraries are stored
     */
    public final static String DEST_LIBRARY_FOLDER = "plugins/LWC/lib/";

    /**
     * The queue of files that need to be downloaded
     */
    private final Queue<UpdaterFile> fileQueue = new ConcurrentLinkedQueue<UpdaterFile>();

    /**
     * The update branch to use
     */
    private UpdateBranch updateBranch;

    /**
     * The update method to use
     */
    private UpdateMethod updateMethod;

    /**
     * The latest plugin version
     */
    private Version latestVersion;

    public void init() {
        final LWC lwc = LWC.getInstance();
        updateBranch = UpdateBranch.match(lwc.getConfiguration().getString("updater.branch", "STABLE"));
        updateMethod = UpdateMethod.match(lwc.getConfiguration().getString("updater.method", "MANUAL"));

        if (updateMethod == UpdateMethod.AUTOMATIC) {
            this.loadVersions(true, new Runnable() {

                public void run() {
                    tryAutoUpdate(false);

                    if (updateAvailable()) {
                        lwc.log("Update available! Latest version: " + latestVersion);
                    }
                }

            });
        }

        // verify we have local files (e.g sqlite.jar, etc)
        this.verifyFiles();
        this.downloadFiles();
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
                    int read = 0;

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
     * Check if an update is available or not. Assumes the latest version has already been grabbed
     *
     * @return
     */
    public boolean updateAvailable() {
        if (latestVersion == null) {
            return false;
        }

        Version current = LWCInfo.FULL_VERSION;
        return !(current.equals(latestVersion) || (current.getBuildNumber() > 0 && latestVersion.getBuildNumber() > 0 && current.getBuildNumber() == latestVersion.getBuildNumber()));
    }

    /**
     * Try and automatically update, if automatic updating is enabled.
     *
     * @param forceUpdate
     * @return true if LWC was updated
     */
    public boolean tryAutoUpdate(boolean forceUpdate) {
        if (!forceUpdate) {
            // do we qualify for an automatic update?
            if (!shouldAutoUpdate()) {
                return false;
            }
        }

        // we shouldn't update if the current version is the same as the latest, or their build numbers are equal
        if (!updateAvailable()) {
            return false;
        }

        LWC.getInstance().log(Colors.Red + "LWC update found!");

        // make sure all class files are loaded in our jar file before we overwrite it
        loadAllClasses();

        // update_site/version/LWC.jar
        UpdaterFile file = new UpdaterFile("plugins/LWC.jar", getLatestDownloadURL());

        // queue it
        fileQueue.offer(file);

        // immediately download
        downloadFiles();
        return true;
    }

    /**
     * Load all the classes in the current jar file so it can be overwritten
     */
    private void loadAllClasses() {
        LWC lwc = LWC.getInstance();
        
        try {
            // Load the jar
            JarFile jar = new JarFile(lwc.getPlugin().getFile());

            // Walk through all of the entries
            Enumeration<JarEntry> enumeration = jar.entries();

            while (enumeration.hasMoreElements()) {
                JarEntry entry = enumeration.nextElement();
                String name = entry.getName();
                
                // is it a class file?
                if (name.endsWith(".class")) {
                    // convert to package
                    String path = name.replaceAll("/", ".");
                    path = path.substring(0, path.length() - ".class".length());

                    // Load it
                    this.getClass().getClassLoader().loadClass(path);
                }
            }
        } catch (IOException e) {
            exceptionCaught(e);
        } catch (ClassNotFoundException e) {
            exceptionCaught(e);
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
     * Get the latest download URL. Could vary depending upon which update branch is used
     *
     * @return
     */
    public String getLatestDownloadURL() {
        if (updateBranch == null) {
            return "";
        }

        switch (updateBranch) {
            case BLEEDING_EDGE:
                return JENKINS + latestVersion.getBuildNumber() + "/artifact/build/LWC.jar";

            default:
                return UPDATE_SITE + PLUGIN_LOCATION + "branch/" + updateBranch.getBranch() + "/" + latestVersion.getRawVersion() + "/LWC.jar";
        }
    }

    /**
     * Check if we should try to automatically update
     *
     * @return
     */
    public boolean shouldAutoUpdate() {
        if (updateBranch == null) {
            return false;
        }

        if (updateMethod != UpdateMethod.AUTOMATIC) {
            return false;
        }

        Version current = LWCInfo.FULL_VERSION;

        switch (updateBranch) {
            // We only want to compare the build number for bleeding edge
            case BLEEDING_EDGE:
                return latestVersion.getBuildNumber() > current.getBuildNumber() && (latestVersion.getBuildNumber() > 0 && current.getBuildNumber() > 0);

            // For everything else, we are fine with the default routine
            default:
                return latestVersion.newerThan(current);
        }
    }

    /**
     * Load the latest version
     *
     * @param background
     */
    public void loadVersions(boolean background) {
        this.loadVersions(background, null);
    }

    /**
     * Load the latest version
     *
     * @param background if true, will be run in the background
     * @param callback   The callback will be ran after the version is loaded
     */
    public void loadVersions(boolean background, final Runnable callback) {
        class Background_Check_Thread implements Runnable {
            public void run() {

                switch (updateBranch) {
                    /**
                     * The bleeding edge branch instead reads the latest build number from Jenkins and relies
                     * upon that. The nature of bleeding edge is to stay up to the latest build.
                     */
                    case BLEEDING_EDGE:
                        try {
                            URL url = new URL(JENKINS + "lastSuccessfulBuild/buildNumber");
                            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                            latestVersion = new Version("b" + reader.readLine());
                            reader.close();
                        } catch (MalformedURLException e) {
                            exceptionCaught(e);
                        } catch (IOException e) {
                            exceptionCaught(e);
                        }
                        break;


                    default:
                        // by default, use the LATEST file
                        try {
                            URL url = new URL(UPDATE_SITE + PLUGIN_LOCATION + "branch/" + updateBranch.getBranch() + "/LATEST");
                            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

                            // read in the first line
                            String line = reader.readLine();
                            reader.close();

                            // parse it and we are done
                            latestVersion = new Version(line);
                        } catch (MalformedURLException e) {
                            exceptionCaught(e);
                        } catch (IOException e) {
                            exceptionCaught(e);
                        }
                        break;
                }

                // run the callback
                if (callback != null) {
                    callback.run();
                }

            }
        }

        Background_Check_Thread worker = new Background_Check_Thread();

        if (background) {
            new Thread(worker).start();
        } else {
            worker.run();
        }
    }

    /**
     * @return the full path to the native library for sqlite
     */
    public String getFullNativeLibraryPath() {
        return getOSSpecificFolder() + getOSSpecificFileName();
    }

    /**
     * @return the latest plugin version
     */
    public Version getLatestVersion() {
        return latestVersion;
    }

    /**
     * @return the update branch that is being used
     */
    public UpdateBranch getUpdateBranch() {
        return updateBranch;
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
