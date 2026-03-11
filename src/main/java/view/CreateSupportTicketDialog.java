package view;

import model.UserSession;
import service.SupportTicketService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

/**
 * =============================================================
 * CreateSupportTicketDialog
 * =============================================================
 * Dialog used by employees to submit IT support tickets.
 *
 * Features:
 * - Category selection (computer, network, printer, etc.)
 * - Priority selection (low, medium, high)
 * - Description field
 * - Optional screenshot attachment
 *
 * When submitted:
 * → Ticket is sent to SupportTicketService
 * → Stored in the system
 * → IT staff can view and resolve the issue
 *
 * Architecture layer: VIEW
 */
public class CreateSupportTicketDialog extends JDialog {

    /** Current logged-in session */
    private final UserSession session;

    /** Service responsible for ticket creation */
    private final SupportTicketService supportTicketService;

    /** Callback executed after successful ticket creation */
    private final Runnable onSuccess;

    // Theme colors used throughout the dialog
    private static final Color BG = new Color(230, 242, 255);
    private static final Color CARD = Color.WHITE;
    private static final Color BORDER = new Color(220, 220, 220);
    private static final Color PRIMARY = new Color(52, 120, 246);
    private static final Color TEXT = new Color(35, 35, 35);
    private static final Color MUTED = new Color(95, 95, 95);
    private static final Color FIELD_BG = new Color(248, 250, 255);

    // ===============================
    // Form Fields
    // ===============================

    /** Ticket title field */
    private final JTextField txtTitle = new JTextField();

    /** Category dropdown */
    private final JComboBox<String> cmbCategory = new JComboBox<>(
            new String[]{"Computer Issue", "Network Issue", "Printer Issue", "System Access", "Software Bug", "Other"}
    );

    /** Priority dropdown */
    private final JComboBox<String> cmbPriority = new JComboBox<>(
            new String[]{"Low", "Medium", "High"}
    );

    /** Issue description text area */
    private final JTextArea txtDescription = new JTextArea(7, 20);

    /** Screenshot path field */
    private final JTextField txtImagePath = new JTextField();

    // ===============================
    // Buttons
    // ===============================

    /** Button to browse for screenshot */
    private final JButton btnBrowse = new JButton("Browse");

    /** Cancel dialog button */
    private final JButton btnCancel = new JButton("Cancel");

    /** Submit ticket button */
    private final JButton btnSubmit = new JButton("Submit Ticket");

    /**
     * Constructor.
     *
     * @param owner JFrame owner
     * @param session current user session
     * @param supportTicketService service used to submit tickets
     * @param onSuccess callback executed after successful submission
     */
    public CreateSupportTicketDialog(Frame owner,
                                     UserSession session,
                                     SupportTicketService supportTicketService,
                                     Runnable onSuccess) {

        super(owner, "Create New Ticket", true);

        this.session = session;
        this.supportTicketService = supportTicketService;
        this.onSuccess = onSuccess;

        setSize(700, 700);
        setResizable(false);
        setLocationRelativeTo(owner);

        getContentPane().setBackground(BG);

        setLayout(new BorderLayout(12, 12));

        // Build dialog sections
        add(buildHeader(), BorderLayout.NORTH);
        add(buildFormCard(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        // Apply styling
        styleControls();

        // Connect button events
        wireEvents();
    }

    /**
     * Builds the top header of the dialog.
     * Shows title and short description.
     */
    private JPanel buildHeader() {

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(14, 14, 0, 14));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD);

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel lblMini = new JLabel("Support Ticket");
        lblMini.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMini.setForeground(PRIMARY);

        JLabel lblTitle = new JLabel("Create New Ticket");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(TEXT);

        JLabel lblSub = new JLabel("Describe the issue clearly so IT can help you faster.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(MUTED);

        left.add(lblMini);
        left.add(Box.createVerticalStrut(4));
        left.add(lblTitle);
        left.add(Box.createVerticalStrut(6));
        left.add(lblSub);

        card.add(left, BorderLayout.WEST);
        wrap.add(card, BorderLayout.CENTER);

        return wrap;
    }

    /**
     * Builds the main form area containing
     * category, priority, description and screenshot upload.
     */
    private JPanel buildFormCard() {

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(12, 14, 0, 14));

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD);

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(22, 22, 22, 22)
        ));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(0, 0, 14, 0);
        gc.anchor = GridBagConstraints.WEST;

        // CATEGORY
        card.add(sectionLabel("Category"), gc);

        gc.gridy++;
        cmbCategory.setPreferredSize(new Dimension(100, 42));
        card.add(cmbCategory, gc);

        // PRIORITY
        gc.gridy++;
        card.add(sectionLabel("Priority"), gc);

        gc.gridy++;
        cmbPriority.setPreferredSize(new Dimension(100, 42));
        card.add(cmbPriority, gc);

        // DESCRIPTION
        gc.gridy++;
        card.add(sectionLabel("Description"), gc);

        gc.gridy++;
        gc.fill = GridBagConstraints.BOTH;
        gc.weighty = 1;

        JScrollPane descSp = new JScrollPane(txtDescription);
        descSp.setBorder(BorderFactory.createLineBorder(BORDER));
        descSp.setPreferredSize(new Dimension(100, 180));
        descSp.getVerticalScrollBar().setUnitIncrement(14);

        card.add(descSp, gc);

        // SCREENSHOT
        gc.gridy++;
        gc.weighty = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;

        card.add(sectionLabel("Attach Screenshot (Optional)"), gc);

        gc.gridy++;

        JPanel fileRow = new JPanel(new BorderLayout(12, 0));
        fileRow.setOpaque(false);

        txtImagePath.setPreferredSize(new Dimension(100, 42));
        btnBrowse.setPreferredSize(new Dimension(120, 42));

        fileRow.add(txtImagePath, BorderLayout.CENTER);
        fileRow.add(btnBrowse, BorderLayout.EAST);

        card.add(fileRow, gc);

        // Scroll wrapper for long forms
        JScrollPane scroll = new JScrollPane(card);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        wrap.add(scroll, BorderLayout.CENTER);

        return wrap;
    }

    /**
     * Builds the bottom button section.
     */
    private JPanel buildFooter() {

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(12, 14, 14, 14));

        JPanel card = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        card.setBackground(CARD);

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(4, 8, 4, 8)
        ));

        card.add(btnCancel);
        card.add(btnSubmit);

        btnBrowse.setPreferredSize(new Dimension(110, 42));
        btnSubmit.setPreferredSize(new Dimension(150, 42));
        btnCancel.setPreferredSize(new Dimension(100, 42));

        wrap.add(card, BorderLayout.CENTER);

        return wrap;
    }

    /**
     * Creates section labels used in the form.
     */
    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(TEXT);
        return lbl;
    }

    /**
     * Applies styling to fields and buttons.
     */
    private void styleControls() {

        styleField(txtTitle);
        styleField(txtImagePath);

        // Image path should not be editable manually
        txtImagePath.setEditable(false);

        cmbCategory.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbPriority.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        txtDescription.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtDescription.setBackground(FIELD_BG);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setBorder(new EmptyBorder(10, 12, 10, 12));

        styleOutline(btnBrowse);
        styleOutline(btnCancel);
        stylePrimary(btnSubmit);

        btnBrowse.setPreferredSize(new Dimension(95, 38));
        btnSubmit.setPreferredSize(new Dimension(140, 40));
        btnCancel.setPreferredSize(new Dimension(95, 40));

        cmbPriority.setSelectedItem("Medium");
    }

    /**
     * Styles text fields.
     */
    private void styleField(JTextField field) {

        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBackground(FIELD_BG);

        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(10, 12, 10, 12)
        ));
    }

    /**
     * Styles primary action button.
     */
    private void stylePrimary(JButton button) {

        button.setFocusPainted(false);
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
    }

    /**
     * Styles outline buttons.
     */
    private void styleOutline(JButton button) {

        button.setFocusPainted(false);
        button.setBackground(Color.WHITE);
        button.setForeground(TEXT);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(10, 16, 10, 16)
        ));
    }

    /**
     * Connects UI events to methods.
     */
    private void wireEvents() {

        btnBrowse.addActionListener(e -> onBrowse());
        btnCancel.addActionListener(e -> dispose());
        btnSubmit.addActionListener(e -> onSubmit());
    }

    /**
     * Opens file chooser for screenshot selection.
     */
    private void onBrowse() {

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Screenshot");

        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {

            File file = chooser.getSelectedFile();

            txtImagePath.setText(file.getAbsolutePath());

            txtImagePath.setCaretPosition(0);
        }
    }

    /**
     * Submits the support ticket.
     */
    private void onSubmit() {

        try {

            supportTicketService.submitTicket(
                    session,
                    txtTitle.getText().trim(),
                    String.valueOf(cmbCategory.getSelectedItem()),
                    txtDescription.getText().trim(),
                    txtImagePath.getText().trim(),
                    String.valueOf(cmbPriority.getSelectedItem())
            );

            JOptionPane.showMessageDialog(this, "Support ticket submitted successfully.");

            // Refresh panel after successful ticket creation
            if (onSuccess != null) onSuccess.run();

            dispose();

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