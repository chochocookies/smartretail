package com.app.smartretail.view.component;

import java.awt.BasicStroke;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.app.smartretail.utils.Session;
import com.app.smartretail.utils.UITheme;

/**
 * SidebarPanel — Starline grouped sidebar, no dark mode toggle.
 * Active: lime-yellow pill. Groups: MAIN, MASTER DATA, ANALYTICS, MANAGEMENT.
 */
public class SidebarPanel extends JPanel {

    public interface NavListener { void onNavigate(String id); }

    private final NavListener listener;
    private String activeId = "dashboard";
    private final List<NavButton> navBtns = new ArrayList<>();
    private JButton btnLogout;

    // Role → allowed nav IDs
    private static final Map<String, List<String>> ROLE_ITEMS = new HashMap<>();
    static {
        ROLE_ITEMS.put("ADMIN",
            Arrays.asList("dashboard","pos","purchase","stok",
                          "barang","kategori","customer","pegawai",
                          "laporan","analitik",
                          "users","settings","help"));
        ROLE_ITEMS.put("KASIR",
            Arrays.asList("dashboard","pos",
                          "customer",
                          "help"));
        ROLE_ITEMS.put("STAFF_GUDANG",
            Arrays.asList("dashboard","purchase","stok","barang",
                          "help"));
        ROLE_ITEMS.put("SUPERVISOR",
            Arrays.asList("dashboard","pos","purchase","stok",
                          "barang","customer","pegawai",
                          "laporan","analitik",
                          "settings","help"));
    }

    public SidebarPanel(NavListener listener) {
        this.listener = listener;
        setPreferredSize(new Dimension(215, 0));
        setBackground(UITheme.BG_SIDEBAR);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.BORDER_DEFAULT));
        build();
    }

    private void build() {
        // Brand
        JPanel brand = new JPanel(new BorderLayout(10, 0));
        brand.setOpaque(false);
        brand.setBorder(new EmptyBorder(20, 16, 18, 16));
        JLabel logoLbl = new JLabel(Icons.logoIcon("SR", UITheme.ACCENT_BLUE, 34));
        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));
        JLabel name = new JLabel("Starline");
        name.setFont(new Font("Segoe UI", Font.BOLD, 15));
        name.setForeground(UITheme.TEXT_PRIMARY);
        JLabel sub = new JLabel("Retail System");
        sub.setFont(UITheme.FONT_LABEL);
        sub.setForeground(UITheme.TEXT_MUTED);
        brandText.add(name); brandText.add(sub);
        brand.add(logoLbl, BorderLayout.WEST);
        brand.add(brandText, BorderLayout.CENTER);

        // Nav
        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(4, 10, 8, 10));

        String role = Session.currentUser != null ? Session.currentUser.getRole() : "KASIR";
        List<String> allowed = ROLE_ITEMS.getOrDefault(role, ROLE_ITEMS.get("KASIR"));

        // ── MAIN ──────────────────────────────────────────────────
        groupLabel(nav, "MAIN");
        addItem(nav, "dashboard", Icons::paintDashboard, "Dashboard",   allowed);
        addItem(nav, "pos",       Icons::paintPos,       "POS",         allowed);
        addItem(nav, "purchase",  Icons::paintBox,       "Purchase",    allowed);
        addItem(nav, "stok",      Icons::paintWarehouse, "Inventory",   allowed);

        // ── MASTER DATA ───────────────────────────────────────────
        groupLabel(nav, "MASTER DATA");
        addItem(nav, "barang",    Icons::paintTag,       "Products",    allowed);
        addItem(nav, "kategori",  Icons::paintFolder,    "Category",    allowed);
        addItem(nav, "customer",  Icons::paintUsers,     "Customers",   allowed);
        addItem(nav, "pegawai",   Icons::paintPayroll,   "Employees",   allowed);

        // ── ANALYTICS ─────────────────────────────────────────────
        groupLabel(nav, "ANALYTICS");
        addItem(nav, "laporan",   Icons::paintReport,    "Reports",     allowed);
        addItem(nav, "analitik",  Icons::paintChart,     "Analytics",   allowed);

        // ── MANAGEMENT ────────────────────────────────────────────
        groupLabel(nav, "MANAGEMENT");
        addItem(nav, "users",     Icons::paintUsers,  "User Mgmt",   allowed);
        addItem(nav, "settings",  Icons::paintSettings,  "Settings",    allowed);
        addItem(nav, "help",      Icons::paintHelp,      "Help & Guide",allowed);

        JScrollPane scroll = new JScrollPane(nav);
        scroll.setOpaque(false); scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));

        add(brand, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(buildUserPanel(), BorderLayout.SOUTH);
    }

    private void groupLabel(JPanel nav, String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lbl.setForeground(UITheme.TEXT_MUTED);
        lbl.setBorder(new EmptyBorder(10, 10, 3, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        nav.add(lbl);
    }

    private void addItem(JPanel nav, String id, Icons.Painter painter, String label, List<String> allowed) {
        if (!allowed.contains(id)) return;
        NavButton btn = new NavButton(id, painter, label);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.addActionListener(e -> {
            activeId = id;
            navBtns.forEach(Component::repaint);
            if (listener != null) listener.onNavigate(id);
        });
        navBtns.add(btn);
        nav.add(btn);
        nav.add(Box.createVerticalStrut(1));
    }

    private JPanel buildUserPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setBackground(UITheme.BG_SIDEBAR);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_DEFAULT),
            new EmptyBorder(12, 14, 14, 12)));

        String uName = Session.currentUser != null ? Session.currentUser.getNamaLengkap() : "User";
        String uRole = Session.currentUser != null ? Session.currentUser.getRole() : "";
        String init  = getInitials(uName);
        Color  aC    = roleColor(uRole);
        String disp  = uName.length() > 16 ? uName.substring(0, 15) + "…" : uName;

        JLabel avatar = new JLabel(Icons.avatarIcon(init, aC, 32));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        JLabel lName = new JLabel(disp);
        lName.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lName.setForeground(UITheme.TEXT_PRIMARY);
        JLabel lRole = new JLabel(uRole);
        lRole.setFont(UITheme.FONT_LABEL);
        lRole.setForeground(aC);
        info.add(lName); info.add(lRole);

        // Logout button — large & clearly painted
        btnLogout = new JButton("") {
            boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hover=true; repaint(); }
                    public void mouseExited(MouseEvent e)  { hover=false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (hover) {
                    g2.setColor(new Color(239, 68, 68, 22));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                // Draw logout arrow icon — bigger, clear
                Color ic = hover ? UITheme.ACCENT_CORAL : new Color(180, 185, 200);
                g2.setColor(ic);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx=getWidth()/2, cy=getHeight()/2;
                // Arrow pointing right →
                g2.drawLine(cx-7, cy, cx+6, cy);
                g2.drawLine(cx+2, cy-4, cx+6, cy);
                g2.drawLine(cx+2, cy+4, cx+6, cy);
                // Door/exit frame
                g2.drawLine(cx-7, cy-6, cx-7, cy+6);
                g2.drawLine(cx-7, cy-6, cx-2, cy-6);
                g2.drawLine(cx-7, cy+6, cx-2, cy+6);
                g2.dispose();
            }
        };
        btnLogout.setPreferredSize(new Dimension(36, 36));
        btnLogout.setMinimumSize(new Dimension(36, 36));
        btnLogout.setOpaque(false);
        btnLogout.setContentAreaFilled(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.setToolTipText("Logout");

        p.add(avatar, BorderLayout.WEST);
        p.add(info, BorderLayout.CENTER);
        p.add(btnLogout, BorderLayout.EAST);
        return p;
    }

    public void setActive(String id) {
        activeId = id;
        navBtns.forEach(Component::repaint);
    }
    public JButton getLogoutButton() { return btnLogout; }

    private String getInitials(String n) {
        if (n == null || n.isEmpty()) return "?";
        String[] p = n.trim().split(" ");
        return p.length == 1 ? p[0].substring(0, Math.min(2, p[0].length())).toUpperCase()
                             : ("" + p[0].charAt(0) + p[p.length-1].charAt(0)).toUpperCase();
    }

    private Color roleColor(String r) {
        switch (r == null ? "" : r) {
            case "ADMIN":        return UITheme.ACCENT_CORAL;
            case "SUPERVISOR":   return UITheme.ACCENT_AMBER;
            case "STAFF_GUDANG": return UITheme.ACCENT_TEAL;
            default:             return UITheme.ACCENT_BLUE;
        }
    }

    // ── NavButton ─────────────────────────────────────────────────
    private class NavButton extends JButton {
        final String id;
        final Icons.Painter painter;

        NavButton(String id, Icons.Painter painter, String label) {
            this.id = id;
            this.painter = painter;
            setText(label);
            setFont(UITheme.FONT_NAV);
            setForeground(UITheme.TEXT_SECONDARY);
            setHorizontalAlignment(SwingConstants.LEFT);
            setOpaque(false); setContentAreaFilled(false);
            setBorderPainted(false); setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(195, 36));
            setPreferredSize(new Dimension(195, 36));
            setBorder(new EmptyBorder(0, 8, 0, 8));
        }

        @Override protected void paintComponent(Graphics g) {
            boolean active = id.equals(activeId);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (active) {
                g2.setColor(UITheme.ACCENT_LIME);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            } else if (getModel().isRollover()) {
                g2.setColor(UITheme.BG_HOVER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }

            // Icon
            Color iconColor = active ? UITheme.TEXT_ON_LIME
                            : getModel().isRollover() ? UITheme.TEXT_PRIMARY
                            : UITheme.TEXT_MUTED;
            g2.translate(8, (getHeight() - 18) / 2);
            if (painter != null) painter.paint(g2, 9, 9, 8, iconColor);
            g2.translate(-8, -(getHeight() - 18) / 2);

            // Label
            g2.setFont(active ? new Font("Segoe UI", Font.BOLD, 13) : UITheme.FONT_NAV);
            g2.setColor(active ? UITheme.TEXT_ON_LIME
                    : getModel().isRollover() ? UITheme.TEXT_PRIMARY : UITheme.TEXT_SECONDARY);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), 32, getHeight() / 2 + fm.getAscent() / 2 - 1);
            g2.dispose();
        }
    }
}
