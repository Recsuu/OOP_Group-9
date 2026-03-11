package service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * =============================================================
 * SERVICE: PasswordService
 * =============================================================
 * PURPOSE
 * Handles password hashing and verification for authentication.
 *
 * SECURITY APPROACH
 * Passwords are never stored in plain text. Instead, they are
 * converted to SHA-256 hashes before saving to the CSV storage.
 *
 * METHODS
 * - hash(): converts a plain password into SHA-256 hash
 * - verify(): checks if a raw password matches the stored hash
 */

public class PasswordService {

    // Converts plain password to SHA-256 hash
    public String hash(String plainPassword) throws Exception {

        // Validate input
        if (plainPassword == null || plainPassword.isBlank()) {
            throw new Exception("Password cannot be empty.");
        }

        // Create SHA-256 digest
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        // Convert password into bytes then hash
        byte[] hashed = md.digest(plainPassword.getBytes(StandardCharsets.UTF_8));

        // Convert byte array into hex string
        StringBuilder sb = new StringBuilder(hashed.length * 2);
        for (byte b : hashed) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    /**
     * Verifies if a raw password matches the stored hash.
     */
    public boolean verify(String rawPassword, String storedHash) throws Exception {

        // If no stored hash, verification fails
        if (storedHash == null || storedHash.isBlank()) return false;

        // Hash the entered password
        String rawHash = hash(rawPassword);

        // Compare hashes
        return storedHash.equalsIgnoreCase(rawHash);
    }
}