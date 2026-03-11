package view;

import dao.*;
import model.*;
import roles.*;
import service.*;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * =============================================================
 * VIEW CLASS: MainDashboardFrame
 * =============================================================
 *
 * PURPOSE
 * This class represents the main dashboard window of the
 * MotorPH Payroll System after a user successfully logs in.
 *
 * RESPONSIBILITIES
 * - Display the main application layout
 * - Provide navigation between system modules
 * - Initialize and inject services required by each module
 * - Control which modules are visible based on user permissions
 *
 * ARCHITECTURE ROLE
 *
 * DAO Layer
 * -> EmployeeDAO reads employee records
 * -> AttendanceDAO reads attendance data
 * -> LeaveRequestDAO reads leave requests
 * -> CredentialDAO reads credential records
 * -> SupportTicketDAO reads support ticket records
 * -> UserActivityLogDAO stores audit logs
 *
 * Service Layer
 * -> EmployeeService manages employee data
 * -> PayrollService calculates payroll
 * -> LeaveService manages leave requests
 * -> AttendanceService manages clock in/out
 * -> SupportTicketService handles support tickets
 * -> UserDirectoryService manages user accounts
 * -> AuthService manages authentication
 * -> AuditService records system activity
 *
 * View Layer (THIS CLASS)
 * -> Displays the main dashboard UI
 * -> Controls navigation using CardLayout
 *
 * SECURITY
 * Navigation items are displayed depending on
 * the permissions of the currently logged-in user.
 */

public class MainDashboardFrame extends JFrame {

    /** Current user session */
    private final UserSession session;

    /** Logged-in system user */
    private final User currentUser;

    /** Services used by different modules */
    private final AttendanceService attendanceService;
    private final UserDirectoryService userDirectoryService;
    private final AuthService authService;

    private final EmployeeService employeeService;
    private final PayrollService payrollService;
    private final LeaveService leaveService;
    private final SupportTicketService supportTicketService;

    /** Audit service used for logging system activity */
    private final AuditService auditService;

    /**
     * CardLayout container where all application views are stored.
     * Each panel represents a different module.
     */
    private final JPanel contentPanel = new JPanel(new CardLayout());

    /**
     * Navigation button storage used to highlight
     * the currently active menu button.
     */
    private final Map<String, JButton> navButtons = new HashMap<>();

    /** Currently selected navigation button */
    private JButton activeButton;

    // Theme
    private static final Color BG = new Color(230, 242, 255);
    private static final Color CARD = Color.WHITE;
    private static final Color BORDER = new Color(220, 220, 220);
    private static final Color PRIMARY = new Color(52, 120, 246);
    private static final Color TEXT = new Color(35, 35, 35);
    private static final Color MUTED = new Color(90, 90, 90);

    /**
     * =============================================================
     * CONSTRUCTOR
     * =============================================================
     *
     * Initializes the main dashboard window and prepares
     * all required services used across system modules.
     */
    public MainDashboardFrame(UserSession session) {
        super("MotorPH Dashboard");

        this.session = session;
        this.currentUser = session.getUser();

        /**
         * DAO initialization.
         * These classes retrieve data from CSV storage.
         */
        EmployeeDAO employeeDAO = new EmployeeDAO("data/EmployeeDetails.csv");
        AttendanceDAO attendanceDAO = new AttendanceDAO("data/attendance.csv");
        LeaveRequestDAO leaveDAO = new LeaveRequestDAO("data/leave_requests.csv");
        CredentialDAO credentialDAO = new CredentialDAO("data/credentials.csv");
        PasswordService passwordService = new PasswordService();

        /**
         * Audit service records important system actions.
         */
        this.auditService = new AuditService(new UserActivityLogDAO("data/user_logs.csv"));

        /**
         * Support ticket service handles ticket management.
         */
        SupportTicketDAO supportTicketDAO = new SupportTicketDAO("data/support_tickets.csv");
        this.supportTicketService = new SupportTicketService(supportTicketDAO, this.auditService);

        /**
         * Core services used by system modules.
         */
        this.employeeService = new EmployeeService(employeeDAO);
        this.payrollService = new PayrollService(employeeDAO, attendanceDAO);
        this.leaveService = new LeaveService(leaveDAO);
        this.attendanceService = new AttendanceService(attendanceDAO);
        this.userDirectoryService = new UserDirectoryService(credentialDAO);

        /**
         * AuthService handles password changes and authentication.
         */
        this.authService = new AuthService(credentialDAO, employeeDAO, passwordService, this.auditService);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Main dashboard window size
        setSize(1280, 820);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
        setResizable(true);

        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);

        contentPanel.setBackground(BG);

        /**
         * Build main UI sections
         */
        add(buildTopBar(), BorderLayout.NORTH);
        add(buildSideMenu(), BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        /**
         * Register all views inside the CardLayout
         */
        registerViews();
    }

    /**
     * Builds the top bar of the dashboard.
     *
     * Displays:
     * - Logged-in username
     * - User role
     * - Employee number
     * - Logout button
     */
    private JPanel buildTopBar() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(BG);
        wrap.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));

        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(CARD);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        String role = session.getAccount().getRole();
        String empNo = session.getAccount().getEmployeeNumber();

        JLabel lbl = new JLabel("Logged in as: " + currentUser.getUsername()
                + "  |  Role: " + role
                + (empNo == null || empNo.isEmpty() ? "" : "  |  Emp#: " + empNo));

        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(MUTED);

        /**
         * Logout button
         * Records logout event then returns to login screen.
         */
        JButton btnLogout = new JButton("Logout");
        styleOutline(btnLogout);

        btnLogout.addActionListener(e -> {
            auditService.log(currentUser.getUsername(), "LOGOUT", "User logged out");
            SessionManager.logout(currentUser.getUsername());
            new LogInFrame().setVisible(true);
            dispose();
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(btnLogout);

        bar.add(lbl, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);

        wrap.add(bar, BorderLayout.CENTER);
        return wrap;
    }

    /**
     * Builds the side navigation menu.
     *
     * Menu options appear depending on the permissions
     * of the logged-in user.
     */
    private JPanel buildSideMenu() {

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(BG);
        wrap.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 0));
        wrap.setPreferredSize(new Dimension(250, 0));

        JPanel menuCard = new JPanel();
        menuCard.setLayout(new BoxLayout(menuCard, BoxLayout.Y_AXIS));
        menuCard.setBackground(CARD);

        menuCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel navTitle = new JLabel("Navigation");
        navTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        navTitle.setForeground(TEXT);
        navTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        menuCard.add(navTitle);
        menuCard.add(Box.createVerticalStrut(10));

        /**
         * Navigation items
         * Each module is added only if the user has permission.
         */

        addNavButton(menuCard, "HOME", "Home");
        addNavButton(menuCard, "MY_PROFILE", "My Profile");

        if (currentUser.can(Permission.CRUD_EMPLOYEE) || currentUser.can(Permission.ADMIN_ALL)) {
            addNavButton(menuCard, "EMPLOYEES", "Employee Records");
        }

        if (currentUser.can(Permission.COMPUTE_PAYROLL)
                || currentUser.can(Permission.VIEW_PAYROLL)
                || currentUser.can(Permission.ADMIN_ALL)) {
            addNavButton(menuCard, "PAYROLL", "Payroll");
        }

        if (currentUser.can(Permission.VIEW_OWN_PAYSLIP)) {
            addNavButton(menuCard, "MY_PAYSLIP", "My Payslip");
        }

        if (currentUser.can(Permission.FILE_LEAVE) || currentUser.can(Permission.MANAGE_LEAVE)) {
            addNavButton(menuCard, "LEAVE", "Leave Requests");
        }

        if (currentUser.can(Permission.FILE_SUPPORT_TICKET)
                || currentUser.can(Permission.MANAGE_SUPPORT_TICKET)
                || currentUser.can(Permission.ADMIN_ALL)) {
            addNavButton(menuCard, "SUPPORT_TICKETS", "Support Tickets");
        }

        if (currentUser.can(Permission.ADMIN_ALL) || currentUser.can(Permission.VIEW_AUDIT_LOGS)) {
            addNavButton(menuCard, "USERS_LOG", "Users Log");
        }

        menuCard.add(Box.createVerticalGlue());
        wrap.add(menuCard, BorderLayout.CENTER);

        return wrap;
    }

    /**
     * Adds a navigation button and registers it
     * to the CardLayout navigation system.
     */
    private void addNavButton(JPanel menuCard, String cardName, String label) {

        JButton btn = new JButton(label);
        styleNavButton(btn);

        btn.addActionListener(e -> showCard(cardName));

        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setPreferredSize(new Dimension(220, 40));

        menuCard.add(btn);
        menuCard.add(Box.createVerticalStrut(8));

        navButtons.put(cardName, btn);
    }

    /**
     * Registers all view panels inside the CardLayout container.
     * Each module panel is created and mapped to a card name.
     */
    private void registerViews() {

        contentPanel.add(new HomePanel(
                session,
                attendanceService,
                leaveService,
                employeeService,
                userDirectoryService,
                this::showCard,
                auditService
        ), "HOME");

        if (currentUser.can(Permission.CRUD_EMPLOYEE) || currentUser.can(Permission.ADMIN_ALL)) {
            contentPanel.add(new EmployeeRecordsPanel(currentUser, employeeService, userDirectoryService), "EMPLOYEES");
        }

        if (currentUser.can(Permission.COMPUTE_PAYROLL)
                || currentUser.can(Permission.VIEW_PAYROLL)
                || currentUser.can(Permission.ADMIN_ALL)) {
            contentPanel.add(new PayrollPanel(session, payrollService, employeeService, auditService), "PAYROLL");
        }

        if (currentUser.can(Permission.VIEW_OWN_PAYSLIP)) {
            contentPanel.add(new MyPayslipPanel(session, payrollService), "MY_PAYSLIP");
        }

        if (currentUser.can(Permission.FILE_LEAVE) || currentUser.can(Permission.MANAGE_LEAVE) || currentUser.can(Permission.ADMIN_ALL)) {
            contentPanel.add(new LeavePanel(session, leaveService, auditService), "LEAVE");
        }

        contentPanel.add(new MyProfilePanel(session, authService, employeeService, this::showCard), "MY_PROFILE");

        if (currentUser.can(Permission.ADMIN_ALL) || currentUser.can(Permission.VIEW_AUDIT_LOGS)) {
            contentPanel.add(new UsersLogPanel(session, auditService), "USERS_LOG");
        }

        if (currentUser.can(Permission.FILE_SUPPORT_TICKET)
                || currentUser.can(Permission.MANAGE_SUPPORT_TICKET)
                || currentUser.can(Permission.ADMIN_ALL)) {
            contentPanel.add(new SupportTicketPanel(session, supportTicketService), "SUPPORT_TICKETS");
        }

        showCard("HOME");
    }

    /**
     * Displays a specific panel inside the CardLayout.
     */
    private void showCard(String name) {
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, name);
        setActiveNav(name);
    }

    /**
     * Highlights the currently active navigation button.
     */
    private void setActiveNav(String cardName) {

        JButton btn = navButtons.get(cardName);
        if (btn == null) return;

        if (activeButton != null) {
            activeButton.setBackground(Color.WHITE);
            activeButton.setForeground(TEXT);
            activeButton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 220, 220)),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)
            ));
        }

        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        activeButton = btn;
    }

    // ---------------- Styles ----------------

    /**
     * Styles navigation buttons in the sidebar.
     */
    private void styleNavButton(JButton b) {
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBackground(Color.WHITE);
        b.setForeground(TEXT);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
    }

    /**
     * Styles outline buttons such as the logout button.
     */
    private void styleOutline(JButton b) {
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBackground(Color.WHITE);
        b.setForeground(TEXT);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
    }
}