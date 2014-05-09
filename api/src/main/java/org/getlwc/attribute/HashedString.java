package org.getlwc.attribute;

import java.security.MessageDigest;

public class HashedString {

    /**
     * The hashed value
     */
    private String hashed;

    public HashedString(String value, boolean hashed) {
        if (hashed) {
            this.hashed = value;
        } else {
            this.hashed = sha256(value);
        }
    }

    /**
     * Get the hashed string
     *
     * @return
     */
    public String getHashedString() {
        return hashed;
    }

    /**
     * Checks if the given unhashed string matches the internal hashed string
     *
     * @param unhashed
     * @return
     */
    public boolean matches(String unhashed) {
        return sha256(unhashed).equals(hashed);
    }

    @Override
    public String toString() {
        return hashed;
    }

    /**
     * Hash a string as sha256
     *
     * @param input
     * @return
     */
    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
