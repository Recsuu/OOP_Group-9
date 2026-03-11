/*
 * NetBeans auto-generated license header
 */
package model;

/**
 * =============================================================
 * CLASS: AttendanceRecord
 * LAYER: Model
 * =============================================================
 *
 * PURPOSE:
 * This class represents a single attendance record for an employee
 * in the MotorPH Payroll System.
 *
 * Each record stores the employee's clock-in and clock-out
 * information for a specific date.
 *
 * USED BY:
 * - AttendanceDAO (for reading and writing CSV records)
 * - AttendanceService (for attendance logic)
 * - PayrollService (for calculating hours worked)
 *
 * OOP PRINCIPLE USED:
 * Encapsulation
 * - All attributes are private
 * - Access is controlled through getters and setters
 *
 * DATA STORED:
 * employeeNumber → unique employee identifier
 * lastName       → employee last name
 * firstName      → employee first name
 * date           → attendance date
 * logIn          → time employee clocked in
 * logOut         → time employee clocked out
 */

import java.time.LocalDate;
import java.time.LocalTime;

public class AttendanceRecord {

    /**
     * Unique employee identifier.
     * This links the attendance record to the employee profile.
     */
    private String employeeNumber;

    /**
     * Employee last name.
     * Stored for easier display in attendance tables.
     */
    private String lastName;

    /**
     * Employee first name.
     */
    private String firstName;

    /**
     * Date of attendance.
     */
    private LocalDate date;

    /**
     * Time the employee clocked in.
     */
    private LocalTime logIn;

    /**
     * Time the employee clocked out.
     */
    private LocalTime logOut;

    /**
     * Constructor used when creating a full attendance record.
     *
     * @param employeeNumber employee ID
     * @param lastName employee last name
     * @param firstName employee first name
     * @param date attendance date
     * @param logIn clock-in time
     * @param logOut clock-out time
     */
    public AttendanceRecord(String employeeNumber, String lastName, String firstName,
                            LocalDate date, LocalTime logIn, LocalTime logOut) {

        this.employeeNumber = employeeNumber;
        this.lastName = lastName;
        this.firstName = firstName;
        this.date = date;
        this.logIn = logIn;
        this.logOut = logOut;
    }

    /**
     * Returns the employee number associated with this attendance record.
     */
    public String getEmployeeNumber() {
        return employeeNumber;
    }

    /**
     * Updates the employee number.
     */
    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    /**
     * Returns employee last name.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Updates employee last name.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Returns employee first name.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Updates employee first name.
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Returns the attendance date.
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Updates the attendance date.
     */
    public void setDate(LocalDate date) {
        this.date = date;
    }

    /**
     * Returns the clock-in time of the employee.
     */
    public LocalTime getLogIn() {
        return logIn;
    }

    /**
     * Updates the clock-in time.
     */
    public void setLogIn(LocalTime logIn) {
        this.logIn = logIn;
    }

    /**
     * Returns the clock-out time of the employee.
     */
    public LocalTime getLogOut() {
        return logOut;
    }

    /**
     * Updates the clock-out time.
     */
    public void setLogOut(LocalTime logOut) {
        this.logOut = logOut;
    }
}