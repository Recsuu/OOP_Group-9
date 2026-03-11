package view;

import model.Permission;
import model.SupportTicket;
import model.UserSession;
import service.SupportTicketService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class SupportTicketDetailsDialog extends JDialog {

    private static final DateTimeFormatter DTF =
            DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

    // Theme
    private static final Color BG = new Color(230, 242, 255);
    private static final Color CARD = Color.WHITE;
    private static final Color BORDER = new Color(220, 220, 220);
    private static final Color PRIMARY = new Color(52, 120, 246);
    private static final Color TEXT = new Color(35, 35, 35);
    private static final Color MUTED = new Color(95, 95, 95);
    private static final Color FIELD_BG = new Color(248, 250, 255);

    private final UserSession session;
    private final SupportTicketService supportTicketService;
    private final String ticketId;
    private final Runnable onSuccess;

    private final JLabel lblTicketId = new JLabel("-");
    private final JLabel lblEmployee = new JLabel("-");
    private final JLabel lblRole = new JLabel("-");
    private final JLabel lblSubmitted = new JLabel("-");
    private final JLabel lblUpdated = new JLabel("-");

    private final JLabel chipPriority = new JLabel("-");
    private final JLabel chipStatus = new JLabel("-");

    private final JTextField txtTitle = new JTextField();
    private final JTextField txtCategory = new JTextField();
    private final JTextArea txtDescription = new JTextArea(10, 20);
    private final JTextField txtImagePath = new JTextField();

    private final JTextArea txtLatestComment = new JTextArea(8, 20);
    private final JComboBox<String> cmbStatus = new JComboBox<>(
            new String[]{"Pending", "In Progress", "Resolved", "Completed"}
    );
    private final JTextArea txtNewComment = new JTextArea(8, 20);

    private final JButton btnClose = new JButton("Close");
    private final JButton btnSave = new JButton("Save Update");

    public SupportTicketDetailsDialog(Frame owner,
                                      UserSession session,
                                      SupportTicketService supportTicketService,
                                      String ticketId,
                                      Runnable onSuccess) {
        super(owner, "Ticket Details", true);
        this.session = session;
        this.supportTicketService = supportTicketService;
        this.ticketId = ticketId;
        this.onSuccess = onSuccess;

        setSize(1100, 760);
        setResizable(false);
        setLocationRelativeTo(owner);

        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(12, 12));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        styleControls();
        wireEvents();
        loadData();
    }

    private JPanel buildHeader() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(14, 14, 0, 14));

        JPanel card = new JPanel(new BorderLayout(12, 12));
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

        JLabel lblTitle = new JLabel("Ticket Details");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(TEXT);

        JLabel lblSub = new JLabel("Review the issue on the left and update the ticket on the right.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(MUTED);

        left.add(lblMini);
        left.add(Box.createVerticalStrut(4));
        left.add(lblTitle);
        left.add(Box.createVerticalStrut(6));
        left.add(lblSub);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(chipPriority);
        right.add(chipStatus);

        card.add(left, BorderLayout.WEST);
        card.add(right, BorderLayout.EAST);

        wrap.add(card, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel buildBody() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(12, 14, 0, 14));

        JPanel body = new JPanel(new GridLayout(1, 2, 12, 12));
        body.setOpaque(false);

        JScrollPane leftScroll = new JScrollPane(buildLeftIssueCard());
        leftScroll.setBorder(null);
        leftScroll.getViewport().setBackground(BG);
        leftScroll.getVerticalScrollBar().setUnitIncrement(14);
        leftScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel rightPanel = buildRightITCard();

        body.add(leftScroll);
        body.add(rightPanel);

        wrap.add(body, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel buildLeftIssueCard() {
        JPanel card = createCard(new BorderLayout(12, 12));

        JLabel title = new JLabel("Issue Information");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT);

        // MAIN CONTENT PANEL
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setOpaque(false);

        // ---------------- TOP META GRID ----------------
        JPanel metaGrid = new JPanel(new GridLayout(3, 2, 10, 10));
        metaGrid.setOpaque(false);
        metaGrid.add(metaTile("Ticket ID", lblTicketId));
        metaGrid.add(metaTile("Employee", lblEmployee));
        metaGrid.add(metaTile("Department / Role", lblRole));
        metaGrid.add(metaTile("Submitted", lblSubmitted));
        metaGrid.add(metaTile("Last Updated", lblUpdated));
        metaGrid.add(metaTile("Category", txtAsLabel(txtCategory)));

        // ---------------- FORM AREA ----------------
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(0, 0, 10, 0);

        // Issue Title
        form.add(sectionLabel("Issue Title"), gc);

        gc.gridy++;
        txtTitle.setPreferredSize(new Dimension(100, 42));
        form.add(txtTitle, gc);

        // Description label
        gc.gridy++;
        gc.insets = new Insets(8, 0, 10, 0);
        form.add(sectionLabel("Description"), gc);

        // Description area - IMPORTANT: this one grows
        gc.gridy++;
        gc.weighty = 1;
        gc.fill = GridBagConstraints.BOTH;

        JScrollPane descSp = new JScrollPane(txtDescription);
        descSp.setBorder(BorderFactory.createLineBorder(BORDER));
        descSp.getVerticalScrollBar().setUnitIncrement(14);
        form.add(descSp, gc);

        // Screenshot path
        gc.gridy++;
        gc.weighty = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(12, 0, 10, 0);
        form.add(sectionLabel("Screenshot Path"), gc);

        gc.gridy++;
        txtImagePath.setPreferredSize(new Dimension(100, 42));
        form.add(txtImagePath, gc);

        content.add(metaGrid, BorderLayout.NORTH);
        content.add(form, BorderLayout.CENTER);

        card.add(title, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);

        return card;
    }

    private JPanel buildRightITCard() {
        JPanel card = createCard(new BorderLayout(12, 12));

        boolean canManage = session.getUser().can(Permission.MANAGE_SUPPORT_TICKET)
                || session.getUser().can(Permission.ADMIN_ALL);

        JLabel title = new JLabel(canManage ? "IT Update Panel" : "IT Update");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(sectionLabel("Latest IT Comment"));
        content.add(Box.createVerticalStrut(6));

        JScrollPane latestSp = new JScrollPane(txtLatestComment);
        latestSp.setBorder(BorderFactory.createLineBorder(BORDER));
        latestSp.setPreferredSize(new Dimension(100, 200));
        latestSp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        content.add(latestSp);
        content.add(Box.createVerticalStrut(14));

        if (canManage) {
            content.add(sectionLabel("Change Status"));
            content.add(Box.createVerticalStrut(6));
            content.add(cmbStatus);
            content.add(Box.createVerticalStrut(14));

            content.add(sectionLabel("Add / Update Comment"));
            content.add(Box.createVerticalStrut(6));

            JScrollPane newCommentSp = new JScrollPane(txtNewComment);
            newCommentSp.setBorder(BorderFactory.createLineBorder(BORDER));
            newCommentSp.setPreferredSize(new Dimension(100, 240));
            newCommentSp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
            content.add(newCommentSp);
            content.add(Box.createVerticalStrut(12));

            JLabel helper = new JLabel("<html><body style='width:340px'>Example: “Please restart the PC and reconnect to Wi-Fi.” or “Issue resolved after printer driver update.”</body></html>");
            helper.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            helper.setForeground(MUTED);
            helper.setAlignmentX(Component.LEFT_ALIGNMENT);
            content.add(helper);
        } else {
            JLabel helper = new JLabel("<html><body style='width:340px'>Only IT/Admin can update the status and comment on this ticket.</body></html>");
            helper.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            helper.setForeground(MUTED);
            helper.setAlignmentX(Component.LEFT_ALIGNMENT);
            content.add(helper);
        }

        card.add(title, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);

        return card;
    }

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

        card.add(btnClose);
        card.add(btnSave);

        wrap.add(card, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel createCard(LayoutManager layout) {
        JPanel card = new JPanel(layout);
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(18, 18, 18, 18)
        ));
        return card;
    }

    private JPanel metaTile(String title, JComponent valueComp) {
        JPanel tile = new JPanel(new BorderLayout(6, 6));
        tile.setBackground(FIELD_BG);
        tile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(232, 236, 242)),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTitle.setForeground(MUTED);

        if (valueComp instanceof JLabel) {
            valueComp.setFont(new Font("Segoe UI", Font.BOLD, 13));
            valueComp.setForeground(TEXT);
        }

        tile.add(lblTitle, BorderLayout.NORTH);
        tile.add(valueComp, BorderLayout.CENTER);
        return tile;
    }

    private JLabel txtAsLabel(JTextField field) {
        JLabel lbl = new JLabel("-");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(TEXT);
        return lbl;
    }

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(TEXT);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private void styleControls() {
        styleReadOnlyField(txtTitle);
        styleReadOnlyField(txtCategory);
        styleReadOnlyField(txtImagePath);

        txtDescription.setEditable(false);
        txtDescription.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtDescription.setBackground(FIELD_BG);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setBorder(new EmptyBorder(12, 12, 12, 12));
        txtDescription.setRows(14);
        txtDescription.setCaretPosition(0);

        txtLatestComment.setEditable(false);
        txtLatestComment.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtLatestComment.setBackground(FIELD_BG);
        txtLatestComment.setLineWrap(true);
        txtLatestComment.setWrapStyleWord(true);
        txtLatestComment.setBorder(new EmptyBorder(10, 12, 10, 12));

        boolean canManage = session.getUser().can(Permission.MANAGE_SUPPORT_TICKET)
                || session.getUser().can(Permission.ADMIN_ALL);

        cmbStatus.setEnabled(canManage);
        cmbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        txtNewComment.setEditable(canManage);
        txtNewComment.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtNewComment.setBackground(canManage ? FIELD_BG : new Color(245, 245, 245));
        txtNewComment.setLineWrap(true);
        txtNewComment.setWrapStyleWord(true);
        txtNewComment.setBorder(new EmptyBorder(10, 12, 10, 12));

        styleOutline(btnClose);
        stylePrimary(btnSave);

        btnSave.setVisible(canManage);
        btnSave.setPreferredSize(new Dimension(140, 40));
        btnClose.setPreferredSize(new Dimension(95, 40));

        styleChip(chipPriority);
        styleChip(chipStatus);
    }

    private void styleReadOnlyField(JTextField field) {
        field.setEditable(false);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBackground(FIELD_BG);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(10, 12, 10, 12)
        ));
    }

    private void stylePrimary(JButton button) {
        button.setFocusPainted(false);
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
    }

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

    private void styleChip(JLabel label) {
        label.setOpaque(true);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setBorder(new EmptyBorder(8, 12, 8, 12));
    }

    private void applyPriorityChip(String priority) {
        chipPriority.setText(safe(priority).isEmpty() ? "-" : priority);

        if ("High".equalsIgnoreCase(priority)) {
            chipPriority.setBackground(new Color(255, 232, 236));
            chipPriority.setForeground(new Color(214, 48, 88));
        } else if ("Medium".equalsIgnoreCase(priority)) {
            chipPriority.setBackground(new Color(255, 244, 219));
            chipPriority.setForeground(new Color(190, 120, 0));
        } else {
            chipPriority.setBackground(new Color(235, 240, 248));
            chipPriority.setForeground(new Color(79, 97, 134));
        }
    }

    private void applyStatusChip(String status) {
        chipStatus.setText(safe(status).isEmpty() ? "-" : status);

        if ("Pending".equalsIgnoreCase(status)) {
            chipStatus.setBackground(new Color(255, 244, 212));
            chipStatus.setForeground(new Color(176, 125, 0));
        } else if ("In Progress".equalsIgnoreCase(status)) {
            chipStatus.setBackground(new Color(223, 235, 255));
            chipStatus.setForeground(new Color(38, 93, 206));
        } else {
            chipStatus.setBackground(new Color(214, 241, 223));
            chipStatus.setForeground(new Color(14, 127, 75));
        }
    }

    private void wireEvents() {
        btnClose.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> onSave());
    }

    private void loadData() {
        try {
            SupportTicket ticket = supportTicketService.getTicketById(session, ticketId);

            lblTicketId.setText(safe(ticket.getTicketId()));
            lblEmployee.setText(safe(ticket.getEmployeeName()));
            lblRole.setText(safe(ticket.getDepartmentOrRole()));
            lblSubmitted.setText(ticket.getSubmittedAt() == null ? "-" : ticket.getSubmittedAt().format(DTF));
            lblUpdated.setText(ticket.getUpdatedAt() == null ? "-" : ticket.getUpdatedAt().format(DTF));

            txtTitle.setText(safe(ticket.getTitle()));
            txtCategory.setText(safe(ticket.getCategory()));
            txtDescription.setText(safe(ticket.getDescription()));
            txtImagePath.setText(safe(ticket.getImagePath()));

            applyPriorityChip(ticket.getPriority());
            applyStatusChip(ticket.getStatus());

            String latestComment = safe(ticket.getItComment());
            if (latestComment.isEmpty()) {
                txtLatestComment.setText("No IT update yet.");
            } else {
                String by = safe(ticket.getCommentedBy());
                String at = ticket.getCommentedAt() == null ? "" : " (" + ticket.getCommentedAt().format(DTF) + ")";
                txtLatestComment.setText(latestComment + (by.isEmpty() ? "" : "\n\nBy: " + by + at));
            }

            cmbStatus.setSelectedItem(ticket.getStatus());

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            dispose();
        }
    }

    private void onSave() {
        try {
            supportTicketService.updateTicketByIT(
                    session,
                    ticketId,
                    String.valueOf(cmbStatus.getSelectedItem()),
                    txtNewComment.getText().trim()
            );

            JOptionPane.showMessageDialog(this, "Ticket updated successfully.");
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

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}