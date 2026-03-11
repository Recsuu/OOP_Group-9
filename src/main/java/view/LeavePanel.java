package view;

import model.*;
import service.AuditService;
import service.LeaveService;
import dao.EmployeeDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * =============================================================
 * LeavePanel
 * =============================================================
 * This panel manages leave requests in the MotorPH system.
 *
 * Features:
 * - Employees can file leave requests
 * - HR can approve or reject leave requests
 * - Requests can be filtered (ALL or PENDING)
 * - Displays leave request history
 *
 * Architecture Layer: VIEW
 * This panel communicates with:
 * - LeaveService → handles leave business logic
 * - AuditService → records system activity logs
 * - EmployeeDAO → retrieves employee details for display
 */
public class LeavePanel extends JPanel {

    /** Current logged-in user session */
    private final UserSession session;

    /** Service responsible for leave operations */
    private final LeaveService leaveService;

    /** Service responsible for recording audit logs */
    private final AuditService auditService;

    /** DAO used to retrieve employee information from CSV */
    private final EmployeeDAO employeeDAO = new EmployeeDAO("data/EmployeeDetails.csv");

    // ===============================
    // UI Components
    // ===============================

    /** Filter dropdown for leave status */
    private final JComboBox<String> cmbFilter = new JComboBox<>(new String[]{"ALL", "PENDING"});

    /** Button to open leave filing dialog */
    private final JButton btnFileLeave = new JButton("File a Leave");

    /** Refresh table button */
    private final JButton btnRefresh = new JButton("Refresh");

    /** Approve leave request button */
    private final JButton btnApprove = new JButton("Approve");

    /** Reject leave request button */
    private final JButton btnReject = new JButton("Reject");

    /**
     * Table model storing leave request records.
     * Cells are non-editable.
     */
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Request ID", "Emp#", "Type", "Start", "End", "Status", "Submitted", "Decided By", "Reject Reason"}, 0
    ) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };

    /** JTable displaying leave requests */
    private final JTable table = new JTable(model);

    // ===============================
    // Theme Colors
    // ===============================

    private static final Color BG = new Color(230, 242, 255);
    private static final Color CARD = Color.WHITE;
    private static final Color BORDER = new Color(220, 220, 220);
    private static final Color PRIMARY = new Color(52, 120, 246);

    /**
     * Legacy constructor kept for backward compatibility.
     */
    public LeavePanel(UserSession session, LeaveService leaveService) {
        this(session, leaveService, null);
    }

    /**
     * Main constructor.
     */
    public LeavePanel(UserSession session, LeaveService leaveService, AuditService auditService) {

        this.session = session;
        this.leaveService = leaveService;
        this.auditService = auditService;

        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        setBackground(BG);

        // Build UI sections
        add(buildTopCard(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
        add(buildBottomCard(), BorderLayout.SOUTH);

        // Register button events
        wireEvents();

        // Apply role-based visibility
        applyRoleUI();

        // Load table data
        reloadTable();
    }

    // ============================================================
    // UI BUILDERS
    // ============================================================

    /**
     * Builds the top control card.
     * Contains title, filter, refresh and file leave button.
     */
    private JPanel buildTopCard() {

        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(CARD);

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel title = new JLabel("Leave Requests");

        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(40, 40, 40));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JLabel lblFilter = new JLabel("Filter:");
        lblFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblFilter.setForeground(new Color(60, 60, 60));

        cmbFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbFilter.setPreferredSize(new Dimension(140, 28));

        stylePrimary(btnFileLeave);
        styleOutline(btnRefresh);

        right.add(lblFilter);
        right.add(cmbFilter);
        right.add(btnRefresh);
        right.add(btnFileLeave);

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        row.add(title, BorderLayout.WEST);
        row.add(right, BorderLayout.EAST);

        card.add(row, BorderLayout.CENTER);

        return card;
    }

    /**
     * Converts employee number stored in the audit log
     * into a readable employee name.
     */
    private String resolveDecidedByDisplay(String decidedBy) {

        if (decidedBy == null || decidedBy.isBlank()) {
            return "-";
        }

        try {

            // expected format: role_empNumber
            String[] parts = decidedBy.split("_", 2);

            if (parts.length < 2) {
                return decidedBy;
            }

            String empNo = parts[1].trim();

            Employee emp = employeeDAO.findById(empNo).orElse(null);

            if (emp == null) {
                return decidedBy;
            }

            String fullName = (safe(emp.getFirstName()) + " " + safe(emp.getLastName())).trim();

            return fullName.isEmpty() ? decidedBy : fullName;

        } catch (Exception ex) {

            return decidedBy;
        }
    }

    /**
     * Safe string utility.
     */
    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Builds the leave request table card.
     */
    private JPanel buildTableCard() {

        JPanel card = new JPanel(new BorderLayout(10, 10));

        card.setBackground(CARD);

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        table.setRowHeight(26);
        table.setFillsViewportHeight(true);

        // Disable editing
        table.setDefaultEditor(Object.class, null);

        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setReorderingAllowed(false);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane sp = new JScrollPane(table);

        sp.getViewport().setBackground(Color.WHITE);

        card.add(sp, BorderLayout.CENTER);

        JLabel hint = new JLabel("Tip: HR can approve/reject PENDING requests. You cannot approve/reject your own request.");

        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setForeground(new Color(90, 90, 90));

        card.add(hint, BorderLayout.SOUTH);

        return card;
    }

    /**
     * Builds bottom action buttons (Approve / Reject).
     */
    private JPanel buildBottomCard() {

        JPanel card = new JPanel(new BorderLayout());

        card.setBackground(CARD);

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        right.setOpaque(false);

        styleOutline(btnApprove);
        styleOutline(btnReject);

        right.add(btnApprove);
        right.add(btnReject);

        card.add(right, BorderLayout.EAST);

        return card;
    }

    /**
     * Styles primary button.
     */
    private void stylePrimary(JButton b) {

        b.setFocusPainted(false);
        b.setBackground(PRIMARY);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        b.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));

        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    /**
     * Styles outline button.
     */
    private void styleOutline(JButton b) {

        b.setFocusPainted(false);

        b.setBackground(Color.WHITE);

        b.setForeground(new Color(40, 40, 40));

        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));

        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // ============================================================
    // EVENT HANDLING
    // ============================================================

    /**
     * Connects UI buttons to event handlers.
     */
    private void wireEvents() {

        btnRefresh.addActionListener(e -> reloadTable());

        cmbFilter.addActionListener(e -> reloadTable());

        btnFileLeave.addActionListener(e -> openFileLeaveDialog());

        btnApprove.addActionListener(e -> decide(true));

        btnReject.addActionListener(e -> decide(false));
    }

    /**
     * Applies role-based UI permissions.
     */
    private void applyRoleUI() {

        boolean canManage = session.getUser().can(Permission.MANAGE_LEAVE);

        boolean canFile = session.getUser().can(Permission.FILE_LEAVE);

        btnFileLeave.setEnabled(canFile);

        btnApprove.setVisible(canManage);
        btnReject.setVisible(canManage);
    }

    // ---------------- FILE LEAVE (MODAL) ----------------

    private void openFileLeaveDialog() {
        String myEmpNo = (session.getAccount() == null) ? "" : session.getAccount().getEmployeeNumber();
        if (myEmpNo == null || myEmpNo.isBlank()) {
            JOptionPane.showMessageDialog(this, "No Employee # linked to your account.");
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "File a Leave", true);
        dialog.setLayout(new BorderLayout(12, 12));
        dialog.setSize(560, 420);
        dialog.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(BG);
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints lblGc = new GridBagConstraints();
        lblGc.insets = new Insets(8, 6, 8, 10);
        lblGc.anchor = GridBagConstraints.EAST;
        lblGc.gridx = 0;
        lblGc.weightx = 0;

        GridBagConstraints fldGc = new GridBagConstraints();
        fldGc.insets = new Insets(8, 0, 8, 6);
        fldGc.anchor = GridBagConstraints.WEST;
        fldGc.gridx = 1;
        fldGc.weightx = 1;
        fldGc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtEmp = new JTextField(18);
        txtEmp.setText(myEmpNo);
        txtEmp.setEnabled(false);
        txtEmp.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JComboBox<LeaveType> cmb = new JComboBox<>(LeaveType.values());
        cmb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmb.setPreferredSize(new Dimension(220, 30));

        JTextField txtS = new JTextField(18);
        JTextField txtE = new JTextField(18);
        txtS.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtE.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JTextArea txtR = new JTextArea(6, 26);
        txtR.setLineWrap(true);
        txtR.setWrapStyleWord(true);
        txtR.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JScrollPane reasonSp = new JScrollPane(txtR);
        reasonSp.getViewport().setBackground(Color.WHITE);
        reasonSp.setPreferredSize(new Dimension(320, 130));

        int row = 0;

        lblGc.gridy = row; fldGc.gridy = row;
        form.add(lbl("Employee #:"), lblGc);
        form.add(txtEmp, fldGc);
        row++;

        lblGc.gridy = row; fldGc.gridy = row;
        form.add(lbl("Leave Type:"), lblGc);
        form.add(cmb, fldGc);
        row++;

        lblGc.gridy = row; fldGc.gridy = row;
        form.add(lbl("Start (yyyy-mm-dd):"), lblGc);
        form.add(txtS, fldGc);
        row++;

        lblGc.gridy = row; fldGc.gridy = row;
        form.add(lbl("End (yyyy-mm-dd):"), lblGc);
        form.add(txtE, fldGc);
        row++;

        GridBagConstraints reasonLblGc = (GridBagConstraints) lblGc.clone();
        reasonLblGc.gridy = row;
        reasonLblGc.anchor = GridBagConstraints.NORTHEAST;

        GridBagConstraints reasonFldGc = (GridBagConstraints) fldGc.clone();
        reasonFldGc.gridy = row;
        reasonFldGc.fill = GridBagConstraints.BOTH;
        reasonFldGc.weighty = 1;

        form.add(lbl("Reason:"), reasonLblGc);
        form.add(reasonSp, reasonFldGc);

        card.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton btnCancel = new JButton("Cancel");
        JButton btnSubmit = new JButton("Submit");
        styleOutline(btnCancel);
        stylePrimary(btnSubmit);

        btnCancel.setPreferredSize(new Dimension(110, 34));
        btnSubmit.setPreferredSize(new Dimension(110, 34));

        actions.add(btnCancel);
        actions.add(btnSubmit);

        JPanel cardBottom = new JPanel(new BorderLayout());
        cardBottom.setOpaque(false);
        cardBottom.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        cardBottom.add(actions, BorderLayout.EAST);

        card.add(cardBottom, BorderLayout.SOUTH);

        root.add(card, BorderLayout.CENTER);
        dialog.setContentPane(root);

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSubmit.addActionListener(e -> {
            try {
                LeaveType type = (LeaveType) cmb.getSelectedItem();
                LocalDate start = LocalDate.parse(txtS.getText().trim());
                LocalDate end = LocalDate.parse(txtE.getText().trim());
                String reason = txtR.getText().trim();

                leaveService.submitLeave(session, type, start, end, reason);

                if (auditService != null) {
                    auditService.log(session.getUser().getUsername(), "FILE_LEAVE",
                            "Filed leave (" + type + ") " + start + " to " + end);
                }

                JOptionPane.showMessageDialog(dialog, "Leave request submitted.");
                dialog.dispose();
                reloadTable();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }

    private JLabel lbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(new Color(60, 60, 60));
        return l;
    }

    // ---------------- TABLE ----------------

    private void reloadTable() {
        try {
            model.setRowCount(0);

            List<LeaveRequest> all = leaveService.listForSession(session);

            boolean pendingOnly = "PENDING".equalsIgnoreCase(String.valueOf(cmbFilter.getSelectedItem()));
            List<LeaveRequest> rows;

            if (!pendingOnly) {
                rows = all;
            } else {
                rows = new ArrayList<>();
                for (LeaveRequest r : all) {
                    if (r.getStatus() == LeaveStatus.PENDING) {
                        rows.add(r);
                    }
                }
            }

            for (LeaveRequest r : rows) {
                String decidedByDisplay = resolveDecidedByDisplay(r.getDecidedBy());

                model.addRow(new Object[]{
                        r.getRequestId(),
                        r.getEmployeeNumber(),
                        r.getType(),
                        r.getStartDate(),
                        r.getEndDate(),
                        r.getStatus(),
                        r.getSubmittedAt(),
                        decidedByDisplay,
                        r.getRejectReason() == null ? "" : r.getRejectReason()
                });
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------------- APPROVE/REJECT ----------------

    private void decide(boolean approve) {
        try {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a request first.");
                return;
            }

            String requestId = String.valueOf(model.getValueAt(row, 0));
            String status = String.valueOf(model.getValueAt(row, 5));

            if (!"PENDING".equalsIgnoreCase(status)) {
                JOptionPane.showMessageDialog(this, "Only PENDING requests can be decided.");
                return;
            }

            if (approve) {
                leaveService.approve(session, requestId);

                if (auditService != null) {
                    auditService.log(session.getUser().getUsername(), "APPROVE_LEAVE", "Approved requestId=" + requestId);
                }

                JOptionPane.showMessageDialog(this, "Approved.");
            } else {
                String reason = JOptionPane.showInputDialog(this, "Enter reject reason (required):");
                if (reason == null || reason.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Reject reason is required.");
                    return;
                }

                leaveService.reject(session, requestId, reason.trim());

                if (auditService != null) {
                    auditService.log(session.getUser().getUsername(), "REJECT_LEAVE",
                            "Rejected requestId=" + requestId + " reason=" + reason.trim());
                }

                JOptionPane.showMessageDialog(this, "Rejected.");
            }

            reloadTable();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}