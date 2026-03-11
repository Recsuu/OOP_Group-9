package view;

import model.Permission;
import model.SupportTicket;
import model.UserSession;
import service.SupportTicketService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SupportTicketPanel extends JPanel {

    private final UserSession session;
    private final SupportTicketService supportTicketService;

    // Theme
    private static final Color BG = new Color(230, 242, 255);
    private static final Color CARD = Color.WHITE;
    private static final Color BORDER = new Color(220, 220, 220);
    private static final Color PRIMARY = new Color(52, 120, 246);
    private static final Color TEXT = new Color(35, 35, 35);
    private static final Color MUTED = new Color(95, 95, 95);
    private static final Color SOFT = new Color(248, 250, 255);

    private static final DateTimeFormatter DF =
            DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

    // Top controls
    private final JTextField txtSearch = new JTextField();
    private final JComboBox<String> cmbStatus = new JComboBox<>(
            new String[]{"All Status", "Pending", "In Progress", "Resolved", "Completed"}
    );
    private final JButton btnCreate = new JButton("+ Create New Ticket");
    private final JButton btnRefresh = new JButton("Refresh");
    private final JButton btnView = new JButton("View Details");

    // Stats
    private final JLabel valTotal = new JLabel("0");
    private final JLabel valPending = new JLabel("0");
    private final JLabel valInProgress = new JLabel("0");
    private final JLabel valResolved = new JLabel("0");

    // Table
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Ticket ID", "Employee", "Issue Title", "Category", "Priority", "Status", "Latest Update", "Submitted"},
            0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(model);

    // Data
    private List<SupportTicket> allTickets = new ArrayList<>();
    private List<SupportTicket> filteredTickets = new ArrayList<>();

    public SupportTicketPanel(UserSession session, SupportTicketService supportTicketService) {
        this.session = session;
        this.supportTicketService = supportTicketService;

        setLayout(new BorderLayout(12, 12));
        setBackground(BG);
        setBorder(new EmptyBorder(14, 14, 14, 14));

        add(buildTopSection(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);

        styleControls();
        styleTable();
        wireEvents();

        reloadTickets();
    }

    private JPanel buildTopSection() {
        JPanel wrap = new JPanel(new BorderLayout(12, 12));
        wrap.setOpaque(false);

        wrap.add(buildHeroCard(), BorderLayout.NORTH);
        wrap.add(buildStatsRow(), BorderLayout.CENTER);

        return wrap;
    }

    private JPanel buildHeroCard() {
        JPanel card = new JPanel(new BorderLayout(14, 14));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(16, 18, 16, 18)
        ));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel lblMini = new JLabel("IT Support");
        lblMini.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMini.setForeground(PRIMARY);

        JLabel lblTitle = new JLabel("Support Tickets");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(TEXT);

        String subtitleText = session.getUser().can(Permission.MANAGE_SUPPORT_TICKET) || session.getUser().can(Permission.ADMIN_ALL)
                ? "Track, review, and update submitted issues across the system."
                : "Submit your technical concerns and monitor IT updates in one place.";

        JLabel lblSub = new JLabel(subtitleText);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(MUTED);

        left.add(lblMini);
        left.add(Box.createVerticalStrut(4));
        left.add(lblTitle);
        left.add(Box.createVerticalStrut(6));
        left.add(lblSub);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        txtSearch.setPreferredSize(new Dimension(220, 38));
        txtSearch.setToolTipText("Search ticket, issue title, category, or employee");

        cmbStatus.setPreferredSize(new Dimension(145, 38));

        right.add(txtSearch);
        right.add(cmbStatus);
        right.add(btnRefresh);

        if (session.getUser().can(Permission.FILE_SUPPORT_TICKET) || session.getUser().can(Permission.ADMIN_ALL)) {
            right.add(btnCreate);
        }

        card.add(left, BorderLayout.WEST);
        card.add(right, BorderLayout.EAST);

        return card;
    }

    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 12));
        row.setOpaque(false);

        row.add(statCard("Total Tickets", valTotal));
        row.add(statCard("Pending", valPending));
        row.add(statCard("In Progress", valInProgress));
        row.add(statCard("Resolved / Done", valResolved));

        return row;
    }

    private JPanel statCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(6, 6));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTitle.setForeground(MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(TEXT);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout(12, 12));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Ticket List");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT);

        JLabel hint = new JLabel("Tip: Double-click any row to open ticket details");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setForeground(MUTED);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(hint);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(btnView);

        card.add(left, BorderLayout.NORTH);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(232, 236, 242)));
        sp.getViewport().setBackground(Color.WHITE);

        card.add(sp, BorderLayout.CENTER);
        card.add(right, BorderLayout.SOUTH);

        return card;
    }

    private void styleControls() {
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(9, 12, 9, 12)
        ));

        cmbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        stylePrimary(btnCreate);
        styleOutline(btnRefresh);
        styleOutline(btnView);
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

    private void styleTable() {
        table.setRowHeight(34);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(232, 240, 255));
        table.setSelectionForeground(TEXT);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setReorderingAllowed(false);
        header.setBackground(new Color(245, 247, 250));

        // column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(110); // ticket id
        table.getColumnModel().getColumn(1).setPreferredWidth(130); // employee
        table.getColumnModel().getColumn(2).setPreferredWidth(190); // title
        table.getColumnModel().getColumn(3).setPreferredWidth(110); // category
        table.getColumnModel().getColumn(4).setPreferredWidth(85);  // priority
        table.getColumnModel().getColumn(5).setPreferredWidth(110); // status
        table.getColumnModel().getColumn(6).setPreferredWidth(230); // latest update
        table.getColumnModel().getColumn(7).setPreferredWidth(150); // submitted

        // custom badge renderers
        table.getColumnModel().getColumn(4).setCellRenderer(new PriorityRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusRenderer());

        // normal renderer
        DefaultTableCellRenderer base = new DefaultTableCellRenderer();
        base.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        base.setBorder(new EmptyBorder(0, 10, 0, 10));
        base.setForeground(TEXT);

        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i == 4 || i == 5) continue;
            table.getColumnModel().getColumn(i).setCellRenderer(base);
        }
    }

    private void wireEvents() {
        btnRefresh.addActionListener(e -> reloadTickets());

        btnCreate.addActionListener(e -> {
            Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
            CreateSupportTicketDialog dialog = new CreateSupportTicketDialog(
                    owner,
                    session,
                    supportTicketService,
                    this::reloadTickets
            );
            dialog.setVisible(true);
        });

        btnView.addActionListener(e -> openSelectedTicket());

        txtSearch.addActionListener(e -> applyFilters());
        cmbStatus.addActionListener(e -> applyFilters());

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    openSelectedTicket();
                }
            }
        });
    }

    private void reloadTickets() {
        try {
            allTickets = supportTicketService.listTicketsForSession(session);
            applyFilters();
            refreshStats();
        } catch (Exception ex) {
            allTickets = new ArrayList<>();
            filteredTickets = new ArrayList<>();
            refreshTable();
            refreshStats();

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void applyFilters() {
        String keyword = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();
        String selectedStatus = String.valueOf(cmbStatus.getSelectedItem());

        filteredTickets = new ArrayList<>();

        for (SupportTicket ticket : allTickets) {
            boolean matchesKeyword =
                    keyword.isEmpty()
                            || safe(ticket.getTicketId()).toLowerCase().contains(keyword)
                            || safe(ticket.getEmployeeName()).toLowerCase().contains(keyword)
                            || safe(ticket.getTitle()).toLowerCase().contains(keyword)
                            || safe(ticket.getCategory()).toLowerCase().contains(keyword)
                            || safe(ticket.getItComment()).toLowerCase().contains(keyword);

            boolean matchesStatus =
                    "All Status".equalsIgnoreCase(selectedStatus)
                            || safe(ticket.getStatus()).equalsIgnoreCase(selectedStatus);

            if (matchesKeyword && matchesStatus) {
                filteredTickets.add(ticket);
            }
        }

        refreshTable();
    }

    private void refreshTable() {
        model.setRowCount(0);

        for (SupportTicket ticket : filteredTickets) {
            String submitted = ticket.getSubmittedAt() == null ? "-" : ticket.getSubmittedAt().format(DF);
            String latestUpdate = safe(ticket.getItComment());

            if (latestUpdate.length() > 42) {
                latestUpdate = latestUpdate.substring(0, 39) + "...";
            }

            model.addRow(new Object[]{
                    safe(ticket.getTicketId()),
                    safe(ticket.getEmployeeName()),
                    safe(ticket.getTitle()),
                    safe(ticket.getCategory()),
                    safe(ticket.getPriority()),
                    safe(ticket.getStatus()),
                    latestUpdate.isEmpty() ? "-" : latestUpdate,
                    submitted
            });
        }
    }

    private void refreshStats() {
        int total = allTickets.size();
        int pending = 0;
        int inProgress = 0;
        int resolved = 0;

        for (SupportTicket ticket : allTickets) {
            String status = safe(ticket.getStatus());

            if ("Pending".equalsIgnoreCase(status)) pending++;
            else if ("In Progress".equalsIgnoreCase(status)) inProgress++;
            else if ("Resolved".equalsIgnoreCase(status) || "Completed".equalsIgnoreCase(status)) resolved++;
        }

        valTotal.setText(String.valueOf(total));
        valPending.setText(String.valueOf(pending));
        valInProgress.setText(String.valueOf(inProgress));
        valResolved.setText(String.valueOf(resolved));
    }

    private void openSelectedTicket() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a ticket first.");
            return;
        }

        String ticketId = String.valueOf(model.getValueAt(row, 0));

        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        SupportTicketDetailsDialog dialog = new SupportTicketDetailsDialog(
                owner,
                session,
                supportTicketService,
                ticketId,
                this::reloadTickets
        );
        dialog.setVisible(true);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private class PriorityRenderer extends DefaultTableCellRenderer {
        public PriorityRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String text = value == null ? "" : value.toString();

            lbl.setText(text);
            lbl.setBorder(new EmptyBorder(6, 10, 6, 10));
            lbl.setOpaque(true);

            if ("High".equalsIgnoreCase(text)) {
                lbl.setBackground(new Color(255, 232, 236));
                lbl.setForeground(new Color(214, 48, 88));
            } else if ("Medium".equalsIgnoreCase(text)) {
                lbl.setBackground(new Color(255, 244, 219));
                lbl.setForeground(new Color(190, 120, 0));
            } else {
                lbl.setBackground(new Color(235, 240, 248));
                lbl.setForeground(new Color(79, 97, 134));
            }

            if (isSelected) {
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(170, 190, 235)),
                        new EmptyBorder(6, 10, 6, 10)
                ));
            }

            return lbl;
        }
    }

    private class StatusRenderer extends DefaultTableCellRenderer {
        public StatusRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String text = value == null ? "" : value.toString();

            lbl.setText(text);
            lbl.setBorder(new EmptyBorder(6, 10, 6, 10));
            lbl.setOpaque(true);

            if ("Pending".equalsIgnoreCase(text)) {
                lbl.setBackground(new Color(255, 244, 212));
                lbl.setForeground(new Color(176, 125, 0));
            } else if ("In Progress".equalsIgnoreCase(text)) {
                lbl.setBackground(new Color(223, 235, 255));
                lbl.setForeground(new Color(38, 93, 206));
            } else {
                lbl.setBackground(new Color(214, 241, 223));
                lbl.setForeground(new Color(14, 127, 75));
            }

            if (isSelected) {
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(170, 190, 235)),
                        new EmptyBorder(6, 10, 6, 10)
                ));
            }

            return lbl;
        }
    }
}