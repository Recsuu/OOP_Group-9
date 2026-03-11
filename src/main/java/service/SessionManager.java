package service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * =============================================================
 * SERVICE CLASS: SessionManager
 * =============================================================
 * PURPOSE
 * Keeps track of currently logged-in users.
 *
 * FEATURES
 * - Register active sessions
 * - Remove sessions on logout
 * - Retrieve active user count
 *
 * NOTE
 * This implementation stores sessions in memory.
 */

public class SessionManager {

    // Stores active usernames
    private static final Set<String> activeUsers = new HashSet<>();

    private SessionManager() {}

    /**
     * Registers a new login session.
     */
    public static synchronized void login(String username) {
        if (username != null && !username.isBlank()) {
            activeUsers.add(username.trim());
        }
    }

    /**
     * Removes a session when user logs out.
     */
    public static synchronized void logout(String username) {
        if (username != null && !username.isBlank()) {
            activeUsers.remove(username.trim());
        }
    }

    /**
     * Returns the number of active sessions.
     */
    public static synchronized int getActiveSessionCount() {
        return activeUsers.size();
    }

    /**
     * Returns a snapshot of currently active users.
     */
    public static synchronized Set<String> getActiveUsersSnapshot() {
        return Collections.unmodifiableSet(new HashSet<>(activeUsers));
    }
}