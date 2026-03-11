package view;

import model.AttendanceRecord;
import model.LeaveRequest;
import model.UserSession;
import service.*;
import dao.UserActivityLogDAO;
import model.UserActivityLogEntry;
import model.LeaveStatus;

import java.util.Comparator;
import java.util.stream.Collectors;
import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * =============================================================
 * HomePanel
 * =============================================================
 * Main dashboard panel shown after login.
 *
 * Responsibilities:
 * - Display greeting and real-time clock
 * - Show role-based dashboard metrics
 * - Display attendance controls (Clock In / Clock Out)
 * - Provide quick navigation buttons
 * - Show notifications
 * - Show admin analytics for administrators
 *
 * Architecture Layer: VIEW
 * This class interacts with multiple service classes to retrieve
 * data and update UI components accordingly.
 */
public class HomePanel extends JPanel {

    /**
     * Navigator interface used by the HomePanel to request
     * navigation to another panel in MainDashboardFrame.
     */
    public interface Navigator {
        void goTo(String cardName);
    }

    /** Logged-in user session */
    private final UserSession session;

    /** Service handling attendance operations */
    private final AttendanceService attendanceService;

    /** Service handling leave requests */
    private final LeaveService leaveService;

    /** Service retrieving employee information */
    private final EmployeeService employeeService;

    /** Service retrieving credential/user directory information */
    private final UserDirectoryService userDirectoryService;

    /** Navigator used to switch dashboard panels */
    private final Navigator navigator;

    /** DAO used for reading system activity logs from CSV storage */
    private final UserActivityLogDAO userLogDAO = new UserActivityLogDAO("data/user_logs.csv");

    /** Formatter used for notification timestamps */
    private static final DateTimeFormatter NOTIF_DTF =
            DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

    /** Audit service responsible for recording system activity */
    private final AuditService auditService;

    /** Greeting label in the header */
    private final JLabel lblHello = new JLabel();

    /** System clock label */
    private final JLabel lblTime = new JLabel();

    /** Dashboard tile showing role/position */
    private final JLabel valRoleLabel = new JLabel("-");

    /** Primary dashboard metric */
    private final JLabel valPrimaryMetric = new JLabel("-");

    /** Secondary dashboard metric */
    private final JLabel valSecondaryMetric = new JLabel("-");

    /** Attendance status label */
    private final JLabel lblAttendance = new JLabel("Today: -");

    /** Clock-in button */
    private final JButton btnClockIn = new JButton("Clock In");

    /** Clock-out button */
    private final JButton btnClockOut = new JButton("Clock Out");

    /** Quick navigation buttons */
    private final JButton btnMyProfile = new JButton("My Profile");
    private final JButton btnMyPayslip = new JButton("My Payslip");
    private final JButton btnLeave = new JButton("Leave Requests");

    // --- ADMIN ANALYTICS UI ---
    /** Container panel for admin analytics */
    private final JPanel adminAnalyticsPanel = new JPanel(new BorderLayout(12, 12));

    /** Admin metric labels */
    private final JLabel valAdminUsers = new JLabel("-");
    private final JLabel valAdminSessions = new JLabel("-");
    private final JLabel valAdminFailed = new JLabel("-");
    private final JLabel valAdminRegular = new JLabel("-");
    private final JLabel valAdminProbationary = new JLabel("-");

    /** Panel displaying status chips */
    private final JPanel statusChipsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

    /** Panel displaying simple charts */
    private final JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 12, 12));

    /** Labels used for chart bars */
    private final JLabel lblBarRegular = new JLabel();
    private final JLabel lblBarProbationary = new JLabel();

    /** Panel for recent activity logs */
    private final JPanel recentActivityPanel = new JPanel();

    /** Notification list model */
    private final DefaultListModel<String> notifModel = new DefaultListModel<>();

    /** Notification list UI component */
    private final JList<String> notifList = new JList<>(notifModel);

    /** UI theme colors */
    private static final Color BG = new Color(230, 242, 255);
    private static final Color CARD = Color.WHITE;
    private static final Color BORDER = new Color(220, 220, 220);
    private static final Color PRIMARY = new Color(52, 120, 246);

    /**
     * Creates a small analytics card for the admin dashboard.
     */
    private JPanel adminMiniCard(String title, JLabel value) {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBackground(new Color(248, 250, 255));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setForeground(new Color(90, 90, 90));

        value.setFont(new Font("Segoe UI", Font.BOLD, 18));
        value.setForeground(new Color(30, 30, 30));

        p.add(t, BorderLayout.NORTH);
        p.add(value, BorderLayout.CENTER);
        return p;
    }

    /**
     * Creates a colored status chip used in the admin analytics panel.
     */
    private JLabel createStatusChip(String text, Color bg, Color fg) {
        JLabel chip = new JLabel(text);
        chip.setOpaque(true);
        chip.setBackground(bg);
        chip.setForeground(fg);
        chip.setFont(new Font("Segoe UI", Font.BOLD, 12));
        chip.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        return chip;
    }

    /**
     * Creates a simple bar chart card used in admin analytics.
     */
    private JPanel simpleBarCard(String title, JLabel barLabel, Color fillColor) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JLabel h = new JLabel(title);
        h.setFont(new Font("Segoe UI", Font.BOLD, 13));
        h.setForeground(new Color(35, 35, 35));

        JPanel barBg = new JPanel(new BorderLayout());
        barBg.setBackground(new Color(235, 238, 245));
        barBg.setPreferredSize(new Dimension(200, 28));

        barLabel.setOpaque(true);
        barLabel.setBackground(fillColor);
        barLabel.setForeground(Color.WHITE);
        barLabel.setHorizontalAlignment(SwingConstants.CENTER);
        barLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        barLabel.setPreferredSize(new Dimension(0, 28));

        barBg.add(barLabel, BorderLayout.WEST);

        card.add(h, BorderLayout.NORTH);
        card.add(barBg, BorderLayout.CENTER);

        return card;
    }

    /**
     * Creates a UI row item representing recent activity.
     */
    private JPanel recentActivityItem(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(45, 45, 45));

        p.add(lbl, BorderLayout.CENTER);
        return p;
    }

    /** Formatter used for the real-time clock display */
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    // old constructor preserved (in case other code still calls it)
    public HomePanel(UserSession session,
                     AttendanceService attendanceService,
                     LeaveService leaveService,
                     EmployeeService employeeService,
                     UserDirectoryService userDirectoryService,
                     Navigator navigator) {
        this(session, attendanceService, leaveService, employeeService, userDirectoryService, navigator, null);
    }

    // new constructor
    public HomePanel(UserSession session,
                     AttendanceService attendanceService,
                     LeaveService leaveService,
                     EmployeeService employeeService,
                     UserDirectoryService userDirectoryService,
                     Navigator navigator,
                     AuditService auditService) {

        this.session = session;
        this.attendanceService = attendanceService;
        this.leaveService = leaveService;
        this.employeeService = employeeService;
        this.userDirectoryService = userDirectoryService;
        this.navigator = navigator;
        this.auditService = auditService;

        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        setBackground(BG);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        wireEvents();
        startClock();
        startAutoRefresh();
        refreshHome();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        String displayName = session.getUser().getUsername(); // fallback

        try {
            model.Employee emp = employeeService.getEmployeeForSession(session);
            if (emp != null) {
                String first = emp.getFirstName() == null ? "" : emp.getFirstName().trim();

                if (!first.isEmpty()) {
                    displayName = first;
                }
            }
        } catch (Exception ignored) {
            // keep username fallback
        }

        lblHello.setText("Hi, " + displayName + "!");
        lblHello.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHello.setForeground(new Color(30, 30, 30));

        lblTime.setHorizontalAlignment(SwingConstants.RIGHT);
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTime.setForeground(new Color(80, 80, 80));

        header.add(lblHello, BorderLayout.WEST);
        header.add(lblTime, BorderLayout.EAST);

        return header;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(12, 12));
        body.setOpaque(false);

        body.add(buildRoleDashboardRow(), BorderLayout.NORTH);

        JPanel main = new JPanel(new GridLayout(1, 2, 12, 12));
        main.setOpaque(false);

        JPanel leftCol = new JPanel(new GridLayout(2, 1, 12, 12));
        leftCol.setOpaque(false);

        leftCol.add(buildAttendanceCard());
        leftCol.add(buildQuickActionsCard());

        main.add(leftCol);
        main.add(buildNotificationsCard());

        body.add(main, BorderLayout.CENTER);
        return body;
    }

    private JPanel buildRoleDashboardRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 12, 12));
        row.setOpaque(false);

        // ADMIN
        if (session.getUser().can(model.Permission.ADMIN_ALL)) {
            row.add(dashboardTile("Role", valRoleLabel, false));
            row.add(dashboardTile("Total Employees", valPrimaryMetric, false));
            row.add(dashboardTile("Pending Leave Requests", valSecondaryMetric, false));
            return row;
        }

        // HR
        if (session.getUser().can(model.Permission.CRUD_EMPLOYEE)) {
            row.add(dashboardTile("Position", valRoleLabel, false));
            row.add(dashboardTile("Total Employees", valPrimaryMetric, false));
            row.add(dashboardTile("Pending Leave Requests", valSecondaryMetric, false));
            return row;
        }

        // Accountant
        if (session.getUser().can(model.Permission.COMPUTE_PAYROLL)) {
            row.add(dashboardTile("Position", valRoleLabel, false));
            row.add(dashboardTile("Total Payroll Active", valPrimaryMetric, false));
            row.add(dashboardTile("My Pending Leaves", valSecondaryMetric, false));
            return row;
        }

        // Employee
        row.add(dashboardTile("Position", valRoleLabel, false));
        row.add(dashboardTile("Total Hours Worked (Overall)", valPrimaryMetric, false));
        row.add(dashboardTile("My Pending Leaves", valSecondaryMetric, false));
        return row;
    }

    private JPanel dashboardTile(String title, JLabel value, boolean emphasize) {
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

    // Your improved attendance card (buttons visible)
    private JPanel buildAttendanceCard() {
        JPanel card = cardPanel("Attendance");

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        JLabel lblBigTime = new JLabel(LocalTime.now().format(TIME_FMT));
        lblBigTime.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblBigTime.setFont(new Font("Segoe UI", Font.BOLD, 34));
        lblBigTime.setForeground(new Color(30, 30, 30));

        JLabel lblSub = new JLabel("Current Time");
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(new Color(90, 90, 90));

        lblAttendance.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblAttendance.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblAttendance.setForeground(new Color(50, 50, 50));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        btnRow.setOpaque(false);

        stylePrimary(btnClockIn);
        stylePrimary(btnClockOut);

        btnClockIn.setPreferredSize(new Dimension(120, 38));
        btnClockOut.setPreferredSize(new Dimension(120, 38));

        btnRow.add(btnClockIn);
        btnRow.add(btnClockOut);

        inner.add(Box.createVerticalGlue());
        inner.add(lblSub);
        inner.add(Box.createVerticalStrut(8));
        inner.add(lblBigTime);
        inner.add(Box.createVerticalStrut(14));
        inner.add(lblAttendance);
        inner.add(Box.createVerticalStrut(16));
        inner.add(btnRow);
        inner.add(Box.createVerticalGlue());

        Timer bigClockTimer = new Timer(1000, e -> lblBigTime.setText(LocalTime.now().format(TIME_FMT)));
        bigClockTimer.start();

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildQuickActionsCard() {
        JPanel card = cardPanel("Quick Actions");

        JPanel inner = new JPanel(new GridLayout(3, 1, 10, 10));
        inner.setOpaque(false);

        styleOutline(btnMyProfile);
        styleOutline(btnMyPayslip);
        styleOutline(btnLeave);

        inner.add(btnMyProfile);
        inner.add(btnMyPayslip);
        inner.add(btnLeave);

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildNotificationsCard() {
        if (session.getUser().can(model.Permission.ADMIN_ALL)) {
            return buildAdminAnalyticsCard();
        }

        JPanel card = cardPanel("Notifications");

        notifList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        notifList.setVisibleRowCount(10);
        notifList.setFixedCellHeight(24);
        notifList.setSelectionBackground(Color.WHITE);
        notifList.setSelectionForeground(new Color(40, 40, 40));

        JScrollPane sp = new JScrollPane(notifList);
        sp.getViewport().setBackground(Color.WHITE);
        sp.setBorder(BorderFactory.createEmptyBorder());

        JButton btnRefresh = new JButton("Refresh");
        styleOutline(btnRefresh);
        btnRefresh.addActionListener(e -> refreshHome());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.add(btnRefresh);

        card.add(sp, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }

    private JPanel cardPanel(String title) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(new Color(40, 40, 40));

        card.add(lbl, BorderLayout.NORTH);
        return card;
    }

    private void wireEvents() {
        btnClockIn.addActionListener(e -> onClockIn());
        btnClockOut.addActionListener(e -> onClockOut());

        btnMyProfile.addActionListener(e -> { if (navigator != null) navigator.goTo("MY_PROFILE"); });
        btnMyPayslip.addActionListener(e -> { if (navigator != null) navigator.goTo("MY_PAYSLIP"); });
        btnLeave.addActionListener(e -> { if (navigator != null) navigator.goTo("LEAVE"); });
    }

    private void startClock() {
        Timer t = new Timer(1000, e -> lblTime.setText("Time: " + LocalTime.now().format(TIME_FMT)));
        t.start();
    }

    private void startAutoRefresh() {
        Timer t = new Timer(5000, e -> refreshHome());
        t.start();
    }

    
    private void refreshHome() {
        valRoleLabel.setText(getDisplayPosition());

        // always refresh attendance card
        refreshAttendance();

        // ADMIN tiles
        if (session.getUser().can(model.Permission.ADMIN_ALL)) {
            try {
                valPrimaryMetric.setText(String.valueOf(
                        employeeService.countEmployeesForPayroll(session.getUser())
                ));
            } catch (Exception ex) {
                ex.printStackTrace();
                valPrimaryMetric.setText("-");
            }

            try {
                long pending = leaveService.listForSession(session).stream()
                        .filter(r -> r.getStatus() == model.LeaveStatus.PENDING)
                        .count();
                valSecondaryMetric.setText(String.valueOf(pending));
            } catch (Exception ex) {
                ex.printStackTrace();
                valSecondaryMetric.setText("-");
            }

            loadNotifications();
            return;
        }

        // HR tiles
        if (session.getUser().can(model.Permission.CRUD_EMPLOYEE)) {
            try {
                valPrimaryMetric.setText(String.valueOf(
                        employeeService.countEmployeesForPayroll(session.getUser())
                ));
            } catch (Exception ex) {
                ex.printStackTrace();
                valPrimaryMetric.setText("-");
            }

            try {
                long pending = leaveService.listForSession(session).stream()
                        .filter(r -> r.getStatus() == model.LeaveStatus.PENDING)
                        .count();
                valSecondaryMetric.setText(String.valueOf(pending));
            } catch (Exception ex) {
                ex.printStackTrace();
                valSecondaryMetric.setText("-");
            }

            loadNotifications();
            return;
        }

        // Accountant tiles
        if (session.getUser().can(model.Permission.COMPUTE_PAYROLL)) {
            try {
                valPrimaryMetric.setText(String.valueOf(
                        employeeService.countEmployeesForPayroll(session.getUser())
                ));
            } catch (Exception ex) {
                ex.printStackTrace();
                valPrimaryMetric.setText("-");
            }

            try {
                long pending = leaveService.listForSession(session).stream()
                        .filter(r -> r.getStatus() == model.LeaveStatus.PENDING)
                        .count();
                valSecondaryMetric.setText(String.valueOf(pending));
            } catch (Exception ex) {
                ex.printStackTrace();
                valSecondaryMetric.setText("-");
            }

            loadNotifications();
            return;
        }

        // Employee tiles
        try {
            String empNo = session.getAccount() == null ? null : session.getAccount().getEmployeeNumber();
            double hours = attendanceService.getTotalHoursWorkedOverall(empNo);
            valPrimaryMetric.setText(String.format("%.2f", hours));
        } catch (Exception ex) {
            ex.printStackTrace();
            valPrimaryMetric.setText("-");
        }

        try {
            long pending = leaveService.listForSession(session).stream()
                    .filter(r -> r.getStatus() == model.LeaveStatus.PENDING)
                    .count();
            valSecondaryMetric.setText(String.valueOf(pending));
        } catch (Exception ex) {
            ex.printStackTrace();
            valSecondaryMetric.setText("-");
        }

        loadNotifications();
    }
    private String getEmployeeNumber() {
        return (session.getAccount() == null) ? null : session.getAccount().getEmployeeNumber();
    }

    private void refreshDashboardTiles() {
        try {
            // IT
            if (session.getUser().can(model.Permission.ADMIN_ALL)) {
                int totalUsers = userDirectoryService.getActiveUserCount(session.getUser()); // credentials count
                int activeSessions = SessionManager.getActiveSessionCount();

                valPrimaryMetric.setText(String.valueOf(totalUsers));
                valSecondaryMetric.setText(String.valueOf(activeSessions));
                return;
            }

            // HR
            if (session.getUser().can(model.Permission.CRUD_EMPLOYEE)) {
                int total = employeeService.getAllEmployees(session.getUser()).size();
                valPrimaryMetric.setText(String.valueOf(total));

                List<LeaveRequest> leaves = leaveService.listForSession(session);
                long pending = leaves.stream()
                        .filter(r -> r.getStatus() != null)
                        .filter(r -> "PENDING".equalsIgnoreCase(r.getStatus().toString()))
                        .count();

                valSecondaryMetric.setText(String.valueOf(pending));
                return;
            }

            // Accountant
            if (session.getUser().can(model.Permission.COMPUTE_PAYROLL)) {
                int totalPayrollActive = employeeService.countEmployeesForPayroll(session.getUser());
                valPrimaryMetric.setText(String.valueOf(totalPayrollActive));

                List<LeaveRequest> leaves = leaveService.listForSession(session);
                long myPending = leaves.stream()
                        .filter(r -> r.getStatus() != null)
                        .filter(r -> "PENDING".equalsIgnoreCase(r.getStatus().toString()))
                        .count();

                valSecondaryMetric.setText(String.valueOf(myPending));
                return;
            }

            // Employee
            String empNo = getEmployeeNumber();
            double hours = attendanceService.getTotalHoursWorkedOverall(empNo);
            valPrimaryMetric.setText(String.format("%.2f hrs", hours));

            List<LeaveRequest> leaves = leaveService.listForSession(session);
            long pending = leaves.stream()
                    .filter(r -> r.getStatus() != null)
                    .filter(r -> "PENDING".equalsIgnoreCase(r.getStatus().toString()))
                    .count();

            valSecondaryMetric.setText(String.valueOf(pending));

        } catch (Exception e) {
            valPrimaryMetric.setText("-");
            valSecondaryMetric.setText("-");
        }
    }

    private void refreshAttendance() {
        try {
            String empNo = getEmployeeNumber();

            if (empNo == null || empNo.isBlank()) {
                lblAttendance.setText("Today: (No employee number linked)");
                btnClockIn.setEnabled(false);
                btnClockOut.setEnabled(false);
                return;
            }

            AttendanceRecord r = attendanceService.getTodayRecord(empNo);

            if (r == null) {
                lblAttendance.setText("Today: No record yet");
            } else {
                String in = (r.getLogIn() == null) ? "-" : r.getLogIn().toString();
                String out = (r.getLogOut() == null) ? "-" : r.getLogOut().toString();
                lblAttendance.setText("Today: IN " + in + " | OUT " + out);
            }

            btnClockIn.setEnabled(true);
            btnClockOut.setEnabled(true);

        } catch (Exception ex) {
            lblAttendance.setText("Today: Error reading attendance");
        }
    }

    private void refreshNotifications() {
        notifModel.clear();

        // IT: show security + recent activity
        if (session.getUser().can(model.Permission.ADMIN_ALL) && auditService != null) {
            try {
                int failedToday = auditService.getFailedLoginsToday(session.getUser());
                notifModel.addElement("• Failed logins today: " + failedToday);

                List<model.UserActivityLogEntry> recent = auditService.getRecentLogs(session.getUser(), 6);
                if (recent.isEmpty()) {
                    notifModel.addElement("• No activity yet.");
                } else {
                    for (model.UserActivityLogEntry e : recent) {
                        notifModel.addElement("• " + e.getTimestamp() + " | " + e.getUsername() + " | " + e.getAction());
                    }
                }
                return;
            } catch (Exception ex) {
                notifModel.addElement("• Logs unavailable.");
                return;
            }
        }

        // Non-IT (keep simple)
        notifModel.addElement("• Tip: Use the menu to access your modules.");
    }

    private void onClockIn() {
        try {
            String empNo = getEmployeeNumber();
            attendanceService.clockIn(session.getUser(), empNo);

            if (auditService != null) {
                auditService.log(session.getUser().getUsername(), "CLOCK_IN",
                        "employeeNumber=" + safeText(empNo));
            }

            JOptionPane.showMessageDialog(this, "Clock-in recorded.");
            refreshAttendance();
            loadNotifications();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Clock In Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onClockOut() {
        try {
            String empNo = getEmployeeNumber();
            attendanceService.clockOut(session.getUser(), empNo);

            if (auditService != null) {
                auditService.log(session.getUser().getUsername(), "CLOCK_OUT",
                        "employeeNumber=" + safeText(empNo));
            }

            JOptionPane.showMessageDialog(this, "Clock-out recorded.");
            refreshAttendance();
            loadNotifications();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Clock Out Failed", JOptionPane.ERROR_MESSAGE);
        }
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
    
    private String getDisplayPosition() {
        try {
            model.Employee emp = employeeService.getEmployeeForSession(session);
            if (emp != null && emp.getPosition() != null && !emp.getPosition().trim().isEmpty()) {
                return emp.getPosition().trim();
            }
        } catch (Exception ignored) {
            // fallback below
        }
        return "-";
    }
    
    private void loadNotifications() {
        notifModel.clear();

        try {
            // ADMIN keeps current analytics panel behavior
            if (session.getUser().can(model.Permission.ADMIN_ALL)) {
                int totalUsers = userDirectoryService.getActiveUserCount(session.getUser());
                int activeSessions = service.SessionManager.getActiveSessionCount();
                int failedToday = (auditService == null) ? 0 : auditService.getFailedLoginsToday(session.getUser());

                java.util.List<model.Employee> employees = employeeService.getAllEmployees(session.getUser());

                long regularCount = employees.stream()
                        .filter(e -> e.getStatus() != null)
                        .filter(e -> "Regular".equalsIgnoreCase(e.getStatus().trim()))
                        .count();

                long probationaryCount = employees.stream()
                        .filter(e -> e.getStatus() != null)
                        .filter(e -> "Probationary".equalsIgnoreCase(e.getStatus().trim()))
                        .count();

                valAdminUsers.setText(String.valueOf(totalUsers));
                valAdminSessions.setText(String.valueOf(activeSessions));
                valAdminFailed.setText(String.valueOf(failedToday));
                valAdminRegular.setText(String.valueOf(regularCount));
                valAdminProbationary.setText(String.valueOf(probationaryCount));

                statusChipsPanel.removeAll();
                statusChipsPanel.add(createStatusChip("Users: " + totalUsers, new Color(227, 242, 253), new Color(13, 71, 161)));
                statusChipsPanel.add(createStatusChip("Sessions: " + activeSessions, new Color(232, 245, 233), new Color(27, 94, 32)));
                statusChipsPanel.add(createStatusChip("Failed Logins: " + failedToday, new Color(255, 235, 238), new Color(183, 28, 28)));

                int totalEmployees = (int) (regularCount + probationaryCount);
                int barMax = Math.max(totalEmployees, 1);

                int regularWidth = (int) Math.max(40, (220.0 * regularCount) / barMax);
                int probationaryWidth = (int) Math.max(40, (220.0 * probationaryCount) / barMax);

                lblBarRegular.setPreferredSize(new Dimension(regularWidth, 28));
                lblBarRegular.setText(String.valueOf(regularCount));

                lblBarProbationary.setPreferredSize(new Dimension(probationaryWidth, 28));
                lblBarProbationary.setText(String.valueOf(probationaryCount));

                recentActivityPanel.removeAll();
                if (auditService != null) {
                    java.util.List<model.UserActivityLogEntry> logs = auditService.getRecentLogs(session.getUser(), 8);

                    if (logs.isEmpty()) {
                        recentActivityPanel.add(recentActivityItem("No recent system activity."));
                    } else {
                        for (model.UserActivityLogEntry log : logs) {
                            recentActivityPanel.add(recentActivityItem(
                                    formatLogLine(log)
                            ));
                        }
                    }
                } else {
                    recentActivityPanel.add(recentActivityItem("Audit service unavailable."));
                }

                recentActivityPanel.revalidate();
                recentActivityPanel.repaint();
                statusChipsPanel.revalidate();
                statusChipsPanel.repaint();
                chartsPanel.revalidate();
                chartsPanel.repaint();
                return;
            }

            // Non-admin notifications
            java.util.List<String> items = new java.util.ArrayList<>();

            String currentUsername = session.getUser().getUsername();
            String empNo = (session.getAccount() == null) ? null : session.getAccount().getEmployeeNumber();

            // 1) Recent personal activity from user_logs.csv
            java.util.List<UserActivityLogEntry> myLogs = userLogDAO.findAll().stream()
                    .filter(log -> currentUsername.equalsIgnoreCase(log.getUsername()))
                    .sorted(Comparator.comparing(UserActivityLogEntry::getTimestamp).reversed())
                    .limit(6)
                    .collect(Collectors.toList());

            for (UserActivityLogEntry log : myLogs) {
                items.add(formatPersonalNotification(log));
            }

            // 2) Attendance-based notification from attendance.csv
            if (empNo != null && !empNo.isBlank()) {
                model.AttendanceRecord today = attendanceService.getTodayRecord(empNo);

                if (today != null) {
                    if (today.getLogIn() != null && today.getLogOut() == null) {
                        items.add("Attendance | You clocked in today at " + today.getLogIn() + ".");
                    } else if (today.getLogIn() != null && today.getLogOut() != null) {
                        items.add("Attendance | Your attendance is complete today. In: " + today.getLogIn() + " | Out: " + today.getLogOut());
                    }
                }
            }

            // 3) Leave request updates from leave_requests.csv
            java.util.List<LeaveRequest> myLeaves = leaveService.listForSession(session).stream()
                    .sorted(Comparator.comparing(LeaveRequest::getSubmittedAt,
                            Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                    .limit(6)
                    .collect(Collectors.toList());

            for (LeaveRequest leave : myLeaves) {
                if (leave.getStatus() == LeaveStatus.APPROVED && leave.getDecisionAt() != null) {
                    items.add("Leave Approved | " + leave.getType() + " leave was approved at "
                            + leave.getDecisionAt().format(NOTIF_DTF) + ".");
                } else if (leave.getStatus() == LeaveStatus.REJECTED && leave.getDecisionAt() != null) {
                    String reason = leave.getDecisionReason() == null || leave.getDecisionReason().isBlank()
                            ? ""
                            : " Reason: " + leave.getDecisionReason();
                    items.add("Leave Rejected | " + leave.getType() + " leave was rejected at "
                            + leave.getDecisionAt().format(NOTIF_DTF) + "." + reason);
                } else if (leave.getStatus() == LeaveStatus.PENDING && leave.getSubmittedAt() != null) {
                    items.add("Leave Pending | Your " + leave.getType() + " leave is still pending since "
                            + leave.getSubmittedAt().format(NOTIF_DTF) + ".");
                }
            }

            // 4) Fallback
            if (items.isEmpty()) {
                items.add("No new notifications yet.");
            }

            // Keep newest first, remove duplicates while preserving order
            java.util.LinkedHashSet<String> unique = new java.util.LinkedHashSet<>(items);
            for (String item : unique) {
                notifModel.addElement(item);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            notifModel.clear();
            notifModel.addElement("Unable to load notifications.");
        }
    }

    private String formatLogLine(UserActivityLogEntry log) {
        if (log == null || log.getTimestamp() == null) return "System activity recorded.";

        String time = log.getTimestamp().format(NOTIF_DTF);
        String user = safeText(log.getUsername());
        String action = safeText(log.getAction());

        return time + " | " + user + " | " + action;
    }

    private String formatPersonalNotification(UserActivityLogEntry log) {
        if (log == null || log.getTimestamp() == null) return "Activity recorded.";

        String time = log.getTimestamp().format(NOTIF_DTF);
        String action = safeText(log.getAction());

        switch (action.toUpperCase()) {
            case "LOGIN_SUCCESS":
                return "Welcome back | You logged in at " + time + ".";
            case "CLOCK_IN":
                return "Attendance | You clocked in at " + time + ".";
            case "CLOCK_OUT":
                return "Attendance | You clocked out at " + time + ".";
            case "FILE_LEAVE":
                return "Leave Request | You submitted a leave request at " + time + ".";
            case "CHANGE_PASSWORD":
                return "Account Security | You changed your password at " + time + ".";
            case "CHANGE_USERNAME":
                return "Account Update | You changed your username at " + time + ".";
            default:
                return action + " | " + time;
        }
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }
    private JPanel buildAdminAnalyticsCard() {
        JPanel card = cardPanel("Admin Analytics");

        adminAnalyticsPanel.setOpaque(false);

        JPanel topStats = new JPanel(new GridLayout(1, 3, 10, 10));
        topStats.setOpaque(false);
        topStats.add(adminMiniCard("System Users", valAdminUsers));
        topStats.add(adminMiniCard("Active Sessions", valAdminSessions));
        topStats.add(adminMiniCard("Failed Logins Today", valAdminFailed));

        JPanel middleStats = new JPanel(new GridLayout(1, 2, 10, 10));
        middleStats.setOpaque(false);
        middleStats.add(adminMiniCard("Regular Employees", valAdminRegular));
        middleStats.add(adminMiniCard("Probationary Employees", valAdminProbationary));

        statusChipsPanel.setOpaque(false);

        chartsPanel.setOpaque(false);
        chartsPanel.removeAll();
        chartsPanel.add(simpleBarCard("Regular Employees", lblBarRegular, new Color(46, 125, 50)));
        chartsPanel.add(simpleBarCard("Probationary Employees", lblBarProbationary, new Color(239, 108, 0)));

        recentActivityPanel.setOpaque(false);
        recentActivityPanel.setLayout(new BoxLayout(recentActivityPanel, BoxLayout.Y_AXIS));

        JScrollPane recentSp = new JScrollPane(recentActivityPanel);
        recentSp.setBorder(BorderFactory.createTitledBorder("Recent Activity"));
        recentSp.getViewport().setBackground(Color.WHITE);

        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));

        topStats.setAlignmentX(Component.LEFT_ALIGNMENT);
        middleStats.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusChipsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        chartsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        recentSp.setAlignmentX(Component.LEFT_ALIGNMENT);

        stack.add(topStats);
        stack.add(Box.createVerticalStrut(12));
        stack.add(middleStats);
        stack.add(Box.createVerticalStrut(12));
        stack.add(statusChipsPanel);
        stack.add(Box.createVerticalStrut(12));
        stack.add(chartsPanel);
        stack.add(Box.createVerticalStrut(12));
        stack.add(recentSp);

        adminAnalyticsPanel.removeAll();
        adminAnalyticsPanel.add(stack, BorderLayout.CENTER);

        JButton btnRefresh = new JButton("Refresh");
        styleOutline(btnRefresh);
        btnRefresh.addActionListener(e -> refreshHome());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.add(btnRefresh);

        card.add(adminAnalyticsPanel, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }
}