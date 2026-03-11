/*
 * NetBeans auto-generated license header
 */
package model;

/**
 * =============================================================
 * CLASS: Credential
 * LAYER: Model
 * =============================================================
 *
 * PURPOSE:
 * This class represents the login credentials of a system user
 * in the MotorPH Payroll System.
 *
 * It stores authentication-related information such as:
 * - Username
 * - Password hash
 * - Role of the user
 * - Linked employee number
 * - Failed login attempts
 * - Last login timestamp
 *
 * RELATIONSHIP WITH EMPLOYEE:
 * Each credential is linked to an employee using the
 * employeeNumber field.
 *
 * This allows the system to connect login accounts
 * with employee records.
 *
 * USED BY:
 * - CredentialDAO (for reading/writing credentials.csv)
 * - AuthenticationService (for login verification)
 * - Role-Based Access Control (RBAC)
 *
 * OOP PRINCIPLE USED:
 * Encapsulation
 * - Fields are private
 * - Access controlled using getters and setters
 */

import java.time.LocalDateTime;

public class Credential {

    /**
     * Username used for system login.
     */
    private String username;

    /**
     * Hashed version of the user's password.
     * Storing hashed passwords improves security
     * compared to storing plain text passwords.
     */
    private String passwordHash;

    /**
     * Role of the user in the system.
     * Determines access permissions (RBAC).
     *
     * Example roles:
     * ADMIN
     * HR
     * ACCOUNTING
     * IT
     * USER
     */
    private Role role;

    /**
     * Employee number associated with this account.
     * This links the credential to an Employee record.
     *
     * Example:
     * EmployeeDAO uses this to retrieve employee details.
     */
    private String employeeNumber;

    /**
     * Number of failed login attempts.
     * Used for security monitoring and potential account locking.
     */
    private int failedAttempts;

    /**
     * Timestamp of the user's last successful login.
     */
    private LocalDateTime lastLogin;

    /**
     * Constructor used to initialize credential information.
     *
     * @param username system username
     * @param passwordHash hashed password
     * @param role user role
     * @param employeeNumber linked employee ID
     * @param failedAttempts number of failed login attempts
     * @param lastLogin timestamp of last login
     */
    public Credential(String username,
                      String passwordHash,
                      Role role,
                      String employeeNumber,
                      int failedAttempts,
                      LocalDateTime lastLogin) {

        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.employeeNumber = employeeNumber;
        this.failedAttempts = failedAttempts;
        this.lastLogin = lastLogin;
    }

    /**
     * Returns the username used for login.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the stored password hash.
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Returns the user's system role.
     */
    public Role getRole() {
        return role;
    }

    /**
     * Returns the employee number linked to this credential.
     */
    public String getEmployeeNumber() {
        return employeeNumber;
    }

    /**
     * Returns the number of failed login attempts.
     */
    public int getFailedAttempts() {
        return failedAttempts;
    }

    /**
     * Updates the failed login attempt counter.
     */
    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    /**
     * Returns the timestamp of the last successful login.
     */
    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    /**
     * Updates the last login timestamp.
     */
    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    /**
     * Updates the stored password hash.
     * This is typically used when the user changes their password.
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}