package com.app.smartretail.view.dashboard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.app.smartretail.controller.DashboardController;
import com.app.smartretail.model.Barang;
import com.app.smartretail.utils.AlertUtil;
import com.app.smartretail.utils.FormatUtil;
import com.app.smartretail.utils.Session;
import com.app.smartretail.utils.UITheme;
import com.app.smartretail.view.analitik.AnalitikForm;
import com.app.smartretail.view.auth.LoginForm;
import com.app.smartretail.view.component.Icons;
import com.app.smartretail.view.component.SidebarPanel;
import com.app.smartretail.view.master.BarangForm;
import com.app.smartretail.view.master.CustomerForm;
import com.app.smartretail.view.master.KategoriForm;
import com.app.smartretail.view.master.UserForm;
import com.app.smartretail.view.pegawai.PegawaiForm;
import com.app.smartretail.view.report.ReportForm;
import com.app.smartretail.view.settings.HelpForm;
import com.app.smartretail.view.settings.SettingsForm;
import com.app.smartretail.view.transaksi.PenjualanForm;
import com.app.smartretail.view.transaksi.PurchaseForm;
import com.app.smartretail.view.transaksi.StokForm;

public class DashboardForm extends JFrame {

    private final DashboardController ctrl = new DashboardController();
    private SidebarPanel sidebar;
    private JPanel contentArea;

    public DashboardForm() {
        UITheme.apply();
        initUI();
        navigateTo("dashboard");
    }

    private void initUI() {
        String uName = Session.currentUser != null ? Session.currentUser.getNamaLengkap() : "";
        setTitle("SRMS — " + uName);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 760);
        setMinimumSize(new Dimension(1100, 640));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_PAGE);

        sidebar = new SidebarPanel(this::navigateTo);
        sidebar.getLogoutButton().addActionListener(e -> doLogout());

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UITheme.BG_SURFACE);
        main.add(buildTopBar(), BorderLayout.NORTH);

        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(UITheme.BG_SURFACE);
        main.add(contentArea, BorderLayout.CENTER);

        root.add(sidebar, BorderLayout.WEST);
        root.add(main, BorderLayout.CENTER);
        setContentPane(root);
    }

    // ── Top bar ───────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UITheme.BG_CARD);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_DEFAULT),
            new EmptyBorder(10, 22, 10, 18)));

        String uName = Session.currentUser != null ? Session.currentUser.getNamaLengkap() : "Admin";
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel lWelcome = new JLabel("Welcome, " + uName);
        lWelcome.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lWelcome.setForeground(UITheme.TEXT_PRIMARY);
        JLabel lSub = new JLabel("Here's what happening in your store.");
        lSub.setFont(UITheme.FONT_SMALL);
        lSub.setForeground(UITheme.TEXT_SECONDARY);
        left.add(lWelcome);
        left.add(lSub);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        // Search icon button
        right.add(topBtn(Icons.SEARCH, "Cari", UITheme.TEXT_SECONDARY));
        // Bell with badge
        JPanel bellWrap = new JPanel(null);
        bellWrap.setOpaque(false);
        bellWrap.setPreferredSize(new Dimension(36, 36));
        JButton bellBtn = topBtn(Icons.BELL, "Notifikasi", UITheme.TEXT_SECONDARY);
        bellBtn.setBounds(0, 0, 36, 36);
        JLabel badge = new JLabel("2");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 8));
        badge.setForeground(Color.WHITE);
        badge.setOpaque(true);
        badge.setBackground(UITheme.ACCENT_CORAL);
        badge.setBounds(20, 2, 14, 14);
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        bellWrap.add(bellBtn); bellWrap.add(badge);
        right.add(bellWrap);
        right.add(topBtn(Icons.CALENDAR, "Kalender", UITheme.TEXT_SECONDARY));

        // Avatar
        String role = Session.currentUser != null ? Session.currentUser.getRole() : "KASIR";
        String init = getInitials(uName);
        JLabel avatar = new JLabel(Icons.avatarIcon(init, roleColor(role), 34));
        avatar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        avatar.setBorder(new EmptyBorder(0, 4, 0, 0));
        right.add(avatar);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JButton topBtn(Icon ico, String tip, Color fg) {
        JButton b = new JButton(ico) {
            @Override protected void paintComponent(Graphics g) {
                if (getModel().isRollover()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(UITheme.BG_HOVER);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        b.setPreferredSize(new Dimension(36, 36));
        b.setMinimumSize(new Dimension(36, 36));
        b.setOpaque(false); b.setContentAreaFilled(false);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setToolTipText(tip);
        return b;
    }

    // ── Navigation ────────────────────────────────────────────────
    public void navigateTo(String id) {
        sidebar.setActive(id);
        JPanel view;
        switch (id) {
            case "pos":       view = new PenjualanForm();          break;
            case "purchase":  view = new PurchaseForm();           break;
            case "stok":      view = new StokForm();               break;
            case "barang":    view = new BarangForm();             break;
            case "kategori":  view = new KategoriForm();           break;
            case "customer":  view = new CustomerForm();           break;
            case "pegawai":   view = new PegawaiForm();            break;
            case "laporan":   view = new ReportForm();             break;
            case "analitik":  view = new AnalitikForm();           break;
            case "users":     view = new UserForm();               break;
            case "settings":  view = new SettingsForm();           break;
            case "help":      view = new HelpForm();               break;
            default:          view = buildDashboard();             break;
        }
        contentArea.removeAll();
        contentArea.add(view, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }

    // ── Dashboard Page ────────────────────────────────────────────
    private JPanel buildDashboard() {
        JPanel page = new JPanel(new BorderLayout(0, 0));
        page.setBackground(UITheme.BG_SURFACE);
        page.setBorder(new EmptyBorder(22, 24, 22, 24));

        // Metric cards
        JPanel cardsRow = new JPanel(new GridLayout(1, 3, 16, 0));
        cardsRow.setOpaque(false);
        cardsRow.setBorder(new EmptyBorder(0, 0, 18, 0));

        JLabel valRevenue   = mval("Rp 0");
        JLabel valOrders    = mval("0");
        JLabel valCustomers = mval("0");
        int[] spR = {30,55,40,70,50,80,65,90,55,85};
        int[] spO = {5,12,8,15,10,18,14,20,11,17};
        int[] spC = {100,105,103,108,107,112,110,115,112,118};
        cardsRow.add(tintCard("Total Revenue",   valRevenue,   "↑ 10.5%   From Last Day", UITheme.CARD_AMBER_BG,  Icons::paintPayroll, UITheme.ACCENT_AMBER,  spR));
        cardsRow.add(tintCard("Total Orders",    valOrders,    "↑ 10.5%   From Last Day", UITheme.CARD_PURPLE_BG, Icons::paintBox,     UITheme.ACCENT_PURPLE, spO));
        cardsRow.add(tintCard("Total Customers", valCustomers, "↑ 10.5%   From Last Day", UITheme.CARD_TEAL_BG,   Icons::paintUsers,   UITheme.ACCENT_TEAL,   spC));

        // Middle row
        JPanel midRow = new JPanel(new GridLayout(1, 2, 16, 0));
        midRow.setOpaque(false);
        midRow.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel chartCard = UITheme.card();
        chartCard.setLayout(new BorderLayout(0, 10));
        JPanel cHdr = new JPanel(new BorderLayout()); cHdr.setOpaque(false);
        JLabel cTitle = new JLabel("Orders Overview");
        cTitle.setFont(UITheme.FONT_H2); cTitle.setForeground(UITheme.TEXT_PRIMARY);
        JPanel leg = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); leg.setOpaque(false);
        leg.add(ldot(UITheme.ACCENT_AMBER, "Orders")); leg.add(ldot(UITheme.ACCENT_PURPLE, "Profit"));
        cHdr.add(cTitle, BorderLayout.WEST); cHdr.add(leg, BorderLayout.EAST);
        JPanel bar = UITheme.barChart(new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul"},
            new int[]{30,55,40,60,50,80,65}, UITheme.ACCENT_AMBER);
        bar.setPreferredSize(new Dimension(0, 160));
        chartCard.add(cHdr, BorderLayout.NORTH); chartCard.add(bar, BorderLayout.CENTER);

        JPanel donutCard = UITheme.card();
        donutCard.setLayout(new BorderLayout(0, 10));
        JLabel dTitle = new JLabel("Sale Analytics");
        dTitle.setFont(UITheme.FONT_H2); dTitle.setForeground(UITheme.TEXT_PRIMARY);
        int[] dv = {70,20,10}; Color[] dc = {UITheme.ACCENT_TEAL, UITheme.ACCENT_AMBER, UITheme.ACCENT_PURPLE};
        JPanel donut = UITheme.donutChart(dv, dc, "100%");
        donut.setPreferredSize(new Dimension(110, 110));
        JPanel donutArea = new JPanel(new BorderLayout(10, 0)); donutArea.setOpaque(false);
        donutArea.add(donut, BorderLayout.WEST);
        donutArea.add(buildDonutLegend(new String[]{"Returned","Completed","Distributed"}, dc, new String[]{"70%","20%","10%"}), BorderLayout.CENTER);
        donutCard.add(dTitle, BorderLayout.NORTH); donutCard.add(donutArea, BorderLayout.CENTER);

        midRow.add(chartCard); midRow.add(donutCard);

        // Bottom row
        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 16, 0));
        bottomRow.setOpaque(false);

        // Sales summary
        JPanel salesCard = UITheme.card();
        salesCard.setLayout(new BorderLayout(0, 10));
        JLabel sTitle = new JLabel("Sales Summary");
        sTitle.setFont(UITheme.FONT_H2); sTitle.setForeground(UITheme.TEXT_PRIMARY);
        JPanel sStats = new JPanel(new GridLayout(1, 3)); sStats.setOpaque(false);
        JLabel vTot = mval("—"); JLabel vMonth = mval("—"); JLabel vToday = mval("—");
        sStats.add(sCol("Total Sales", vTot));
        sStats.add(sCol("This Month",  vMonth));
        sStats.add(sCol("Today",       vToday));
        JLabel growth = new JLabel("  ↑ 20% increased");
        growth.setFont(UITheme.FONT_SMALL); growth.setForeground(UITheme.ACCENT_GREEN);
        salesCard.add(sTitle, BorderLayout.NORTH);
        salesCard.add(sStats, BorderLayout.CENTER);
        salesCard.add(growth, BorderLayout.SOUTH);

        // Stok rendah table
        JPanel stokCard = UITheme.card();
        stokCard.setLayout(new BorderLayout(0, 8));
        JPanel stHdr = new JPanel(new BorderLayout()); stHdr.setOpaque(false);
        JLabel stTitle = new JLabel("Low Stock Products");
        stTitle.setFont(UITheme.FONT_H2); stTitle.setForeground(UITheme.TEXT_PRIMARY);
        JLabel stBadge = UITheme.badge("Needs Restock", UITheme.ACCENT_CORAL, UITheme.ACCENT_CORAL);
        stHdr.add(stTitle, BorderLayout.WEST); stHdr.add(stBadge, BorderLayout.EAST);
        String[] stCols = {"Code","Product","Stok","Min"};
        DefaultTableModel stMdl = new DefaultTableModel(stCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable stTbl = new JTable(stMdl); UITheme.styleTable(stTbl); stTbl.setRowHeight(32);
        stTbl.getColumnModel().getColumn(0).setMaxWidth(80);
        stTbl.getColumnModel().getColumn(2).setMaxWidth(50);
        stTbl.getColumnModel().getColumn(3).setMaxWidth(50);
        stTbl.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c){
                Component cp=super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                cp.setBackground(sel?new Color(238,242,255):(r%2==0?UITheme.BG_CARD:UITheme.BG_ROW_ALT));
                cp.setForeground(c==2?UITheme.ACCENT_CORAL:UITheme.TEXT_PRIMARY);
                ((JLabel)cp).setBorder(new EmptyBorder(0,12,0,12));
                return cp;
            }
        });
        stokCard.add(stHdr, BorderLayout.NORTH);
        stokCard.add(UITheme.styledScroll(stTbl), BorderLayout.CENTER);

        bottomRow.add(salesCard); bottomRow.add(stokCard);

        JPanel center = new JPanel(); center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(cardsRow); center.add(midRow); center.add(bottomRow);
        page.add(center, BorderLayout.CENTER);

        // Load async
        new SwingWorker<Map<String,Object>,Void>(){
            protected Map<String,Object> doInBackground(){ return ctrl.getSummary(); }
            protected void done(){
                try {
                    Map<String,Object> d=get();
                    double omzetH = (double)d.getOrDefault("omzetHariIni",0.0);
                    double omzetB = (double)d.getOrDefault("omzetBulanIni",0.0);
                    valRevenue.setText(FormatUtil.formatRupiah(omzetH));
                    valOrders.setText(String.valueOf(d.getOrDefault("transaksiHariIni",0)));
                    valCustomers.setText(String.valueOf(d.getOrDefault("totalBarang",0)));
                    vTot.setText(FormatUtil.formatRupiah(omzetB*12));
                    vMonth.setText(FormatUtil.formatRupiah(omzetB));
                    vToday.setText(FormatUtil.formatRupiah(omzetH));
                    List<Barang> lr=ctrl.getStokRendah();
                    for (Barang b:lr)
                        stMdl.addRow(new Object[]{b.getKodeBarang(),b.getNamaBarang(),b.getStok(),b.getStokMinimum()});
                } catch(Exception ex){ ex.printStackTrace(); }
            }
        }.execute();

        return page;
    }

    // ── Helpers ───────────────────────────────────────────────────
    private JPanel tintCard(String label, JLabel valLbl, String sub, Color tint,
                             Icons.Painter ip, Color accent, int[] spark) {
        JPanel c = UITheme.tintCard(tint);
        c.setLayout(new BorderLayout(0, 8));
        JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false);
        JPanel txt = new JPanel(); txt.setOpaque(false);
        txt.setLayout(new BoxLayout(txt, BoxLayout.Y_AXIS));
        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(UITheme.FONT_LABEL);
        lbl.setForeground(UITheme.TEXT_SECONDARY);
        txt.add(lbl); txt.add(Box.createVerticalStrut(4)); txt.add(valLbl);
        // Icon bubble
        JPanel ib = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255,255,255,110));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.dispose();
            }
        };
        ib.setOpaque(false);
        ib.setPreferredSize(new Dimension(48,48));
        JLabel ico = new JLabel(Icons.tinted(ip, 26, accent));
        ib.add(ico);
        top.add(txt, BorderLayout.CENTER); top.add(ib, BorderLayout.EAST);
        JLabel subLbl = new JLabel(sub);
        subLbl.setFont(UITheme.FONT_SMALL); subLbl.setForeground(UITheme.ACCENT_GREEN);
        c.add(top, BorderLayout.CENTER); c.add(subLbl, BorderLayout.SOUTH);
        if (spark != null) {
            JPanel sp = UITheme.sparkline(spark, accent);
            sp.setPreferredSize(new Dimension(0, 30));
            c.add(sp, BorderLayout.NORTH);
        }
        return c;
    }

    private JLabel mval(String v) {
        JLabel l = new JLabel(v);
        l.setFont(new Font("Segoe UI", Font.BOLD, 20));
        l.setForeground(UITheme.TEXT_PRIMARY);
        return l;
    }

    private JPanel sCol(String label, JLabel val) {
        JPanel p = new JPanel(); p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel lbl = new JLabel(label); lbl.setFont(UITheme.FONT_LABEL); lbl.setForeground(UITheme.TEXT_SECONDARY);
        p.add(lbl); p.add(val); return p;
    }

    private JPanel buildDonutLegend(String[] labels, Color[] colors, String[] pcts) {
        JPanel p = new JPanel(); p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(14, 4, 0, 0));
        for (int i = 0; i < labels.length; i++) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 3)); row.setOpaque(false);
            row.add(new JLabel(Icons.dot(colors[i])));
            JLabel lbl = new JLabel(pcts[i] + "  " + labels[i]);
            lbl.setFont(UITheme.FONT_SMALL); lbl.setForeground(UITheme.TEXT_SECONDARY);
            row.add(lbl); p.add(row);
        }
        return p;
    }

    private JPanel ldot(Color c, String label) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0)); p.setOpaque(false);
        JLabel lbl = new JLabel(label); lbl.setFont(UITheme.FONT_SMALL); lbl.setForeground(UITheme.TEXT_SECONDARY);
        p.add(new JLabel(Icons.dot(c))); p.add(lbl); return p;
    }

    private void doLogout() {
        if (AlertUtil.showConfirm(this, "Apakah Anda yakin ingin keluar?")) {
            Session.logout(); dispose(); new LoginForm().setVisible(true);
        }
    }

    private String getInitials(String n) {
        if (n == null || n.isEmpty()) return "?";
        String[] p = n.trim().split(" ");
        return p.length == 1 ? p[0].substring(0, Math.min(2, p[0].length())).toUpperCase()
                             : ("" + p[0].charAt(0) + p[p.length-1].charAt(0)).toUpperCase();
    }

    private Color roleColor(String r) {
        switch (r == null ? "" : r) {
            case "ADMIN": return UITheme.ACCENT_CORAL;
            case "SUPERVISOR": return UITheme.ACCENT_AMBER;
            case "STAFF_GUDANG": return UITheme.ACCENT_TEAL;
            default: return UITheme.ACCENT_BLUE;
        }
    }
}
