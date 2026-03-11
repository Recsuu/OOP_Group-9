package model;

import java.time.LocalDateTime;

/**
 * =============================================================
 * CLASS: SupportTicket
 * LAYER: Model
 * =============================================================
 *
 * PURPOSE
 * Represents an IT support request submitted by an employee
 * within the MotorPH Payroll System.
 *
 * Employees can report technical issues related to the system
 * such as:
 * - Login problems
 * - Payroll errors
 * - System bugs
 * - Account access issues
 *
 * WORKFLOW
 * -------------------------------------------------------------
 * 1. Employee submits a support ticket
 * 2. Ticket status is set to "OPEN" or "PENDING"
 * 3. IT staff reviews the ticket
 * 4. IT staff can add a comment or solution
 * 5. Ticket status is updated (IN_PROGRESS, RESOLVED, CLOSED)
 *
 * DATA SOURCE
 * -------------------------------------------------------------
 * Tickets are stored in a CSV file and managed through
 * the SupportTicketDAO class.
 *
 * IMPORTANT DESIGN NOTE
 * -------------------------------------------------------------
 * Phase 1 implementation only stores the latest IT comment.
 * Future versions could support multiple comments or a
 * threaded conversation system.
 *
 * OOP PRINCIPLE USED
 * -------------------------------------------------------------
 * Encapsulation
 * - All fields are private
 * - Access controlled via getters and setters
 */

public class SupportTicket {

    /**
     * Unique identifier of the ticket.
     */
    private String ticketId;

    /**
     * Employee number associated with the ticket.
     * Links to Employee.employeeNumber.
     */
    private String employeeNumber;

    /**
     * Username of the person who reported the ticket.
     */
    private String reportedByUsername;

    /**
     * Full name of the employee who submitted the ticket.
     */
    private String employeeName;

    /**
     * Department or role of the employee.
     */
    private String departmentOrRole;

    /**
     * Title or short summary of the problem.
     */
    private String title;

    /**
     * Category of the support request.
     * Example: System Bug, Login Issue, Payroll Issue.
     */
    private String category;

    /**
     * Detailed description of the issue reported.
     */
    private String description;

    /**
     * Optional path to an uploaded screenshot or image.
     */
    private String imagePath;

    /**
     * Priority level of the ticket.
     * Example: LOW, MEDIUM, HIGH, URGENT.
     */
    private String priority;

    /**
     * Current status of the ticket.
     * Example: OPEN, IN_PROGRESS, RESOLVED, CLOSED.
     */
    private String status;

    /**
     * Timestamp when the ticket was submitted.
     */
    private LocalDateTime submittedAt;

    /**
     * Timestamp of the most recent update to the ticket.
     */
    private LocalDateTime updatedAt;

    /**
     * Latest IT comment on the ticket.
     */
    private String itComment;

    /**
     * Username of the IT staff who added the comment.
     */
    private String commentedBy;

    /**
     * Timestamp when the comment was added.
     */
    private LocalDateTime commentedAt;

    /**
     * Constructor used when creating or loading
     * a support ticket object.
     */
    public SupportTicket(String ticketId,
                         String employeeNumber,
                         String reportedByUsername,
                         String employeeName,
                         String departmentOrRole,
                         String title,
                         String category,
                         String description,
                         String imagePath,
                         String priority,
                         String status,
                         LocalDateTime submittedAt,
                         LocalDateTime updatedAt,
                         String itComment,
                         String commentedBy,
                         LocalDateTime commentedAt) {

        this.ticketId = ticketId;
        this.employeeNumber = employeeNumber;
        this.reportedByUsername = reportedByUsername;
        this.employeeName = employeeName;
        this.departmentOrRole = departmentOrRole;
        this.title = title;
        this.category = category;
        this.description = description;
        this.imagePath = imagePath;
        this.priority = priority;
        this.status = status;
        this.submittedAt = submittedAt;
        this.updatedAt = updatedAt;
        this.itComment = itComment;
        this.commentedBy = commentedBy;
        this.commentedAt = commentedAt;
    }

    // =========================
    // GETTERS AND SETTERS
    // =========================

    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }

    public String getEmployeeNumber() { return employeeNumber; }
    public void setEmployeeNumber(String employeeNumber) { this.employeeNumber = employeeNumber; }

    public String getReportedByUsername() { return reportedByUsername; }
    public void setReportedByUsername(String reportedByUsername) { this.reportedByUsername = reportedByUsername; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getDepartmentOrRole() { return departmentOrRole; }
    public void setDepartmentOrRole(String departmentOrRole) { this.departmentOrRole = departmentOrRole; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getItComment() { return itComment; }
    public void setItComment(String itComment) { this.itComment = itComment; }

    public String getCommentedBy() { return commentedBy; }
    public void setCommentedBy(String commentedBy) { this.commentedBy = commentedBy; }

    public LocalDateTime getCommentedAt() { return commentedAt; }
    public void setCommentedAt(LocalDateTime commentedAt) { this.commentedAt = commentedAt; }

}