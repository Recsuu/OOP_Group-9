/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * =============================================================
 * MODEL CLASS: LeaveRequest
 * =============================================================
 *
 * PURPOSE
 * Represents a leave request filed by an employee in the MotorPH system.
 *
 * This class acts as a DATA MODEL that stores all information
 * related to employee leave requests.
 *
 * DATA STORED
 * - Request ID
 * - Employee Number
 * - Leave Type (Vacation, Sick Leave, etc.)
 * - Leave Date Range
 * - Reason for Leave
 * - Current Status (Pending / Approved / Rejected)
 * - Submission Timestamp
 * - Decision Timestamp
 * - Decision Maker (HR/Admin)
 * - Decision Reason (mainly used when rejected)
 *
 * NOTE
 * This model is stored in the CSV database through LeaveRequestDAO.
 *
 * DESIGN NOTES
 * - Compatibility constructors are included so old CSV data
 *   can still be loaded without breaking the system.
 */

public class LeaveRequest {

    // Unique identifier for the leave request
    private String requestId;

    // Employee number of the employee filing the leave
    private String employeeNumber;

    // Type of leave (Vacation Leave, Sick Leave, etc.)
    private LeaveType type;

    // Leave start date
    private LocalDate startDate;

    // Leave end date
    private LocalDate endDate;

    // Reason provided by the employee for filing leave
    private String reason;

    // Current status of the request (PENDING / APPROVED / REJECTED)
    private LeaveStatus status;

    // Timestamp when the request was submitted
    private LocalDateTime submittedAt;

    // Timestamp when HR/Admin made a decision
    private LocalDateTime decisionAt;

    // Username of the HR/Admin who decided
    private String decidedBy;

    // Reason provided when a leave request is rejected
    // Stored in CSV for audit/reference
    private String decisionReason;

    /**
     * =============================================================
     * CONSTRUCTOR (OLD VERSION - 10 columns)
     * =============================================================
     *
     * This constructor exists for backward compatibility.
     * Older CSV files may only contain 10 columns and
     * do not include decisionReason.
     */
    public LeaveRequest(String requestId, String employeeNumber, LeaveType type,
                        LocalDate startDate, LocalDate endDate, String reason,
                        LeaveStatus status, LocalDateTime submittedAt,
                        LocalDateTime decisionAt, String decidedBy) {

        // Calls the new constructor with an empty decision reason
        this(requestId, employeeNumber, type, startDate, endDate, reason,
                status, submittedAt, decisionAt, decidedBy, "");
    }

    /**
     * =============================================================
     * CONSTRUCTOR (NEW VERSION - 11 columns)
     * =============================================================
     *
     * Used when the system already supports decision reasons
     * for approved or rejected leave requests.
     */
    public LeaveRequest(String requestId, String employeeNumber, LeaveType type,
                        LocalDate startDate, LocalDate endDate, String reason,
                        LeaveStatus status, LocalDateTime submittedAt,
                        LocalDateTime decisionAt, String decidedBy,
                        String decisionReason) {

        this.requestId = requestId;
        this.employeeNumber = employeeNumber;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.status = status;
        this.submittedAt = submittedAt;
        this.decisionAt = decisionAt;
        this.decidedBy = decidedBy;

        // Prevent null values for decision reason
        this.decisionReason = (decisionReason == null) ? "" : decisionReason;
    }

    // =========================
    // GETTERS
    // =========================

    public String getRequestId() { return requestId; }
    public String getEmployeeNumber() { return employeeNumber; }
    public LeaveType getType() { return type; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getReason() { return reason; }
    public LeaveStatus getStatus() { return status; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public LocalDateTime getDecisionAt() { return decisionAt; }
    public String getDecidedBy() { return decidedBy; }

    /**
     * Returns the reason why the leave request was rejected or decided.
     */
    public String getDecisionReason() { return decisionReason; }

    /**
     * Backward compatibility getter.
     *
     * Some UI components expect the name "rejectReason".
     * Internally we store it as decisionReason.
     */
    public String getRejectReason() { return decisionReason; }

    // =========================
    // SETTERS
    // =========================

    public void setStatus(LeaveStatus status) {
        this.status = status;
    }
    public void setDecisionAt(LocalDateTime decisionAt) {
        this.decisionAt = decisionAt;
    }
    public void setDecidedBy(String decidedBy) {
        this.decidedBy = decidedBy;
    }

    /**
     * Sets the decision reason (used when rejecting leave requests).
     */
    public void setDecisionReason(String decisionReason) {
        this.decisionReason = (decisionReason == null) ? "" : decisionReason;
    }

    /**
     * Alias setter used by older UI code.
     * Reject reason is stored internally as decisionReason.
     */
    public void setRejectReason(String rejectReason) {
        setDecisionReason(rejectReason);
    }
}