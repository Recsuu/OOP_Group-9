/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.AttendanceDAO;
import model.AttendanceRecord;
import roles.User;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * =============================================================
 * SERVICE CLASS: AttendanceService
 * =============================================================
 *
 * PURPOSE
 * This service handles all attendance-related operations
 * for employees in the MotorPH Payroll System.
 *
 * RESPONSIBILITIES
 * - Record employee clock-in time
 * - Record employee clock-out time
 * - Retrieve today's attendance record
 * - Compute total hours worked
 *
 * ARCHITECTURE ROLE
 * DAO Layer
 * -> AttendanceDAO handles CSV storage of attendance records
 *
 * Service Layer (THIS CLASS)
 * -> Applies business logic before saving or retrieving data
 *
 * Model Layer
 * -> AttendanceRecord represents the attendance data structure
 *
 * NOTE
 * This service is used by the GUI panels such as:
 * - Employee Home Panel
 * - Attendance Monitoring
 */

public class AttendanceService {

    /**
     * DAO used to read/write attendance records
     * from the CSV storage.
     */
    private final AttendanceDAO attendanceDAO;

    /**
     * Constructor used to inject the AttendanceDAO dependency.
     * This keeps the service layer separated from storage logic.
     */
    public AttendanceService(AttendanceDAO attendanceDAO) {
        this.attendanceDAO = attendanceDAO;
    }

    /**
     * Utility method that normalizes employee numbers.
     *
     * PURPOSE
     * Ensures employee IDs are consistent regardless of format.
     *
     * Example conversions:
     * "emp_10001" -> "10001"
     * " 10001 "   -> "10001"
     */
    private String normalizeEmpNo(String raw) {

        // If null input, return null
        if (raw == null) return null;

        // Remove leading/trailing spaces
        String empNo = raw.trim();

        // If empty after trimming, treat as invalid
        if (empNo.isEmpty()) return null;

        // Remove prefix "emp_" if present
        if (empNo.toLowerCase().startsWith("emp_")) {
            empNo = empNo.substring(4);
        }

        // Return cleaned employee number
        return empNo.trim();
    }

    /**
     * Records employee clock-in time.
     *
     * PARAMETERS
     * user -> currently logged-in user
     * employeeNumber -> employee identifier
     *
     * FUNCTION
     * Saves the current time as the employee's clock-in record.
     */
    public void clockIn(User user, String employeeNumber) throws Exception {

        // Get today's date
        LocalDate today = LocalDate.now();

        // Get current system time
        LocalTime now = LocalTime.now();

        // Normalize employee ID
        String empNo = normalizeEmpNo(employeeNumber);

        // Validate employee ID
        if (empNo == null) throw new Exception("Employee number is required.");

        // Insert or update clock-in record in CSV
        attendanceDAO.upsertClockIn(empNo, today, now);
    }

    /**
     * Records employee clock-out time.
     *
     * PARAMETERS
     * user -> currently logged-in user
     * employeeNumber -> employee identifier
     *
     * FUNCTION
     * Saves the current time as the employee's clock-out record.
     */
    public void clockOut(User user, String employeeNumber) throws Exception {

        // Get today's date
        LocalDate today = LocalDate.now();

        // Get current system time
        LocalTime now = LocalTime.now();

        // Normalize employee ID
        String empNo = normalizeEmpNo(employeeNumber);

        // Validate employee ID
        if (empNo == null) throw new Exception("Employee number is required.");

        // Insert or update clock-out record in CSV
        attendanceDAO.upsertClockOut(empNo, today, now);
    }

    /**
     * Retrieves today's attendance record for an employee.
     *
     * RETURNS
     * AttendanceRecord if found, otherwise null.
     */
    public AttendanceRecord getTodayRecord(String employeeNumber) throws Exception {

        // Normalize employee ID
        String empNo = normalizeEmpNo(employeeNumber);

        // If invalid employee ID, return null
        if (empNo == null) return null;

        // Retrieve today's record from DAO
        return attendanceDAO.findToday(empNo, LocalDate.now()).orElse(null);
    }

    /**
     * Computes the total hours worked by an employee
     * across all recorded attendance entries.
     *
     * USED BY
     * Employee Home Panel dashboard tile.
     */
    public double getTotalHoursWorkedOverall(String employeeNumber) throws Exception {

        // Normalize employee ID
        String empNo = normalizeEmpNo(employeeNumber);

        // If invalid employee ID, return zero
        if (empNo == null) return 0.0;

        // Retrieve all attendance records then filter by employee number
        List<AttendanceRecord> records = attendanceDAO.findAll().stream()
                .filter(r -> empNo.equals(r.getEmployeeNumber()))
                .collect(Collectors.toList());

        // If no attendance records exist, return zero hours
        if (records.isEmpty()) return 0.0;

        /**
         * Reuse PayrollComputation logic to calculate total hours.
         * The hourlyRate parameter is set to 0 because we only need
         * the hoursWorked value from the computation.
         */
        return PayrollComputation.computeAttendance(records, 0).getHoursWorked();
    }
}