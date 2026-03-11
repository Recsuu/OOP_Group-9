package view;

import model.Payslip;
import model.UserSession;
import service.PayrollService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/**
 * ============================================================
 * MyPayslipPanel
 * ============================================================
 *
 * VIEW LAYER PANEL
 *
 * This panel allows employees to:
 * 1. Select a payroll period (cutoff, month, year)
 * 2. Generate their payslip
 * 3. View salary breakdown (gross pay, deductions, net pay)
 * 4. Print the payslip
 * 5. Simulate sending the payslip via email
 *
 * The panel communicates with:
 * - PayrollService → handles payroll computation
 * - UserSession → identifies the currently logged-in user
 *
 * Architecture:
 * VIEW → SERVICE → DAO → CSV
 */

public class MyPayslipPanel extends JPanel {

    /** Current logged-in user session */
    private final UserSession session;

    /** Service responsible for payroll calculations */
    private final PayrollService payrollService;

    // ===============================
    // THEME COLORS
    // ===============================

    private static final Color BG = new Color(230, 242, 255);
    private static final Color CARD = Color.WHITE;
    private static final Color BORDER = new Color(220, 220, 220);
    private static final Color PRIMARY = new Color(52, 120, 246);

    /** Date formatter for period display */
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** Date formatter for timestamp display */
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /** Philippine currency formatter */
    private static final NumberFormat MONEY =
            NumberFormat.getCurrencyInstance(new Locale("en", "PH"));

    // ===============================
    // UI CONTROLS (Payroll Filters)
    // ===============================

    /** Cutoff selection (1–15 or 16–end) */
    private final JComboBox<String> cmbCutoff = new JComboBox<>(new String[]{
            "Cutoff: 1 - 15",
            "Cutoff: 16 - End of Month"
    });

    /** Month selection */
    private final JComboBox<String> cmbMonth = new JComboBox<>(new String[]{
            "01 Jan", "02 Feb", "03 Mar", "04 Apr", "05 May", "06 Jun",
            "07 Jul", "08 Aug", "09 Sep", "10 Oct", "11 Nov", "12 Dec"
    });

    /** Year selector */
    private final JSpinner spYear =
            new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear(), 2000, 2100, 1));

    /** Label showing computed payroll period */
    private final JLabel lblPeriod = new JLabel("Period: -");

    /** Buttons */
    private final JButton btnGenerate = new JButton("Generate");
    private final JButton btnPrint = new JButton("Print");
    private final JButton btnSendEmail = new JButton("Send to Email");

    // ===============================
    // LAST GENERATED PAYSLIP DATA
    // ===============================

    private Payslip lastPayslip = null;
    private String lastReference = null;
    private String lastCutoffLabel = null;
    private LocalDateTime lastGeneratedAt = null;

    /** Text version of the payslip used for printing */
    private String printablePayslipText = "";

    // ===============================
    // MODERN OUTPUT COMPONENTS
    // ===============================

    /** Summary values */
    private final JLabel valNetPay = new JLabel("-");
    private final JLabel valGrossPay = new JLabel("-");
    private final JLabel valTotalDed = new JLabel("-");

    /** Employee information fields */
    private final JLabel valEmpId = new JLabel("-");
    private final JLabel valName = new JLabel("-");
    private final JLabel valPosition = new JLabel("-");
    private final JLabel valPeriod = new JLabel("-");
    private final JLabel valRef = new JLabel("-");
    private final JLabel valGenerated = new JLabel("-");
    private final JLabel valCutoff = new JLabel("-");

    /** Work summary fields */
    private final JLabel valHours = new JLabel("-");
    private final JLabel valOT = new JLabel("-");

    /** Deductions table */
    private final JTable tblDeductions = new JTable();

    /** Table model for deductions */
    private final DefaultTableModel dedModel =
            new DefaultTableModel(new Object[]{"Deduction", "Amount"}, 0) {

                /** Prevents table editing */
                @Override public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };

    /**
     * Constructor
     * Initializes layout and loads default values.
     */
    public MyPayslipPanel(UserSession session, PayrollService payrollService) {

        this.session = session;
        this.payrollService = payrollService;

        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        setBackground(BG);

        add(buildTopCard(), BorderLayout.NORTH);
        add(buildOutputCard(), BorderLayout.CENTER);

        wireEvents();

        // Default month = current month
        cmbMonth.setSelectedIndex(LocalDate.now().getMonthValue() - 1);

        refreshPeriodLabel();
    }

    // ============================================================
    // TOP PANEL (FILTERS + ACTIONS)
    // ============================================================

    /**
     * Builds the top control card containing:
     * - Title
     * - Payroll filters
     * - Action buttons
     */
    private JPanel buildTopCard() {

        JPanel card = new JPanel(new BorderLayout(10, 10));

        card.setBackground(CARD);

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel title = new JLabel("My Payslip");

        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(30, 30, 30));

        lblPeriod.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblPeriod.setForeground(new Color(70, 70, 70));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        header.add(title, BorderLayout.WEST);
        header.add(lblPeriod, BorderLayout.EAST);

        JPanel row2 = new JPanel(new GridBagLayout());
        row2.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();

        gc.insets = new Insets(0, 0, 0, 0);
        gc.gridy = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;

        // Container for filters
        JPanel filters = new JPanel(new GridBagLayout());
        filters.setOpaque(false);

        cmbCutoff.setPreferredSize(new Dimension(150, 30));
        cmbMonth.setPreferredSize(new Dimension(80, 30));
        spYear.setPreferredSize(new Dimension(70, 30));

        GridBagConstraints fc = new GridBagConstraints();

        fc.insets = new Insets(0, 0, 0, 10);
        fc.gridy = 0;
        fc.anchor = GridBagConstraints.WEST;

        fc.gridx = 0;
        filters.add(new JLabel("Cutoff:"), fc);

        fc.gridx = 1;
        filters.add(cmbCutoff, fc);

        fc.gridx = 2;
        filters.add(new JLabel("Month:"), fc);

        fc.gridx = 3;
        filters.add(cmbMonth, fc);

        fc.gridx = 4;
        filters.add(new JLabel("Year:"), fc);

        fc.gridx = 5;
        filters.add(spYear, fc);

        // Allow filters to stretch
        fc.gridx = 6;
        fc.weightx = 1;
        filters.add(Box.createHorizontalGlue(), fc);

        // Action buttons container
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        Dimension btnSize = new Dimension(110, 32);

        btnGenerate.setPreferredSize(btnSize);
        btnPrint.setPreferredSize(btnSize);
        btnSendEmail.setPreferredSize(new Dimension(130, 32));

        stylePrimary(btnGenerate);
        styleOutline(btnPrint);
        styleOutline(btnSendEmail);

        actions.add(btnGenerate);
        actions.add(btnPrint);
        actions.add(btnSendEmail);

        gc.gridx = 0;
        gc.weightx = 1;
        row2.add(filters, gc);

        gc.gridx = 1;
        gc.weightx = 0;
        gc.insets = new Insets(0, 10, 0, 0);

        row2.add(actions, gc);

        card.add(header, BorderLayout.NORTH);
        card.add(row2, BorderLayout.CENTER);

        return card;
    }

    // ---------------- OUTPUT ----------------

    private JPanel buildOutputCard() {
        JPanel outer = new JPanel(new BorderLayout(12, 12));
        outer.setOpaque(false);

        // Container card
        JPanel card = new JPanel(new BorderLayout(12, 12));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        // ===== 1) Summary strip =====
        JPanel summary = new JPanel(new GridLayout(1, 3, 12, 12));
        summary.setOpaque(false);
        summary.add(summaryTile("Net Pay", valNetPay, true));
        summary.add(summaryTile("Gross Pay", valGrossPay, false));
        summary.add(summaryTile("Total Deductions", valTotalDed, false));

        // ===== 2) Middle cards (Details + Work) =====
        JPanel mid = new JPanel(new GridLayout(1, 2, 12, 12));
        mid.setOpaque(false);
        mid.add(infoCard());
        mid.add(workCard());

        // ===== 3) Deductions table (fixed height) =====
        tblDeductions.setModel(dedModel);
        tblDeductions.setRowHeight(26);
        tblDeductions.getTableHeader().setReorderingAllowed(false);
        
        tblDeductions.setShowGrid(false);
        tblDeductions.setIntercellSpacing(new Dimension(0, 0));
        tblDeductions.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        JScrollPane sp = new JScrollPane(tblDeductions);
        sp.getViewport().setBackground(Color.WHITE);
        sp.setBorder(BorderFactory.createTitledBorder("Deductions"));

        // FIX: prevent it from taking the whole page
        sp.setPreferredSize(new Dimension(0, 190)); // adjust 170-220 if you want

        // If table is empty, show a nicer empty state height
        tblDeductions.setFillsViewportHeight(true);

        // ===== Stack everything vertically =====
        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));

        stack.add(summary);
        stack.add(Box.createVerticalStrut(12));
        stack.add(mid);
        stack.add(Box.createVerticalStrut(12));
        stack.add(sp);

        card.add(stack, BorderLayout.CENTER);
        outer.add(card, BorderLayout.CENTER);

        return outer;
    }

    private JPanel summaryTile(String title, JLabel value, boolean emphasize) {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBackground(new Color(248, 250, 255));
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

    private JPanel infoCard() {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JLabel h = new JLabel("Payslip Details");
        h.setFont(new Font("Segoe UI", Font.BOLD, 13));
        h.setForeground(new Color(40, 40, 40));

        JPanel g = new JPanel(new GridBagLayout());
        g.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;

        int r = 0;
        addInfoRow(g, gc, r++, "Reference No.", valRef);
        addInfoRow(g, gc, r++, "Generated At", valGenerated);
        addInfoRow(g, gc, r++, "Cutoff", valCutoff);
        addInfoRow(g, gc, r++, "Employee ID", valEmpId);
        addInfoRow(g, gc, r++, "Name", valName);
        addInfoRow(g, gc, r++, "Position", valPosition);
        addInfoRow(g, gc, r++, "Period", valPeriod);

        card.add(h, BorderLayout.NORTH);
        card.add(g, BorderLayout.CENTER);
        return card;
    }

    private JPanel workCard() {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JLabel h = new JLabel("Work Summary");
        h.setFont(new Font("Segoe UI", Font.BOLD, 13));
        h.setForeground(new Color(40, 40, 40));

        JPanel g = new JPanel(new GridBagLayout());
        g.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;

        int r = 0;
        addInfoRow(g, gc, r++, "Hours Worked", valHours);
        addInfoRow(g, gc, r++, "Overtime Hours", valOT);

        card.add(h, BorderLayout.NORTH);
        card.add(g, BorderLayout.CENTER);
        return card;
    }

    private void addInfoRow(JPanel p, GridBagConstraints gc, int row, String label, JLabel value) {
        JLabel l = new JLabel(label + ":");
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(new Color(90, 90, 90));

        value.setFont(new Font("Segoe UI", Font.BOLD, 12));
        value.setForeground(new Color(30, 30, 30));

        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        p.add(l, gc);

        gc.gridx = 1; gc.gridy = row; gc.weightx = 1;
        p.add(value, gc);
    }

    // ---------------- EVENTS ----------------

    private void wireEvents() {
        btnGenerate.addActionListener(e -> onGenerate());
        btnPrint.addActionListener(e -> onPrint());
        btnSendEmail.addActionListener(e -> onSendEmail());

        cmbCutoff.addActionListener(e -> refreshPeriodLabel());
        cmbMonth.addActionListener(e -> refreshPeriodLabel());
        spYear.addChangeListener(e -> refreshPeriodLabel());
        
        // Fix Year spinner: remove comma (2,024) and prevent clipping
        spYear.setEditor(new JSpinner.NumberEditor(spYear, "0000"));
        JFormattedTextField yearField = ((JSpinner.NumberEditor) spYear.getEditor()).getTextField();
        yearField.setColumns(4);
        yearField.setHorizontalAlignment(SwingConstants.LEFT);
        spYear.setPreferredSize(new Dimension(70, 30));
    }
    
    private void refreshPeriodLabel() {
        LocalDate[] range = computeSelectedRange();
        lblPeriod.setText("Period: " + DF.format(range[0]) + " to " + DF.format(range[1]));
    }

    private String getEmployeeId() {
        return (session.getAccount() == null) ? null : session.getAccount().getEmployeeNumber();
    }

    private String getSelectedCutoffLabel() {
        return (cmbCutoff.getSelectedIndex() == 0) ? "1–15" : "16–End";
    }

    private LocalDate[] computeSelectedRange() {
        int month = cmbMonth.getSelectedIndex() + 1;
        int year = (int) spYear.getValue();
        YearMonth ym = YearMonth.of(year, month);

        boolean firstCutoff = cmbCutoff.getSelectedIndex() == 0;

        LocalDate start = firstCutoff ? LocalDate.of(year, month, 1)
                                      : LocalDate.of(year, month, 16);

        LocalDate end = firstCutoff ? LocalDate.of(year, month, 15)
                                    : LocalDate.of(year, month, ym.lengthOfMonth());

        return new LocalDate[]{start, end};
    }

    private void onGenerate() {
        try {
            String employeeId = getEmployeeId();
            if (employeeId == null || employeeId.isBlank()) {
                JOptionPane.showMessageDialog(this, "No linked employee number for this account.");
                return;
            }

            LocalDate[] range = computeSelectedRange();
            LocalDate start = range[0];
            LocalDate end = range[1];

            Payslip payslip = payrollService.generatePayslip(session, employeeId.trim(), start, end);

            lastPayslip = payslip;
            lastReference = generateReference();
            lastCutoffLabel = getSelectedCutoffLabel();
            lastGeneratedAt = LocalDateTime.now();

            displayPayslip(payslip);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onPrint() {
        if (lastPayslip == null) {
            JOptionPane.showMessageDialog(this, "Generate a payslip first.");
            return;
        }

        try {
            JTextArea printArea = new JTextArea(printablePayslipText);
            printArea.setFont(new Font("Consolas", Font.PLAIN, 11));

            boolean ok = printArea.print(); // opens printer dialog
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "Payslip printed.\nAlso: Check your email, the payslip has been sent there.",
                        "Printed",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Printing cancelled.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSendEmail() {
        if (lastPayslip == null) {
            JOptionPane.showMessageDialog(this, "Generate a payslip first.");
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Check your email, the payslip has been sent there.",
                "Payslip Sent",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // ---------------- DISPLAY ----------------

    private void displayPayslip(Payslip p) {

        // summary
        valNetPay.setText(money(p.getNetPay()));
        valGrossPay.setText(money(p.getGrossPay()));
        valTotalDed.setText(money(p.getDeductions().getTotal()));

        // details
        valRef.setText(nvl(lastReference));
        valGenerated.setText(lastGeneratedAt == null ? "-" : TS.format(lastGeneratedAt));
        valCutoff.setText(nvl(lastCutoffLabel));

        valEmpId.setText(nvl(p.getEmployeeNumber()));
        valName.setText(nvl(p.getEmployeeName()));
        valPosition.setText(nvl(p.getPosition()));
        valPeriod.setText(DF.format(p.getPeriodStart()) + " to " + DF.format(p.getPeriodEnd()));

        // work
        valHours.setText(String.format("%.2f", p.getHoursWorked()));
        valOT.setText(String.format("%.2f", p.getOvertimeHours()));

        // deductions table
        dedModel.setRowCount(0);
        dedModel.addRow(new Object[]{"SSS", money(p.getDeductions().getSss())});
        dedModel.addRow(new Object[]{"PhilHealth", money(p.getDeductions().getPhilHealth())});
        dedModel.addRow(new Object[]{"Pag-IBIG", money(p.getDeductions().getPagIbig())});
        dedModel.addRow(new Object[]{"Tax", money(p.getDeductions().getTax())});
        dedModel.addRow(new Object[]{"Late", money(p.getDeductions().getLateDeduction())});
        dedModel.addRow(new Object[]{"Other", money(p.getDeductions().getOther())});

        // printable text (for printing)
        printablePayslipText = buildPrintableText(p);
    }

    private String buildPrintableText(Payslip p) {
        StringBuilder sb = new StringBuilder();

        sb.append("MOTORPH PAYSLIP\n");
        sb.append("----------------------------------------\n");
        sb.append("Reference No.: ").append(nvl(lastReference)).append("\n");
        sb.append("Generated At : ").append(lastGeneratedAt == null ? "-" : TS.format(lastGeneratedAt)).append("\n");
        sb.append("Cutoff       : ").append(nvl(lastCutoffLabel)).append("\n");
        sb.append("\n");

        sb.append("Employee ID  : ").append(nvl(p.getEmployeeNumber())).append("\n");
        sb.append("Name         : ").append(nvl(p.getEmployeeName())).append("\n");
        sb.append("Position     : ").append(nvl(p.getPosition())).append("\n");
        sb.append("Period       : ").append(DF.format(p.getPeriodStart())).append(" to ").append(DF.format(p.getPeriodEnd())).append("\n");
        sb.append("\n");

        sb.append("WORK SUMMARY\n");
        sb.append("Hours Worked : ").append(String.format("%.2f", p.getHoursWorked())).append("\n");
        sb.append("Overtime Hrs : ").append(String.format("%.2f", p.getOvertimeHours())).append("\n");
        sb.append("\n");

        sb.append("PAY\n");
        sb.append("Gross Pay    : ").append(money(p.getGrossPay())).append("\n");
        sb.append("\n");

        sb.append("DEDUCTIONS\n");
        sb.append("SSS          : ").append(money(p.getDeductions().getSss())).append("\n");
        sb.append("PhilHealth   : ").append(money(p.getDeductions().getPhilHealth())).append("\n");
        sb.append("Pag-IBIG     : ").append(money(p.getDeductions().getPagIbig())).append("\n");
        sb.append("Tax          : ").append(money(p.getDeductions().getTax())).append("\n");
        sb.append("Late         : ").append(money(p.getDeductions().getLateDeduction())).append("\n");
        sb.append("Other        : ").append(money(p.getDeductions().getOther())).append("\n");
        sb.append("----------------------------------------\n");
        sb.append("Total Deduct.: ").append(money(p.getDeductions().getTotal())).append("\n");
        sb.append("\n");

        sb.append("NET PAY      : ").append(money(p.getNetPay())).append("\n");
        sb.append("----------------------------------------\n");

        return sb.toString();
    }

    // ---------------- HELPERS ----------------

    private String money(double amount) {
        return MONEY.format(amount);
    }

    private String nvl(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    private String generateReference() {
        String raw = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        return "PS-" + raw.substring(0, 10);
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
}