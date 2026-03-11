package view;

import model.Employee;
import model.UserSession;
import service.AuthService;
import service.EmployeeService;

import javax.swing.*;
import java.awt.*;

/**
 * =============================================================
 * MyProfilePanel
 * =============================================================
 *
 * VIEW LAYER PANEL
 *
 * Displays the profile information of the currently logged-in
 * employee in the MotorPH system.
 *
 * Features:
 * - Shows employee information (name, employee number, role)
 * - Displays government numbers (TIN, SSS, PhilHealth, Pag-IBIG)
 * - Allows the user to change username
 * - Allows the user to change password
 * - Allows navigation back to the Home panel
 *
 * Architecture:
 * VIEW → SERVICE → DAO → CSV
 */

public class MyProfilePanel extends JPanel {

    /**
     * Navigator interface
     * Used to switch panels inside the main dashboard.
     */
    public interface Navigator {
        void goTo(String cardName);
    }

    /** Current logged-in session */
    private final UserSession session;

    /** Authentication service (handles username/password changes) */
    private final AuthService authService;

    /** Navigator used for switching cards in the dashboard */
    private final Navigator navigator;

    /** Employee service used to retrieve employee data */
    private final EmployeeService employeeService;

    // =============================================================
    // THEME COLORS
    // =============================================================

    private static final Color BG = new Color(230, 242, 255);
    private static final Color CARD = Color.WHITE;
    private static final Color BORDER = new Color(230, 230, 230);
    private static final Color PRIMARY = new Color(52, 120, 246);
    private static final Color MUTED = new Color(95, 95, 95);
    private static final Color TILE_BG = new Color(248, 250, 255);

    // =============================================================
    // PROFILE DISPLAY FIELDS
    // =============================================================

    /**
     * Large name field (TextArea used so long names wrap properly)
     */
    private final JTextArea bigName = new JTextArea();

    /** Employee number label */
    private final JLabel empNoVal = new JLabel("-");

    /** Role label */
    private final JLabel roleVal = new JLabel("-");

    /**
     * Position field (TextArea allows wrapping of long job titles)
     */
    private final JTextArea posVal = new JTextArea();

    /** Government numbers */
    private final JLabel tinVal = new JLabel("-");
    private final JLabel sssVal = new JLabel("-");
    private final JLabel philVal = new JLabel("-");
    private final JLabel pagibigVal = new JLabel("-");

    // =============================================================
    // BUTTONS
    // =============================================================

    /** Navigate back to Home dashboard */
    private final JButton btnBackHome = new JButton("Back to Home");

    /** Change username button */
    private final JButton btnChangeUsername = new JButton("Change Username");

    /** Change password button */
    private final JButton btnChangePassword = new JButton("Change Password");

    /**
     * Constructor
     * Initializes layout and loads employee profile data.
     */
    public MyProfilePanel(UserSession session,
                          AuthService authService,
                          EmployeeService employeeService,
                          Navigator navigator) {

        this.session = session;
        this.authService = authService;
        this.employeeService = employeeService;
        this.navigator = navigator;

        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        setBackground(BG);

        // Configure wrap behavior for employee name
        bigName.setEditable(false);
        bigName.setOpaque(false);
        bigName.setLineWrap(true);
        bigName.setWrapStyleWord(true);
        bigName.setFont(new Font("Segoe UI", Font.BOLD, 34));
        bigName.setForeground(new Color(25, 25, 25));
        bigName.setBorder(null);
        bigName.setFocusable(false);
        bigName.setText("-");

        // Configure wrap behavior for position
        posVal.setEditable(false);
        posVal.setOpaque(false);
        posVal.setLineWrap(true);
        posVal.setWrapStyleWord(true);
        posVal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        posVal.setForeground(new Color(25, 25, 25));
        posVal.setBorder(null);
        posVal.setFocusable(false);
        posVal.setText("-");

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        wireEvents();
        loadProfile();
    }

    // =============================================================
    // LAYOUT BUILDERS
    // =============================================================

    /**
     * Builds the top navigation bar.
     * Contains:
     * - Panel title
     * - Back to Home button
     */
    private JPanel buildTopBar() {

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel title = new JLabel("My Profile");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(30, 30, 30));

        styleOutline(btnBackHome);

        top.add(title, BorderLayout.WEST);
        top.add(btnBackHome, BorderLayout.EAST);

        return top;
    }

    /**
     * Builds the main panel body.
     * Layout: 2 columns
     *
     * Left: Profile information
     * Right: Account settings
     */
    private JPanel buildBody() {

        JPanel body = new JPanel(new GridLayout(1, 2, 12, 12));
        body.setOpaque(false);

        body.add(buildLeftProfileCard());
        body.add(buildRightAccountCard());

        return body;
    }

    /**
     * Builds the left card containing:
     * - Name
     * - Employee number
     * - Role
     * - Position
     * - Government IDs
     */
    private JPanel buildLeftProfileCard() {

        JPanel card = cardPanel();
        card.setLayout(new BorderLayout());

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        JLabel secTitle = new JLabel("Profile Information");
        secTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        secTitle.setForeground(new Color(35, 35, 35));
        secTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLabel = new JLabel("Name");
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nameLabel.setForeground(MUTED);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        bigName.setAlignmentX(Component.LEFT_ALIGNMENT);

        inner.add(secTitle);
        inner.add(Box.createVerticalStrut(10));
        inner.add(nameLabel);
        inner.add(Box.createVerticalStrut(6));
        inner.add(bigName);
        inner.add(Box.createVerticalStrut(14));

        // Employee stats row
        JPanel stats = new JPanel(new GridLayout(1, 3, 12, 12));
        stats.setOpaque(false);
        stats.setAlignmentX(Component.LEFT_ALIGNMENT);

        stats.add(statCard("Employee", empNoVal));
        stats.add(statCard("Role", roleVal));
        stats.add(statCardWrap("Position", posVal));

        inner.add(stats);
        inner.add(Box.createVerticalStrut(12));

        // Government ID tiles
        JPanel tiles = new JPanel(new GridLayout(2, 2, 12, 12));
        tiles.setOpaque(false);
        tiles.setAlignmentX(Component.LEFT_ALIGNMENT);

        tiles.add(infoTile("TIN #", tinVal));
        tiles.add(infoTile("SSS #", sssVal));
        tiles.add(infoTile("PhilHealth #", philVal));
        tiles.add(infoTile("Pag-ibig #", pagibigVal));

        inner.add(tiles);
        inner.add(Box.createVerticalGlue());

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    /**
     * Builds the right card containing account settings.
     * Allows the user to:
     * - Change username
     * - Change password
     */
    private JPanel buildRightAccountCard() {

        JPanel card = cardPanel();
        card.setLayout(new BorderLayout());

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Account Settings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(new Color(35, 35, 35));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel desc = new JLabel(
                "<html><body style='width:320px'>Manage your account credentials here. Changing username will affect your future logins.</body></html>"
        );

        desc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        desc.setForeground(MUTED);
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);

        inner.add(title);
        inner.add(Box.createVerticalStrut(6));
        inner.add(desc);
        inner.add(Box.createVerticalStrut(14));

        stylePrimary(btnChangeUsername);
        stylePrimary(btnChangePassword);

        btnChangeUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnChangePassword.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnChangeUsername.setPreferredSize(new Dimension(220, 38));
        btnChangePassword.setPreferredSize(new Dimension(220, 38));

        inner.add(btnChangeUsername);
        inner.add(Box.createVerticalStrut(10));
        inner.add(btnChangePassword);
        inner.add(Box.createVerticalGlue());

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    // ---------------- COMPONENTS ----------------

    private JPanel cardPanel() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));
        return card;
    }

    private JPanel statCard(String label, JLabel value) {
        JPanel tile = new JPanel(new BorderLayout(0, 6));
        tile.setBackground(CARD);
        tile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(235, 235, 235)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(MUTED);

        value.setFont(new Font("Segoe UI", Font.BOLD, 14));
        value.setForeground(new Color(25, 25, 25));
        value.setVerticalAlignment(SwingConstants.TOP);

        tile.add(lbl, BorderLayout.NORTH);
        tile.add(value, BorderLayout.CENTER);
        return tile;
    }

    private JPanel statCardWrap(String label, JTextArea valueArea) {
        JPanel tile = new JPanel(new BorderLayout(0, 6));
        tile.setBackground(CARD);
        tile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(235, 235, 235)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(MUTED);

        valueArea.setMargin(new Insets(0, 0, 0, 0));

        tile.add(lbl, BorderLayout.NORTH);
        tile.add(valueArea, BorderLayout.CENTER);
        return tile;
    }

    private JPanel infoTile(String label, JLabel value) {
        JPanel tile = new JPanel(new BorderLayout(0, 6));
        tile.setBackground(TILE_BG);
        tile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(235, 235, 235)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(MUTED);

        value.setFont(new Font("Segoe UI", Font.BOLD, 13));
        value.setForeground(new Color(25, 25, 25));
        value.setVerticalAlignment(SwingConstants.TOP);

        tile.add(lbl, BorderLayout.NORTH);
        tile.add(value, BorderLayout.CENTER);
        return tile;
    }

    // ---------------- EVENTS ----------------

    private void wireEvents() {
        btnBackHome.addActionListener(e -> {
            if (navigator != null) navigator.goTo("HOME");
        });

        btnChangePassword.addActionListener(e -> openChangePasswordDialog());
        btnChangeUsername.addActionListener(e -> openChangeUsernameDialog());
    }

    private void openChangePasswordDialog() {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        ChangePasswordDialog dialog = new ChangePasswordDialog(owner, authService, session.getUser().getUsername());
        dialog.setVisible(true);
    }

    private void openChangeUsernameDialog() {
        String current = session.getUser().getUsername();

        JTextField txtNew = new JTextField(18);
        JPasswordField txtConfirmPass = new JPasswordField(18);

        JPanel p = new JPanel(new GridLayout(0, 1, 6, 6));
        p.add(new JLabel("Current Username: " + current));
        p.add(new JLabel("New Username:"));
        p.add(txtNew);
        p.add(new JLabel("Confirm Password:"));
        p.add(txtConfirmPass);

        int res = JOptionPane.showConfirmDialog(
                this, p, "Change Username",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (res != JOptionPane.OK_OPTION) return;

        String newUsername = txtNew.getText().trim();
        String confirmPass = new String(txtConfirmPass.getPassword());

        if (newUsername.isBlank()) {
            JOptionPane.showMessageDialog(this, "New username cannot be empty.");
            return;
        }

        try {
            authService.updateUsername(current, newUsername, confirmPass);

            JOptionPane.showMessageDialog(this, "Username updated. Please log in again.");

            Window w = SwingUtilities.getWindowAncestor(this);
            if (w != null) w.dispose();
            new LogInFrame().setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------------- DATA LOAD ----------------

    private void loadProfile() {
        String empNo = (session.getAccount() != null) ? session.getAccount().getEmployeeNumber() : null;
        String role = (session.getAccount() != null) ? session.getAccount().getRole() : null;

        empNoVal.setText(nvl(empNo));
        roleVal.setText(nvl(role));

        Employee emp = session.getEmployee();

        if (emp == null && empNo != null && !empNo.isBlank()) {
            try {
                emp = employeeService.getEmployeeById(session.getUser(), empNo.trim());
            } catch (Exception ignored) { }
        }

        if (emp == null) {
            try {
                emp = employeeService.getEmployeeForSession(session);
            } catch (Exception ex) {
                bigName.setText("-");
                posVal.setText("-");
                tinVal.setText("-");
                sssVal.setText("-");
                philVal.setText("-");
                pagibigVal.setText("-");
                return;
            }
        }

        bigName.setText(nvl(emp.getFullName()));
        posVal.setText(nvl(emp.getPosition()));

        tinVal.setText(nvl(emp.getTinNumber()));
        sssVal.setText(nvl(emp.getSssNumber()));
        philVal.setText(nvl(emp.getPhilhealthNumber()));
        pagibigVal.setText(nvl(emp.getPagIbigNumber()));
    }

    private String nvl(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    // ---------------- STYLES ----------------

    private void stylePrimary(JButton b) {
        b.setFocusPainted(false);
        b.setBackground(PRIMARY);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
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