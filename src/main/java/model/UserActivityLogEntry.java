package model;

import java.time.LocalDateTime;

/**
 * =============================================================
 * CLASS: UserActivityLogEntry
 * =============================================================
 *
 * PURPOSE
 * Stores system activity logs for auditing and
 * security monitoring.
 *
 * Examples:
 * - LOGIN_SUCCESS
 * - LOGIN_FAILED
 * - PAYROLL_GENERATED
 */

public class UserActivityLogEntry {

    private final LocalDateTime timestamp;
    private final String username;
    private final String action;
    private final String details;

    public UserActivityLogEntry(LocalDateTime timestamp,
                                String username,
                                String action,
                                String details) {
        this.timestamp = timestamp;
        this.username = username;
        this.action = action;
        this.details = (details == null) ? "" : details;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public String getUsername() { return username; }
    public String getAction() { return action; }
    public String getDetails() { return details; }
}