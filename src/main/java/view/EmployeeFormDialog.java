package view;

import model.Employee;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * =============================================================
 * EmployeeFormDialog
 * =============================================================
 * Dialog window used to:
 * 1. Add a new employee
 * 2. Update an existing employee
 * 3. View employee details (read-only mode)
 *
 * The dialog collects all employee information such as:
 * - Personal details
 * - Government IDs
 * - Employment details
 * - Salary components
 *
 * This class belongs to the VIEW layer of the application.
 */
public class EmployeeFormDialog extends JDialog {

    /** Formatter used for parsing and displaying birthday values */
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("M/d/yyyy");

    /** Basic personal information fields */
    private final JTextField txtId = new JTextField(15);
    private final JTextField txtLast = new JTextField(15);
    private final JTextField txtFirst = new JTextField(15);
    private final JTextField txtBirthday = new JTextField(15);
    private final JTextField txtAddress = new JTextField(20);
    private final JTextField txtPhone = new JTextField(15);

    /** Government ID fields */
    private final JTextField txtSSS = new JTextField(15);
    private final JTextField txtPhilHealth = new JTextField(15);
    private final JTextField txtTIN = new JTextField(15);
    private final JTextField txtPagIbig = new JTextField(15);

    /** Employment status dropdown */
    private final JComboBox<String> cmbStatus =
        new JComboBox<>(new String[]{"Regular", "Probationary"});

    /** Employment details */
    private final JTextField txtPosition = new JTextField(15);
    private final JTextField txtSupervisor = new JTextField(15);

    /** Salary and allowances */
    private final JTextField txtBasicSalary = new JTextField(15);
    private final JTextField txtRiceSubsidy = new JTextField(15);
    private final JTextField txtPhoneAllowance = new JTextField(15);
    private final JTextField txtClothingAllowance = new JTextField(15);
    private final JTextField txtGrossSemiMonthly = new JTextField(15);
    private final JTextField txtHourlyRate = new JTextField(15);

    /** Employee object that will be returned after saving */
    private Employee employee;

    /** Determines if the dialog is read-only (view mode) */
    private final boolean readOnlyMode;

    /** UI color scheme for consistent styling */
    private static final Color BG = new Color(230, 242, 255);
    private static final Color CARD = Color.WHITE;
    private static final Color BORDER = new Color(220, 220, 220);
    private static final Color FIELD_BG = new Color(248, 250, 255);
    private static final Color TEXT = new Color(35, 35, 35);
    private static final Color MUTED = new Color(90, 90, 90);
    private static final Color PRIMARY = new Color(52, 120, 246);

    /**
     * Constructor used when creating or editing employees.
     */
    public EmployeeFormDialog(Employee existing) {
        this(existing, false);
    }

    /**
     * Main constructor.
     *
     * @param existing     Employee object if editing
     * @param readOnlyMode true if dialog should be view-only
     */
    public EmployeeFormDialog(Employee existing, boolean readOnlyMode) {

        this.readOnlyMode = readOnlyMode;

        // Set window title depending on mode
        setTitle(readOnlyMode
                ? "Employee Full Details"
                : (existing == null ? "Add Employee" : "Update Employee"));

        setModal(true);
        setSize(760, 720);
        setLocationRelativeTo(null);

        // Main layout
        setLayout(new BorderLayout());

        // Background color
        getContentPane().setBackground(BG);

        // Build UI sections
        add(buildHeader(), BorderLayout.NORTH);
        add(buildFormCard(), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        // Apply consistent styling to fields
        styleFields();

        // Populate form when editing
        if (existing != null) {
            populateFields(existing);

            // Employee ID cannot be changed
            txtId.setEditable(false);
            txtId.setEnabled(false);
        } else {
            cmbStatus.setSelectedItem("Regular");
        }

        // Disable editing if view-only
        if (readOnlyMode) {
            setFieldsEditable(false);
        }
    }

    /**
     * Builds the dialog header section.
     */
    private JPanel buildHeader() {

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(BG);
        wrap.setBorder(BorderFactory.createEmptyBorder(14, 14, 0, 14));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD);

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        JLabel title = new JLabel(readOnlyMode ? "Employee Full Details" : "Employee Form");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT);

        JLabel subtitle = new JLabel(
                readOnlyMode
                        ? "View-only employee information"
                        : "Fill in all employee details"
        );

        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(MUTED);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(subtitle);

        card.add(left, BorderLayout.WEST);

        wrap.add(card, BorderLayout.CENTER);

        return wrap;
    }

    /**
     * Builds the scrollable form containing all employee fields.
     */
    private JPanel buildFormCard() {

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(BG);
        wrap.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD);

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 8, 7, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        int row = 0;

        // Personal information
        row = addRow(form, gbc, row, "Employee ID", txtId);
        row = addRow(form, gbc, row, "Last Name", txtLast);
        row = addRow(form, gbc, row, "First Name", txtFirst);
        row = addRow(form, gbc, row, "Birthday (M/d/yyyy)", txtBirthday);
        row = addRow(form, gbc, row, "Address", txtAddress);
        row = addRow(form, gbc, row, "Phone Number", txtPhone);

        // Government IDs
        row = addRow(form, gbc, row, "SSS #", txtSSS);
        row = addRow(form, gbc, row, "PhilHealth #", txtPhilHealth);
        row = addRow(form, gbc, row, "TIN #", txtTIN);
        row = addRow(form, gbc, row, "Pag-IBIG #", txtPagIbig);

        // Employment details
        row = addRow(form, gbc, row, "Status", cmbStatus);
        row = addRow(form, gbc, row, "Position", txtPosition);
        row = addRow(form, gbc, row, "Immediate Supervisor", txtSupervisor);

        // Salary details
        row = addRow(form, gbc, row, "Basic Salary", txtBasicSalary);
        row = addRow(form, gbc, row, "Rice Subsidy", txtRiceSubsidy);
        row = addRow(form, gbc, row, "Phone Allowance", txtPhoneAllowance);
        row = addRow(form, gbc, row, "Clothing Allowance", txtClothingAllowance);
        row = addRow(form, gbc, row, "Gross Semi-monthly Rate", txtGrossSemiMonthly);
        row = addRow(form, gbc, row, "Hourly Rate", txtHourlyRate);

        JScrollPane sp = new JScrollPane(form);
        sp.setBorder(null);
        sp.getViewport().setBackground(CARD);

        card.add(sp, BorderLayout.CENTER);
        wrap.add(card, BorderLayout.CENTER);

        return wrap;
    }

    /**
     * Adds a label and field pair to the form layout.
     */
    private int addRow(JPanel form, GridBagConstraints gbc, int row, String label, JComponent field) {

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.gridwidth = 1;

        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(TEXT);

        form.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;

        form.add(field, gbc);

        return row + 1;
    }

    /**
     * Builds bottom action buttons.
     */
    private JPanel buildButtons() {

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(BG);
        wrap.setBorder(BorderFactory.createEmptyBorder(0, 14, 14, 14));

        JPanel card = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        card.setBackground(CARD);

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

        JButton btnClose = new JButton(readOnlyMode ? "Close" : "Cancel");

        styleOutline(btnClose);

        btnClose.addActionListener(e -> dispose());

        card.add(btnClose);

        if (!readOnlyMode) {

            JButton btnSave = new JButton("Save");

            stylePrimary(btnSave);

            btnSave.addActionListener(e -> onSave());

            card.add(btnSave);
        }

        wrap.add(card, BorderLayout.CENTER);

        return wrap;
    }

    /**
     * Applies consistent styling to text fields.
     */
    private void styleFields() {

        JTextField[] fields = {
                txtId, txtLast, txtFirst, txtBirthday, txtAddress, txtPhone,
                txtSSS, txtPhilHealth, txtTIN, txtPagIbig,
                txtPosition, txtSupervisor,
                txtBasicSalary, txtRiceSubsidy, txtPhoneAllowance,
                txtClothingAllowance, txtGrossSemiMonthly, txtHourlyRate
        };

        for (JTextField f : fields) {

            f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            f.setBackground(FIELD_BG);
            f.setForeground(TEXT);

            f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(210, 220, 235)),
                    BorderFactory.createEmptyBorder(7, 9, 7, 9)
            ));
        }

        cmbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbStatus.setBackground(FIELD_BG);
        cmbStatus.setForeground(TEXT);
        cmbStatus.setBorder(BorderFactory.createLineBorder(new Color(210, 220, 235)));
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

    private void populateFields(Employee existing) {
        txtId.setText(safe(existing.getEmployeeNumber()));
        txtLast.setText(safe(existing.getLastName()));
        txtFirst.setText(safe(existing.getFirstName()));
        txtBirthday.setText(existing.getBirthday() == null ? "" : existing.getBirthday().format(DATE_FMT));
        txtAddress.setText(safe(existing.getAddress()));
        txtPhone.setText(safe(existing.getPhoneNumber()));

        txtSSS.setText(safe(existing.getSssNumber()));
        txtPhilHealth.setText(safe(existing.getPhilhealthNumber()));
        txtTIN.setText(safe(existing.getTinNumber()));
        txtPagIbig.setText(safe(existing.getPagIbigNumber()));

        cmbStatus.setSelectedItem(existing.getStatus());
        txtPosition.setText(safe(existing.getPosition()));
        txtSupervisor.setText(safe(existing.getImmediateSupervisor()));

        txtBasicSalary.setText(String.valueOf(existing.getBasicSalary()));
        txtRiceSubsidy.setText(String.valueOf(existing.getRiceSubsidy()));
        txtPhoneAllowance.setText(String.valueOf(existing.getPhoneAllowance()));
        txtClothingAllowance.setText(String.valueOf(existing.getClothingAllowance()));
        txtGrossSemiMonthly.setText(String.valueOf(existing.getGrossSemiMonthlyRate()));
        txtHourlyRate.setText(String.valueOf(existing.getHourlyRate()));
    }

    private void setFieldsEditable(boolean editable) {
        txtId.setEditable(false);
        txtId.setEnabled(false);

        txtLast.setEditable(editable);
        txtFirst.setEditable(editable);
        txtBirthday.setEditable(editable);
        txtAddress.setEditable(editable);
        txtPhone.setEditable(editable);

        txtSSS.setEditable(editable);
        txtPhilHealth.setEditable(editable);
        txtTIN.setEditable(editable);
        txtPagIbig.setEditable(editable);

        cmbStatus.setEnabled(editable);
        txtPosition.setEditable(editable);
        txtSupervisor.setEditable(editable);

        txtBasicSalary.setEditable(editable);
        txtRiceSubsidy.setEditable(editable);
        txtPhoneAllowance.setEditable(editable);
        txtClothingAllowance.setEditable(editable);
        txtGrossSemiMonthly.setEditable(editable);
        txtHourlyRate.setEditable(editable);

        if (!editable) {
            JTextField[] fields = {
                    txtLast, txtFirst, txtBirthday, txtAddress, txtPhone,
                    txtSSS, txtPhilHealth, txtTIN, txtPagIbig, txtPosition, txtSupervisor,
                    txtBasicSalary, txtRiceSubsidy, txtPhoneAllowance,
                    txtClothingAllowance, txtGrossSemiMonthly, txtHourlyRate
            };

            for (JTextField f : fields) {
                f.setBackground(new Color(245, 247, 250));
                f.setCaretColor(new Color(245, 247, 250));
            }
        }
    }

    public void setEmployeeId(String id) {
        txtId.setText(id);
        txtId.setEnabled(false);
        txtId.setEditable(false);
    }

    private void onSave() {
        try {
            String id = txtId.getText().trim();
            String last = txtLast.getText().trim();
            String first = txtFirst.getText().trim();
            String birthdayRaw = txtBirthday.getText().trim();
            String address = txtAddress.getText().trim();
            String phone = txtPhone.getText().trim();

            String sss = txtSSS.getText().trim();
            String philHealth = txtPhilHealth.getText().trim();
            String tin = txtTIN.getText().trim();
            String pagIbig = txtPagIbig.getText().trim();

            String status = cmbStatus.getSelectedItem().toString();
            String position = txtPosition.getText().trim();
            String supervisor = txtSupervisor.getText().trim();

            String basicSalaryRaw = txtBasicSalary.getText().trim();
            String riceSubsidyRaw = txtRiceSubsidy.getText().trim();
            String phoneAllowanceRaw = txtPhoneAllowance.getText().trim();
            String clothingAllowanceRaw = txtClothingAllowance.getText().trim();
            String grossSemiMonthlyRaw = txtGrossSemiMonthly.getText().trim();
            String hourlyRateRaw = txtHourlyRate.getText().trim();

            if (id.isEmpty() || last.isEmpty() || first.isEmpty() || birthdayRaw.isEmpty()
                    || address.isEmpty() || phone.isEmpty()
                    || sss.isEmpty() || philHealth.isEmpty() || tin.isEmpty() || pagIbig.isEmpty()
                    || status.isEmpty() || position.isEmpty()
                    || basicSalaryRaw.isEmpty() || riceSubsidyRaw.isEmpty()
                    || phoneAllowanceRaw.isEmpty() || clothingAllowanceRaw.isEmpty()
                    || grossSemiMonthlyRaw.isEmpty() || hourlyRateRaw.isEmpty()) {

                JOptionPane.showMessageDialog(this, "Please fill in all required fields.");
                return;
            }

            if (!sss.matches("\\d{2}-\\d{7}-\\d{1}")) {
                JOptionPane.showMessageDialog(this,
                        "Invalid SSS format.\nExample: 44-4506057-3");
                return;
            }

            if (!philHealth.matches("\\d{12}")) {
                JOptionPane.showMessageDialog(this,
                        "Invalid PhilHealth number.\nExample: 820126853951");
                return;
            }

            if (!tin.matches("\\d{3}-\\d{3}-\\d{3}-\\d{3}")) {
                JOptionPane.showMessageDialog(this,
                        "Invalid TIN format.\nExample: 442-605-657-000");
                return;
            }

            if (!pagIbig.matches("\\d{12}")) {
                JOptionPane.showMessageDialog(this,
                        "Invalid Pag-IBIG number.\nExample: 691295330870");
                return;
            }

            LocalDate birthday = LocalDate.parse(birthdayRaw, DATE_FMT);

            double basicSalary = Double.parseDouble(basicSalaryRaw);
            double riceSubsidy = Double.parseDouble(riceSubsidyRaw);
            double phoneAllowance = Double.parseDouble(phoneAllowanceRaw);
            double clothingAllowance = Double.parseDouble(clothingAllowanceRaw);
            double grossSemiMonthly = Double.parseDouble(grossSemiMonthlyRaw);
            double hourlyRate = Double.parseDouble(hourlyRateRaw);

            employee = new Employee(
                    id,
                    last,
                    first,
                    birthday,
                    address,
                    phone,
                    sss,
                    philHealth,
                    tin,
                    pagIbig,
                    status,
                    position,
                    supervisor,
                    basicSalary,
                    riceSubsidy,
                    phoneAllowance,
                    clothingAllowance,
                    grossSemiMonthly,
                    hourlyRate
            );

            dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Salary and allowance fields must be valid numbers.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Invalid input.\nCheck birthday format (example: 10/11/1983).");
        }
    }

    public Employee getEmployee() {
        return employee;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}