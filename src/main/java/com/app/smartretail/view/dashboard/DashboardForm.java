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
import com.app.smartretail.view.auth.LoginForm;
import com.app.smartretail.view.component.Icons;
import com.app.smartretail.view.component.SidebarPanel;
import com.app.smartretail.view.master.BarangForm;
import com.app.smartretail.view.master.CustomerForm;
import com.app.smartretail.view.master.KategoriForm;
import com.app.smartretail.view.master.SupplierForm;
import com.app.smartretail.view.master.UserForm;
import com.app.smartretail.view.report.ReportForm;
import com.app.smartretail.view.transaksi.PembelianForm;
import com.app.smartretail.view.transaksi.PenjualanForm;
import com.app.smartretail.view.transaksi.StokForm;

public class DashboardForm extends JFrame {

    private final DashboardController ctrl = new DashboardController();
    private SidebarPanel sidebar;
    private JPanel contentArea;
    private JLabel lblWelcome;

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

        // Sidebar
        sidebar = new SidebarPanel(this::navigateTo);
        sidebar.getLogoutButton().addActionListener(e -> doLogout());

        // Main: topbar + content
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UITheme.BG_SURFACE);

        JPanel topbar = buildTopBar();
        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(UITheme.BG_SURFACE);

        main.add(topbar, BorderLayout.NORTH);
        main.add(contentArea, BorderLayout.CENTER);

        root.add(sidebar, BorderLayout.WEST);
        root.add(main, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UITheme.BG_CARD);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0, UITheme.BORDER_DEFAULT),
            new EmptyBorder(12,22,12,18)));

        // Welcome text
        JPanel leftBlock = new JPanel();
        leftBlock.setOpaque(false);
        leftBlock.setLayout(new BoxLayout(leftBlock, BoxLayout.Y_AXIS));
        String uName = Session.currentUser != null ? Session.currentUser.getNamaLengkap() : "Admin";
        lblWelcome = new JLabel("Welcome, " + uName);
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblWelcome.setForeground(UITheme.TEXT_PRIMARY);
        JLabel lSub = new JLabel("Here's what happening in your store.");
        lSub.setFont(UITheme.FONT_SMALL);
        lSub.setForeground(UITheme.TEXT_SECONDARY);
        leftBlock.add(lblWelcome);
        leftBlock.add(lSub);

        // Right: icons
        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightBtns.setOpaque(false);

        rightBtns.add(topIconBtn(Icons.SEARCH,   "Cari"));
        rightBtns.add(topIconBtn(Icons.MOON,     "Mode Gelap"));
        rightBtns.add(topIconBtn(Icons.BELL,     "Notifikasi"));
        rightBtns.add(topIconBtn(Icons.CALENDAR, "Kalender"));

        // Avatar
        String role = Session.currentUser != null ? Session.currentUser.getRole() : "KASIR";
        String init = Session.currentUser != null ? getInitials(Session.currentUser.getNamaLengkap()) : "?";
        Color aC = roleColor(role);
        JLabel avatarLbl = new JLabel(Icons.avatarIcon(init, aC, 34));
        avatarLbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        rightBtns.add(avatarLbl);

        bar.add(leftBlock, BorderLayout.WEST);
        bar.add(rightBtns, BorderLayout.EAST);
        return bar;
    }

    private JButton topIconBtn(Icon ico, String tip) {
        JButton b = new JButton(ico) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(UITheme.BG_HOVER);
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                }
                g2.dispose(); super.paintComponent(g);
            }
        };
        b.setPreferredSize(new Dimension(32,32));
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
            case "pos":       view = new PenjualanForm(); break;
            case "pembelian": view = new PembelianForm(); break;
            case "stok":      view = new StokForm();      break;
            case "barang":    view = new BarangForm();    break;
            case "kategori":  view = new KategoriForm();  break;
            case "supplier":  view = new SupplierForm();  break;
            case "customer":  view = new CustomerForm();  break;
            case "sales":
            case "laporan":
            case "grafik":    view = new ReportForm();    break;
            case "users":     view = new UserForm();      break;
            case "settings":  view = buildSettingsPanel();break;
            default:          view = buildDashboard();    break;
        }
        contentArea.removeAll();
        contentArea.add(view, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }

    // ── Dashboard ─────────────────────────────────────────────────
    private JPanel buildDashboard() {
        JPanel page = new JPanel(new BorderLayout(0,0));
        page.setBackground(UITheme.BG_SURFACE);
        page.setBorder(new EmptyBorder(22,24,22,24));

        // ── Metric cards row ─────────────────────────────────────
        JPanel cardsRow = new JPanel(new GridLayout(1,3,16,0));
        cardsRow.setOpaque(false);
        cardsRow.setBorder(new EmptyBorder(0,0,18,0));

        JLabel valRevenue = metricVal("Rp 0");
        JLabel valOrders  = metricVal("0");
        JLabel valCustomers = metricVal("0");

        int[] spkRev = {30,55,40,70,50,80,65,90,55,85};
        int[] spkOrd = {5,12,8,15,10,18,14,20,11,17};
        int[] spkCst = {100,105,103,108,107,112,110,115,112,118};

        cardsRow.add(tintMetricCard("Total Revenue", valRevenue,
            "10.5%   From Last Day", UITheme.CARD_AMBER_BG,
            Icons.tinted(Icons::paintPayroll, 34, UITheme.ACCENT_AMBER), spkRev, UITheme.ACCENT_AMBER));
        cardsRow.add(tintMetricCard("Total Orders", valOrders,
            "10.5%   From Last Day", UITheme.CARD_PURPLE_BG,
            Icons.tinted(Icons::paintBox, 34, UITheme.ACCENT_PURPLE), spkOrd, UITheme.ACCENT_PURPLE));
        cardsRow.add(tintMetricCard("Total Customers", valCustomers,
            "10.5%   From Last Day", UITheme.CARD_TEAL_BG,
            Icons.tinted(Icons::paintUsers, 34, UITheme.ACCENT_TEAL), spkCst, UITheme.ACCENT_TEAL));

        // ── Middle row: chart + donut + top products ──────────────
        JPanel midRow = new JPanel(new GridLayout(1,2,16,0));
        midRow.setOpaque(false);
        midRow.setBorder(new EmptyBorder(0,0,16,0));

        // Orders chart card
        JPanel chartCard = UITheme.card();
        chartCard.setLayout(new BorderLayout(0,10));
        JPanel chartHdr = new JPanel(new BorderLayout());
        chartHdr.setOpaque(false);
        JLabel chartTitle = new JLabel("Orders Overview");
        chartTitle.setFont(UITheme.FONT_H2); chartTitle.setForeground(UITheme.TEXT_PRIMARY);
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));
        legend.setOpaque(false);
        legend.add(legendDot(UITheme.ACCENT_AMBER, "Orders"));
        legend.add(legendDot(UITheme.ACCENT_PURPLE, "Profit"));
        chartHdr.add(chartTitle, BorderLayout.WEST);
        chartHdr.add(legend, BorderLayout.EAST);
        String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul"};
        int[] mvals = {30,55,40,60,50,80,65};
        JPanel barC = UITheme.barChart(months, mvals, UITheme.ACCENT_AMBER);
        barC.setPreferredSize(new Dimension(0,160));
        chartCard.add(chartHdr, BorderLayout.NORTH);
        chartCard.add(barC, BorderLayout.CENTER);

        // Sale Analytics
        JPanel analyticsCard = UITheme.card();
        analyticsCard.setLayout(new BorderLayout(0,10));
        JLabel aTitle = new JLabel("Sale Analytics");
        aTitle.setFont(UITheme.FONT_H2); aTitle.setForeground(UITheme.TEXT_PRIMARY);
        int[] dvals = {70,20,10};
        Color[] dcols = {UITheme.ACCENT_TEAL, UITheme.ACCENT_AMBER, UITheme.ACCENT_PURPLE};
        JPanel donut = UITheme.donutChart(dvals, dcols, "100%");
        donut.setPreferredSize(new Dimension(130,130));
        JPanel donutArea = new JPanel(new BorderLayout(10,0));
        donutArea.setOpaque(false);
        JPanel donutLegend = buildDonutLegend(
            new String[]{"Returned","Completed","Distributed"},
            dvals, dcols, new String[]{"70%","20%","10%"});
        donutArea.add(donut, BorderLayout.WEST);
        donutArea.add(donutLegend, BorderLayout.CENTER);
        analyticsCard.add(aTitle, BorderLayout.NORTH);
        analyticsCard.add(donutArea, BorderLayout.CENTER);

        midRow.add(chartCard);
        midRow.add(analyticsCard);

        // ── Bottom: Sales summary + Stok rendah table ─────────────
        JPanel bottomRow = new JPanel(new GridLayout(1,2,16,0));
        bottomRow.setOpaque(false);

        // Sales summary card
        JPanel salesCard = UITheme.card();
        salesCard.setLayout(new BorderLayout(0,10));
        JLabel salesTitle = new JLabel("Sales");
        salesTitle.setFont(UITheme.FONT_H2); salesTitle.setForeground(UITheme.TEXT_PRIMARY);
        JPanel salesStats = new JPanel(new GridLayout(1,3,0,0));
        salesStats.setOpaque(false);
        salesStats.add(salesStatCol("Total Sales",  "—"));
        salesStats.add(salesStatCol("This Month",   "—"));
        salesStats.add(salesStatCol("Today",        "—"));
        JLabel salesGrowth = new JLabel("  20% increased");
        salesGrowth.setFont(UITheme.FONT_SMALL);
        salesGrowth.setForeground(UITheme.ACCENT_GREEN);
        salesCard.add(salesTitle, BorderLayout.NORTH);
        salesCard.add(salesStats, BorderLayout.CENTER);
        salesCard.add(salesGrowth, BorderLayout.SOUTH);

        // Stok rendah table card
        JPanel stokCard = UITheme.card();
        stokCard.setLayout(new BorderLayout(0,10));
        JPanel stokHdr = new JPanel(new BorderLayout());
        stokHdr.setOpaque(false);
        JLabel stokTitle = new JLabel("Top Products — Low Stock");
        stokTitle.setFont(UITheme.FONT_H2); stokTitle.setForeground(UITheme.TEXT_PRIMARY);
        stokHdr.add(stokTitle, BorderLayout.WEST);
        stokCard.add(stokHdr, BorderLayout.NORTH);

        String[] stokCols = {"Product","Code","Stok","Status"};
        DefaultTableModel stokMdl = new DefaultTableModel(stokCols,0){public boolean isCellEditable(int r,int c){return false;}};
        JTable stokTbl = new JTable(stokMdl);
        UITheme.styleTable(stokTbl);
        stokTbl.setRowHeight(34);
        stokTbl.getColumnModel().getColumn(1).setMaxWidth(80);
        stokTbl.getColumnModel().getColumn(2).setMaxWidth(60);
        stokTbl.getColumnModel().getColumn(3).setMaxWidth(80);
        stokTbl.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component cp=super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                cp.setBackground(sel?new Color(238,242,255):(r%2==0?UITheme.BG_CARD:UITheme.BG_ROW_ALT));
                cp.setForeground(c==3 ? UITheme.ACCENT_CORAL : UITheme.TEXT_PRIMARY);
                ((JLabel)cp).setBorder(new EmptyBorder(0,12,0,12));
                return cp;
            }
        });
        stokCard.add(UITheme.styledScroll(stokTbl), BorderLayout.CENTER);

        bottomRow.add(salesCard);
        bottomRow.add(stokCard);

        // Assemble
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(cardsRow);
        center.add(midRow);
        center.add(bottomRow);
        page.add(center, BorderLayout.CENTER);

        // Load data async
        new SwingWorker<Map<String,Object>,Void>(){
            protected Map<String,Object> doInBackground(){ return ctrl.getSummary(); }
            protected void done(){
                try {
                    Map<String,Object> d=get();
                    valRevenue.setText(FormatUtil.formatRupiah((double)d.getOrDefault("omzetHariIni",0.0)));
                    valOrders.setText(String.valueOf(d.getOrDefault("transaksiHariIni",0)));
                    valCustomers.setText(String.valueOf(d.getOrDefault("totalBarang",0)));

                    // Sales summary
                    double omzetBulan = (double)d.getOrDefault("omzetBulanIni",0.0);
                    double omzetHari  = (double)d.getOrDefault("omzetHariIni",0.0);
                    JPanel ss = (JPanel) salesCard.getComponent(1);
                    ((JPanel)ss.getComponent(0)).remove(0);
                    ((JPanel)ss.getComponent(0)).add(salesStatCol("Total Sales", FormatUtil.formatRupiah(omzetBulan*12)));
                    ((JPanel)ss.getComponent(1)).remove(0);
                    ((JPanel)ss.getComponent(1)).add(salesStatCol("This Month",  FormatUtil.formatRupiah(omzetBulan)));
                    ((JPanel)ss.getComponent(2)).remove(0);
                    ((JPanel)ss.getComponent(2)).add(salesStatCol("Today",       FormatUtil.formatRupiah(omzetHari)));

                    List<Barang> lr = ctrl.getStokRendah();
                    for (Barang b : lr)
                        stokMdl.addRow(new Object[]{
                            b.getNamaBarang(), b.getKodeBarang(), b.getStok(), "Low Stock"
                        });

                    // Notify if stok rendah
                    if (!lr.isEmpty())
                        AlertUtil.showWarning(null, lr.size()+" barang memiliki stok di bawah minimum!");
                } catch(Exception ex){ ex.printStackTrace(); }
            }
        }.execute();

        return page;
    }

    // ── Factory helpers ───────────────────────────────────────────
    private JPanel tintMetricCard(String label, JLabel valLbl, String sub,
                                   Color tint, Icon ico, int[] spark, Color accent) {
        JPanel card = UITheme.tintCard(tint);
        card.setLayout(new BorderLayout(0,8));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JPanel textCol = new JPanel();
        textCol.setOpaque(false);
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        JLabel lLbl = new JLabel(label.toUpperCase());
        lLbl.setFont(UITheme.FONT_LABEL);
        lLbl.setForeground(new Color(UITheme.TEXT_SECONDARY.getRed(),
            UITheme.TEXT_SECONDARY.getGreen(), UITheme.TEXT_SECONDARY.getBlue(), 180));
        textCol.add(lLbl);
        textCol.add(Box.createVerticalStrut(4));
        textCol.add(valLbl);

        JLabel icoLbl = new JLabel(ico);
        icoLbl.setOpaque(true);
        icoLbl.setBackground(new Color(255,255,255,80));
        icoLbl.setBorder(new EmptyBorder(8,8,8,8));
        icoLbl.setPreferredSize(new Dimension(50,50));
        // rounded icon background
        JPanel icoBg = new JPanel(new FlowLayout(FlowLayout.CENTER,0,0)){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255,255,255,100));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.dispose();
            }
        };
        icoBg.setOpaque(false);
        icoBg.setPreferredSize(new Dimension(50,50));
        icoBg.add(icoLbl);

        topRow.add(textCol, BorderLayout.CENTER);
        topRow.add(icoBg, BorderLayout.EAST);

        JLabel subLbl = new JLabel("↑ " + sub);
        subLbl.setFont(UITheme.FONT_SMALL);
        subLbl.setForeground(UITheme.ACCENT_GREEN);

        card.add(topRow, BorderLayout.CENTER);
        card.add(subLbl, BorderLayout.SOUTH);
        if (spark != null) {
            JPanel sp = UITheme.sparkline(spark, accent);
            sp.setPreferredSize(new Dimension(0,32));
            card.add(sp, BorderLayout.NORTH);
        }
        return card;
    }

    private JLabel metricVal(String v) {
        JLabel l = new JLabel(v);
        l.setFont(new Font("Segoe UI",Font.BOLD,22));
        l.setForeground(UITheme.TEXT_PRIMARY);
        return l;
    }

    private JPanel salesStatCol(String label, String val) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_LABEL); lbl.setForeground(UITheme.TEXT_SECONDARY);
        JLabel vLbl = new JLabel(val);
        vLbl.setFont(new Font("Segoe UI",Font.BOLD,18)); vLbl.setForeground(UITheme.TEXT_PRIMARY);
        p.add(lbl); p.add(vLbl);
        return p;
    }

    private JPanel buildDonutLegend(String[] labels, int[] vals, Color[] colors, String[] pcts) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(16,6,0,0));
        for (int i=0; i<labels.length; i++) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT,6,3));
            row.setOpaque(false);
            JLabel dot = new JLabel(Icons.dot(colors[i]));
            JLabel lbl = new JLabel(pcts[i] + "  " + labels[i]);
            lbl.setFont(UITheme.FONT_SMALL); lbl.setForeground(UITheme.TEXT_SECONDARY);
            row.add(dot); row.add(lbl);
            p.add(row);
        }
        return p;
    }

    private JPanel legendDot(Color c, String label) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT,4,0));
        p.setOpaque(false);
        JLabel dot = new JLabel(Icons.dot(c));
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_SMALL); lbl.setForeground(UITheme.TEXT_SECONDARY);
        p.add(dot); p.add(lbl); return p;
    }

    private JPanel buildSettingsPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UITheme.BG_SURFACE);
        p.setBorder(new EmptyBorder(24,24,24,24));
        JLabel t = UITheme.pageTitle("Settings");
        JLabel s = new JLabel("Konfigurasi aplikasi");
        s.setFont(UITheme.FONT_BODY); s.setForeground(UITheme.TEXT_SECONDARY);
        JPanel hdr = new JPanel(); hdr.setOpaque(false);
        hdr.setLayout(new BoxLayout(hdr, BoxLayout.Y_AXIS));
        hdr.add(t); hdr.add(s);
        p.add(hdr, BorderLayout.NORTH);
        JLabel placeholder = new JLabel("Settings — Coming Soon");
        placeholder.setFont(UITheme.FONT_H2);
        placeholder.setForeground(UITheme.TEXT_MUTED);
        placeholder.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(placeholder, BorderLayout.CENTER);
        return p;
    }

    private void doLogout() {
        if (AlertUtil.showConfirm(this, "Apakah Anda yakin ingin keluar?")) {
            Session.logout();
            dispose();
            new LoginForm().setVisible(true);
        }
    }

    private String getInitials(String n) {
        if (n==null||n.isEmpty()) return "?";
        String[] p=n.trim().split(" ");
        return p.length==1?p[0].substring(0,Math.min(2,p[0].length())).toUpperCase()
                          :(""+p[0].charAt(0)+p[p.length-1].charAt(0)).toUpperCase();
    }
    private Color roleColor(String r) {
        switch(r==null?"":r) {
            case "ADMIN":        return UITheme.ACCENT_CORAL;
            case "SUPERVISOR":   return UITheme.ACCENT_AMBER;
            case "STAFF_GUDANG": return UITheme.ACCENT_TEAL;
            default:             return UITheme.ACCENT_BLUE;
        }
    }
}
