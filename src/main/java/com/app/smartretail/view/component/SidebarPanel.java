package com.app.smartretail.view.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.app.smartretail.utils.Session;
import com.app.smartretail.utils.UITheme;

/**
 * SidebarPanel — Modern collapsible sidebar navigation.
 * Supports icon + label nav items with active state indicator.
 */
public class SidebarPanel extends JPanel {

    public interface NavListener { void onNavigate(String id); }

    private NavListener listener;
    private String activeId = "dashboard";
    private final List<NavItem> items = new ArrayList<>();
    private JButton btnLogout;

    public SidebarPanel(NavListener listener) {
        this.listener = listener;
        setPreferredSize(new Dimension(220, 0));
        setBackground(UITheme.BG_SIDEBAR);
        setLayout(new BorderLayout());
        build();
    }

    private void build() {
        // ── Logo / Brand ──
        JPanel brand = new JPanel(new BorderLayout());
        brand.setOpaque(false);
        brand.setBorder(new EmptyBorder(24, 20, 20, 20));

        JLabel logo = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Icon circle
                g2.setColor(UITheme.ACCENT_BLUE);
                g2.fillOval(0, 2, 32, 32);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                g2.drawString("SR", 6, 22);
                g2.dispose();
            }
            { setPreferredSize(new Dimension(36, 36)); setOpaque(false); }
        };

        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));
        JLabel name = new JLabel("SRMS");
        name.setFont(new Font("Segoe UI", Font.BOLD, 15));
        name.setForeground(UITheme.TEXT_PRIMARY);
        JLabel sub = new JLabel("Retail System");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.TEXT_MUTED);
        brandText.add(name); brandText.add(sub);

        brand.add(logo, BorderLayout.WEST);
        brand.add(brandText, BorderLayout.CENTER);

        // ── Navigation ──
        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(6, 12, 0, 12));

        addGroup(nav, "OVERVIEW");
        addItem(nav, "dashboard",  Icons.DASHBOARD,   "Dashboard");
        addItem(nav, "pos",        Icons.CART,        "Penjualan (POS)");
        addItem(nav, "pembelian",  Icons.BOX,         "Pembelian");
        addItem(nav, "stok",       Icons.WAREHOUSE,   "Manajemen Stok");

        addGroup(nav, "MASTER DATA");
        addItem(nav, "barang",     Icons.TAG,         "Data Barang");
        addItem(nav, "kategori",   Icons.FOLDER,      "Kategori");
        addItem(nav, "supplier",   Icons.TRUCK,       "Supplier");
        addItem(nav, "customer",   Icons.USERS,       "Customer");

        addGroup(nav, "ANALITIK");
        addItem(nav, "laporan",    Icons.REPORT,      "Laporan");
        addItem(nav, "grafik",     Icons.CHART,       "Grafik & Analitik");

        if (Session.isAdmin()) {
            addGroup(nav, "PENGATURAN");
            addItem(nav, "users",  Icons.USER_SHIELD, "Kelola User");
        }

        JScrollPane navScroll = new JScrollPane(nav);
        navScroll.setOpaque(false);
        navScroll.getViewport().setOpaque(false);
        navScroll.setBorder(BorderFactory.createEmptyBorder());
        navScroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));

        // ── User profile bottom ──
        JPanel userPanel = new JPanel(new BorderLayout(10, 0));
        userPanel.setOpaque(false);
        userPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_DEFAULT),
            new EmptyBorder(14, 16, 14, 16)));

        // Avatar
        JPanel avatar = new JPanel() {
            String initials = Session.currentUser != null
                ? getInitials(Session.currentUser.getNamaLengkap()) : "?";
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.ACCENT_PURPLE);
                g2.fillOval(0, 0, 34, 34);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initials, 17 - fm.stringWidth(initials)/2, 22);
                g2.dispose();
            }
            { setPreferredSize(new Dimension(34, 34)); setOpaque(false); }
        };

        JPanel userInfo = new JPanel();
        userInfo.setOpaque(false);
        userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));
        String uName = Session.currentUser != null ? Session.currentUser.getNamaLengkap() : "";
        if (uName.length() > 16) uName = uName.substring(0, 15) + "…";
        JLabel uLbl = new JLabel(uName);
        uLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        uLbl.setForeground(UITheme.TEXT_PRIMARY);
        String role = Session.currentUser != null ? Session.currentUser.getRole() : "";
        JLabel rLbl = new JLabel(role);
        rLbl.setFont(UITheme.FONT_LABEL);
        rLbl.setForeground(UITheme.ACCENT_BLUE);
        userInfo.add(uLbl); userInfo.add(rLbl);

        btnLogout = new JButton("↪") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 88, 100, 40));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnLogout.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        btnLogout.setForeground(UITheme.TEXT_MUTED);
        btnLogout.setOpaque(false);
        btnLogout.setContentAreaFilled(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.setToolTipText("Logout");
        btnLogout.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnLogout.setForeground(UITheme.ACCENT_CORAL); }
            public void mouseExited(MouseEvent e)  { btnLogout.setForeground(UITheme.TEXT_MUTED); }
        });

        userPanel.add(avatar, BorderLayout.WEST);
        userPanel.add(userInfo, BorderLayout.CENTER);
        userPanel.add(btnLogout, BorderLayout.EAST);

        add(brand, BorderLayout.NORTH);
        add(navScroll, BorderLayout.CENTER);
        add(userPanel, BorderLayout.SOUTH);
    }

    private void addGroup(JPanel nav, String label) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_LABEL);
        lbl.setForeground(UITheme.TEXT_MUTED);
        lbl.setBorder(new EmptyBorder(12, 8, 4, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        nav.add(lbl);
    }

    private void addItem(JPanel nav, String id, Icon icon, String label) {
        NavButton btn = new NavButton(id, icon, label);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.addActionListener(e -> {
            activeId = id;
            refreshActive(nav);
            if (listener != null) listener.onNavigate(id);
        });
        items.add(new NavItem(id, btn));
        nav.add(btn);
        nav.add(Box.createVerticalStrut(2));
    }

    private void refreshActive(JPanel nav) {
        for (NavItem ni : items) ni.btn.repaint();
    }

    public void setActive(String id) {
        activeId = id;
        for (NavItem ni : items) ni.btn.repaint();
    }

    public JButton getLogoutButton() { return btnLogout; }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split(" ");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[0].charAt(0) + parts[parts.length-1].charAt(0)).toUpperCase();
    }

    private class NavItem { String id; NavButton btn; NavItem(String i, NavButton b){id=i;btn=b;} }

    private class NavButton extends JButton {
        final String id;
        final Icon ico;
        NavButton(String id, Icon ico, String label) {
            super(label);
            this.id = id; this.ico = ico;
            setFont(UITheme.FONT_BODY);
            setForeground(UITheme.TEXT_SECONDARY);
            setHorizontalAlignment(SwingConstants.LEFT);
            setIconTextGap(10);
            setIcon(ico);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(196, 38));
            setPreferredSize(new Dimension(196, 38));
            setBorder(new EmptyBorder(0, 10, 0, 10));
        }
        @Override protected void paintComponent(Graphics g) {
            boolean active = id.equals(activeId);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (active) {
                g2.setColor(new Color(82, 130, 255, 25));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(UITheme.ACCENT_BLUE);
                g2.fillRoundRect(getWidth()-3, 6, 3, getHeight()-12, 3, 3);
                setForeground(UITheme.TEXT_PRIMARY);
            } else if (getModel().isRollover()) {
                g2.setColor(new Color(255,255,255,8));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                setForeground(UITheme.TEXT_PRIMARY);
            } else {
                setForeground(UITheme.TEXT_SECONDARY);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
