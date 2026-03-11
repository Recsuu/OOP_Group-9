package service;

import dao.AttendanceDAO;
import dao.EmployeeDAO;
import model.AttendanceRecord;
import model.DeductionBreakdown;
import model.Employee;
import model.Payslip;
import model.Permission;
import model.UserSession;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * =============================================================
 * SERVICE CLASS: PayrollService
 * =============================================================
 *
 * PURPOSE
 * This service is responsible for generating payroll and
 * payslips for employees in the MotorPH system.
 *
 * ROLE IN THE ARCHITECTURE
 * DAO Layer
 *   -> EmployeeDAO retrieves employee information
 *   -> AttendanceDAO retrieves attendance records
 *
 * Service Layer (THIS CLASS)
 *   -> Validates permissions
 *   -> Retrieves data from DAO layer
 *   -> Sends data to PayrollComputation for calculations
 *   -> Builds a Payslip object
 *
 * View Layer (GUI)
 *   -> Calls this service to display payroll results
 *
 * SECURITY
 * This class also performs role-based access control (RBAC)
 * to make sure only authorized users can generate payroll.
 */

public class PayrollService {

    // DAO used to access employee data stored in CSV
    private final EmployeeDAO employeeDAO;

    // DAO used to access attendance records
    private final AttendanceDAO attendanceDAO;

    /**
     * Constructor used to inject DAO dependencies.
     * This keeps the service independent from storage implementation.
     */
    public PayrollService(EmployeeDAO employeeDAO, AttendanceDAO attendanceDAO) {
        this.employeeDAO = employeeDAO;
        this.attendanceDAO = attendanceDAO;
    }

    // =========================================================
    // PRIMARY METHOD USED BY THE GUI PANELS
    // =========================================================
    /**
     * Generates a payslip for a specific employee within a date range.
     *
     * PARAMETERS
     * session     -> active user session
     * employeeId  -> employee number
     * start       -> payroll start date
     * end         -> payroll end date
     *
     * RETURNS
     * Payslip object containing:
     * - hours worked
     * - overtime
     * - gross pay
     * - deductions
     * - net pay
     */
    public Payslip generatePayslip(UserSession session, String employeeId, LocalDate start, LocalDate end) throws Exception {

        // Validate session existence
        if (session == null || session.getUser() == null) {
            throw new Exception("Session is required.");
        }

        // Retrieve logged-in user
        var user = session.getUser();

        /**
         * Determine the role permissions of the current user.
         * Admin -> full system access
         * Payroll Staff -> can compute payroll
         * Employee -> can only view their own payslip
         */
        boolean isAdmin = user.can(Permission.ADMIN_ALL);
        boolean isPayrollStaff = user.can(Permission.COMPUTE_PAYROLL) || user.can(Permission.VIEW_PAYROLL);
        boolean isEmployeeViewer = user.can(Permission.VIEW_OWN_PAYSLIP);

        // If user has none of the required permissions, deny access
        if (!(isAdmin || isPayrollStaff || isEmployeeViewer)) {
            throw new Exception("Access denied.");
        }

        /**
         * Employee-level access restriction
         * Employees can only generate their own payslip.
         * The employee number is retrieved from the logged-in account.
         */
        if (isEmployeeViewer && !(isAdmin || isPayrollStaff)) {

            String ownEmployeeId = (session.getAccount() == null) ? null : session.getAccount().getEmployeeNumber();

            if (ownEmployeeId == null || ownEmployeeId.isBlank()) {
                throw new Exception("No linked employee number for this account.");
            }

            if (!ownEmployeeId.equals(employeeId)) {
                throw new Exception("Access denied. Employees can only view their own payslip.");
            }
        }

        // Validate parameters
        if (employeeId == null || employeeId.isBlank()) throw new Exception("Employee ID is required.");
        if (start == null || end == null) throw new Exception("Start and end dates are required.");
        if (end.isBefore(start)) throw new Exception("End date cannot be before start date.");

        /**
         * Retrieve employee information from EmployeeDAO.
         * This loads employee details from the CSV file.
         */
        Employee emp = employeeDAO.findById(employeeId)
                .orElseThrow(() -> new Exception("Employee not found: " + employeeId));

        /**
         * Retrieve attendance records from AttendanceDAO.
         * Only records within the requested payroll period are included.
         */
        List<AttendanceRecord> filtered = attendanceDAO.findAll().stream()
                .filter(r -> employeeId.equals(r.getEmployeeNumber()))
                .filter(r -> !r.getDate().isBefore(start) && !r.getDate().isAfter(end))
                .collect(Collectors.toList());

        // If no attendance exists, payroll cannot be generated
        if (filtered.isEmpty()) {
            throw new Exception("Cannot generate payslip: no attendance record found for this employee in the selected period.");
        }

        /**
         * Perform attendance calculations using PayrollComputation.
         * This returns hours worked, overtime, and late deductions.
         */
        PayrollComputation.AttendanceSummary summary =
                PayrollComputation.computeAttendance(filtered, emp.getHourlyRate());

        /**
         * Compute weekly allowance based on employee benefits.
         */
        double weeklyAllowance = PayrollComputation.computeWeeklyAllowance(emp);

        /**
         * Compute gross pay including:
         * - regular pay
         * - overtime pay
         * - allowances
         */
        double gross = PayrollComputation.computeGrossPay(
                summary.getHoursWorked(),
                summary.getOvertimeHours(),
                emp.getHourlyRate(),
                weeklyAllowance
        );

        /**
         * Compute mandatory deductions such as:
         * - SSS
         * - PhilHealth
         * - Pag-Ibig
         * - Withholding Tax
         * - Late penalties
         */
        DeductionBreakdown deductions =
                PayrollComputation.computeDeductions(gross, summary.getLateDeduction());

        // Final net salary after deductions
        double net = gross - deductions.getTotal();

        // Build employee full name
        String name = emp.getFirstName() + " " + emp.getLastName();

        /**
         * Create the Payslip object which will be returned
         * to the GUI for display.
         */
        return new Payslip(
                emp.getEmployeeNumber(),
                name,
                emp.getPosition(),
                start,
                end,
                summary.getHoursWorked(),
                summary.getOvertimeHours(),
                gross,
                deductions,
                net
        );
    }

    /**
     * Convenience overload that generates payroll using month and year.
     * Automatically calculates the first and last day of the month.
     */
    public Payslip generatePayslip(UserSession session, String employeeId, int month, int year) throws Exception {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return generatePayslip(session, employeeId, start, end);
    }

    // =========================================================
    // BACKWARD-COMPATIBLE METHODS
    // =========================================================
    /**
     * Older parts of the system may pass roles.User instead of UserSession.
     * These methods wrap the user into a temporary session
     * to maintain compatibility with older code.
     */

    public Payslip generatePayslip(roles.User user, String employeeId, LocalDate start, LocalDate end) throws Exception {
        UserSession temp = new UserSession(user, null, null);
        return generatePayslip(temp, employeeId, start, end);
    }

    public Payslip generatePayslip(roles.User user, String employeeId, int month, int year) throws Exception {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return generatePayslip(user, employeeId, start, end);
    }
}