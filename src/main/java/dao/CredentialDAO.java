package dao;

/**
 * =============================================================
 * CLASS: CredentialDAO
 * LAYER: DAO (Data Access Object)
 * =============================================================
 *
 * PURPOSE:
 * This class manages the user credential records stored in the
 * system's CSV file. It handles reading, searching, and updating
 * user login credentials.
 *
 * The DAO layer is responsible for interacting with the data source.
 * In this project, the data source is a CSV file instead of a database.
 *
 * RESPONSIBILITIES:
 * - Load all user credential records
 * - Find a credential by username
 * - Update username
 * - Update credential information
 * - Maintain failed login attempts and last login timestamp
 *
 * CSV FORMAT:
 * username,passwordHash,role,employeeNumber,failedAttempts,lastLogin
 *
 * COLUMN EXPLANATION:
 * 0 → username
 * 1 → passwordHash
 * 2 → role
 * 3 → employeeNumber
 * 4 → failedAttempts
 * 5 → lastLogin
 *
 * IMPORTANT:
 * This class only handles file operations.
 * Authentication logic and password validation should be handled
 * in the service layer.
 *
 * USED BY:
 * - AuthenticationService
 * - Login system
 * - Profile update features
 *
 * 
 */

import model.Credential;
import model.Role;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CredentialDAO {

    /**
     * Path to the credential CSV file.
     * Example: data/credentials.csv
     */
    private final String filePath;

    /**
     * Constructor used to initialize the DAO
     * with the location of the credentials file.
     */
    public CredentialDAO(String filePath) {
        this.filePath = filePath;
    }

    /**
     * =============================================================
     * METHOD: findAll()
     * =============================================================
     *
     * Reads the entire credential CSV file and converts each row
     * into a Credential object.
     *
     * PROCESS:
     * 1. Open credentials CSV file
     * 2. Skip header row
     * 3. Read each credential entry
     * 4. Convert values into Credential object
     * 5. Store in a list
     * 6. Return the complete list
     *
     * @return list of all credentials
     */
    public List<Credential> findAll() throws IOException {

        List<Credential> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String line;
            boolean header = true;

            while ((line = br.readLine()) != null) {

                // Skip header row
                if (header) {
                    header = false;
                    continue;
                }

                // Skip empty lines
                if (line.trim().isEmpty()) continue;

                String[] c = line.split(",", -1);

                /**
                 * Expected CSV columns:
                 * 0=username
                 * 1=passwordHash
                 * 2=role
                 * 3=employeeNumber
                 * 4=failedAttempts
                 * 5=lastLogin
                 */

                String username = safeGet(c, 0);
                String passwordHash = safeGet(c, 1);

                // Convert role string into Role enum
                Role role = Role.valueOf(safeGet(c, 2));

                // Employee number may be blank (for IT accounts)
                String employeeNumber = safeGet(c, 3);

                /**
                 * Failed login attempts counter
                 */
                int failedAttempts = 0;

                String attemptsRaw = safeGet(c, 4);
                if (!attemptsRaw.isEmpty()) {
                    failedAttempts = Integer.parseInt(attemptsRaw);
                }

                /**
                 * Last login timestamp
                 */
                String lastLoginRaw = safeGet(c, 5);

                LocalDateTime lastLogin =
                        (lastLoginRaw.equalsIgnoreCase("null") || lastLoginRaw.isEmpty())
                                ? null
                                : LocalDateTime.parse(lastLoginRaw);

                // Create Credential object
                list.add(new Credential(
                        username,
                        passwordHash,
                        role,
                        employeeNumber,
                        failedAttempts,
                        lastLogin
                ));
            }
        }

        return list;
    }

    /**
     * =============================================================
     * METHOD: findByUsername()
     * =============================================================
     *
     * Searches for a credential record that matches
     * the given username.
     *
     * @param username username to search
     *
     * @return Optional credential record
     */
    public Optional<Credential> findByUsername(String username) throws IOException {

        return findAll().stream()
                .filter(c -> c.getUsername().equals(username))
                .findFirst();
    }

    /**
     * =============================================================
     * METHOD: updateUsername()
     * =============================================================
     *
     * Updates a user's username inside the credential CSV file.
     *
     * PROCESS:
     * 1. Validate input
     * 2. Read all rows from CSV
     * 3. Check if new username already exists
     * 4. Locate the old username
     * 5. Replace username value
     * 6. Write updated rows back to file
     */
    public void updateUsername(String oldUsername, String newUsername) throws Exception {

        if (oldUsername == null || oldUsername.isBlank())
            throw new Exception("Old username required.");

        if (newUsername == null || newUsername.isBlank())
            throw new Exception("New username required.");

        // Read header and rows
        List<String[]> rows = new ArrayList<>();
        String headerLine;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            headerLine = br.readLine();

            if (headerLine == null)
                throw new IOException("Empty credentials file.");

            String line;

            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) continue;

                rows.add(line.split(",", -1));
            }
        }

        /**
         * Ensure the new username is not already taken.
         */
        for (String[] row : rows) {

            String u = safeGet(row, 0);

            if (u.equalsIgnoreCase(newUsername)) {
                throw new Exception("Username already taken.");
            }
        }

        /**
         * Locate row with matching old username
         * and update column 0.
         */
        boolean found = false;

        for (String[] row : rows) {

            String u = safeGet(row, 0);

            if (u.equals(oldUsername)) {

                String[] padded = padTo6(row);

                padded[0] = newUsername;

                for (int i = 0; i < padded.length; i++) {
                    row[i] = padded[i];
                }

                found = true;
                break;
            }
        }

        if (!found)
            throw new Exception("User not found.");

        /**
         * Rewrite updated rows back to CSV file.
         */
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {

            bw.write(headerLine);
            bw.newLine();

            for (String[] r : rows) {

                bw.write(String.join(",", padTo6(r)));
                bw.newLine();
            }
        }
    }

    /**
     * =============================================================
     * METHOD: update()
     * =============================================================
     *
     * Updates the credential information of an existing user.
     *
     * Updated fields may include:
     * - password hash
     * - role
     * - employee number
     * - failed login attempts
     * - last login timestamp
     */
    public void update(Credential updated) throws IOException {

        List<String[]> rows = new ArrayList<>();
        String headerLine;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            headerLine = br.readLine();

            if (headerLine == null)
                throw new IOException("Empty credentials file.");

            String line;

            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) continue;

                rows.add(line.split(",", -1));
            }
        }

        boolean found = false;

        for (String[] row : rows) {

            if (row.length < 1) continue;

            String username = safeGet(row, 0);

            if (username.equals(updated.getUsername())) {

                // Ensure row has correct number of columns
                String[] padded = padTo6(row);

                padded[1] = updated.getPasswordHash();
                padded[2] = updated.getRole().name();
                padded[3] = (updated.getEmployeeNumber() == null)
                        ? ""
                        : updated.getEmployeeNumber();
                padded[4] = String.valueOf(updated.getFailedAttempts());
                padded[5] = (updated.getLastLogin() == null)
                        ? "null"
                        : updated.getLastLogin().toString();

                for (int i = 0; i < padded.length; i++) {
                    row[i] = padded[i];
                }

                found = true;
                break;
            }
        }

        if (!found)
            throw new IOException("Credential not found: " + updated.getUsername());

        /**
         * Write updated data back to CSV.
         */
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {

            bw.write(headerLine);
            bw.newLine();

            for (String[] r : rows) {

                bw.write(String.join(",", padTo6(r)));
                bw.newLine();
            }
        }
    }

    /**
     * =============================================================
     * METHOD: padTo6()
     * =============================================================
     *
     * Ensures that each CSV row always contains 6 columns.
     * Missing values are filled with default values.
     */
    private String[] padTo6(String[] row) {

        String[] out = new String[6];

        for (int i = 0; i < out.length; i++) {

            out[i] = (i < row.length) ? row[i] : "";
        }

        if (out[5] == null || out[5].trim().isEmpty())
            out[5] = "null";

        if (out[4] == null || out[4].trim().isEmpty())
            out[4] = "0";

        if (out[3] == null)
            out[3] = "";

        return out;
    }

    /**
     * =============================================================
     * METHOD: safeGet()
     * =============================================================
     *
     * Safely retrieves a value from a CSV row array.
     * Prevents null values and index errors.
     */
    private String safeGet(String[] arr, int idx) {

        if (arr == null || idx < 0 || idx >= arr.length || arr[idx] == null)
            return "";

        return arr[idx].trim();
    }
}