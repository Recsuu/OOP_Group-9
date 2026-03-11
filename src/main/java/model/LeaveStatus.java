package model;

/**
 * =============================================================
 * ENUM: LeaveStatus
 * =============================================================
 *
 * PURPOSE
 * Represents the current status of a leave request.
 *
 * WORKFLOW
 * -------------------------------------------------------------
 * PENDING  → Waiting for HR approval
 * APPROVED → Leave request accepted
 * REJECTED → Leave request denied
 */

public enum LeaveStatus {

    PENDING,
    APPROVED,
    REJECTED

}