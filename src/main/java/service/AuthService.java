package service;

import dao.CredentialDAO;
import dao.EmployeeDAO;
import model.*;
import roles.RoleFactory;
import roles.User;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * =============================================================
 * SERVICE CLASS: AuthService
 * =============================================================
 *
 * PURPOSE
 * This service handles user authentication and account updates
 * for the MotorPH Payroll System.
 *
 * RESPONSIBILITIES
 * - Validate login credentials
 * - Load user role and permissions
 * - Create a runtime session
 * - Update password
 * - Update username
 *
 * ARCHITECTURE ROLE
 * DAO Layer
 * -> CredentialDAO retrieves account credentials from CSV
 * -> EmployeeDAO retrieves employee information
 *
 * Service Layer (THIS CLASS)
 * -> Performs authentication logic
 * -> Validates passwords
 * -> Initializes user sessions
 *
 * Model Layer
 * -> UserSession holds active user information
 *
 * SECURITY FEATURES
 * - Password hashing using PasswordService
 * - Login failure tracking
 * - Optional audit logging
 */

public class AuthService {

    /**
     * DAO used to retrieve and update user credentials.
     */
    private final CredentialDAO credentialDAO;

    /**
     * DAO used to retrieve employee data linked to accounts.
     */
    private final EmployeeDAO employeeDAO;

    /**
     * Service used to hash and verify passwords securely.
     */
    private final PasswordService passwordService;

    /**
     * Optional audit logging service used to record
     * authentication events.
     */
    private final AuditService auditService;

    /**
     * Legacy constructor without audit logging.
     * This keeps compatibility with older code.
     */
    public AuthService(CredentialDAO credentialDAO,
                       EmployeeDAO employeeDAO,
                       PasswordService passwordService) {
        this(credentialDAO, employeeDAO, passwordService, null);
    }

    /**
     * Main constructor including optional audit logging.
     */
    public AuthService(CredentialDAO credentialDAO,
                       EmployeeDAO employeeDAO,
                       PasswordService passwordService,
                       AuditService auditService) {

        this.credentialDAO = credentialDAO;
        this.employeeDAO = employeeDAO;
        this.passwordService = passwordService;
        this.auditService = auditService;
    }

    /**
     * Handles the login process.
     *
     * STEPS
     * 1. Retrieve credentials from storage
     * 2. Verify password
     * 3. Load role-based user
     * 4. Link employee record
     * 5. Create runtime session
     */
    public UserSession login(String username, String rawPassword) throws Exception {

        // Retrieve credential record by username
        Optional<Credential> opt = credentialDAO.findByUsername(username);

        // If account does not exist
        if (opt.isEmpty()) {

            // Log failed attempt if audit service is available
            if (auditService != null) auditService.log(username, "LOGIN_FAILED", "User not found");

            throw new Exception("Invalid username or password.");
        }

        Credential credential = opt.get();

        /**
         * Verify password using PasswordService.
         */
        if (!passwordService.verify(rawPassword, credential.getPasswordHash())) {

            // Increment failed login counter
            credential.setFailedAttempts(credential.getFailedAttempts() + 1);
            credentialDAO.update(credential);

            // Log failed login
            if (auditService != null) auditService.log(username, "LOGIN_FAILED", "Wrong password");

            throw new Exception("Invalid username or password.");
        }

        /**
         * Successful login logic.
         */
        credential.setFailedAttempts(0);
        credential.setLastLogin(LocalDateTime.now());

        // Save updated login information
        credentialDAO.update(credential);

        /**
         * Create a role-based user object using RoleFactory.
         * This enables role-based permissions in the system.
         */
        User roleUser = RoleFactory.create(
                credential.getRole().name(),
                credential.getUsername()
        );

        /**
         * Create UserAccount bridge object.
         * This connects authentication credentials
         * to employee records.
         */
        UserAccount account = new UserAccount(
                credential.getUsername(),
                credential.getRole().name(),
                credential.getEmployeeNumber()
        );

        /**
         * Load employee record linked to the account.
         */
        Employee employee = null;

        if (credential.getEmployeeNumber() != null && !credential.getEmployeeNumber().isEmpty()) {
            employee = employeeDAO.findById(credential.getEmployeeNumber()).orElse(null);
        }

        /**
         * Register session as active in SessionManager.
         */
        SessionManager.login(credential.getUsername());

        /**
         * Record successful login event in audit logs.
         */
        if (auditService != null) {
            auditService.log(
                    credential.getUsername(),
                    "LOGIN_SUCCESS",
                    "Role=" + credential.getRole().name()
            );
        }

        /**
         * Return active user session containing:
         * - role-based user
         * - account info
         * - linked employee
         */
        return new UserSession(roleUser, account, employee);
    }

    /**
     * Updates the password for a user account.
     */
    public void updatePassword(String username, String newRawPassword) throws Exception {

        Optional<Credential> opt = credentialDAO.findByUsername(username);

        if (opt.isEmpty()) throw new Exception("User not found.");

        Credential credential = opt.get();

        // Hash new password before storing
        String hashed = passwordService.hash(newRawPassword);

        credential.setPasswordHash(hashed);

        // Update credential record
        credentialDAO.update(credential);

        // Log password change
        if (auditService != null) {
            auditService.log(username, "CHANGE_PASSWORD", "Password updated");
        }
    }

    /**
     * Updates the username for an existing account.
     */
    public void updateUsername(String currentUsername, String newUsername, String confirmPassword) throws Exception {

        // Validate inputs
        if (currentUsername == null || currentUsername.isBlank())
            throw new Exception("Current username required.");

        if (newUsername == null || newUsername.isBlank())
            throw new Exception("New username required.");

        if (newUsername.contains(" "))
            throw new Exception("Username cannot contain spaces.");

        // Retrieve current credential
        Credential cred = credentialDAO.findByUsername(currentUsername)
                .orElseThrow(() -> new Exception("User not found."));

        /**
         * Verify password before allowing username change.
         */
        if (!passwordService.verify(confirmPassword, cred.getPasswordHash())) {
            throw new Exception("Incorrect password.");
        }

        // Update username in storage
        credentialDAO.updateUsername(currentUsername, newUsername);

        /**
         * Update active session mapping
         * if the user is currently logged in.
         */
        SessionManager.logout(currentUsername);
        SessionManager.login(newUsername);

        // Log username change
        if (auditService != null) {
            auditService.log(newUsername, "CHANGE_USERNAME", "from=" + currentUsername);
        }
    }
}