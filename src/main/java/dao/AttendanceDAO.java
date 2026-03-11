package dao;

/**
 * =============================================================
 * CLASS: AttendanceDAO
 * LAYER: DAO (Data Access Object)
 * =============================================================
 *
 * PURPOSE:
 * This class is responsible for reading and writing attendance
 * records from the CSV file used by the MotorPH Payroll System.
 *
 * The DAO layer directly communicates with the data source.
 * In this project, the data source is a CSV file instead of a database.
 *
 * RESPONSIBILITIES:
 * - Read attendance records from attendance.csv
 * - Find attendance entries for employees
 * - Insert or update clock-in and clock-out records
 * - Write updated data back to the CSV file
 *
 * IMPORTANT:
 * This class does NOT contain payroll logic.
 * It only handles file storage operations.
 *
 * USED BY:
 * - AttendanceService
 * - PayrollService
 * - Attendance Clock In / Clock Out features
 *
 * CSV FORMAT EXPECTED:
 * employeeNumber,lastName,firstName,date,logIn,logOut
 *
 * OOP PRINCIPLE USED:
 * Separation of Concerns
 * (DAO handles data access while Service handles business logic)
 *
 * AUTHOR: recsy
 */

import model.AttendanceRecord;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAO {

    /**
     * Path of the attendance CSV file.
     * Example: data/attendance.csv
     */
    private final String filePath;

    /**
     * Constructor used to initialize the DAO with the
     * location of the CSV attendance file.
     *
     * @param filePath location of attendance CSV
     */
    public AttendanceDAO(String filePath) {
        this.filePath = filePath;
    }

    /**
     * =============================================================
     * METHOD: findAll()
     * =============================================================
     *
     * Reads the entire attendance CSV file and converts each row
     * into an AttendanceRecord object.
     *
     * PROCESS:
     * 1. Open CSV file
     * 2. Skip header row
     * 3. Read each attendance row
     * 4. Convert CSV values into AttendanceRecord object
     * 5. Store objects inside a list
     * 6. Return the complete list
     *
     * @return list of attendance records
     */
    public List<AttendanceRecord> findAll() throws IOException {

        List<AttendanceRecord> list = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {

            String[] row;
            boolean header = true;

            while ((row = reader.readNext()) != null) {

                // Skip header row
                if (header) {
                    header = false;
                    continue;
                }

                // Skip incomplete rows
                if (row.length < 6) continue;

                try {

                    // Convert CSV row into AttendanceRecord object
                    list.add(new AttendanceRecord(
                            row[0].trim(),                        // employeeNumber
                            row[1].trim(),                        // lastName
                            row[2].trim(),                        // firstName
                            parseAttendanceDate(row[3]),          // date
                            parseAttendanceTime(row[4]),          // logIn
                            parseAttendanceTime(row[5])           // logOut
                    ));

                } catch (Exception ex) {

                    /**
                     * If a row contains invalid data format
                     * (ex: incorrect time/date format),
                     * it will simply be skipped.
                     */
                    continue;
                }
            }
        }

        return list;
    }

    /**
     * Formatter used to convert LocalDate to CSV date format.
     * Expected format inside CSV: MM/dd/yyyy
     */
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("MM/dd/yyyy");

    /**
     * Formatter used to convert LocalTime to CSV time format.
     * Expected format inside CSV: HH:mm
     */
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("H:mm");


    /**
     * =============================================================
     * METHOD: findToday()
     * =============================================================
     *
     * Searches attendance list for today's record of a specific employee.
     *
     * @param employeeNumber employee ID
     * @param date current date
     *
     * @return Optional attendance record
     */
    public Optional<AttendanceRecord> findToday(String employeeNumber, LocalDate date) throws Exception {

        for (AttendanceRecord r : findAll()) {

            if (r.getEmployeeNumber().equals(employeeNumber)
                    && r.getDate().equals(date)) {

                return Optional.of(r);
            }
        }

        return Optional.empty();
    }

    /**
     * =============================================================
     * METHOD: upsertClockIn()
     * =============================================================
     *
     * Records the clock-in time of an employee.
     * If an attendance row already exists, it updates the time.
     * If no row exists yet, it creates a new record.
     */
    public void upsertClockIn(String employeeNumber, LocalDate date, LocalTime time) throws Exception {
        upsert(employeeNumber, date, time, null);
    }

    /**
     * =============================================================
     * METHOD: upsertClockOut()
     * =============================================================
     *
     * Records the clock-out time of an employee.
     */
    public void upsertClockOut(String employeeNumber, LocalDate date, LocalTime time) throws Exception {
        upsert(employeeNumber, date, null, time);
    }

    /**
     * =============================================================
     * METHOD: upsert()
     * =============================================================
     *
     * This method updates or inserts attendance records.
     *
     * LOGIC:
     * 1. Read all rows from CSV
     * 2. Check if employee already has record for that date
     * 3. If found → update logIn or logOut
     * 4. If not found → create new row
     * 5. Rewrite updated CSV file
     *
     * CSV FORMAT:
     * empNo,lastName,firstName,date,logIn,logOut
     */
    private void upsert(String employeeNumber, LocalDate date, LocalTime logIn, LocalTime logOut) throws Exception {

        // Temporary list to hold CSV rows
        java.util.List<String[]> rows = new java.util.ArrayList<>();

        String[] header;

        // Read existing CSV rows
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {

            header = reader.readNext();

            if (header == null)
                throw new Exception("attendance.csv is empty.");

            String[] row;

            while ((row = reader.readNext()) != null) {
                rows.add(row);
            }
        }

        String targetDate = DATE_FMT.format(date);

        boolean found = false;

        // Search existing attendance row
        for (String[] r : rows) {

            if (r.length < 6) continue;

            String emp = r[0].trim();
            String d = r[3].trim();

            if (emp.equals(employeeNumber) && d.equals(targetDate)) {

                // Update existing row
                if (logIn != null)
                    r[4] = TIME_FMT.format(logIn);

                if (logOut != null)
                    r[5] = TIME_FMT.format(logOut);

                found = true;
                break;
            }
        }

        /**
         * If employee does not yet have attendance for the day,
         * create a new row.
         */
        if (!found) {

            String[] newRow = new String[6];

            newRow[0] = employeeNumber;
            newRow[1] = ""; // last name (optional)
            newRow[2] = ""; // first name (optional)
            newRow[3] = targetDate;
            newRow[4] = (logIn == null) ? "" : TIME_FMT.format(logIn);
            newRow[5] = (logOut == null) ? "" : TIME_FMT.format(logOut);

            rows.add(newRow);
        }

        /**
         * Rewrite the CSV file with updated rows.
         */
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {

            writer.writeNext(header);

            for (String[] r : rows) {

                // Ensure each row has exactly 6 columns
                String[] out = new String[6];

                for (int i = 0; i < 6; i++) {
                    out[i] = (i < r.length) ? r[i] : "";
                }

                writer.writeNext(out);
            }
        }
    }

    /**
     * Formatter used when parsing attendance time
     * from CSV text to LocalTime object.
     *
     * Accepts formats like:
     * 8:05
     * 08:05
     */
    private static final java.time.format.DateTimeFormatter ATT_TIME_FMT =
            java.time.format.DateTimeFormatter.ofPattern("H:mm");

    /**
     * Converts CSV time string into LocalTime object.
     *
     * Example input: "08:30"
     */
    private static java.time.LocalTime parseAttendanceTime(String raw) throws Exception {

        try {

            return java.time.LocalTime.parse(raw.trim(), ATT_TIME_FMT);

        } catch (Exception e) {

            throw new Exception(
                    "Invalid attendance time format: "
                            + raw
                            + " (expected H:mm)"
            );
        }
    }

    /**
     * Formatter used for converting CSV date
     * into LocalDate object.
     */
    private static final java.time.format.DateTimeFormatter ATT_DATE_FMT =
            java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy");

    /**
     * Converts CSV date string into LocalDate object.
     *
     * Example input: 03/11/2026
     */
    private static java.time.LocalDate parseAttendanceDate(String raw) throws Exception {

        try {

            return java.time.LocalDate.parse(raw.trim(), ATT_DATE_FMT);

        } catch (Exception e) {

            throw new Exception(
                    "Invalid attendance date format: "
                            + raw
                            + " (expected MM/dd/yyyy)"
            );
        }
    }

}