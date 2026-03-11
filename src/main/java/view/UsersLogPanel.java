package view;

import model.UserActivityLogEntry;
import model.UserSession;
import service.AuditService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class UsersLogPanel extends JPanel {

    private final UserSession session;
    private final AuditService auditService;

    private final JTextField txtUsername = new JTextField(14);
    private final JComboBox<String> cmbAction = new JComboBox<>(new String[]{
            "ALL",
            "LOGIN_SUCCESS",
            "LOGIN_FAILED",
            "LOGOUT",
            "CHANGE_PASSWORD",
            "CHANGE_USERNAME",
            "FILE_LEAVE",
            "APPROVE_LEAVE",
            "REJECT_LEAVE",
            "GENERATE_PAYROLL"
    });

    // ✅ Keep refresh only
    private final JButton btnRefresh = new JButton("Refresh");

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Time", "Username", "Action", "Details"}, 0
    ) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };

    private final JTable table = new JTable(model);

    // ✅ Cache logs so filter is instant (no repeated DAO calls)
    private List<UserActivityLogEntry> cachedLogs = new ArrayList<>();

    // Theme
    private static final Color BG = new Color(230, 242, 255);
    private static final Color CARD = Color.WHITE;
    private static final Color BORDER = new Color(220, 220, 220);
    private static final Color PRIMARY = new Color(52, 120, 246);

    public UsersLogPanel(UserSession session, AuditService auditService) {
        this.session = session;
        this.auditService = auditService;

        setLayout(new BorderLayout(12, 12));
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        add(buildTopCard(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);

        // ✅ Refresh pulls from service then applies filters
        btnRefresh.addActionListener(e -> reloadLogs());

        // ✅ Auto-filter on typing
        txtUsername.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        // ✅ Auto-filter on action change
        cmbAction.addActionListener(e -> applyFilters());

        // ✅ Enter key applies too
        txtUsername.addActionListener(e -> applyFilters());

        styleControls();
        styleTable();

        reloadLogs();
    }

    private void styleControls() {
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtUsername.setPreferredSize(new Dimension(180, 30));

        cmbAction.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbAction.setPreferredSize(new Dimension(200, 30));

        btnRefresh.setFocusPainted(false);
        btnRefresh.setBackground(PRIMARY);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnRefresh.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleTable() {
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // cleaner look
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setReorderingAllowed(false);
    }

    // ---------------- UI ----------------

    private JPanel buildTopCard() {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel title = new JLabel("User Logs");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(30, 30, 30));

        JPanel filters = new JPanel(new GridBagLayout());
        filters.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 8, 4, 8);
        gc.anchor = GridBagConstraints.WEST;
        gc.gridy = 0;

        JLabel uLbl = new JLabel("Username:");
        uLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        uLbl.setForeground(new Color(60, 60, 60));

        JLabel aLbl = new JLabel("Action:");
        aLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        aLbl.setForeground(new Color(60, 60, 60));

        gc.gridx = 0;
        filters.add(uLbl, gc);

        gc.gridx = 1;
        filters.add(txtUsername, gc);

        gc.gridx = 2;
        filters.add(aLbl, gc);

        gc.gridx = 3;
        filters.add(cmbAction, gc);

        gc.gridx = 4;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        filters.add(Box.createHorizontalGlue(), gc);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(btnRefresh);

        card.add(title, BorderLayout.NORTH);

        JPanel mid = new JPanel(new BorderLayout());
        mid.setOpaque(false);
        mid.add(filters, BorderLayout.CENTER);
        mid.add(right, BorderLayout.EAST);

        card.add(mid, BorderLayout.CENTER);

        return card;
    }

    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(Color.WHITE);
        sp.setBorder(BorderFactory.createLineBorder(new Color(235, 235, 235)));

        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    // ---------------- DATA ----------------

    private void reloadLogs() {
        try {
            model.setRowCount(0);

            cachedLogs = auditService.getAllLogs(session.getUser());
            applyFilters();

        } catch (Exception ex) {
            ex.printStackTrace(); // IMPORTANT
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyFilters() {
        String uFilter = txtUsername.getText().trim().toLowerCase();
        String aFilter = String.valueOf(cmbAction.getSelectedItem());

        model.setRowCount(0);

        for (UserActivityLogEntry e : cachedLogs) {
            if (!uFilter.isEmpty()) {
                String uname = (e.getUsername() == null) ? "" : e.getUsername().toLowerCase();
                if (!uname.contains(uFilter)) continue;
            }

            if (aFilter != null && !"ALL".equalsIgnoreCase(aFilter)) {
                String act = (e.getAction() == null) ? "" : e.getAction();
                if (!aFilter.equalsIgnoreCase(act)) continue;
            }

            model.addRow(new Object[]{
                    e.getTimestamp() == null ? "" : e.getTimestamp().toString(),
                    e.getUsername(),
                    e.getAction(),
                    e.getDetails()
            });
        }
    }
}