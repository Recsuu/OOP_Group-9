package service;

import dao.UserActivityLogDAO;
import model.Permission;
import model.UserActivityLogEntry;
import roles.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * =============================================================
 * SERVICE CLASS: AuditService
 * =============================================================
 *
 * PURPOSE
 * This service handles system activity logging and audit tracking.
 *
 * In the MotorPH system, audit logs are used to record important
 * user actions such as:
 *
 * - Login attempts (success or failure)
 * - Password changes
 * - Ticket updates
 * - Other critical system activities
 *
 * ARCHITECTURE ROLE
 * DAO Layer
 * -> UserActivityLogDAO stores and retrieves logs from CSV.
 *
 * Service Layer (THIS CLASS)
 * -> Controls who can access audit logs.
 * -> Records system activity events.
 *
 * Model Layer
 * -> UserActivityLogEntry represents a single log record.
 *
 * SECURITY
 * Only users with the following permissions may access logs:
 * - ADMIN_ALL
 * - VIEW_AUDIT_LOGS
 */

public class AuditService {

    /**
     * DAO used to store and retrieve activity logs.
     */
    private final UserActivityLogDAO logDAO;

    /**
     * Constructor used to inject the log DAO dependency.
     */
    public AuditService(UserActivityLogDAO logDAO) {
        this.logDAO = logDAO;
    }

    /**
     * Records a new activity log entry.
     *
     * PARAMETERS
     * username -> user performing the action
     * action   -> type of action (LOGIN_SUCCESS, LOGIN_FAILED, etc.)
     * details  -> additional information related to the event
     *
     * NOTE
     * Logging failures should not break the application.
     */
    public void log(String username, String action, String details) {

        try {

            // Create a new log entry with the current timestamp
            logDAO.append(new UserActivityLogEntry(
                    LocalDateTime.now(),
                    username,
                    action,
                    details
            ));

        } catch (Exception ignore) {

            /**
             * If logging fails, the system will ignore the error
             * so the application continues running normally.
             */
        }
    }

    /**
     * Returns all audit log entries.
     *
     * Access is restricted to authorized users.
     */
    public List<UserActivityLogEntry> getAllLogs(User user) throws Exception {

        // Verify that the user has permission to view audit logs
        authorizeAuditAccess(user);

        // Retrieve all stored logs
        return logDAO.findAll();
    }

    /**
     * Returns the number of failed login attempts recorded today.
     *
     * Used for monitoring security activity.
     */
    public int getFailedLoginsToday(User user) throws Exception {

        // Validate permission
        authorizeAuditAccess(user);

        // Count today's failed login entries
        return logDAO.findTodayFailedLogins().size();
    }

    /**
     * Returns the most recent audit log entries.
     *
     * PARAMETERS
     * max -> maximum number of logs to return
     */
    public List<UserActivityLogEntry> getRecentLogs(User user, int max) throws Exception {

        // Validate permission
        authorizeAuditAccess(user);

        // Retrieve all logs
        List<UserActivityLogEntry> all = logDAO.findAll();

        // If the total logs are fewer than the limit, return all
        if (all.size() <= max) return all;

        // Otherwise return only the most recent subset
        return all.subList(0, max);
    }

    /**
     * Validates whether a user is authorized to view audit logs.
     *
     * Allowed roles:
     * - System Administrator
     * - Users with VIEW_AUDIT_LOGS permission
     */
    private void authorizeAuditAccess(User user) throws Exception {

        // Reject if no user session exists
        if (user == null) {
            throw new Exception("Access denied.");
        }

        // Check role-based permissions
        if (!(user.can(Permission.ADMIN_ALL) || user.can(Permission.VIEW_AUDIT_LOGS))) {
            throw new Exception("Access denied.");
        }
    }
}