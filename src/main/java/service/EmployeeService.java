/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

/**
 *
 * @author recsy
 */
import dao.EmployeeDAO;
import model.Employee;
import model.Permission;
import roles.User;

import java.util.List;

/**
 * =============================================================
 * SERVICE CLASS: EmployeeService
 * =============================================================
 *
 * PURPOSE
 * Handles all business logic related to employee records
 * in the MotorPH Payroll System.
 *
 * RESPONSIBILITIES
 * - Retrieve employee data
 * - Create employee records
 * - Update employee records
 * - Delete employee records
 * - Update employee salary
 * - Generate next employee ID
 *
 * ARCHITECTURE ROLE
 * DAO Layer
 * -> EmployeeDAO reads and writes employee data to CSV storage.
 *
 * Service Layer (THIS CLASS)
 * -> Applies validation rules and permission checks
 * -> Coordinates employee-related operations
 *
 * Model Layer
 * -> Employee represents the employee data structure.
 *
 * SECURITY
 * Most operations require CRUD_EMPLOYEE permission.
 * Salary updates are restricted to Accounting users.
 */

public class EmployeeService {

    /**
     * DAO used to access employee records stored in CSV.
     */
    private final EmployeeDAO employeeDAO;

    /**
     * Constructor injecting EmployeeDAO dependency.
     */
    public EmployeeService(EmployeeDAO employeeDAO) {
        this.employeeDAO = employeeDAO;
    }

    /**
     * Returns all employees.
     * Only users with CRUD_EMPLOYEE permission are allowed.
     */
    public List<Employee> getAllEmployees(User user) throws Exception {
        authorize(user, Permission.CRUD_EMPLOYEE);
        return employeeDAO.findAll();
    }

    /**
     * Returns total number of employees used by the
     * ACCOUNTANT dashboard tile "Total Payroll Active".
     */
    public int countEmployeesForPayroll(User user) throws Exception {

        // Only Payroll or Admin users may access this
        if (user == null || !(user.can(Permission.COMPUTE_PAYROLL) || user.can(Permission.ADMIN_ALL))) {
            throw new Exception("Access denied.");
        }

        return employeeDAO.findAll().size();
    }

    /**
     * Retrieves a specific employee by ID.
     */
    public Employee getEmployeeById(User user, String employeeId) throws Exception {

        // Permission validation
        authorize(user, Permission.CRUD_EMPLOYEE);

        return employeeDAO.findById(employeeId)
                .orElseThrow(() -> new Exception("Employee not found: " + employeeId));
    }

    /**
     * Adds a new employee record.
     */
    public void addEmployee(User user, Employee employee) throws Exception {

        // Permission validation
        authorize(user, Permission.CRUD_EMPLOYEE);

        // Prevent duplicate employee IDs
        boolean exists = employeeDAO.findById(employee.getEmployeeNumber()).isPresent();
        if (exists) throw new Exception("Employee ID already exists.");

        // Save new employee to CSV storage
        employeeDAO.insert(employee);
    }

    /**
     * Updates an existing employee record.
     * Used primarily by HR or Admin roles.
     */
    public void updateEmployee(User user, Employee employee) throws Exception {

        // Allow Admin or HR-level updates
        if (!(user.can(Permission.ADMIN_ALL) || user.can(Permission.CRUD_EMPLOYEE))) {
            throw new Exception("Access denied: cannot update employee.");
        }

        employeeDAO.update(employee);
    }

    /**
     * Deletes an employee record.
     */
    public void deleteEmployee(User user, String employeeId) throws Exception {

        // Permission validation
        authorize(user, Permission.CRUD_EMPLOYEE);

        employeeDAO.delete(employeeId);
    }

    /**
     * Updates an employee salary.
     * Only the Accounting department is allowed to modify salary.
     */
    public void updateEmployee(User user, String employeeId, double newSalary) throws Exception {

        // Restrict to Accountant role
        if (!(user instanceof roles.AccountantUser)) {
            throw new Exception("Access denied: only Accounting can update salary.");
        }

        // Validate salary value
        if (newSalary < 0) {
            throw new Exception("Salary must be non-negative.");
        }

        // Retrieve employee record
        Employee emp = employeeDAO.findById(employeeId)
                .orElseThrow(() -> new Exception("Employee not found: " + employeeId));

        // Update salary
        emp.setBasicSalary(newSalary);

        // Save updated record
        employeeDAO.update(emp);
    }

    /**
     * Alternative method for updating employee salary.
     * Used when employee number is provided directly.
     */
    public void updateBasicSalary(User user, String employeeNumber, double newBasicSalary) throws Exception {

        // Only Accounting role allowed
        if (!(user instanceof roles.AccountantUser)) {
            throw new Exception("Access denied: only Accounting can update salary.");
        }

        // Validate employee number
        if (employeeNumber == null || employeeNumber.trim().isEmpty()) {
            throw new Exception("Employee number is required.");
        }

        // Validate salary value
        if (newBasicSalary <= 0) {
            throw new Exception("Salary must be greater than 0.");
        }

        String empNo = employeeNumber.trim();

        // Retrieve employee record
        Employee emp = employeeDAO.findById(empNo)
                .orElseThrow(() -> new Exception("Employee not found: " + empNo));

        // Update salary
        emp.setBasicSalary(newBasicSalary);

        // Save update
        employeeDAO.update(emp);
    }

    /**
     * Retrieves the employee linked to the current session.
     * Used when a logged-in user wants to access their own profile.
     */
    public Employee getEmployeeForSession(model.UserSession session) throws Exception {

        if (session == null || session.getUser() == null)
            throw new Exception("Session required.");

        if (session.getAccount() == null)
            throw new Exception("Account required.");

        String empNo = session.getAccount().getEmployeeNumber();

        if (empNo == null || empNo.isBlank())
            throw new Exception("No employee number linked.");

        return employeeDAO.findById(empNo.trim())
                .orElseThrow(() -> new Exception("Employee not found: " + empNo));
    }

    /**
     * Generic permission validation helper method.
     */
    private void authorize(User user, Permission permission) throws Exception {

        if (user == null || !user.can(permission)) {
            throw new Exception("Access denied.");
        }
    }

    /**
     * Generates the next available employee ID.
     *
     * This scans existing employee records and finds
     * the highest numeric ID, then returns the next value.
     */
    public String generateNextEmployeeId(User user) throws Exception {

        // Permission validation
        authorize(user, Permission.CRUD_EMPLOYEE);

        List<Employee> employees = employeeDAO.findAll();

        int maxId = 0;

        for (Employee e : employees) {

            if (e == null || e.getEmployeeNumber() == null) continue;

            String raw = e.getEmployeeNumber().trim();
            if (raw.isEmpty()) continue;

            try {

                int id = Integer.parseInt(raw);

                if (id > maxId) {
                    maxId = id;
                }

            } catch (NumberFormatException ignored) {

                /**
                 * Skip employee IDs that are not numeric.
                 * This prevents system crashes if an invalid
                 * ID format appears in the CSV file.
                 */
            }
        }

        // Return next available ID
        return String.valueOf(maxId + 1);
    }
}