package model;

/**
 * =============================================================
 * ENUM: Permission
 * =============================================================
 *
 * PURPOSE
 * Defines system permissions used in Role-Based
 * Access Control.
 */

public enum Permission {

    ADMIN_ALL,

    VIEW_EMPLOYEE,
    CRUD_EMPLOYEE,

    COMPUTE_PAYROLL,
    VIEW_PAYROLL,
    UPDATE_SALARY,

    VIEW_OWN_PAYSLIP,

    FILE_LEAVE,
    MANAGE_LEAVE,

    VIEW_AUDIT_LOGS,

    FILE_SUPPORT_TICKET,
    MANAGE_SUPPORT_TICKET
}