package com.app.smartretail.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * PasswordHasher - Hash password menggunakan SHA-256
 * Untuk produksi, sebaiknya gunakan BCrypt dari library
 */
public class PasswordHasher {

    private PasswordHasher() {}

    public static String hash(String plainText) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(plainText.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 tidak tersedia", e);
        }
    }

    public static boolean verify(String plainText, String hashed) {
        return hash(plainText).equals(hashed);
    }
}
