package view;

import model.DeductionBreakdown;
import model.Payslip;
import model.Permission;
import model.UserSession;
import service.AuditService;
import service.EmployeeService;
import service.PayrollService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalDate;

public class PayrollPanel extends JPanel {

    private final UserSession session;
    private final PayrollService payrollService;
    private final EmployeeService employeeService;
    private final AuditService auditService;

    private final JTextField txtEmployeeId = new JTextField(12);


    private final JComboBox<String> cmbMonth = new JComboBox<>(new String[]{
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    });
    
    private final JSpinner spYear = new JSpinner(
            new SpinnerNumberModel(LocalDate.now().getYear(), 2000, 2100, 1)
    );
    private final JButton btnGenerate = new JButton("Generate Payslip");
    private final JButton btnUpdateSalary = new JButton("Update Salary");

    private final JLabel lblEmployeeName = new JLabel("Name: -");
    private final JLabel lblPosition = new JLabel("Position: -");
    private final JLabel lblPeriod = new JLabel("Period: -");

    // ✅ Summary tiles
    private final JLabel valNetPay = new JLabel("-");
    private final JLabel valGrossPay = new JLabel("-");
    private final JLabel valTotalDed = new JLabel("-");

    private final DecimalFormat df = new DecimalFormat("#,##0.00");

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Item", "Value"}, 0
    ) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };

    private final JTable table = new JTable(tableModel);

    // Theme
    private static final Color BG = new Color(230, 242, 255);
    private static final Color CARD = Color.WHITE;
    private static final Color BORDER = new Color(220, 220, 220);
    private static final Color PRIMARY = new Color(52, 120, 246);
    private static final Color SOFT_TILE = new Color(248, 250, 255);

    public PayrollPanel(UserSession session, PayrollService payrollService, EmployeeService employeeService) {
        this(session, payrollService, employeeService, null);
    }

    public PayrollPanel(UserSession session, PayrollService payrollService, EmployeeService employeeService, AuditService auditService) {
        this.session = session;
        this.payrollService = payrollService;
        this.employeeService = employeeService;
        this.auditService = auditService;

        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        setBackground(BG);

        add(buildTopCard(), BorderLayout.NORTH);
        add(buildMainCard(), BorderLayout.CENTER);

        btnGenerate.addActionListener(e -> onGenerate());
        btnUpdateSalary.addActionListener(e -> onUpdateSalary());

        applyRoleUI();
        initEmptyTable();
    }

    // ---------------- UI BUILDERS ----------------

    private JPanel buildTopCard() {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel title = new JLabel("Payroll");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(40, 40, 40));
        card.add(title, BorderLayout.NORTH);

        // Inputs row (LEFT) + actions row (RIGHT)
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);

        JPanel inputs = new JPanel(new GridBagLayout());
        inputs.setOpaque(false);

        styleField(txtEmployeeId);
        cmbMonth.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbMonth.setPreferredSize(new Dimension(120, 30));
        spYear.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        spYear.setPreferredSize(new Dimension(90, 30));
        
        JSpinner.NumberEditor yearEditor = new JSpinner.NumberEditor(spYear, "####");
        spYear.setEditor(yearEditor);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.gridy = 0;

        // Employee ID
        gc.gridx = 0; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        inputs.add(label("Employee ID:"), gc);

        gc.gridx = 1; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        inputs.add(txtEmployeeId, gc);

        // Month
        gc.gridx = 2; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        inputs.add(label("Month:"), gc);

        gc.gridx = 3;
        inputs.add(cmbMonth, gc);

        // Year
        gc.gridx = 4;
        inputs.add(label("Year:"), gc);

        gc.gridx = 5;
        inputs.add(spYear, gc);

        // Spacer so inputs stay left
        gc.gridx = 6; gc.weightx = 2; gc.fill = GridBagConstraints.HORIZONTAL;
        inputs.add(Box.createHorizontalGlue(), gc);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        stylePrimary(btnGenerate);
        styleOutline(btnUpdateSalary);

        btnGenerate.setPreferredSize(new Dimension(150, 34));
        btnUpdateSalary.setPreferredSize(new Dimension(140, 34));
        btnUpdateSalary.setVisible(
            session.getUser().can(Permission.UPDATE_SALARY)
        );

        actions.add(btnGenerate);
        actions.add(btnUpdateSalary);

        row.add(inputs, BorderLayout.CENTER);
        row.add(actions, BorderLayout.EAST);

        JPanel info = new JPanel(new GridLayout(1, 3, 12, 0));
        info.setOpaque(false);

        styleInfoLabel(lblEmployeeName);
        styleInfoLabel(lblPosition);
        styleInfoLabel(lblPeriod);

        info.add(lblEmployeeName);
        info.add(lblPosition);
        info.add(lblPeriod);

        JPanel body = new JPanel(new BorderLayout(10, 10));
        body.setOpaque(false);
        body.add(row, BorderLayout.NORTH);
        body.add(info, BorderLayout.SOUTH);

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildMainCard() {
        JPanel outer = new JPanel(new BorderLayout(12, 12));
        outer.setOpaque(false);

        JPanel card = new JPanel(new BorderLayout(12, 12));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        // Summary tiles
        JPanel summary = new JPanel(new GridLayout(1, 3, 12, 12));
        summary.setOpaque(false);
        summary.add(summaryTile("Net Pay", valNetPay, true));
        summary.add(summaryTile("Gross Pay", valGrossPay, false));
        summary.add(summaryTile("Total Deductions", valTotalDed, false));

        // Centered section title
        JLabel title = new JLabel("Payslip Breakdown", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(35, 35, 35));
        title.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        styleTable(table);

        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(Color.WHITE);
        sp.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(summary);
        top.add(Box.createVerticalStrut(14));
        top.add(title);
        top.add(Box.createVerticalStrut(8));

        card.add(top, BorderLayout.NORTH);
        card.add(sp, BorderLayout.CENTER);

        outer.add(card, BorderLayout.CENTER);
        return outer;
    }

    private JPanel summaryTile(String title, JLabel value, boolean emphasize) {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBackground(SOFT_TILE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setForeground(new Color(90, 90, 90));

        value.setFont(new Font("Segoe UI", Font.BOLD, emphasize ? 20 : 16));
        value.setForeground(new Color(30, 30, 30));

        p.add(t, BorderLayout.NORTH);
        p.add(value, BorderLayout.CENTER);
        return p;
    }

    private void styleTable(JTable tbl) {
        tbl.setModel(tableModel);
        tbl.setRowHeight(32);
        tbl.setFillsViewportHeight(true);
        tbl.setDefaultEditor(Object.class, null);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tbl.setShowGrid(false);
        tbl.setIntercellSpacing(new Dimension(0, 0));
        tbl.setFocusable(false);

        tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tbl.getTableHeader().setReorderingAllowed(false);
        tbl.getTableHeader().setBackground(new Color(245, 247, 250));
        tbl.getTableHeader().setForeground(new Color(40, 40, 40));

        // Cleaner column widths
        if (tbl.getColumnModel().getColumnCount() >= 2) {
            tbl.getColumnModel().getColumn(0).setPreferredWidth(260);
            tbl.getColumnModel().getColumn(1).setPreferredWidth(260);
        }

        // Custom renderer for cleaner layout + section rows
        javax.swing.table.DefaultTableCellRenderer renderer = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                lbl.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                lbl.setForeground(new Color(45, 45, 45));
                lbl.setBackground(Color.WHITE);

                String item = String.valueOf(table.getValueAt(row, 0));

                // Section rows
                if ("SUMMARY".equalsIgnoreCase(item)
                        || "DEDUCTIONS".equalsIgnoreCase(item)
                        || "NET PAY".equalsIgnoreCase(item)) {

                    lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    lbl.setForeground(new Color(25, 25, 25));
                    lbl.setBackground(new Color(248, 250, 255));

                    if (column == 1 && !"NET PAY".equalsIgnoreCase(item)) {
                        lbl.setText("");
                    }
                }

                // Blank spacer rows
                if (item == null || item.trim().isEmpty()) {
                    lbl.setText("");
                    lbl.setBackground(Color.WHITE);
                }

                // Right-align values column except section rows
                if (column == 1
                        && !"SUMMARY".equalsIgnoreCase(item)
                        && !"DEDUCTIONS".equalsIgnoreCase(item)
                        && !"NET PAY".equalsIgnoreCase(item)) {
                    lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                } else {
                    lbl.setHorizontalAlignment(SwingConstants.LEFT);
                }

                return lbl;
            }
        };

    tbl.getColumnModel().getColumn(0).setCellRenderer(renderer);
    tbl.getColumnModel().getColumn(1).setCellRenderer(renderer);
}

    // ---------------- STYLES ----------------

    private JLabel label(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(new Color(60, 60, 60));
        return l;
    }

    private void styleInfoLabel(JLabel l) {
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(new Color(40, 40, 40));
    }

    private void styleField(JTextField f) {
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setPreferredSize(new Dimension(160, 30));
    }

    private void stylePrimary(JButton b) {
        b.setFocusPainted(false);
        b.setBackground(PRIMARY);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

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

    // ---------------- RBAC ----------------

    private void applyRoleUI() {
        boolean canGeneratePayroll =
                session.getUser().can(model.Permission.COMPUTE_PAYROLL)
                || session.getUser().can(model.Permission.VIEW_PAYROLL)
                || session.getUser().can(model.Permission.ADMIN_ALL);

        boolean canUpdateSalary =
                session.getAccount() != null
                && "ACCOUNTANT".equalsIgnoreCase(session.getAccount().getRole());

        btnGenerate.setEnabled(canGeneratePayroll);
        btnUpdateSalary.setVisible(canUpdateSalary);
        btnUpdateSalary.setEnabled(canUpdateSalary);
    }

    // ---------------- DATA ----------------

    private void initEmptyTable() {
        // reset tiles
        valNetPay.setText("-");
        valGrossPay.setText("-");
        valTotalDed.setText("-");

        tableModel.setRowCount(0);
        tableModel.addRow(new Object[]{"SUMMARY", ""});
        tableModel.addRow(new Object[]{"Hours Worked", "-"});
        tableModel.addRow(new Object[]{"Overtime Hours", "-"});
        tableModel.addRow(new Object[]{"Gross Pay", "-"});
        tableModel.addRow(new Object[]{"", ""});
        tableModel.addRow(new Object[]{"DEDUCTIONS", ""});
        tableModel.addRow(new Object[]{"SSS", "-"});
        tableModel.addRow(new Object[]{"PhilHealth", "-"});
        tableModel.addRow(new Object[]{"Pag-IBIG", "-"});
        tableModel.addRow(new Object[]{"Withholding Tax", "-"});
        tableModel.addRow(new Object[]{"Late Deduction", "-"});
        tableModel.addRow(new Object[]{"Other", "-"});
        tableModel.addRow(new Object[]{"Total Deductions", "-"});
        tableModel.addRow(new Object[]{"", ""});
        tableModel.addRow(new Object[]{"NET PAY", "-"});
    }

    private void onGenerate() {
        try {
            String empId = txtEmployeeId.getText().trim();
            if (empId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter Employee ID.");
                return;
            }

            int month = cmbMonth.getSelectedIndex() + 1;
            int year = (Integer) spYear.getValue();

            LocalDate start = LocalDate.of(year, month, 1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

            // ✅ correct signature
            Payslip payslip = payrollService.generatePayslip(session, empId, start, end);

            if (auditService != null) {
                auditService.log(session.getUser().getUsername(), "GENERATE_PAYROLL",
                        "Generated payroll empId=" + empId + " start=" + start + " end=" + end);
            }

            lblEmployeeName.setText("Name: " + payslip.getEmployeeName());
            lblPosition.setText("Position: " + payslip.getPosition());
            lblPeriod.setText("Period: " + payslip.getPeriodStart() + " to " + payslip.getPeriodEnd());

            DeductionBreakdown d = payslip.getDeductions();
            double sss = (d == null) ? 0 : d.getSss();
            double phil = (d == null) ? 0 : d.getPhilHealth();
            double pagibig = (d == null) ? 0 : d.getPagIbig();
            double tax = (d == null) ? 0 : d.getTax();
            double late = (d == null) ? 0 : d.getLateDeduction();
            double other = (d == null) ? 0 : d.getOther();
            double totalDed = (d == null) ? 0 : d.getTotal();

            // ✅ tiles
            valGrossPay.setText(df.format(payslip.getGrossPay()));
            valTotalDed.setText(df.format(totalDed));
            valNetPay.setText(df.format(payslip.getNetPay()));

            // ✅ table
            tableModel.setRowCount(0);
            tableModel.addRow(new Object[]{"SUMMARY", ""});
            tableModel.addRow(new Object[]{"Hours Worked", df.format(payslip.getHoursWorked())});
            tableModel.addRow(new Object[]{"Overtime Hours", df.format(payslip.getOvertimeHours())});
            tableModel.addRow(new Object[]{"Gross Pay", df.format(payslip.getGrossPay())});
            tableModel.addRow(new Object[]{"", ""});

            tableModel.addRow(new Object[]{"DEDUCTIONS", ""});
            tableModel.addRow(new Object[]{"SSS", df.format(sss)});
            tableModel.addRow(new Object[]{"PhilHealth", df.format(phil)});
            tableModel.addRow(new Object[]{"Pag-IBIG", df.format(pagibig)});
            tableModel.addRow(new Object[]{"Withholding Tax", df.format(tax)});
            tableModel.addRow(new Object[]{"Late Deduction", df.format(late)});
            tableModel.addRow(new Object[]{"Other", df.format(other)});
            tableModel.addRow(new Object[]{"Total Deductions", df.format(totalDed)});
            tableModel.addRow(new Object[]{"", ""});

            tableModel.addRow(new Object[]{"NET PAY", df.format(payslip.getNetPay())});

        } catch (Exception ex) {
            initEmptyTable();
            lblEmployeeName.setText("Name: -");
            lblPosition.setText("Position: -");
            lblPeriod.setText("Period: -");
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onUpdateSalary() {
        try {
            boolean canUpdateSalary =
                    session.getAccount() != null
                    && "ACCOUNTANT".equalsIgnoreCase(session.getAccount().getRole());

            if (!canUpdateSalary) {
                JOptionPane.showMessageDialog(this, "Only Accounting can update salary.");
                return;
            }

            Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
            String defaultEmpNo = txtEmployeeId.getText().trim();

            UpdateSalaryDialog dialog = new UpdateSalaryDialog(owner, session.getUser(), employeeService, defaultEmpNo);
            dialog.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}