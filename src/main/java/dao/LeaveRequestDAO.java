/*
 * NetBeans auto-generated license header
 */
package dao;

/**
 * =============================================================
 * CLASS: LeaveRequestDAO
 * LAYER: DAO (Data Access Object)
 * =============================================================
 *
 * PURPOSE:
 * This class handles all data operations related to employee
 * leave requests in the MotorPH Payroll System.
 *
 * It reads and writes leave request records stored in a CSV file.
 *
 * RESPONSIBILITIES:
 * - Load leave request records
 * - Find leave requests by employee number
 * - Find leave request by request ID
 * - Create new leave request
 * - Update existing leave request status or details
 *
 * CSV FILE STRUCTURE:
 * requestId,employeeNumber,type,startDate,endDate,reason,
 * status,submittedAt,decisionAt,decidedBy,decisionReason
 *
 * IMPORTANT:
 * The DAO layer should only handle file storage operations.
 * Leave approval logic should be handled in the Service layer.
 *
 * USED BY:
 * - LeaveService
 * - HR Leave Approval Module
 * - Employee Leave Request Module
 *
 * AUTHOR: recsy
 */

import model.LeaveRequest;
import model.LeaveStatus;
import model.LeaveType;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class LeaveRequestDAO {

    /**
     * Path of the leave request CSV file.
     */
    private final String filePath;

    /**
     * Constructor used to initialize DAO with
     * the location of the leave request file.
     */
    public LeaveRequestDAO(String filePath) {
        this.filePath = filePath;
    }

    /**
     * =============================================================
     * METHOD: findAll()
     * =============================================================
     *
     * Reads all leave requests from the CSV file
     * and converts them into LeaveRequest objects.
     *
     * PROCESS:
     * 1. Ensure the CSV file exists and has a header
     * 2. Read each line
     * 3. Convert row into LeaveRequest object
     * 4. Store results in a list
     * 5. Sort by submitted date (latest first)
     */
    public List<LeaveRequest> findAll() throws IOException {

        ensureFileHasHeader();

        List<LeaveRequest> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String line;
            boolean header = true;

            while ((line = br.readLine()) != null) {

                // Skip header row
                if (header) {
                    header = false;
                    continue;
                }

                // Skip empty rows
                if (line.trim().isEmpty()) continue;

                String[] c = line.split(",", -1);

                // Ensure row always contains 11 columns
                String[] cols = padTo11(c);

                String requestId = get(cols, 0);
                String employeeNumber = get(cols, 1);

                LeaveType type = LeaveType.valueOf(get(cols, 2));

                LocalDate startDate = LocalDate.parse(get(cols, 3));
                LocalDate endDate = LocalDate.parse(get(cols, 4));

                String reason = unescape(get(cols, 5));

                LeaveStatus status = LeaveStatus.valueOf(get(cols, 6));

                LocalDateTime submittedAt = parseDateTime(get(cols, 7));
                LocalDateTime decisionAt = parseDateTime(get(cols, 8));

                String decidedBy = get(cols, 9);

                String decisionReason = unescape(get(cols, 10));

                // Convert CSV row into LeaveRequest object
                list.add(new LeaveRequest(
                        requestId,
                        employeeNumber,
                        type,
                        startDate,
                        endDate,
                        reason,
                        status,
                        submittedAt,
                        decisionAt,
                        decidedBy,
                        decisionReason
                ));
            }
        }

        /**
         * Sort leave requests by submission date
         * newest first for better UI display.
         */
        list.sort(
                Comparator.comparing(
                        LeaveRequest::getSubmittedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ).reversed()
        );

        return list;
    }

    /**
     * =============================================================
     * METHOD: findByEmployeeNumber()
     * =============================================================
     *
     * Returns all leave requests submitted
     * by a specific employee.
     */
    public List<LeaveRequest> findByEmployeeNumber(String employeeNumber) throws IOException {

        List<LeaveRequest> all = findAll();
        List<LeaveRequest> out = new ArrayList<>();

        for (LeaveRequest lr : all) {

            if (lr.getEmployeeNumber().equals(employeeNumber)) {
                out.add(lr);
            }
        }

        return out;
    }

    /**
     * =============================================================
     * METHOD: findById()
     * =============================================================
     *
     * Searches for a leave request by its request ID.
     */
    public Optional<LeaveRequest> findById(String requestId) throws IOException {

        return findAll().stream()
                .filter(r -> r.getRequestId().equals(requestId))
                .findFirst();
    }

    /**
     * =============================================================
     * METHOD: create()
     * =============================================================
     *
     * Inserts a new leave request record into the CSV file.
     */
    public void create(LeaveRequest request) throws IOException {

        ensureFileHasHeader();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {

            bw.write(toCsvRow(request));
            bw.newLine();
        }
    }

    /**
     * =============================================================
     * METHOD: update()
     * =============================================================
     *
     * Updates an existing leave request record.
     *
     * Typically used when HR approves or rejects
     * a leave request.
     */
    public void update(LeaveRequest updated) throws IOException {

        ensureFileHasHeader();

        List<String[]> rows = new ArrayList<>();

        String headerLine;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            headerLine = br.readLine();

            if (headerLine == null)
                throw new IOException("Empty leave file.");

            String line;

            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) continue;

                rows.add(line.split(",", -1));
            }
        }

        boolean found = false;

        for (int i = 0; i < rows.size(); i++) {

            String[] r = padTo11(rows.get(i));

            if (get(r, 0).equals(updated.getRequestId())) {

                rows.set(i, toCsvCols(updated));

                found = true;
                break;
            }
        }

        if (!found)
            throw new IOException(
                    "Leave request not found: "
                            + updated.getRequestId());

        // Rewrite updated CSV file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {

            bw.write(headerLine);
            bw.newLine();

            for (String[] r : rows) {

                bw.write(String.join(",", padTo11(r)));
                bw.newLine();
            }
        }
    }

    // =============================================================
    // HELPER METHODS
    // =============================================================

    /**
     * Ensures that the leave CSV file exists and
     * contains the correct header structure.
     */
    private void ensureFileHasHeader() throws IOException {

        File f = new File(filePath);

        if (!f.exists()) {

            File parent = f.getParentFile();

            if (parent != null)
                parent.mkdirs();

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {

                bw.write("requestId,employeeNumber,type,startDate,endDate,reason,status,submittedAt,decisionAt,decidedBy,decisionReason");
                bw.newLine();
            }

            return;
        }

        // If file exists but empty, add header
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {

            String first = br.readLine();

            if (first == null || first.trim().isEmpty()) {

                try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {

                    bw.write("requestId,employeeNumber,type,startDate,endDate,reason,status,submittedAt,decisionAt,decidedBy,decisionReason");
                    bw.newLine();
                }
            }
        }
    }

    /**
     * Converts LeaveRequest object into CSV row string.
     */
    private String toCsvRow(LeaveRequest r) {
        return String.join(",", toCsvCols(r));
    }

    /**
     * Converts LeaveRequest object into CSV column array.
     */
    private String[] toCsvCols(LeaveRequest r) {

        String[] out = new String[11];

        out[0] = r.getRequestId();
        out[1] = r.getEmployeeNumber();
        out[2] = r.getType().name();
        out[3] = r.getStartDate().toString();
        out[4] = r.getEndDate().toString();
        out[5] = escape(r.getReason());
        out[6] = r.getStatus().name();
        out[7] = (r.getSubmittedAt() == null)
                ? "null"
                : r.getSubmittedAt().toString();
        out[8] = (r.getDecisionAt() == null)
                ? "null"
                : r.getDecisionAt().toString();
        out[9] = (r.getDecidedBy() == null)
                ? ""
                : r.getDecidedBy();
        out[10] = escape(r.getDecisionReason());

        return out;
    }

    /**
     * Ensures every CSV row contains 11 columns.
     */
    private String[] padTo11(String[] row) {

        String[] out = new String[11];

        for (int i = 0; i < out.length; i++) {

            out[i] = (i < row.length) ? row[i] : "";
        }

        if (out[7] == null || out[7].isEmpty()) out[7] = "null";
        if (out[8] == null || out[8].isEmpty()) out[8] = "null";
        if (out[9] == null) out[9] = "";
        if (out[10] == null) out[10] = "";

        return out;
    }

    /**
     * Safe getter for array index.
     */
    private String get(String[] arr, int idx) {

        if (arr == null || idx < 0 || idx >= arr.length)
            return "";

        return arr[idx] == null ? "" : arr[idx];
    }

    /**
     * Parses date-time values from CSV.
     */
    private LocalDateTime parseDateTime(String s) {

        if (s == null) return null;

        s = s.trim();

        if (s.isEmpty() || "null".equalsIgnoreCase(s))
            return null;

        return LocalDateTime.parse(s);
    }

    /**
     * Escapes special characters before writing to CSV.
     */
    private String escape(String s) {

        if (s == null) return "";

        return s.replace("\n", "\\n")
                .replace(",", "，");
    }

    /**
     * Restores escaped text when reading CSV.
     */
    private String unescape(String s) {

        if (s == null) return "";

        return s.replace("\\n", "\n")
                .replace("，", ",");
    }
}