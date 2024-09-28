package org.dfproductions.budgeting.backend;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordHashing {

    // Method to hash the password with a generated salt and return both as a single string
    public static String hashPassword(String password) throws NoSuchAlgorithmException {
        // Generate a random salt
        byte[] salt = generateSalt();

        // Hash the password with the salt
        byte[] hashedPassword = hash(password, salt);

        // Combine the salt (in hexadecimal) and the hashed password (in Base64)
        String saltAsString = bytesToHex(salt);
        String hashedPasswordAsString = Base64.getEncoder().encodeToString(hashedPassword);

        // Return the salt and the hashed password, separated by a colon
        return saltAsString + ":" + hashedPasswordAsString;
    }

    // Method to generate a salt using SecureRandom
    private static byte[] generateSalt() throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];  // 16 bytes salt
        sr.nextBytes(salt);
        return salt;
    }

    // Method to hash the password with the salt using SHA-256
    private static byte[] hash(String password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt);  // Add the salt to the hash input
        return md.digest(password.getBytes());  // Hash the password
    }

    // Method to convert byte array to a hexadecimal string
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    // Method to convert a hexadecimal string back to a byte array
    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }

    public static boolean verifyPassword(String password, String storedHash) throws NoSuchAlgorithmException {
        // Split the stored salt and hash
        String[] parts = storedHash.split(":");
        byte[] salt = hexToBytes(parts[0]);  // Convert the salt back from hex
        byte[] storedHashedPassword = Base64.getDecoder().decode(parts[1]);

        // Hash the input password with the same salt
        byte[] hashedPassword = hash(password, salt);

        // Compare the hashes
        return MessageDigest.isEqual(hashedPassword, storedHashedPassword);
    }

}
