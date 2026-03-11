package service;

import dao.LeaveRequestDAO;
import model.*;
import roles.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * =============================================================
 * SERVICE CLASS: LeaveService
 * =============================================================
 *
 * PURPOSE
 * Handles all business logic related to employee leave requests
 * in the MotorPH Payroll System.
 *
 * RESPONSIBILITIES
 * - Submit leave requests
 * - Approve leave requests
 * - Reject leave requests
 * - Retrieve leave records for employees or HR/Admin
 *
 * ARCHITECTURE ROLE
 * DAO Layer
 * -> LeaveRequestDAO handles reading and writing leave data to CSV.
 *
 * Service Layer (THIS CLASS)
 * -> Validates leave requests
 * -> Enforces permission rules
 * -> Applies business logic (approval / rejection)
 *
 * Model Layer
 * -> LeaveRequest represents a leave application record.
 *
 * SECURITY
 * - Employees can only file their own leave
 * - HR/Admin can approve or reject leave
 * - HR cannot approve their own leave request
 */

public class LeaveService {

    /**
     * DAO responsible for accessing leave records from storage.
     */
    private final LeaveRequestDAO leaveDAO;

    /**
     * Constructor injecting LeaveRequestDAO dependency.
     */
    public LeaveService(LeaveRequestDAO leaveDAO) {
        this.leaveDAO = leaveDAO;
    }

    /**
     * Normalizes employee number format.
     *
     * Some employee numbers may contain the prefix "emp_".
     * This method removes the prefix and trims whitespace.
     */
    private String normalizeEmpNo(String raw) {

        if (raw == null) return null;

        String empNo = raw.trim();

        if (empNo.isEmpty()) return null;

        if (empNo.toLowerCase().startsWith("emp_"))
            empNo = empNo.substring(4);

        return empNo.trim();
    }

    /**
     * Submits a new leave request.
     *
     * This method validates permissions and required fields
     * before saving the leave request.
     */
    public LeaveRequest submitLeave(UserSession session,
                                    LeaveType type,
                                    LocalDate start,
                                    LocalDate end,
                                    String reason) throws Exception {

        User user = session.getUser();

        // Verify that the user has permission to file leave
        if (!(user.can(Permission.ADMIN_ALL) || user.can(Permission.FILE_LEAVE))) {
            throw new Exception("Access denied: cannot file leave.");
        }

        // Retrieve employee number linked to the account
        String empNoRaw = (session.getAccount() == null)
                ? null
                : session.getAccount().getEmployeeNumber();

        String empNo = normalizeEmpNo(empNoRaw);

        if (empNo == null || empNo.isEmpty()) {
            throw new Exception("No linked employee number for this account.");
        }

        /**
         * Validate required fields
         */
        if (start == null || end == null)
            throw new Exception("Start and end dates are required.");

        if (end.isBefore(start))
            throw new Exception("End date cannot be before start date.");

        if (type == null)
            throw new Exception("Leave type is required.");

        if (reason == null || reason.trim().isEmpty())
            throw new Exception("Reason is required.");

        /**
         * Create LeaveRequest object
         */
        LeaveRequest req = new LeaveRequest(
                UUID.randomUUID().toString(),  // unique request ID
                empNo,
                type,
                start,
                end,
                reason.trim(),
                LeaveStatus.PENDING,
                LocalDateTime.now(),
                null,
                "",
                "" // decisionReason initially empty
        );

        // Save request to CSV storage
        leaveDAO.create(req);

        return req;
    }

    /**
     * Approves a leave request.
     */
    public void approve(UserSession session, String requestId) throws Exception {

        decide(session, requestId, LeaveStatus.APPROVED, "");
    }

    /**
     * Reject method for backward compatibility.
     * Older code may call reject without specifying a reason.
     */
    public void reject(UserSession session, String requestId) throws Exception {

        reject(session, requestId, "");
    }

    /**
     * Rejects a leave request with a reason.
     */
    public void reject(UserSession session, String requestId, String rejectionReason) throws Exception {

        decide(session, requestId, LeaveStatus.REJECTED, rejectionReason);
    }

    /**
     * Core decision logic for approving or rejecting leave.
     */
    private void decide(UserSession session,
                        String requestId,
                        LeaveStatus newStatus,
                        String decisionReason) throws Exception {

        User user = session.getUser();

        // Check permission to manage leave
        if (!(user.can(Permission.ADMIN_ALL) || user.can(Permission.MANAGE_LEAVE))) {
            throw new Exception("Access denied: cannot manage leave.");
        }

        // Retrieve leave request
        LeaveRequest req = leaveDAO.findById(requestId)
                .orElseThrow(() -> new Exception("Leave request not found."));

        // Only pending requests can be decided
        if (req.getStatus() != LeaveStatus.PENDING) {
            throw new Exception("Only PENDING requests can be decided.");
        }

        /**
         * HR rule:
         * HR cannot approve or reject their own leave request.
         * Another HR must handle it.
         */
        String myEmpNo = normalizeEmpNo(
                session.getAccount() == null
                        ? null
                        : session.getAccount().getEmployeeNumber()
        );

        if (!user.can(Permission.ADMIN_ALL)
                && myEmpNo != null
                && myEmpNo.equals(req.getEmployeeNumber())) {

            throw new Exception("You cannot approve/reject your own leave request. Another HR must decide it.");
        }

        /**
         * If rejected, a rejection reason is required.
         */
        if (newStatus == LeaveStatus.REJECTED) {

            if (decisionReason == null || decisionReason.trim().isEmpty()) {
                throw new Exception("Rejection reason is required.");
            }

            req.setDecisionReason(decisionReason.trim());

        } else {

            // Clear decision reason if request is approved
            req.setDecisionReason("");
        }

        /**
         * Update decision fields
         */
        req.setStatus(newStatus);
        req.setDecisionAt(LocalDateTime.now());
        req.setDecidedBy(user.getUsername());

        // Save updated leave request
        leaveDAO.update(req);
    }

    /**
     * Retrieves leave records visible to the current session.
     *
     * HR/Admin can see all requests.
     * Employees can only see their own leave requests.
     */
    public List<LeaveRequest> listForSession(UserSession session) throws Exception {

        User user = session.getUser();

        // HR/Admin view all requests
        if (user.can(Permission.ADMIN_ALL) || user.can(Permission.MANAGE_LEAVE)) {
            return leaveDAO.findAll();
        }

        // Employee view only their own leave records
        String empNo = normalizeEmpNo(
                session.getAccount() == null
                        ? null
                        : session.getAccount().getEmployeeNumber()
        );

        if (empNo == null || empNo.isEmpty()) {
            throw new Exception("No linked employee number for this account.");
        }

        return leaveDAO.findByEmployeeNumber(empNo);
    }
}