package dao;

/**
 * =============================================================
 * CLASS: SupportTicketDAO
 * LAYER: DAO (Data Access Object)
 * =============================================================
 *
 * PURPOSE:
 * This class manages the storage and retrieval of IT support
 * tickets in the MotorPH Payroll System.
 *
 * The data source for support tickets is a CSV file.
 * This DAO class handles reading, writing, and updating
 * ticket records in that file.
 *
 * RESPONSIBILITIES:
 * - Load all support tickets
 * - Find support ticket by ticket ID
 * - Create new support tickets
 * - Update ticket status and IT comments
 *
 * CSV STRUCTURE:
 * ticketId,
 * employeeNumber,
 * reportedByUsername,
 * employeeName,
 * departmentOrRole,
 * title,
 * category,
 * description,
 * imagePath,
 * priority,
 * status,
 * submittedAt,
 * updatedAt,
 * itComment,
 * commentedBy,
 * commentedAt
 *
 * IMPORTANT:
 * This class only handles CSV storage operations.
 * Ticket workflow logic should be handled by the Service layer.
 */

import model.SupportTicket;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class SupportTicketDAO {

    /**
     * Path to the CSV file that stores support tickets.
     */
    private final String filePath;

    /**
     * Constructor that initializes DAO with the
     * location of the support ticket CSV file.
     */
    public SupportTicketDAO(String filePath) {
        this.filePath = filePath;
    }

    /**
     * =============================================================
     * METHOD: findAll()
     * =============================================================
     *
     * Loads all support tickets from the CSV file.
     *
     * PROCESS:
     * 1. Ensure the CSV file exists and has a header
     * 2. Read each line from the file
     * 3. Convert each row into a SupportTicket object
     * 4. Store tickets in a list
     * 5. Sort tickets by submission date (latest first)
     *
     * @return list of support tickets
     */
    public List<SupportTicket> findAll() throws IOException {

        ensureHeader();

        List<SupportTicket> list = new ArrayList<>();

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

                // Ensure row always has 16 columns
                String[] row = padTo16(c);

                // Convert CSV row into SupportTicket object
                list.add(new SupportTicket(
                        row[0],                       // ticketId
                        row[1],                       // employeeNumber
                        row[2],                       // reportedByUsername
                        unescape(row[3]),             // employeeName
                        unescape(row[4]),             // departmentOrRole
                        unescape(row[5]),             // title
                        row[6],                       // category
                        unescape(row[7]),             // description
                        row[8],                       // imagePath
                        row[9],                       // priority
                        row[10],                      // status
                        parseDateTime(row[11]),       // submittedAt
                        parseDateTime(row[12]),       // updatedAt
                        unescape(row[13]),            // IT comment
                        row[14],                      // commentedBy
                        parseDateTime(row[15])        // commentedAt
                ));
            }
        }

        /**
         * Sort tickets so that the most recently submitted
         * tickets appear first in the list.
         */
        list.sort(
                Comparator.comparing(
                        SupportTicket::getSubmittedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ).reversed()
        );

        return list;
    }

    /**
     * =============================================================
     * METHOD: findById()
     * =============================================================
     *
     * Searches for a support ticket using its ticket ID.
     *
     * @param ticketId unique ticket identifier
     * @return Optional support ticket
     */
    public Optional<SupportTicket> findById(String ticketId) throws IOException {

        return findAll().stream()
                .filter(t -> t.getTicketId().equals(ticketId))
                .findFirst();
    }

    /**
     * =============================================================
     * METHOD: create()
     * =============================================================
     *
     * Adds a new support ticket to the CSV file.
     */
    public void create(SupportTicket ticket) throws IOException {

        ensureHeader();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {

            bw.write(toCsvRow(ticket));
            bw.newLine();
        }
    }

    /**
     * =============================================================
     * METHOD: update()
     * =============================================================
     *
     * Updates an existing support ticket.
     *
     * Typically used when:
     * - IT adds comments
     * - Ticket status changes (Open → In Progress → Resolved)
     */
    public void update(SupportTicket updated) throws IOException {

        ensureHeader();

        List<String[]> rows = new ArrayList<>();
        String headerLine;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            headerLine = br.readLine();

            if (headerLine == null)
                throw new IOException("Empty support ticket file.");

            String line;

            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) continue;

                rows.add(line.split(",", -1));
            }
        }

        boolean found = false;

        for (int i = 0; i < rows.size(); i++) {

            String[] row = padTo16(rows.get(i));

            if (row[0].equals(updated.getTicketId())) {

                rows.set(i, toCsvCols(updated));

                found = true;
                break;
            }
        }

        if (!found)
            throw new IOException(
                    "Support ticket not found: "
                            + updated.getTicketId());

        // Rewrite updated CSV file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {

            bw.write(headerLine);
            bw.newLine();

            for (String[] row : rows) {

                bw.write(String.join(",", padTo16(row)));
                bw.newLine();
            }
        }
    }

    /**
     * =============================================================
     * HELPER METHODS
     * =============================================================
     */

    /**
     * Ensures the CSV file exists and contains the correct header.
     */
    private void ensureHeader() throws IOException {

        File f = new File(filePath);

        if (!f.exists()) {

            File parent = f.getParentFile();

            if (parent != null)
                parent.mkdirs();

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {

                bw.write("ticketId,employeeNumber,reportedByUsername,employeeName,departmentOrRole,title,category,description,imagePath,priority,status,submittedAt,updatedAt,itComment,commentedBy,commentedAt");
                bw.newLine();
            }

            return;
        }

        // If file exists but empty, add header
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {

            String first = br.readLine();

            if (first == null || first.trim().isEmpty()) {

                try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {

                    bw.write("ticketId,employeeNumber,reportedByUsername,employeeName,departmentOrRole,title,category,description,imagePath,priority,status,submittedAt,updatedAt,itComment,commentedBy,commentedAt");
                    bw.newLine();
                }
            }
        }
    }

    /**
     * Converts a SupportTicket object into CSV row string.
     */
    private String toCsvRow(SupportTicket t) {
        return String.join(",", toCsvCols(t));
    }

    /**
     * Converts SupportTicket object into CSV column array.
     */
    private String[] toCsvCols(SupportTicket t) {

        String[] out = new String[16];

        out[0] = safe(t.getTicketId());
        out[1] = safe(t.getEmployeeNumber());
        out[2] = safe(t.getReportedByUsername());
        out[3] = escape(t.getEmployeeName());
        out[4] = escape(t.getDepartmentOrRole());
        out[5] = escape(t.getTitle());
        out[6] = safe(t.getCategory());
        out[7] = escape(t.getDescription());
        out[8] = safe(t.getImagePath());
        out[9] = safe(t.getPriority());
        out[10] = safe(t.getStatus());
        out[11] = t.getSubmittedAt() == null ? "null" : t.getSubmittedAt().toString();
        out[12] = t.getUpdatedAt() == null ? "null" : t.getUpdatedAt().toString();
        out[13] = escape(t.getItComment());
        out[14] = safe(t.getCommentedBy());
        out[15] = t.getCommentedAt() == null ? "null" : t.getCommentedAt().toString();

        return out;
    }

    /**
     * Ensures every CSV row has exactly 16 columns.
     */
    private String[] padTo16(String[] row) {

        String[] out = new String[16];

        for (int i = 0; i < out.length; i++) {

            out[i] = (i < row.length) ? row[i] : "";
        }

        return out;
    }

    /**
     * Converts CSV date-time string into LocalDateTime object.
     */
    private LocalDateTime parseDateTime(String raw) {

        if (raw == null || raw.trim().isEmpty() || "null".equalsIgnoreCase(raw))
            return null;

        return LocalDateTime.parse(raw.trim());
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
     * Restores escaped text when reading CSV.
     */
    private String unescape(String s) {

        if (s == null) return "";

        return s.replace("\\n", "\n")
                .replace("，", ",");
    }

    /**
     * Prevents null values when writing CSV.
     */
    private String safe(String s) {

        return s == null ? "" : s.trim();
    }
}