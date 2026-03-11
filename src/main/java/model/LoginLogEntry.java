package model;

import java.time.LocalDateTime;

/**
 * =============================================================
 * CLASS: LoginLogEntry
 * =============================================================
 *
 * PURPOSE
 * Stores login history of users for monitoring
 * system access.
 */

public class LoginLogEntry {

    private String username;
    private LocalDateTime timestamp;

    public LoginLogEntry(String username, LocalDateTime timestamp) {
        this.username = username;
        this.timestamp = timestamp;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

}