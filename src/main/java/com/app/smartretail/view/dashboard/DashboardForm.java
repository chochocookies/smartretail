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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.app.smartretail.controller.DashboardController;
import com.app.smartretail.model.Barang;
import com.app.smartretail.utils.FormatUtil;
import com.app.smartretail.utils.Session;
import com.app.smartretail.utils.UITheme;
import com.app.smartretail.view.auth.LoginForm;
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

    // Dashboard live widgets
    private JLabel valOmzet, valTrx, valBarang, valStokRendah;

    public DashboardForm() {
        UITheme.apply();
        initUI();
        navigateTo("dashboard");
    }

    private void initUI() {
        setTitle("SRMS — " + Session.currentUser.getNamaLengkap());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 780);
        setMinimumSize(new Dimension(1100, 660));
        setLocationRelativeTo(null);
        setUndecorated(true);

        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(UITheme.BG_DARK); g.fillRect(0,0,getWidth(),getHeight());
            }
        };
        root.setOpaque(false);

        // Window controls (undecorated)
        JPanel titleBar = buildTitleBar();

        // Sidebar
        sidebar = new SidebarPanel(this::navigateTo);
        sidebar.getLogoutButton().addActionListener(e -> doLogout());

        // Content
        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(UITheme.BG_DARK);

        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setOpaque(false);
        mainArea.add(titleBar, BorderLayout.NORTH);
        mainArea.add(contentArea, BorderLayout.CENTER);

        root.add(sidebar, BorderLayout.WEST);
        root.add(mainArea, BorderLayout.CENTER);
        setContentPane(root);

        // Resize handle
        addResizeHandle(root);
    }

    private JPanel buildTitleBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UITheme.BG_DARK);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0, UITheme.BORDER_DEFAULT),
            new EmptyBorder(8,18,8,14)));
        bar.setPreferredSize(new Dimension(0, 42));

        JLabel clock = new JLabel();
        clock.setFont(UITheme.FONT_SMALL);
        clock.setForeground(UITheme.TEXT_MUTED);

        java.awt.event.ActionListener updateAction = e -> {
            clock.setText(new java.text.SimpleDateFormat("EEEE, dd MMM yyyy  HH:mm:ss", new java.util.Locale("id", "ID"))
                    .format(new java.util.Date()));
        };

        javax.swing.Timer t = new javax.swing.Timer(1000, updateAction);

        t.setInitialDelay(0); 
        t.start();

        updateAction.actionPerformed(null);


        JPanel winBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        winBtns.setOpaque(false);
        winBtns.add(winBtn(new Color(255,190,60), () -> setState(ICONIFIED)));
        winBtns.add(winBtn(new Color(52,211,153), () -> {
            setExtendedState(getExtendedState() == MAXIMIZED_BOTH ? NORMAL : MAXIMIZED_BOTH);
        }));
        winBtns.add(winBtn(new Color(255,88,100), () -> System.exit(0)));

        bar.add(clock, BorderLayout.WEST);
        bar.add(winBtns, BorderLayout.EAST);

        // Drag
        bar.addMouseListener(new MouseAdapter() {
            int ox, oy;
            public void mousePressed(MouseEvent e) { ox=e.getX(); oy=e.getY(); }
        });
        bar.addMouseMotionListener(new MouseMotionAdapter() {
            int ox, oy;
            {
                bar.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) { ox=e.getX(); oy=e.getY(); }
                });
            }
            public void mouseDragged(MouseEvent e) {
                if (getExtendedState() != MAXIMIZED_BOTH)
                    setLocation(getX()+e.getX()-ox, getY()+e.getY()-oy);
            }
        });
        return bar;
    }

    private JButton winBtn(Color c, Runnable action) {
        JButton b = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? c : new Color(c.getRed(),c.getGreen(),c.getBlue(),120));
                g2.fillOval(0,0,12,12);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(12,12));
        b.setOpaque(false); b.setContentAreaFilled(false);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> action.run());
        return b;
    }

    // ─── Navigation ───────────────────────────────────────────────────
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
            case "laporan":
            case "grafik":    view = new ReportForm();    break;
            case "users":     view = new UserForm();      break;
            default:          view = buildDashboard();    break;
        }
        contentArea.removeAll();
        contentArea.add(view, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }

    // ─── Dashboard Panel ──────────────────────────────────────────────
    private JPanel buildDashboard() {
        JPanel page = new JPanel(new BorderLayout(0, 0));
        page.setBackground(UITheme.BG_DARK);
        page.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Header
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.setBorder(new EmptyBorder(0,0,20,0));
        JLabel title = UITheme.pageTitle("Dashboard");
        JLabel sub = new JLabel("Ringkasan operasional toko hari ini");
        sub.setFont(UITheme.FONT_BODY); sub.setForeground(UITheme.TEXT_SECONDARY);
        JPanel hdrText = new JPanel(); hdrText.setOpaque(false);
        hdrText.setLayout(new BoxLayout(hdrText, BoxLayout.Y_AXIS));
        hdrText.add(title); hdrText.add(sub);
        hdr.add(hdrText, BorderLayout.WEST);
        JButton btnRefresh = UITheme.ghostButton("Refresh", UITheme.ACCENT_BLUE);
        btnRefresh.addActionListener(e -> navigateTo("dashboard"));
        hdr.add(btnRefresh, BorderLayout.EAST);

        // Metric cards row
        JPanel cards = new JPanel(new GridLayout(1, 4, 16, 0));
        cards.setOpaque(false);
        cards.setBorder(new EmptyBorder(0,0,20,0));

        valOmzet      = metricVal("Rp 0");
        valTrx        = metricVal("0");
        valBarang     = metricVal("0");
        valStokRendah = metricVal("0");

        int[] sparkOmzet  = {40,65,45,75,55,80,70,90,60,85};
        int[] sparkTrx    = {5,12,8,15,10,18,14,20,11,17};
        int[] sparkBarang = {120,122,121,125,124,128,127,130,129,132};

        cards.add(metricCard("Total Omzet Hari Ini", valOmzet, "Penjualan bersih", UITheme.ACCENT_BLUE, sparkOmzet));
        cards.add(metricCard("Transaksi Hari Ini", valTrx, "Jumlah transaksi", UITheme.ACCENT_TEAL, sparkTrx));
        cards.add(metricCard("Total Barang", valBarang, "SKU terdaftar", UITheme.ACCENT_PURPLE, sparkBarang));
        cards.add(metricCard("Stok Rendah", valStokRendah, "Perlu restock", UITheme.ACCENT_CORAL, null));

        // Middle row: chart + stok rendah
        JPanel middle = new JPanel(new GridLayout(1, 2, 16, 0));
        middle.setOpaque(false);
        middle.setBorder(new EmptyBorder(0,0,16,0));

        // Sales chart card
        JPanel chartCard = UITheme.card();
        chartCard.setLayout(new BorderLayout(0, 12));
        JLabel chartTitle = new JLabel("Omzet 7 Hari Terakhir");
        chartTitle.setFont(UITheme.FONT_H2); chartTitle.setForeground(UITheme.TEXT_PRIMARY);
        String[] days = {"Sen","Sel","Rab","Kam","Jum","Sab","Min"};
        int[] omzet = {420,380,510,460,620,750,590};
        JPanel bar = UITheme.barChart(days, omzet, UITheme.ACCENT_BLUE);
        bar.setPreferredSize(new Dimension(0, 200));
        chartCard.add(chartTitle, BorderLayout.NORTH);
        chartCard.add(bar, BorderLayout.CENTER);

        // Donut + legend card
        JPanel donutCard = UITheme.card();
        donutCard.setLayout(new BorderLayout(0, 12));
        JLabel donutTitle = new JLabel("Distribusi Penjualan");
        donutTitle.setFont(UITheme.FONT_H2); donutTitle.setForeground(UITheme.TEXT_PRIMARY);
        JPanel donutArea = new JPanel(new BorderLayout(10, 0));
        donutArea.setOpaque(false);
        int[] dvals = {38,26,20,16};
        Color[] dcols = {UITheme.ACCENT_BLUE, UITheme.ACCENT_TEAL, UITheme.ACCENT_AMBER, UITheme.ACCENT_PURPLE};
        JPanel donut = UITheme.donutChart(dvals, dcols, "100%");
        donut.setPreferredSize(new Dimension(120, 120));
        JPanel legend = buildDonutLegend(
            new String[]{"Makanan","Kebersihan","ATK","Lainnya"},
            dvals, dcols);
        donutArea.add(donut, BorderLayout.WEST);
        donutArea.add(legend, BorderLayout.CENTER);
        donutCard.add(donutTitle, BorderLayout.NORTH);
        donutCard.add(donutArea, BorderLayout.CENTER);

        middle.add(chartCard);
        middle.add(donutCard);

        // Stok rendah table card
        JPanel tableCard = UITheme.card();
        tableCard.setLayout(new BorderLayout(0, 10));
        JPanel tableHdr = new JPanel(new BorderLayout());
        tableHdr.setOpaque(false);
        JLabel tblTitle = new JLabel("Peringatan Stok Rendah");
        tblTitle.setFont(UITheme.FONT_H2); tblTitle.setForeground(UITheme.TEXT_PRIMARY);
        JLabel badge = UITheme.badge("Perlu Tindakan", UITheme.ACCENT_CORAL, UITheme.ACCENT_CORAL);
        tableHdr.add(tblTitle, BorderLayout.WEST);
        tableHdr.add(badge, BorderLayout.EAST);

        String[] cols = {"Kode Barang", "Nama Barang", "Kategori", "Stok", "Min", "Status"};
        DefaultTableModel mdl = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tbl = new JTable(mdl);
        UITheme.styleTable(tbl);
        // Color stok rendah rows
        tbl.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                comp.setBackground(sel ? new Color(82,130,255,60) :
                    (r % 2 == 0 ? UITheme.BG_CARD : UITheme.BG_ROW_ALT));
                comp.setForeground(UITheme.TEXT_PRIMARY);
                ((JLabel)comp).setBorder(new EmptyBorder(0, 10, 0, 10));
                return comp;
            }
        });
        tbl.getColumnModel().getColumn(0).setMaxWidth(100);
        tbl.getColumnModel().getColumn(3).setMaxWidth(60);
        tbl.getColumnModel().getColumn(4).setMaxWidth(60);
        tbl.getColumnModel().getColumn(5).setMaxWidth(100);

        tableCard.add(tableHdr, BorderLayout.NORTH);
        tableCard.add(UITheme.styledScroll(tbl), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridLayout(1,1));
        bottom.setOpaque(false);
        bottom.add(tableCard);

        page.add(hdr, BorderLayout.NORTH);
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(cards);
        center.add(middle);
        center.add(bottom);
        page.add(center, BorderLayout.CENTER);

        // Load data async
        new SwingWorker<Map<String,Object>, Void>() {
            protected Map<String,Object> doInBackground() { return ctrl.getSummary(); }
            protected void done() {
                try {
                    Map<String,Object> d = get();
                    valOmzet.setText(FormatUtil.formatRupiah((double)d.getOrDefault("omzetHariIni",0.0)));
                    valTrx.setText(String.valueOf(d.getOrDefault("transaksiHariIni", 0)));
                    valBarang.setText(String.valueOf(d.getOrDefault("totalBarang", 0)));
                    int sr = (int)d.getOrDefault("stokRendah", 0);
                    valStokRendah.setText(String.valueOf(sr));
                    if (sr > 0) valStokRendah.setForeground(UITheme.ACCENT_CORAL);

                    List<Barang> list = ctrl.getStokRendah();
                    for (Barang b : list) mdl.addRow(new Object[]{
                        b.getKodeBarang(), b.getNamaBarang(), b.getNamaKategori(),
                        b.getStok(), b.getStokMinimum(), "⚠ Rendah"
                    });
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();

        return page;
    }

    private JPanel metricCard(String label, JLabel valLbl, String sub, Color accent, int[] spark) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_CARD);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),16,16);
                g2.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),30));
                g2.fillRoundRect(0,getHeight()-5,getWidth(),5,0,0);
                g2.setColor(accent);
                g2.fillRoundRect(0,getHeight()-4,getWidth(),4,0,0);
                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout(0, 8));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(18,20,16,20));

        JLabel lLbl = new JLabel(label.toUpperCase());
        lLbl.setFont(UITheme.FONT_LABEL); lLbl.setForeground(UITheme.TEXT_MUTED);

        JLabel subLbl = new JLabel(sub);
        subLbl.setFont(UITheme.FONT_SMALL); subLbl.setForeground(accent);

        JPanel top = new JPanel(new BorderLayout(0,4));
        top.setOpaque(false);
        top.add(lLbl, BorderLayout.NORTH);
        top.add(valLbl, BorderLayout.CENTER);
        top.add(subLbl, BorderLayout.SOUTH);

        card.add(top, BorderLayout.CENTER);

        if (spark != null) {
            JPanel sp = UITheme.sparkline(spark, accent);
            sp.setPreferredSize(new Dimension(0, 36));
            card.add(sp, BorderLayout.SOUTH);
        }
        return card;
    }

    private JLabel metricVal(String v) {
        JLabel l = new JLabel(v);
        l.setFont(new Font("Segoe UI", Font.BOLD, 22));
        l.setForeground(UITheme.TEXT_PRIMARY);
        return l;
    }

    private JPanel buildDonutLegend(String[] labels, int[] vals, Color[] colors) {
    JPanel p = new JPanel();
    p.setOpaque(false);
    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    p.setBorder(new javax.swing.border.EmptyBorder(10, 0, 0, 0));
    
    int total = 0; 
    for (int v : vals) total += v;

    for (int i = 0; i < labels.length; i++) {
        final int index = i;
        final Color currentColor = colors[index];

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        row.setOpaque(false);

        JLabel dot = new JLabel() {
            Color c = currentColor; 
            
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c); 
                g2.fillOval(0, 3, 8, 8); 
                g2.dispose();
            }
            { 
                setPreferredSize(new Dimension(8, 14)); 
                setOpaque(false); 
            }
        };

        JLabel lbl = new JLabel(labels[index]);
        lbl.setFont(UITheme.FONT_SMALL); 
        lbl.setForeground(UITheme.TEXT_SECONDARY);

        int pct = total > 0 ? vals[index] * 100 / total : 0;
        JLabel pctLbl = new JLabel(pct + "%");
        pctLbl.setFont(UITheme.FONT_SMALL); 
        pctLbl.setForeground(colors[index]);

        row.add(dot); 
        row.add(lbl); 
        row.add(pctLbl);
        p.add(row);
    }
    return p;
}


    private void doLogout() {
        int r = JOptionPane.showConfirmDialog(this,
            "Apakah Anda yakin ingin keluar?", "Konfirmasi Logout",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            Session.logout();
            dispose();
            new LoginForm().setVisible(true);
        }
    }

    private void addResizeHandle(JPanel root) {
        // Simple SE resize
        root.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                boolean near = e.getX() > getWidth()-12 && e.getY() > getHeight()-12;
                setCursor(near ? Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR)
                               : Cursor.getDefaultCursor());
            }
        });
        root.addMouseListener(new MouseAdapter() {
            int ox, oy, ow, oh;
            @Override public void mousePressed(MouseEvent e) { ox=e.getXOnScreen(); oy=e.getYOnScreen(); ow=getWidth(); oh=getHeight(); }
        });
        root.addMouseMotionListener(new MouseMotionAdapter() {
            int ox, oy, ow, oh;
            { root.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) { ox=e.getXOnScreen(); oy=e.getYOnScreen(); ow=getWidth(); oh=getHeight(); }
            });}
            @Override public void mouseDragged(MouseEvent e) {
                if (e.getX() > getWidth()-24 && e.getY() > getHeight()-24)
                    setSize(Math.max(1100, ow+(e.getXOnScreen()-ox)), Math.max(660, oh+(e.getYOnScreen()-oy)));
            }
        });
    }
}
