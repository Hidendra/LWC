package org.getlwc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public class MD5Checksum {

    /**
     * Calculate the MD5 checksum of the given file
     *
     * @param file
     * @return
     * @throws Exception
     */
    public static byte[] calculateChecksum(File file) throws Exception {
        InputStream fis = new FileInputStream(file);

        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;

        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        fis.close();
        return complete.digest();
    }

    /**
     * Calculate the human-readable MD5 checksum of the given file
     *
     * @param file
     * @return
     * @throws Exception
     */
    public static String calculateHumanChecksum(File file) throws Exception {
        byte[] checksum = calculateChecksum(file);
        String result = "";

        for (int i = 0; i < checksum.length; i++) {
            result += Integer.toString((checksum[i] & 0xff) + 0x100, 16).substring(1);
        }

        return result;
    }

}
