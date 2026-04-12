package com.app.smartretail.view.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * SidebarPanel — Starline-style rounded pill sidebar.
 * Active item: lime-yellow pill background, bold text.
 */
public class SidebarPanel extends JPanel {

    public interface NavListener { void onNavigate(String id); }

    private final NavListener listener;
    private String activeId = "dashboard";
    private final List<NavButton> navBtns = new ArrayList<>();
    private JButton btnLogout;

    // Define which items are visible per role
    private static final Map<String, List<String>> ROLE_ITEMS = new HashMap<>();
    static {
        ROLE_ITEMS.put("ADMIN",       Arrays.asList("dashboard","pos","sales","laporan","pembelian","stok","barang","kategori","supplier","customer","grafik","users","settings"));
        ROLE_ITEMS.put("KASIR",       Arrays.asList("dashboard","pos","sales","customer","settings"));
        ROLE_ITEMS.put("STAFF_GUDANG",Arrays.asList("dashboard","pembelian","stok","barang","supplier","settings"));
        ROLE_ITEMS.put("SUPERVISOR",  Arrays.asList("dashboard","pos","sales","laporan","pembelian","stok","barang","grafik","customer","settings"));
    }

    public SidebarPanel(NavListener listener) {
        this.listener = listener;
        setPreferredSize(new Dimension(210, 0));
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

        // Nav items
        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(4, 10, 8, 10));

        String role = Session.currentUser != null ? Session.currentUser.getRole() : "KASIR";
        List<String> allowed = ROLE_ITEMS.getOrDefault(role, ROLE_ITEMS.get("KASIR"));

        addItem(nav, "dashboard", Icons.DASHBOARD, "Dashboard", allowed);
        addItem(nav, "pos",       Icons.POS,       "POS",       allowed);
        addItem(nav, "sales",     Icons.REPORT,    "Sales",     allowed);
        addItem(nav, "laporan",   Icons.CHART,     "Laporan",   allowed);
        addItem(nav, "pembelian", Icons.BOX,       "Purchase",  allowed);
        addItem(nav, "stok",      Icons.WAREHOUSE, "Inventory", allowed);
        addItem(nav, "barang",    Icons.TAG,       "Products",  allowed);
        addItem(nav, "customer",  Icons.USERS,     "Customers & HR", allowed);
        addItem(nav, "supplier",  Icons.TRUCK,     "Supplier",  allowed);
        addItem(nav, "grafik",    Icons.CHART,     "Analytics", allowed);
        addItem(nav, "users",     Icons.PAYROLL,   "Payroll",   allowed);
        addItem(nav, "settings",  Icons.SETTINGS,  "Settings",  allowed);
        nav.add(Box.createVerticalStrut(8));

        // Help always visible
        NavButton helpBtn = new NavButton("help", Icons.HELP, "Help");
        helpBtn.setAlignmentX(LEFT_ALIGNMENT);
        helpBtn.addActionListener(e -> {});
        nav.add(helpBtn);

        JScrollPane scroll = new JScrollPane(nav);
        scroll.setOpaque(false); scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(0,0));

        // Bottom user panel
        JPanel bottom = buildUserPanel();

        add(brand, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private void addItem(JPanel nav, String id, Icon icon, String label, List<String> allowed) {
        if (!allowed.contains(id)) return;
        NavButton btn = new NavButton(id, icon, label);
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.addActionListener(e -> {
            activeId = id;
            navBtns.forEach(Component::repaint);
            if (listener != null) listener.onNavigate(id);
        });
        navBtns.add(btn);
        nav.add(btn);
        nav.add(Box.createVerticalStrut(2));
    }

    private JPanel buildUserPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setBackground(UITheme.BG_SIDEBAR);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_DEFAULT),
            new EmptyBorder(12, 14, 14, 12)));

        String uName = Session.currentUser != null ? Session.currentUser.getNamaLengkap() : "User";
        String uRole = Session.currentUser != null ? Session.currentUser.getRole() : "";
        Color aColor = roleColor(uRole);
        String init  = getInitials(uName);
        String disp  = uName.length() > 16 ? uName.substring(0,15)+"…" : uName;

        JLabel avatar = new JLabel(Icons.avatarIcon(init, aColor, 32));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        JLabel lName = new JLabel(disp);
        lName.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lName.setForeground(UITheme.TEXT_PRIMARY);
        JLabel lRole = new JLabel(uRole);
        lRole.setFont(UITheme.FONT_LABEL);
        lRole.setForeground(aColor);
        info.add(lName); info.add(lRole);

        // Logout button — clearly visible with icon
        btnLogout = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(239,68,68,18));
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                }
                Icons.tinted(Icons::paintLogout, 18, getModel().isRollover()
                    ? UITheme.ACCENT_CORAL : UITheme.TEXT_MUTED)
                    .paintIcon(this, g2, (getWidth()-18)/2, (getHeight()-18)/2);
                g2.dispose();
            }
        };
        btnLogout.setPreferredSize(new Dimension(32,32));
        btnLogout.setOpaque(false); btnLogout.setContentAreaFilled(false);
        btnLogout.setBorderPainted(false); btnLogout.setFocusPainted(false);
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
        if (n==null||n.isEmpty()) return "?";
        String[] p=n.trim().split(" ");
        return p.length==1 ? p[0].substring(0,Math.min(2,p[0].length())).toUpperCase()
                           : (""+p[0].charAt(0)+p[p.length-1].charAt(0)).toUpperCase();
    }
    private Color roleColor(String r) {
        if (r==null) return UITheme.ACCENT_BLUE;
        switch(r) {
            case "ADMIN":        return UITheme.ACCENT_CORAL;
            case "SUPERVISOR":   return UITheme.ACCENT_AMBER;
            case "STAFF_GUDANG": return UITheme.ACCENT_TEAL;
            default:             return UITheme.ACCENT_BLUE;
        }
    }

    // ── NavButton: Starline pill style ────────────────────────────
    private class NavButton extends JButton {
        final String id;
        final Icon ico;
        NavButton(String id, Icon ico, String label) {
            super(label); this.id=id; this.ico=ico;
            setFont(UITheme.FONT_NAV);
            setForeground(UITheme.TEXT_SECONDARY);
            setHorizontalAlignment(SwingConstants.LEFT);
            setIconTextGap(10);
            setIcon(ico);
            setOpaque(false); setContentAreaFilled(false);
            setBorderPainted(false); setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(190, 38));
            setPreferredSize(new Dimension(190, 38));
            setBorder(new EmptyBorder(0, 10, 0, 10));
        }
        @Override protected void paintComponent(Graphics g) {
            boolean active = id.equals(activeId);
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (active) {
                // Lime-yellow pill background
                g2.setColor(UITheme.ACCENT_LIME);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                setForeground(UITheme.TEXT_ON_LIME);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
            } else if (getModel().isRollover()) {
                g2.setColor(UITheme.BG_HOVER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                setForeground(UITheme.TEXT_PRIMARY);
                setFont(UITheme.FONT_NAV);
            } else {
                setForeground(UITheme.TEXT_SECONDARY);
                setFont(UITheme.FONT_NAV);
            }
            g2.dispose();
            // Paint icon with correct color
            if (ico != null) {
                Color iconColor = active ? UITheme.TEXT_ON_LIME :
                    (getModel().isRollover() ? UITheme.TEXT_PRIMARY : UITheme.TEXT_MUTED);
                Icon tinted = Icons.tinted(getNavPainter(id), 18, iconColor);
                Insets ins = getInsets();
                tinted.paintIcon(this, g, ins.left, (getHeight()-18)/2);
            }
            // Paint text manually
            g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            g2.setFont(getFont()); g2.setColor(getForeground());
            g2.drawString(getText(), 36, getHeight()/2+g2.getFontMetrics().getAscent()/2-1);
            g2.dispose();
        }
    }

    private Icons.Painter getNavPainter(String id) {
        switch(id) {
            case "dashboard": return Icons::paintDashboard;
            case "pos":       return Icons::paintPos;
            case "sales":
            case "laporan":   return Icons::paintReport;
            case "pembelian": return Icons::paintBox;
            case "stok":      return Icons::paintWarehouse;
            case "barang":    return Icons::paintTag;
            case "customer":  return Icons::paintUsers;
            case "supplier":  return Icons::paintTruck;
            case "grafik":    return Icons::paintChart;
            case "users":
            case "settings":  return Icons::paintSettings;
            case "help":      return Icons::paintHelp;
            default:          return Icons::paintFolder;
        }
    }
}
