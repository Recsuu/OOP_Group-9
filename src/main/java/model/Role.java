package model;

/**
 * =============================================================
 * ENUM: Role
 * =============================================================
 *
 * PURPOSE
 * Defines the different user roles available in the
 * MotorPH Payroll System.
 *
 * These roles determine what features a user can access
 * through the Role-Based Access Control (RBAC) system.
 *
 * ROLE DEFINITIONS
 * -------------------------------------------------------------
 * ADMIN       → Full system access
 * IT          → System maintenance and support tickets
 * HR          → Employee records and leave management
 * ACCOUNTANT  → Payroll computation and salary updates
 * EMPLOYEE    → Basic employee access (view payslip, file leave)
 */

public enum Role {
    ADMIN,
    IT,
    HR,
    ACCOUNTANT,
    EMPLOYEE
}