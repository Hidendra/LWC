/**
 * This file is part of LWC (https://github.com/Hidendra/LWC)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.griefcraft.util;

import com.griefcraft.logging.Logger;
import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCInfo;
import com.griefcraft.scripting.ModuleLoader;
import com.griefcraft.sql.Database;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Updater {

    /**
     * The logging object for this class
     */
    private Logger logger = Logger.getLogger(getClass().getSimpleName());

    /**
     * List of files to download
     */
    private List<UpdaterFile> needsUpdating = new ArrayList<UpdaterFile>();

    /**
     * The folder where libraries are stored
     */
    public final static String DEST_LIBRARY_FOLDER = "plugins/LWC/";

    /**
     * File used for the distribution
     */
    public final static String DIST_FILE = "lwc/release/LWC.jar";

    /**
     * URL to the base update site
     */
    public final static String UPDATE_SITE = "http://griefcraft.com/bukkit/";

    /**
     * File used to obtain the latest version
     */
    public final static String VERSION_FILE = "lwc/VERSION";

    /**
     * The latest LWC version
     */
    private double latestPluginVersion = 0.00;

    /**
     * Download a file
     *
     * @param updaterFile
     */
    public void download(UpdaterFile updaterFile) {
        needsUpdating.add(updaterFile);

        try {
            update();
        } catch (Exception e) {
        }
    }

    /**
     * Check for dependencies
     */
    public void check() {
        if (Database.DefaultType == Database.Type.SQLite) {
            String[] shared = new String[]{DEST_LIBRARY_FOLDER + "lib/sqlite.jar", getFullNativeLibraryPath()};

            for (String path : shared) {
                File file = new File(path);

                if (file != null && !file.exists() && !file.isDirectory()) {
                    UpdaterFile updaterFile = new UpdaterFile(UPDATE_SITE + "shared/" + path.replaceAll(DEST_LIBRARY_FOLDER, ""));
                    updaterFile.setLocalLocation(path);

                    if (!needsUpdating.contains(updaterFile)) {
                        needsUpdating.add(updaterFile);
                    }
                }
            }
        }

        if (LWC.getInstance().getConfiguration().getBoolean("core.autoUpdate", false)) {
            if (latestPluginVersion > LWCInfo.VERSION) {
                logger.log("Update detected for LWC");
                logger.log("Latest version: " + latestPluginVersion);
            }
        }
    }

    /**
     * Check to see if the distribution is outdated
     *
     * @return
     */
    public boolean checkDist() {
        check();

        if (latestPluginVersion > LWCInfo.VERSION) {
            UpdaterFile updaterFile = new UpdaterFile(UPDATE_SITE + DIST_FILE);
            updaterFile.setLocalLocation("plugins/LWC.jar");

            needsUpdating.add(updaterFile);

            try {
                update();
                logger.log("Updated successful");
                return true;
            } catch (Exception e) {
                logger.log("Update failed: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return false;
    }

    public void downloadConfig(String config) {
        File file = new File(ModuleLoader.ROOT_PATH + config); // where to save to
        UpdaterFile updaterFile = new UpdaterFile(Updater.UPDATE_SITE + "lwc/skel/" + config);

        updaterFile.setLocalLocation(file.getPath());
        download(updaterFile);
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
    public double getLatestPluginVersion() {
        return latestPluginVersion;
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

    /**
     * Load the latest versions
     *
     * @param background if true, will be run in the background
     */
    public void loadVersions(boolean background) {
        class Background_Check_Thread implements Runnable {
            public void run() {
                try {
                    URL url = new URL(UPDATE_SITE + VERSION_FILE);

                    InputStream inputStream = url.openStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                    // load up them versions!
                    latestPluginVersion = Double.parseDouble(bufferedReader.readLine());

                    bufferedReader.close();
                } catch (Exception e) {
                }

                try {
                    if (LWC.getInstance().getConfiguration().getBoolean("core.autoUpdate", false)) {
                        checkDist();
                    } else {
                        check();
                    }

                    update();
                } catch (Exception e) {
                    e.printStackTrace();
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
     * Ensure we have all of the required files (if not, download them)
     */
    public void update() throws Exception {
        if (needsUpdating.size() == 0) {
            return;
        }

        /*
           * Make the folder hierarchy if needed
           */
        File folder = new File(getOSSpecificFolder());
        folder.mkdirs();
        folder = new File(DEST_LIBRARY_FOLDER + "lib/");
        folder.mkdirs();

        logger.log("Need to download " + needsUpdating.size() + " file(s)");

        Iterator<UpdaterFile> iterator = needsUpdating.iterator();

        while (iterator.hasNext()) {
            UpdaterFile item = iterator.next();

            String fileName = item.getRemoteLocation();
            fileName = fileName.substring(fileName.lastIndexOf('/') + 1);

            logger.log(" - Downloading file: " + fileName);

            URL url = new URL(item.getRemoteLocation());
            File file = new File(item.getLocalLocation());

            if (file.exists()) {
                file.delete();
            }

            InputStream inputStream = url.openStream();
            OutputStream outputStream = new FileOutputStream(file);

            saveTo(inputStream, outputStream);

            inputStream.close();
            outputStream.close();

            logger.log("  + Download complete");
            iterator.remove();
        }
    }

    /**
     * Write an input stream to an output stream
     *
     * @param inputStream
     * @param outputStream
     */
    public static void saveTo(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;

        while ((len = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, len);
        }
    }

}
