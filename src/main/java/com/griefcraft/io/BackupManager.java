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

package com.griefcraft.io;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.griefcraft.sql.Database;
import com.griefcraft.sql.PhysDB;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class BackupManager {

    /**
     * The result for a backup operationMode
     */
    public enum Result {
        OK, FAILURE
    }

    /**
     * The folder where backups are stored at
     */
    public static String BACKUP_FOLDER = "plugins/LWC/backups/";

    /**
     * The date format to name backup files with by default
     */
    private static String DATE_FORMAT = "MM-dd-yyyy-HHmm";

    /**
     * The file extension for compressed backups
     */
    private static String FILE_EXTENSION_COMPRESSED = ".lwc.gz";

    /**
     * The file extension of uncompressed backups
     */
    private static String FILE_EXTENSION_UNCOMPRESSED = ".lwc";

    /**
     * The amount of protection block gets to batch at once
     */
    private static int BATCH_SIZE = 250;

    /**
     * The folder backups are stored in
     */
    private File backupFolder;
	
	private final LWC lwc = LWC.getInstance();
	private final Plugin plugin = lwc.getPlugin();

    /**
     * Backup creation flags
     */
    public enum Flag {

        /**
         * Backup protection objects
         */
        BACKUP_PROTECTIONS,

        /**
         * Backup blocks along with their inventory contents (if applicable) also
         */
        BACKUP_BLOCKS,

        /**
         * Compress the backup using GZip
         */
        COMPRESSION

    }

    public BackupManager() {
		BACKUP_FOLDER = plugin.getDataFolder().getAbsolutePath() + "backups/";
		backupFolder = new File(BACKUP_FOLDER);
        if (!backupFolder.exists()) {
            backupFolder.mkdir();
        }
    }

    /**
     * Begin restoring a backup. This should be ran in a separate thread.
     * Any world calls are offloaded to the world thread using the scheduler. No world reads are done, only writes.
     *
     * @param name
     * @return OK if successful, otherwise FAILURE
     */
    public Result restoreBackup(String name) {
        try {
            Backup backup = loadBackup(name);

            if (backup == null) {
                return Result.FAILURE;
            }

            return restoreBackup(backup);
        } catch (IOException e) {
            System.out.println("[BackupManager] Caught: " + e.getMessage());
            return Result.FAILURE;
        }
    }

    /**
     * Begin restoring a backup. This should be ran in a separate thread.
     * Any world calls are offloaded to the world thread using the scheduler. No world reads are done, only writes.
     *
     * @param backup
     * @return OK if successful, otherwise FAILURE
     */
    public Result restoreBackup(Backup backup) {
        try {
            // Read in the backup's header
            backup.readHeader();

            // begin restoring :)
            Restorable restorable;
            int count = 0;
            int protectionCount = 0;
            int blockCount = 0;
            while ((restorable = backup.readRestorable()) != null) {
                restorable.restore();

                if (count % 2000 == 0) {
                    System.out.println("[Backup] Restored restorables: " + count);
                }
                count ++;

                // TODO THIS IS HACKS :-( ALSO ENUM ENUM
                if (restorable.getType() == 0) {
                    protectionCount ++;
                } else if (restorable.getType() == 1) {
                    blockCount ++;
                }
            }

            System.out.println(String.format("[BackupManager] Restored %d restorables. %d were protections, %d blocks.", count, protectionCount, blockCount));
            return Result.OK;
        } catch (IOException e) {
            e.printStackTrace();
            return Result.FAILURE;
        }
    }

    /**
     * Load a backup
     *
     * @param name
     * @return
     */
    public Backup loadBackup(String name) throws IOException {
        File file;

        // Try to load the compressed version
        file = new File(BACKUP_FOLDER, name + FILE_EXTENSION_COMPRESSED);

        if (file.exists()) {
            // Bingo
            return new Backup(file, Backup.OperationMode.READ, EnumSet.of(Flag.COMPRESSION));
        }

        // Try uncompressed
        file = new File(BACKUP_FOLDER, name + FILE_EXTENSION_UNCOMPRESSED);

        if (file.exists()) {
            return new Backup(file, Backup.OperationMode.READ, EnumSet.noneOf(Flag.class));
        }

        // Nothing :-(
        return null;
    }

    /**
     * Create a backup of the given objects.
     * When this returns, it is not guaranteed that the backup is fully written to the disk.
     *
     * @param name
     * @param flags
     * @return
     */
    public Backup createBackup(String name, final EnumSet<Flag> flags) {
        final Plugin plugin = lwc.getPlugin();
        Server server = Bukkit.getServer();
        final BukkitScheduler scheduler = server.getScheduler();
        String extension = flags.contains(Flag.COMPRESSION) ? FILE_EXTENSION_COMPRESSED : FILE_EXTENSION_UNCOMPRESSED;
        File backupFile = new File(backupFolder, name + extension);

        // Our backup file
        try {
            final Backup backup = new Backup(backupFile, Backup.OperationMode.WRITE, flags);

            scheduler.scheduleAsyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    try {
                        System.out.println("Processing backup request now in a separate thread");

                        // the list of protections work off of. We batch updates to the world
                        // so we can more than 20 results/second.
                        final List<Protection> protections = new ArrayList<Protection>(BATCH_SIZE);

                        // amount of protections
                        int totalProtections = lwc.getPhysicalDatabase().getProtectionCount();

                        // Write the header
                        backup.writeHeader();

                        // TODO separate stream logic to somewhere else :)
                        // Create a new database connection, we are just reading
                        PhysDB database = new PhysDB();
                        database.connect();
                        database.load();

                        // TODO separate stream logic to somewhere else :)
                        Statement resultStatement = database.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

                        if (lwc.getPhysicalDatabase().getType() == Database.Type.MySQL) {
                            resultStatement.setFetchSize(Integer.MIN_VALUE);
                        }

                        String prefix = lwc.getPhysicalDatabase().getPrefix();
                        ResultSet result = resultStatement.executeQuery("SELECT id, owner, type, x, y, z, data, blockId, world, password, date, last_accessed FROM " + prefix + "protections");
                        int count = 0;

                        while (result.next()) {
                            final Protection tprotection = database.resolveProtection(result);

                            if (count % 2000 == 0) {
                                System.out.println("[Backup] Parsed protections: " + count + "/" + totalProtections);
                            }
                            count ++;

                            if (protections.size() != BATCH_SIZE) {
                                // Wait until we have BATCH_SIZE protections
                                protections.add(tprotection);

                                if (protections.size() != totalProtections) {
                                    continue;
                                }
                            }

                            // Get all of the blocks in the world
                            Future<Void> getBlocks = scheduler.callSyncMethod(plugin, new Callable<Void>() {
                                public Void call() throws Exception {
                                    for (Protection protection : protections) {
                                        protection.getBlock(); // this will cache it also :D
                                    }

                                    return null;
                                }
                            });

                            // Get all of the blocks
                            getBlocks.get();

                            for (Protection protection : protections) {
                                try {
                                    // if we are writing the block to the backup, do that before we write the protection
                                    if (flags.contains(Flag.BACKUP_BLOCKS)) {
                                        // now we can get the block from the world
                                        Block block = protection.getBlock();

                                        // Wrap the block object in a RestorableBlock object
                                        RestorableBlock rblock = RestorableBlock.wrapBlock(block);

                                        // Write it
                                        backup.writeRestorable(rblock);
                                    }

                                    // Now write the protection after the block if we are writing protections
                                    if (flags.contains(Flag.BACKUP_PROTECTIONS)) {
                                        RestorableProtection rprotection = RestorableProtection.wrapProtection(protection);

                                        // Write it
                                        backup.writeRestorable(rprotection);
                                    }
                                } catch (Exception e) {
                                    System.out.println("Caught: " + e.getMessage() + ". Carrying on...");
                                }
                            }

                            // Clear the protection set, we are done with them
                            protections.clear();
                        }

                        // close the sql statements
                        result.close();
                        resultStatement.close();

                        // close the backup file
                        backup.close();

                        System.out.println("Backup completed!");
                    } catch (Exception e) { // database.connect() throws Exception
                        System.out.println("Backup exception caught: " + e.getMessage());
                    }
                }
            });

            return backup;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Create a backup of all protections, blocks, and their contents
     * When this returns, it is not guaranteed that the backup is fully written to the disk.
     *
     * @param flags
     * @return
     */
    public Backup createBackup(EnumSet<Flag> flags) {
        return createBackup(new SimpleDateFormat(DATE_FORMAT).format(new Date()), flags);
    }

    /**
     * Create a backup of all protections, blocks, and their contents
     * When this returns, it is not guaranteed that the backup is fully written to the disk.
     *
     * @return
     */
    public Backup createBackup() {
        return createBackup(EnumSet.of(Flag.COMPRESSION, Flag.BACKUP_BLOCKS, Flag.BACKUP_PROTECTIONS));
    }

}
