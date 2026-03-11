/*
 * NetBeans auto-generated license header
 */
package dao;

/**
 * =============================================================
 * CLASS: UserActivityLogDAO
 * LAYER: DAO (Data Access Object)
 * =============================================================
 *
 * PURPOSE:
 * This class manages the storage and retrieval of user activity
 * logs in the MotorPH Payroll System.
 *
 * It records system activities such as:
 * - User login attempts
 * - Password changes
 * - Username updates
 * - System actions performed by users
 *
 * The logs are stored in a CSV file and used for:
 * - System monitoring
 * - Security auditing
 * - User activity tracking
 *
 * CSV STRUCTURE:
 * timestamp,username,action,details
 *
 * COLUMN DESCRIPTION:
 * timestamp → Date and time when the activity occurred
 * username  → Username of the user performing the action
 * action    → Type of action performed (e.g., LOGIN_SUCCESS)
 * details   → Additional description of the activity
 *
 * IMPORTANT:
 * This class only handles writing and reading logs.
 * The system logic that decides when to log actions
 * is handled by the Service or Controller layer.
 */

import model.UserActivityLogEntry;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class UserActivityLogDAO {

    /**
     * Path to the CSV file that stores activity logs.
     */
    private final String filePath;

    /**
     * Constructor used to initialize DAO with
     * the location of the activity log file.
     */
    public UserActivityLogDAO(String filePath) {
        this.filePath = filePath;
    }

    /**
     * =============================================================
     * METHOD: append()
     * =============================================================
     *
     * Adds a new activity log entry to the CSV file.
     *
     * PROCESS:
     * 1. Ensure the log file exists and contains a header
     * 2. Convert the log entry into CSV format
     * 3. Append the entry to the end of the file
     */
    public void append(UserActivityLogEntry entry) throws IOException {

        ensureHeader();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {

            bw.write(toCsvRow(entry));
            bw.newLine();
        }
    }

    /**
     * =============================================================
     * METHOD: findAll()
     * =============================================================
     *
     * Reads all user activity logs from the CSV file.
     *
     * PROCESS:
     * 1. Ensure the file has a valid header
     * 2. Read each log record
     * 3. Convert rows into UserActivityLogEntry objects
     * 4. Sort entries by timestamp (latest first)
     *
     * @return list of activity log entries
     */
    public List<UserActivityLogEntry> findAll() throws IOException {

        ensureHeader();

        List<UserActivityLogEntry> out = new ArrayList<>();

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
                 * CSV column order:
                 * timestamp,username,action,details
                 */
                String ts = get(c, 0);
                String user = get(c, 1);
                String action = get(c, 2);
                String details = unescape(get(c, 3));

                LocalDateTime time = LocalDateTime.parse(ts);

                // Convert row into UserActivityLogEntry object
                out.add(new UserActivityLogEntry(time, user, action, details));
            }
        }

        /**
         * Sort logs so the newest activities appear first.
         */
        out.sort(
                Comparator.comparing(UserActivityLogEntry::getTimestamp)
                        .reversed()
        );

        return out;
    }

    /**
     * =============================================================
     * METHOD: findTodayFailedLogins()
     * =============================================================
     *
     * Retrieves all failed login attempts for the current day.
     *
     * This can be used by the system to:
     * - Detect suspicious login activity
     * - Monitor account security
     */
    public List<UserActivityLogEntry> findTodayFailedLogins() throws IOException {

        LocalDate today = LocalDate.now();

        List<UserActivityLogEntry> all = findAll();
        List<UserActivityLogEntry> out = new ArrayList<>();

        for (UserActivityLogEntry e : all) {

            if (e.getTimestamp() == null) continue;

            // Check if log entry occurred today
            if (!today.equals(e.getTimestamp().toLocalDate())) continue;

            // Check if action indicates failed login
            if ("LOGIN_FAILED".equalsIgnoreCase(e.getAction())) {
                out.add(e);
            }
        }

        return out;
    }

    // =============================================================
    // HELPER METHODS
    // =============================================================

    /**
     * Ensures the log file exists and contains the correct header.
     */
    private void ensureHeader() throws IOException {

        File f = new File(filePath);

        if (!f.exists()) {

            File parent = f.getParentFile();

            if (parent != null)
                parent.mkdirs();

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {

                bw.write("timestamp,username,action,details");
                bw.newLine();
            }

            return;
        }

        // If file exists but empty, write header
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {

            String first = br.readLine();

            if (first == null || first.trim().isEmpty()) {

                try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {

                    bw.write("timestamp,username,action,details");
                    bw.newLine();
                }
            }
        }
    }

    /**
     * Converts a UserActivityLogEntry object into a CSV row.
     */
    private String toCsvRow(UserActivityLogEntry e) {

        return e.getTimestamp().toString() + ","
                + safe(e.getUsername()) + ","
                + safe(e.getAction()) + ","
                + escape(e.getDetails());
    }

    /**
     * Escapes special characters before saving to CSV.
     */
    private String escape(String s) {

        if (s == null) return "";

        return s.replace("\n", "\\n")
                .replace(",", "，");
    }

    /**
     * Restores escaped characters when reading from CSV.
     */
    private String unescape(String s) {

        if (s == null) return "";

        return s.replace("\\n", "\n")
                .replace("，", ",");
    }

    /**
     * Prevents null values when writing CSV fields.
     */
    private String safe(String s) {

        return (s == null) ? "" : s.trim();
    }

    /**
     * Safely retrieves a value from a CSV column array.
     */
    private String get(String[] arr, int idx) {

        if (arr == null || idx < 0 || idx >= arr.length)
            return "";

        return (arr[idx] == null) ? "" : arr[idx];
    }
}