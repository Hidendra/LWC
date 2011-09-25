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
import com.griefcraft.scripting.ModuleLoader;
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
import java.util.logging.Logger;

public class Updater {

    public enum UpdateMethod {

        /**
         * Updating will be performed automatically by LWC
         */
        AUTOMATIC,

        /**
         * Updating will be performed manually by the admin
         */
        MANUAL

    }

    public enum UpdateScheme {

        /**
         * Long release schedules and is recommended on any server that wishes to have stable LWC features
         * but does not necessarily want the very latest LWC has to offer in features, bug fixes, and
         * performance.
         */
        STABLE("stable"),

        /**
         * The best middle-ground between getting the newest features and having a stable build. Builds received
         * from the CURRENT branch should never totally break LWC, but the possibility still exists. Builds in
         * this scheme will almost always display -alphaX or -betaX in /lwc admin version but may also display -rcX
         * for builds that are nearing a full release.
         */
        CURRENT("current"),

        /**
         * The most volatile scheme. Should not be used on production servers but offers the most up to date features
         * but testing may not have been performed all around. Performance may also not have been rounded out and
         * any features may be removed at any time. Versioning for this scheme is based off of the current and latest
         * build number.
         */
        BLEEDING_EDGE("bleeding");

        /**
         * The branch name to use on the update site
         */
        private String branch;

        UpdateScheme(String branch) {
            this.branch = branch;
        }

        /**
         * @return
         */
        public String getBranch() {
            return branch;
        }

    }

    /**
     * The logging object for this class
     */
    private final Logger logger = Logger.getLogger("LWC");

    /**
     * URL to the base update site
     */
    public final static String UPDATE_SITE = "http://griefcraft.com/lwc/";

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
     * The update scheme to use
     */
    private UpdateScheme updateScheme;

    /**
     * The update method to use
     */
    private UpdateMethod updateMethod;

    /**
     * The latest plugin version
     */
    private Version latestVersion;

    public void init() {
        // some vars that will probably be removed later, testing purposes at the moment
        LWC lwc = LWC.getInstance();
        updateScheme = UpdateScheme.valueOf(lwc.getConfiguration().getString("core.updateScheme", "BLEEDING_EDGE"));
        updateMethod = UpdateMethod.valueOf(lwc.getConfiguration().getString("core.updateMethod", "MANUAL"));
        logger.info("LWC: Update scheme: " + updateScheme);
        logger.info("LWC: Update method: " + updateMethod);

        this.loadVersions(true, new Runnable() {

            public void run() {
                tryAutoUpdate(false);
                logger.info("LWC: Latest version: " + latestVersion);
            }

        });

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

            while ((updaterFile = fileQueue.poll()) != null) {
                try {
                    File local = new File(updaterFile.getLocalLocation());
                    String remote = updaterFile.getRemoteLocation();

                    logger.info("LWC: Downloading file " + local.getName());

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
                                    logger.info(percentTransferred + "%");
                                }
                            }
                        }
                    }

                    // ok!
                    outputStream.close();
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
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
        Version current = LWCInfo.FULL_VERSION;
        if (current.equals(latestVersion) || (current.getBuildNumber() > 0 && latestVersion.getBuildNumber() > 0 && current.getBuildNumber() == latestVersion.getBuildNumber())) {
            return false;
        }

        logger.info(Colors.Red + "LWC update found!");

        // update_site/download/LWC.jar
        UpdaterFile file = new UpdaterFile("plugins/LWC.jar", getLatestDownloadURL());

        // queue it
        fileQueue.offer(file);

        // immediately download
        downloadFiles();
        return true;
    }

    /**
     * Verify all required files exist
     */
    private void verifyFiles() {
        // SQLite libraries
        if (Database.DefaultType == Database.Type.SQLite) {
            // sqlite.jar
            this.verifyFile(new UpdaterFile(DEST_LIBRARY_FOLDER + "sqlite.jar", "http://griefcraft.com/bukkit/shared/lib/sqlite.jar"));

            // Native library
            this.verifyFile(new UpdaterFile(getFullNativeLibraryPath(), "http://griefcraft.com/bukkit/shared/" + getFullNativeLibraryPath().replaceAll(DEST_LIBRARY_FOLDER, "")));
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

    public void downloadConfig(String config) {
        File file = new File(ModuleLoader.ROOT_PATH + config); // where to save to
        UpdaterFile updaterFile = new UpdaterFile(file.getPath(), "http://griefcraft.com/bukkit/lwc/skel/" + config);

        this.verifyFile(updaterFile);
        this.downloadFiles();
    }

    /**
     * Get the latest download URL. Could vary depending upon which update scheme is used
     *
     * @return
     */
    public String getLatestDownloadURL() {
        if (updateScheme == null) {
            return "";
        }

        switch(updateScheme) {
            case BLEEDING_EDGE:
                return JENKINS + latestVersion.getBuildNumber() + "/artifact/build/LWC.jar";

            default:
                return UPDATE_SITE + "branch/" + updateScheme.getBranch() + "/download/LWC.jar";
        }
    }

    /**
     * Check if we should try to automatically update
     *
     * @return
     */
    public boolean shouldAutoUpdate() {
        if (updateScheme == null) {
            return false;
        }

        if (updateMethod != UpdateMethod.AUTOMATIC) {
            return false;
        }

        Version current = LWCInfo.FULL_VERSION;

        switch (updateScheme) {
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

                switch (updateScheme) {
                    /**
                     * The bleeding edge scheme instead reads the latest build number from Jenkins and relies
                     * upon that. The nature of bleeding edge is to stay up to the latest build.
                     */
                    case BLEEDING_EDGE:
                        try {
                            URL url = new URL(JENKINS + "lastSuccessfulBuild/buildNumber");
                            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                            latestVersion = new Version("b" + reader.readLine());
                            reader.close();
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;


                    default:
                        // by default, use the LATEST file
                        try {
                            URL url = new URL(UPDATE_SITE + updateScheme.getBranch() + "/LATEST");
                            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

                            // read in the first line
                            String line = reader.readLine();
                            reader.close();

                            // parse it and we are done
                            latestVersion = new Version(line);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
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
     * @return the update scheme that is being used
     */
    public UpdateScheme getUpdateScheme() {
        return updateScheme;
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
            return DEST_LIBRARY_FOLDER + "lib/native/Windows/" + arch + "/";
        } else if (osname.contains("mac")) {
            return DEST_LIBRARY_FOLDER + "lib/native/Mac/" + arch + "/";
        } else { /* We assume linux/unix */
            return DEST_LIBRARY_FOLDER + "lib/native/Linux/" + arch + "/";
        }
    }

}
