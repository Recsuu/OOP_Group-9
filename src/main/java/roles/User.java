package roles;

import model.Permission;
import java.util.Set;

/**
 * =============================================================
 * ABSTRACT CLASS: User
 * =============================================================
 *
 * PURPOSE
 * Base class for all system users in the MotorPH Payroll System.
 *
 * This class represents the authenticated system user and
 * defines the role-based permission system used throughout
 * the application.
 *
 * OOP PRINCIPLES USED
 * -------------------------------------------------------------
 * Abstraction
 * - The getPermissions() method is abstract
 * - Each role must define its own permissions
 *
 * Inheritance
 * - AdminUser
 * - HRUser
 * - ITUser
 * - AccountantUser
 * - EmployeeUser
 *
 * Polymorphism
 * - Each subclass overrides getPermissions()
 *
 * ROLE-BASED ACCESS CONTROL (RBAC)
 * -------------------------------------------------------------
 * The system checks permissions using:
 *
 * user.can(Permission.SOME_PERMISSION)
 *
 * which verifies if the user has the required access.
 */

public abstract class User {

    /**
     * Unique username of the logged-in user.
     */
    private final String username;

    protected User(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    /**
     * Each role must define its permissions.
     */
    public abstract Set<Permission> getPermissions();

    /**
     * Checks if the user has a specific permission.
     *
     * Admin users automatically have all permissions.
     */
    public boolean can(Permission permission) {
        Set<Permission> perms = getPermissions();
        return perms.contains(permission) || perms.contains(Permission.ADMIN_ALL);
    }
}