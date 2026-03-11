package view;

import dao.CredentialDAO;
import dao.EmployeeDAO;
import dao.UserActivityLogDAO;
import model.UserSession;
import service.AuditService;
import service.AuthService;
import service.PasswordService;

import javax.swing.*;
import java.awt.*;

/**
 * =============================================================
 * VIEW CLASS: LogInFrame
 * =============================================================
 *
 * PURPOSE
 * This class represents the login window of the MotorPH Payroll
 * System. It is the first screen users interact with when the
 * application starts.
 *
 * RESPONSIBILITIES
 * - Display the login interface
 * - Collect username and password from the user
 * - Send credentials to AuthService for authentication
 * - Open the MainDashboardFrame if login is successful
 * - Allow users to open the Change Password dialog
 *
 * ARCHITECTURE ROLE
 *
 * DAO Layer
 * -> CredentialDAO reads user credentials from CSV
 * -> EmployeeDAO retrieves employee information
 * -> UserActivityLogDAO records system activity logs
 *
 * Service Layer
 * -> AuthService validates login credentials
 * -> PasswordService handles password security
 * -> AuditService records login activity
 *
 * View Layer (THIS CLASS)
 * -> Displays the login UI
 * -> Handles user interaction
 */
public class LogInFrame extends JFrame {

    /** Username input field */
    private final JTextField txtUsername = new JTextField();

    /** Password input field */
    private final JPasswordField txtPassword = new JPasswordField();

    /** Login button used to authenticate the user */
    private final JButton btnLogin = new JButton("Login");

    /** Button used to open the change password dialog */
    private final JButton btnChangePassword = new JButton("Change Password");

    /** AuthService handles authentication logic */
    private final AuthService authService;

    /** Path of the MotorPH logo used in the login screen */
    private static final String LOGO_PATH = "data/motorph_logo.png";

    // Theme
    /** Background color used across the login UI */
    private static final Color BG = new Color(230, 242, 255);

    /** Card background color */
    private static final Color CARD = Color.WHITE;

    /** Border color used for UI components */
    private static final Color BORDER = new Color(220, 220, 220);

    /** Primary accent color used for buttons */
    private static final Color PRIMARY = new Color(52, 120, 246);

    /** Primary text color */
    private static final Color TEXT = new Color(30, 30, 30);

    /** Muted text color used for labels */
    private static final Color MUTED = new Color(90, 90, 90);

    /** Input field background color */
    private static final Color INPUT_BG = new Color(248, 250, 255);

    /**
     * =============================================================
     * CONSTRUCTOR
     * =============================================================
     *
     * Initializes the login window and sets up all required
     * services used for authentication.
     *
     * Steps performed:
     * 1. Create DAO objects for reading CSV data
     * 2. Create services required for authentication
     * 3. Configure JFrame window properties
     * 4. Build the login interface
     * 5. Register button event listeners
     */
    public LogInFrame() {
        super("MotorPH Login");

        CredentialDAO credentialDAO = new CredentialDAO("data/credentials.csv");
        EmployeeDAO employeeDAO = new EmployeeDAO("data/employees.csv");
        PasswordService passwordService = new PasswordService();
        AuditService auditService = new AuditService(new UserActivityLogDAO("data/user_logs.csv"));

        // AuthService is responsible for validating login credentials
        this.authService = new AuthService(credentialDAO, employeeDAO, passwordService, auditService);

        // Configure main window properties
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(760, 520);
        setMinimumSize(new Dimension(720, 500));
        setLocationRelativeTo(null);
        setResizable(false);

        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        // Build and attach the main login UI
        add(buildMainContent(), BorderLayout.CENTER);

        // Button event handlers
        btnLogin.addActionListener(e -> onLogin());
        btnChangePassword.addActionListener(e -> onChangePassword());

        // Pressing ENTER triggers login
        getRootPane().setDefaultButton(btnLogin);
    }

    /**
     * Builds the main container of the login screen.
     *
     * The layout consists of:
     * - Left section for branding and logo
     * - Right section for the login form
     */
    private JPanel buildMainContent() {
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setBackground(BG);
        wrap.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel card = new JPanel(new BorderLayout(24, 24));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(26, 26, 26, 26)
        ));
        card.setPreferredSize(new Dimension(680, 430));

        card.add(buildBrandPanel(), BorderLayout.WEST);
        card.add(buildLoginPanel(), BorderLayout.CENTER);

        wrap.add(card);
        return wrap;
    }

    /**
     * Builds the branding section shown on the left side
     * of the login window.
     *
     * This panel contains:
     * - MotorPH logo
     * - System title
     * - Short system description
     */
    private JPanel buildBrandPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(280, 0));

        JLabel logoLabel = new JLabel(loadScaledLogo(220, 140));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel brandTitle = new JLabel("MotorPH");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        brandTitle.setForeground(TEXT);
        brandTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel brandSubtitle = new JLabel("Payroll Management System");
        brandSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        brandSubtitle.setForeground(MUTED);
        brandSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel note = new JLabel("<html><div style='text-align:center;width:220px;'>Securely access payroll, employee records, leave requests, and payslip tools.</div></html>");
        note.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        note.setForeground(MUTED);
        note.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalGlue());
        panel.add(logoLabel);
        panel.add(Box.createVerticalStrut(14));
        panel.add(brandTitle);
        panel.add(Box.createVerticalStrut(6));
        panel.add(brandSubtitle);
        panel.add(Box.createVerticalStrut(16));
        panel.add(note);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    /**
     * Builds the login form panel where the user enters
     * their credentials.
     */
    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 6, 10, 6));

        JLabel title = new JLabel("Welcome back");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Sign in to continue");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(Box.createVerticalStrut(24));
        panel.add(title);
        panel.add(Box.createVerticalStrut(6));
        panel.add(subtitle);
        panel.add(Box.createVerticalStrut(24));

        JLabel lblU = new JLabel("Username");
        lblU.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblU.setForeground(MUTED);
        lblU.setAlignmentX(Component.LEFT_ALIGNMENT);

        styleInput(txtUsername);
        txtUsername.setToolTipText("Enter username");

        panel.add(lblU);
        panel.add(Box.createVerticalStrut(6));
        panel.add(txtUsername);
        panel.add(Box.createVerticalStrut(16));

        JLabel lblP = new JLabel("Password");
        lblP.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblP.setForeground(MUTED);
        lblP.setAlignmentX(Component.LEFT_ALIGNMENT);

        styleInput(txtPassword);
        txtPassword.setToolTipText("Enter password");

        panel.add(lblP);
        panel.add(Box.createVerticalStrut(6));
        panel.add(txtPassword);
        panel.add(Box.createVerticalStrut(24));

        JPanel actions = new JPanel(new BorderLayout(10, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);

        styleOutline(btnChangePassword);
        stylePrimary(btnLogin);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(btnChangePassword);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(btnLogin);

        actions.add(left, BorderLayout.WEST);
        actions.add(right, BorderLayout.EAST);

        panel.add(actions);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    /**
     * Loads and scales the MotorPH logo image
     * so it fits inside the UI properly.
     */
    private ImageIcon loadScaledLogo(int maxW, int maxH) {
        try {
            ImageIcon original = new ImageIcon(LOGO_PATH);

            if (original.getIconWidth() <= 0 || original.getIconHeight() <= 0) {
                return new ImageIcon();
            }

            int ow = original.getIconWidth();
            int oh = original.getIconHeight();

            double scale = Math.min((double) maxW / ow, (double) maxH / oh);
            int nw = Math.max(1, (int) (ow * scale));
            int nh = Math.max(1, (int) (oh * scale));

            Image scaled = original.getImage().getScaledInstance(nw, nh, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);

        } catch (Exception ex) {
            return new ImageIcon();
        }
    }

    /**
     * Handles the login process when the Login button is clicked.
     *
     * Steps:
     * 1. Read username and password input
     * 2. Send credentials to AuthService
     * 3. If authentication succeeds, open MainDashboardFrame
     * 4. If authentication fails, show an error message
     */
    private void onLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        try {
            UserSession session = authService.login(username, password);

            MainDashboardFrame dash = new MainDashboardFrame(session);
            dash.setVisible(true);

            this.dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Opens the ChangePasswordDialog window.
     *
     * The user must enter their username first
     * so the system knows which account will be updated.
     */
    private void onChangePassword() {
        String username = txtUsername.getText().trim();

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Enter your username first.",
                    "Missing Username",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        ChangePasswordDialog dialog = new ChangePasswordDialog(this, authService, username);
        dialog.setVisible(true);
    }

    /**
     * Applies consistent styling to input fields.
     */
    private void styleInput(JTextField f) {
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setPreferredSize(new Dimension(320, 40));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        f.setBackground(INPUT_BG);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 220, 235)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
    }

    /**
     * Styles the main action button.
     */
    private void stylePrimary(JButton b) {
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBackground(PRIMARY);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    /**
     * Styles outline buttons used for secondary actions.
     */
    private void styleOutline(JButton b) {
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBackground(Color.WHITE);
        b.setForeground(TEXT);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(9, 16, 9, 16)
        ));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    /**
     * Entry point used when running the application directly.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LogInFrame().setVisible(true));
    }
}