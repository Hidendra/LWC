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

import org.bukkit.inventory.ItemStack;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Backup {

    /**
     * The backup file's current revision
     */
    public static final int CURRENT_REVISION = 1;

    /**
     * The result for a backup operation
     */
    public enum Result {
        OK, FAIL
    }

    /**
     * The operations the backup is allowed to perform
     */
    public enum Operation {
        READ, WRITE
    }

    /**
     * The file this backup is located at
     */
    private final File file;

    /**
     * The operation we are allowed to perform
     */
    private final Operation operation;

    /**
     * The flags we are using
     */
    private final EnumSet<BackupManager.Flag> flags;

    /**
     * This backup's revision number
     */
    private int revision;

    /**
     * The time the backup was created it
     */
    private long created;

    /**
     * The backup's input stream if we are reading
     */
    private DataInputStream inputStream;

    /**
     * The backup's output stream if we are writing
     */
    private DataOutputStream outputStream;

    public Backup(File file, Operation operation, EnumSet<BackupManager.Flag> flags) throws IOException {
        this.file = file;
        this.operation = operation;
        this.flags = flags;

        if (!file.exists()) {
            if (operation == Operation.READ) {
                throw new UnsupportedOperationException("The backup could not be read");
            } else {
                file.createNewFile();
            }
        }

        // Set some base data if we're writing
        if (operation == Operation.WRITE) {
            revision = CURRENT_REVISION;
            created = System.currentTimeMillis() / 1000;
        }

        // Are we using compression?
        boolean compression = flags.contains(BackupManager.Flag.COMPRESSION);

        // create the stream we need
        if (operation == Operation.READ) {
            FileInputStream fis = new FileInputStream(file);
            inputStream = new DataInputStream(compression ? new GZIPInputStream(fis) : fis);
        } else if (operation == Operation.WRITE) {
            FileOutputStream fos = new FileOutputStream(file);
            outputStream = new DataOutputStream(compression ? new GZIPOutputStream(fos) : fos);
        }
    }

    /**
     * Begin restoring this backup. This should be ran in a separate thread.
     * Any world calls are offloaded to the world thread using the scheduler. No world reads are done, only writes.
     *
     * @return
     */
    public Result restoreBackup() {
        throw new UnsupportedOperationException("Not yet supported");
    }

    /**
     * Read an entity from the backup file
     *
     * @return
     */
    protected Restorable readRestorable() throws IOException {
        if (operation != Operation.READ) {
            throw new UnsupportedOperationException("READ is not allowed on this backup.");
        }

        // The object type
        int type = inputStream.read() & 0xFF;

        // TODO enum that shit yo
        if (type == 0) { // Protection

        } else if (type == 1) { // Block
            RestorableBlock block = new RestorableBlock();
            block.setId(inputStream.readShort());
            block.setWorld(inputStream.readUTF());
            block.setX(inputStream.readInt());
            block.setY(inputStream.readShort());
            block.setZ(inputStream.readInt());
            block.setData(inputStream.read() & 0xFF);
            int itemCount = inputStream.readShort();
            
            for (int i = 0; i < itemCount; i++) {
                // Read in us some RestorableItems
                int slot = inputStream.readShort();
                int itemId = inputStream.readShort();
                int amount = inputStream.readShort();
                short damage = inputStream.readShort();

                // Create the stack
                ItemStack itemStack = new ItemStack(itemId, amount, damage);

                // add it to the block
                block.setSlot(slot, itemStack);
            }
            
            // Woo!
            return block;
        }

        throw new UnsupportedOperationException("Not yet supported");
    }

    /**
     * Write an entity to the backup file
     *
     * @param restorable
     */
    protected void writeRestorable(Restorable restorable) throws IOException {
        if (operation != Operation.WRITE) {
            throw new UnsupportedOperationException("WRITE is not allowed on this backup.");
        }

        // write the id
        outputStream.write((byte) restorable.getType());

        // Write it
        if (restorable.getType() == 0) { // Protection, also TODO ENUMSSSSSSSSSSS

        } else if (restorable.getType() == 1) { // Block, TODO DID I SAY TO DO THE ENUM YET??
            RestorableBlock rblock = (RestorableBlock) restorable;

            outputStream.writeShort(rblock.getId());
            outputStream.writeUTF(rblock.getWorld());
            outputStream.writeInt(rblock.getX());
            outputStream.writeShort(rblock.getY());
            outputStream.writeInt(rblock.getZ());
            outputStream.write((byte) rblock.getData());
            outputStream.writeShort(rblock.getItems().size());
            
            // Write the items if there are any
            for (Map.Entry<Integer, ItemStack> entry : rblock.getItems().entrySet()) {
                int slot = entry.getKey();
                ItemStack stack = entry.getValue();

                outputStream.writeShort(slot);
                outputStream.writeShort(stack.getTypeId());
                outputStream.writeShort(stack.getAmount());
                outputStream.writeShort(stack.getDurability());
            }

            outputStream.flush();
        }
    }

    /**
     * Read the backup's header
     * 
     * @throws IOException
     */
    protected void readHeader() throws IOException {
        revision = inputStream.readShort();
        created = inputStream.readLong();
        inputStream.read(new byte[10]); // reserved space
    }

    /**
     * Write the backup's header
     *
     * @throws IOException
     */
    protected void writeHeader() throws IOException {
        outputStream.writeShort(revision);
        outputStream.writeLong(created);
        outputStream.write(new byte[10]); // reserved space
        outputStream.flush();
    }
    
    protected void close() throws IOException {
        if (operation == Operation.READ) {
            inputStream.close();
        } else if (operation == Operation.WRITE) {
            outputStream.close();
        }
    }

}
