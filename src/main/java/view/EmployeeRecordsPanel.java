package view;

import model.Employee;
import model.Permission;
import model.Role;
import roles.User;
import service.EmployeeService;
import service.UserDirectoryService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * ============================================================
 * EmployeeRecordsPanel
 * ============================================================
 * This panel displays the list of employees in the MotorPH
 * payroll system. It allows HR users to:
 *
 * - Add employees
 * - Update employee records
 * - Delete employees
 * - View full employee details
 * - Filter employees by role
 *
 * Access Control:
 * Only users with Permission.CRUD_EMPLOYEE can add/update/delete.
 * IT role is view-only.
 *
 * This panel follows the VIEW layer in the system architecture.
 */
public class EmployeeRecordsPanel extends JPanel {

    /** Currently logged-in user */
    private final User currentUser;

    /** Service used to perform employee operations */
    private final EmployeeService employeeService;

    /** Service used to retrieve user directory data (roles, credentials) */
    private final UserDirectoryService userDirectoryService;

    /** JTable used to display employee records */
    private final JTable table = new JTable();

    /** Table model controlling the data inside JTable */
    private final DefaultTableModel tableModel;

    /** CRUD buttons */
    private final JButton btnAdd = new JButton("Add");
    private final JButton btnUpdate = new JButton("Update");
    private final JButton btnDelete = new JButton("Delete");

    /** Utility buttons */
    private final JButton btnRefresh = new JButton("Refresh");
    private final JButton btnView = new JButton("View Full Details");

    /**
     * Dropdown used to filter employees by system role.
     * Default value = ALL
     */
    private final JComboBox<String> cmbRoleFilter = new JComboBox<>(new String[]{
            "ALL",
            "EMPLOYEE",
            "HR",
            "ACCOUNTANT",
            "IT"
    });

    /**
     * Constructor for EmployeeRecordsPanel.
     *
     * Injects required services and the current user session.
     */
    public EmployeeRecordsPanel(User currentUser,
                                EmployeeService employeeService,
                                UserDirectoryService userDirectoryService) {

        this.currentUser = currentUser;
        this.employeeService = employeeService;
        this.userDirectoryService = userDirectoryService;

        // Main layout of the panel
        setLayout(new BorderLayout(10, 10));

        // Background color consistent with other panels
        setBackground(new Color(230, 242, 255));

        // Padding around the panel
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        /**
         * Table model definition
         *
         * This defines the columns of the JTable.
         * Cells are non-editable to prevent direct editing.
         */
        tableModel = new DefaultTableModel(
            new Object[]{"Employee ID", "Full Name", "SSS #", "PhilHealth #", "TIN #", "Pag-IBIG #"},
            0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Apply model to table
        table.setModel(tableModel);

        // Improve table appearance
        table.setRowHeight(26);

        // Prevent column reordering
        table.getTableHeader().setReorderingAllowed(false);

        // Build UI layout
        add(buildTopBar(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);

        // Connect buttons to actions
        wireEvents();

        // Apply role-based permissions
        applyAccessControl();

        // Load employee records into the table
        loadEmployees();
    }

    /**
     * Builds the top header section.
     *
     * Contains:
     * - Title
     * - Role filter dropdown
     */
    private JPanel buildTopBar() {

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(Color.WHITE);

        // Card border styling
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        // Title label
        JLabel lblTitle = new JLabel("Employee Records");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(30, 30, 30));

        // Filter label
        JLabel lblFilter = new JLabel("Role Filter:");
        lblFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblFilter.setForeground(new Color(60, 60, 60));

        // Filter dropdown styling
        cmbRoleFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbRoleFilter.setPreferredSize(new Dimension(180, 30));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        right.add(lblFilter);
        right.add(cmbRoleFilter);

        card.add(lblTitle, BorderLayout.WEST);
        card.add(right, BorderLayout.EAST);

        top.add(card, BorderLayout.CENTER);

        return top;
    }

    /**
     * Creates the white card containing the JTable.
     */
    private JPanel buildTableCard() {

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JScrollPane sp = new JScrollPane(table);

        // Table background
        sp.getViewport().setBackground(Color.WHITE);

        card.add(sp, BorderLayout.CENTER);

        return card;
    }

    /**
     * Builds the bottom button panel.
     */
    private JPanel buildButtonPanel() {

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setBackground(Color.WHITE);

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        // Buttons added in order
        panel.add(btnRefresh);
        panel.add(btnAdd);
        panel.add(btnUpdate);
        panel.add(btnDelete);
        panel.add(btnView);

        wrap.add(panel, BorderLayout.CENTER);

        return wrap;
    }

    /**
     * Connects button events to their methods.
     */
    private void wireEvents() {

        btnRefresh.addActionListener(e -> loadEmployees());
        btnAdd.addActionListener(e -> addEmployee());
        btnUpdate.addActionListener(e -> updateEmployee());
        btnDelete.addActionListener(e -> deleteEmployee());
        btnView.addActionListener(e -> viewEmployee());

        // Reload employees when role filter changes
        cmbRoleFilter.addActionListener(e -> loadEmployees());
    }

    /**
     * Applies role-based access control.
     *
     * Only users with CRUD_EMPLOYEE permission can
     * add, update, or delete employees.
     */
    private void applyAccessControl() {

        boolean canCRUD = currentUser.can(Permission.CRUD_EMPLOYEE);

        btnAdd.setEnabled(canCRUD);
        btnUpdate.setEnabled(canCRUD);
        btnDelete.setEnabled(canCRUD);
    }

    /**
     * Converts selected filter value to Role enum.
     */
    private Role selectedRoleOrNull() {

        String selected = (String) cmbRoleFilter.getSelectedItem();

        if (selected == null || selected.equalsIgnoreCase("ALL"))
            return null;

        return Role.valueOf(selected);
    }

    /**
     * Loads employees into the table.
     *
     * Retrieves employees from EmployeeService and
     * optionally filters them by role.
     */
    private void loadEmployees() {

        try {

            // Clear existing rows
            tableModel.setRowCount(0);

            Map<String, Role> roleMap =
                    userDirectoryService.roleByEmployeeNumber(currentUser);

            List<Employee> employees =
                    employeeService.getAllEmployees(currentUser);

            Role filterRole = selectedRoleOrNull();

            for (Employee e : employees) {

                Role empRole = roleMap.get(e.getEmployeeNumber());

                // Apply role filter if selected
                if (filterRole != null) {

                    if (empRole == null)
                        continue;

                    if (empRole != filterRole)
                        continue;
                }

                tableModel.addRow(new Object[]{
                        e.getEmployeeNumber(),
                        e.getFirstName() + " " + e.getLastName(),
                        e.getSssNumber(),
                        e.getPhilhealthNumber(),
                        e.getTinNumber(),
                        e.getPagIbigNumber()
                });
            }

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Opens dialog to add a new employee.
     */
    private void addEmployee() {

        if (!currentUser.can(Permission.CRUD_EMPLOYEE)) {
            JOptionPane.showMessageDialog(this, "Access denied.");
            return;
        }

        try {

            String newId =
                    employeeService.generateNextEmployeeId(currentUser);

            EmployeeFormDialog dialog =
                    new EmployeeFormDialog(null);

            dialog.setEmployeeId(newId);

            dialog.setVisible(true);

            Employee newEmployee = dialog.getEmployee();

            if (newEmployee == null)
                return;

            employeeService.addEmployee(currentUser, newEmployee);

            loadEmployees();

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Updates selected employee.
     */
    private void updateEmployee() {

        if (!currentUser.can(Permission.CRUD_EMPLOYEE)) {
            JOptionPane.showMessageDialog(this, "Access denied.");
            return;
        }

        try {

            int selected = table.getSelectedRow();

            if (selected == -1) {
                JOptionPane.showMessageDialog(this, "Select a row first.");
                return;
            }

            String empId =
                    tableModel.getValueAt(selected, 0).toString();

            Employee existing =
                    employeeService.getEmployeeById(currentUser, empId);

            EmployeeFormDialog dialog =
                    new EmployeeFormDialog(existing);

            dialog.setVisible(true);

            Employee updated = dialog.getEmployee();

            if (updated == null)
                return;

            employeeService.updateEmployee(currentUser, updated);

            loadEmployees();

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Deletes selected employee.
     */
    private void deleteEmployee() {

        if (!currentUser.can(Permission.CRUD_EMPLOYEE)) {
            JOptionPane.showMessageDialog(this, "Access denied.");
            return;
        }

        try {

            int selected = table.getSelectedRow();

            if (selected == -1) {
                JOptionPane.showMessageDialog(this, "Select a row first.");
                return;
            }

            String empId =
                    tableModel.getValueAt(selected, 0).toString();

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Delete employee " + empId + "?",
                    "Confirm",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {

                employeeService.deleteEmployee(currentUser, empId);

                loadEmployees();
            }

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Opens dialog to view full employee details.
     */
    private void viewEmployee() {

        try {

            int selected = table.getSelectedRow();

            if (selected == -1) {
                JOptionPane.showMessageDialog(this, "Select a row first.");
                return;
            }

            String empId =
                    tableModel.getValueAt(selected, 0).toString();

            Employee emp =
                    employeeService.getEmployeeById(currentUser, empId);

            EmployeeFormDialog dialog =
                    new EmployeeFormDialog(emp, true);

            dialog.setVisible(true);

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}