/*
 * NetBeans auto-generated license header
 */
package dao;

/**
 * =============================================================
 * CLASS: EmployeeDAO
 * LAYER: DAO (Data Access Object)
 * =============================================================
 *
 * PURPOSE:
 * This class handles all data operations related to employees.
 * It reads and writes employee information stored in the CSV file.
 *
 * In the MotorPH Payroll System, the CSV file acts as the database.
 * This DAO class is responsible for managing that file safely.
 *
 * RESPONSIBILITIES:
 * - Load employee records
 * - Search employees by ID
 * - Insert new employees
 * - Update employee information
 * - Delete employee records
 * - Update salary information
 *
 * CSV FILE USED:
 * EmployeeDetails.csv
 *
 * IMPORTANT:
 * DAO classes should only handle file access and storage.
 * Business logic (payroll calculations, validations, etc.)
 * should be handled in the Service layer.
 *
 * USED BY:
 * - EmployeeService
 * - PayrollService
 * - HR Management modules
 *
 * AUTHOR: recsy
 */

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import model.Employee;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmployeeDAO {

    /**
     * Path of the employee CSV file.
     * Example: data/EmployeeDetails.csv
     */
    private final String filePath;

    /**
     * Formatter used to parse birthday values from the CSV file.
     *
     * Expected format inside CSV:
     * MM/dd/yyyy
     * Example: 10/11/1983
     */
    private static final DateTimeFormatter BDAY_FMT =
            DateTimeFormatter.ofPattern("M/d/yyyy");

    /**
     * Constructor that initializes the DAO with the
     * location of the employee CSV file.
     */
    public EmployeeDAO(String filePath) {
        this.filePath = filePath;
    }

    /**
     * =============================================================
     * METHOD: findAll()
     * =============================================================
     *
     * Reads all employee records from the CSV file
     * and converts each row into an Employee object.
     *
     * PROCESS:
     * 1. Open CSV file
     * 2. Skip the header row
     * 3. Read each employee row
     * 4. Convert row into Employee object
     * 5. Store in list
     * 6. Return the complete list
     *
     * @return list of employees
     */
    public List<Employee> findAll() throws IOException {

        List<Employee> list = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {

            String[] row;
            boolean header = true;

            while ((row = reader.readNext()) != null) {

                // Skip header row
                if (header) {
                    header = false;
                    continue;
                }

                // Skip invalid rows
                if (row.length < 19) continue;

                list.add(mapRowToEmployee(row));
            }
        }

        return list;
    }

    /**
     * =============================================================
     * METHOD: findById()
     * =============================================================
     *
     * Searches for an employee using the employee ID.
     *
     * @param employeeId employee number
     *
     * @return Optional employee record
     */
    public Optional<Employee> findById(String employeeId) throws IOException {

        return findAll().stream()
                .filter(e -> e.getEmployeeNumber().equals(employeeId))
                .findFirst();
    }

    /**
     * =============================================================
     * METHOD: insert()
     * =============================================================
     *
     * Adds a new employee record into the CSV file.
     *
     * PROCESS:
     * 1. Load all existing rows
     * 2. Check for duplicate employee ID
     * 3. Convert Employee object into CSV row
     * 4. Add row to list
     * 5. Rewrite the CSV file
     */
    public void insert(Employee employee) throws IOException {

        List<String[]> rows = readAllRawRows(); // includes header

        // Check if employee already exists
        for (int i = 1; i < rows.size(); i++) {

            String[] r = rows.get(i);

            if (r.length > 0 &&
                r[0].trim().equals(employee.getEmployeeNumber())) {

                throw new IOException(
                        "Employee already exists: "
                                + employee.getEmployeeNumber());
            }
        }

        rows.add(mapEmployeeToRow(employee));

        writeAllRawRows(rows);
    }

    /**
     * =============================================================
     * METHOD: update()
     * =============================================================
     *
     * Updates an existing employee record using the employee ID.
     */
    public void update(Employee employee) throws IOException {

        List<String[]> rows = readAllRawRows();

        boolean found = false;

        for (int i = 1; i < rows.size(); i++) {

            String[] r = rows.get(i);

            if (r.length > 0 &&
                r[0].trim().equals(employee.getEmployeeNumber())) {

                rows.set(i, mapEmployeeToRow(employee));
                found = true;
                break;
            }
        }

        if (!found)
            throw new IOException(
                    "Employee not found: "
                            + employee.getEmployeeNumber());

        writeAllRawRows(rows);
    }

    /**
     * =============================================================
     * METHOD: delete()
     * =============================================================
     *
     * Removes an employee record from the CSV file
     * using the employee ID.
     */
    public void delete(String employeeId) throws IOException {

        List<String[]> rows = readAllRawRows();

        boolean removed = false;

        for (int i = 1; i < rows.size(); i++) {

            String[] r = rows.get(i);

            if (r.length > 0 &&
                r[0].trim().equals(employeeId)) {

                rows.remove(i);
                removed = true;
                break;
            }
        }

        if (!removed)
            throw new IOException(
                    "Employee not found: "
                            + employeeId);

        writeAllRawRows(rows);
    }

    /**
     * =============================================================
     * METHOD: updateBasicSalary()
     * =============================================================
     *
     * Updates the basic salary of an employee.
     *
     * This is commonly used by Accounting users
     * when salary adjustments are required.
     *
     * NOTE:
     * Column index 13 represents Basic Salary
     * based on the EmployeeDetails.csv layout.
     */
    public void updateBasicSalary(String employeeNumber, double newBasicSalary) throws Exception {

        List<String[]> rows = new ArrayList<>();

        String headerLine;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            headerLine = br.readLine();

            if (headerLine == null) {
                throw new Exception("Employee file is empty.");
            }

            String line;

            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) continue;

                rows.add(line.split(",", -1));
            }
        }

        boolean found = false;

        for (String[] row : rows) {

            // Column 0 = Employee Number
            if (row[0].trim().equals(employeeNumber)) {

                // Column 13 = Basic Salary
                row[13] = String.valueOf(newBasicSalary);

                found = true;
                break;
            }
        }

        if (!found) {
            throw new Exception("Employee not found: " + employeeNumber);
        }

        // Rewrite CSV file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {

            bw.write(headerLine);
            bw.newLine();

            for (String[] r : rows) {

                bw.write(String.join(",", r));
                bw.newLine();
            }
        }
    }

    // =============================================================
    // HELPER METHODS
    // =============================================================

    /**
     * Converts a CSV row into an Employee object.
     */
    private Employee mapRowToEmployee(String[] c) {

        return new Employee(

                c[0].trim(),                           // Employee #
                c[1].trim(),                           // Last Name
                c[2].trim(),                           // First Name
                LocalDate.parse(c[3].trim(), BDAY_FMT),// Birthday
                c[4].trim(),                           // Address
                c[5].trim(),                           // Phone Number
                c[6].trim(),                           // SSS #
                c[7].trim(),                           // Philhealth #
                c[8].trim(),                           // TIN #
                c[9].trim(),                           // Pag-ibig #
                c[10].trim(),                          // Status
                c[11].trim(),                          // Position
                c[12].trim(),                          // Immediate Supervisor
                parseDouble(c[13]),                    // Basic Salary
                parseDouble(c[14]),                    // Rice Subsidy
                parseDouble(c[15]),                    // Phone Allowance
                parseDouble(c[16]),                    // Clothing Allowance
                parseDouble(c[17]),                    // Gross Semi-monthly Rate
                parseDouble(c[18])                     // Hourly Rate
        );
    }

    /**
     * Converts an Employee object into a CSV row.
     */
    private String[] mapEmployeeToRow(Employee e) {

        return new String[] {

                safe(e.getEmployeeNumber()),
                safe(e.getLastName()),
                safe(e.getFirstName()),
                (e.getBirthday() == null)
                        ? ""
                        : e.getBirthday().format(BDAY_FMT),
                safe(e.getAddress()),
                safe(e.getPhoneNumber()),
                safe(e.getSssNumber()),
                safe(e.getPhilhealthNumber()),
                safe(e.getTinNumber()),
                safe(e.getPagIbigNumber()),
                safe(e.getStatus()),
                safe(e.getPosition()),
                safe(e.getImmediateSupervisor()),
                String.valueOf(e.getBasicSalary()),
                String.valueOf(e.getRiceSubsidy()),
                String.valueOf(e.getPhoneAllowance()),
                String.valueOf(e.getClothingAllowance()),
                String.valueOf(e.getGrossSemiMonthlyRate()),
                String.valueOf(e.getHourlyRate())
        };
    }

    /**
     * Reads all rows from the CSV file including header.
     */
    private List<String[]> readAllRawRows() throws IOException {

        List<String[]> rows = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {

            String[] row;

            while ((row = reader.readNext()) != null) {

                rows.add(row);
            }
        }

        if (rows.isEmpty())
            throw new IOException("Employee file is empty.");

        return rows;
    }

    /**
     * Writes all rows back into the CSV file.
     */
    private void writeAllRawRows(List<String[]> rows) throws IOException {

        try (CSVWriter writer = new CSVWriter(

                new FileWriter(filePath),
                CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.DEFAULT_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END
        )) {

            writer.writeAll(rows);
        }
    }

    /**
     * Safely parses double values.
     * Empty values will default to 0.0
     */
    private double parseDouble(String s) {

        if (s == null) return 0.0;

        String t = s.trim();

        if (t.isEmpty()) return 0.0;

        return Double.parseDouble(t);
    }

    /**
     * Prevents null values when writing CSV.
     */
    private String safe(String s) {

        return (s == null) ? "" : s.trim();
    }
}