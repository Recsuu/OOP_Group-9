package service;

import dao.SupportTicketDAO;
import model.Permission;
import model.SupportTicket;
import model.UserSession;
import roles.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * =============================================================
 * SERVICE CLASS: SupportTicketService
 * =============================================================
 *
 * PURPOSE
 * Handles business logic for the internal IT support ticket
 * system in the MotorPH Payroll System.
 *
 * RESPONSIBILITIES
 * - Allow employees to submit support tickets
 * - Allow IT/Admin to manage and update tickets
 * - Retrieve tickets based on user permissions
 * - Log ticket activity using AuditService
 *
 * ARCHITECTURE ROLE
 * DAO Layer
 * -> SupportTicketDAO reads/writes support tickets from CSV storage
 *
 * Service Layer (THIS CLASS)
 * -> Validates inputs
 * -> Enforces permission checks
 * -> Controls ticket visibility and updates
 *
 * Model Layer
 * -> SupportTicket represents a ticket record
 * -> UserSession represents the currently logged-in user
 *
 * SECURITY RULES
 * - Employees can only view their own tickets
 * - IT/Admin can view and manage all tickets
 */

public class SupportTicketService {

    /**
     * DAO responsible for reading and writing support ticket records.
     */
    private final SupportTicketDAO supportTicketDAO;

    /**
     * Audit service used to log system activity
     * such as ticket creation and updates.
     */
    private final AuditService auditService;

    /**
     * Constructor injecting dependencies.
     */
    public SupportTicketService(SupportTicketDAO supportTicketDAO, AuditService auditService) {
        this.supportTicketDAO = supportTicketDAO;
        this.auditService = auditService;
    }

    /**
     * Submits a new support ticket.
     *
     * Employees or admins can file tickets describing
     * technical issues or system problems.
     */
    public void submitTicket(UserSession session,
                             String title,
                             String category,
                             String description,
                             String imagePath,
                             String priority) throws Exception {

        /**
         * Validate session
         */
        if (session == null || session.getUser() == null) {
            throw new Exception("Session is required.");
        }

        User user = session.getUser();

        /**
         * Verify permission to file a support ticket
         */
        if (!(user.can(Permission.FILE_SUPPORT_TICKET) || user.can(Permission.ADMIN_ALL))) {
            throw new Exception("Access denied.");
        }

        /**
         * Validate required fields
         */
        if (title == null || title.trim().isEmpty()) throw new Exception("Issue title is required.");
        if (category == null || category.trim().isEmpty()) throw new Exception("Category is required.");
        if (description == null || description.trim().isEmpty()) throw new Exception("Description is required.");

        /**
         * Default priority if not provided
         */
        if (priority == null || priority.trim().isEmpty()) priority = "Medium";

        /**
         * Extract employee and user information from session
         */
        String empNo = session.getAccount() == null ? "" : session.getAccount().getEmployeeNumber();
        String username = session.getUser().getUsername();

        /**
         * Default employee name to username if employee record is missing
         */
        String employeeName = username;

        /**
         * Role or department information
         */
        String departmentOrRole = session.getAccount() == null ? "-" : session.getAccount().getRole();

        /**
         * If employee data exists, construct full name
         */
        if (session.getEmployee() != null) {

            String first = session.getEmployee().getFirstName() == null ? "" : session.getEmployee().getFirstName().trim();
            String last = session.getEmployee().getLastName() == null ? "" : session.getEmployee().getLastName().trim();

            String fullName = (first + " " + last).trim();

            if (!fullName.isEmpty()) {
                employeeName = fullName;
            }
        }

        /**
         * Create support ticket object
         */
        SupportTicket ticket = new SupportTicket(
                "TK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(), // unique ticket ID
                empNo == null ? "" : empNo,
                username,
                employeeName,
                departmentOrRole,
                title.trim(),
                category.trim(),
                description.trim(),
                imagePath == null ? "" : imagePath.trim(),
                priority.trim(),
                "Pending",
                LocalDateTime.now(),
                LocalDateTime.now(),
                "",
                "",
                null
        );

        /**
         * Save ticket to storage
         */
        supportTicketDAO.create(ticket);

        /**
         * Log ticket creation in audit logs
         */
        if (auditService != null) {
            auditService.log(username, "FILE_SUPPORT_TICKET", "ticketId=" + ticket.getTicketId());
        }
    }

    /**
     * Retrieves tickets visible to the current user session.
     *
     * IT/Admin can view all tickets.
     * Employees can only see tickets they submitted.
     */
    public List<SupportTicket> listTicketsForSession(UserSession session) throws Exception {

        if (session == null || session.getUser() == null) {
            throw new Exception("Session is required.");
        }

        User user = session.getUser();

        /**
         * Retrieve all tickets
         */
        List<SupportTicket> all = supportTicketDAO.findAll();

        /**
         * IT/Admin view all tickets
         */
        if (user.can(Permission.MANAGE_SUPPORT_TICKET) || user.can(Permission.ADMIN_ALL)) {
            return all;
        }

        /**
         * Regular employees only see their own tickets
         */
        String username = user.getUsername();

        return all.stream()
                .filter(t -> username.equalsIgnoreCase(t.getReportedByUsername()))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a specific support ticket by ID.
     */
    public SupportTicket getTicketById(UserSession session, String ticketId) throws Exception {

        if (session == null || session.getUser() == null) {
            throw new Exception("Session is required.");
        }

        /**
         * Retrieve ticket
         */
        SupportTicket ticket = supportTicketDAO.findById(ticketId)
                .orElseThrow(() -> new Exception("Ticket not found."));

        /**
         * Determine if the user has management permissions
         */
        boolean canManage = session.getUser().can(Permission.MANAGE_SUPPORT_TICKET)
                || session.getUser().can(Permission.ADMIN_ALL);

        /**
         * If not IT/Admin, user must be the ticket reporter
         */
        if (!canManage) {

            if (!session.getUser().getUsername().equalsIgnoreCase(ticket.getReportedByUsername())) {
                throw new Exception("Access denied.");
            }
        }

        return ticket;
    }

    /**
     * Updates ticket status and adds IT comments.
     * Used by IT staff or Admin users.
     */
    public void updateTicketByIT(UserSession session,
                                 String ticketId,
                                 String newStatus,
                                 String comment) throws Exception {

        /**
         * Validate session
         */
        if (session == null || session.getUser() == null) {
            throw new Exception("Session is required.");
        }

        User user = session.getUser();

        /**
         * Verify permission to manage support tickets
         */
        if (!(user.can(Permission.MANAGE_SUPPORT_TICKET) || user.can(Permission.ADMIN_ALL))) {
            throw new Exception("Access denied.");
        }

        /**
         * Validate required fields
         */
        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new Exception("Status is required.");
        }

        /**
         * Retrieve ticket
         */
        SupportTicket ticket = supportTicketDAO.findById(ticketId)
                .orElseThrow(() -> new Exception("Ticket not found."));

        /**
         * Update ticket status and timestamp
         */
        ticket.setStatus(newStatus.trim());
        ticket.setUpdatedAt(LocalDateTime.now());

        /**
         * Save IT comment if provided
         */
        if (comment != null && !comment.trim().isEmpty()) {

            ticket.setItComment(comment.trim());
            ticket.setCommentedBy(user.getUsername());
            ticket.setCommentedAt(LocalDateTime.now());
        }

        /**
         * Save updated ticket
         */
        supportTicketDAO.update(ticket);

        /**
         * Log ticket update event
         */
        if (auditService != null) {
            auditService.log(user.getUsername(), "UPDATE_SUPPORT_TICKET",
                    "ticketId=" + ticketId + ", status=" + newStatus);
        }
    }
}