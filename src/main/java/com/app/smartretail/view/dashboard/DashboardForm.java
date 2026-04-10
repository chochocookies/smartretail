package com.app.smartretail.view.dashboard;

import com.app.smartretail.controller.DashboardController;
import com.app.smartretail.model.Barang;
import com.app.smartretail.utils.AlertUtil;
import com.app.smartretail.utils.FormatUtil;
import com.app.smartretail.utils.Session;
import com.app.smartretail.view.auth.LoginForm;
import com.app.smartretail.view.master.*;
import com.app.smartretail.view.transaksi.*;
import com.app.smartretail.view.report.ReportForm;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Map;

/**
 * DashboardForm - Halaman utama setelah login
 */
public class DashboardForm extends JFrame {

    private DashboardController controller;
    private JLabel lblWelcome, lblOmzetHariIni, lblTransaksiHariIni, lblTotalBarang, lblStokRendah;
    private JTable tblStokRendah;
    private DefaultTableModel modelStokRendah;

    // Sidebar menu buttons
    private JButton btnDashboard, btnBarang, btnKategori, btnSupplier, btnCustomer;
    private JButton btnPenjualan, btnPembelian, btnStok, btnLaporan, btnUser, btnLogout;

    private JPanel contentPanel;

    public DashboardForm() {
        this.controller = new DashboardController();
        initComponents();
        loadDashboardData();
        checkStokRendah();
    }

    private void initComponents() {
        setTitle("SRMS - Dashboard | " + Session.currentUser.getNamaLengkap()
                 + " [" + Session.currentUser.getRole() + "]");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ============ SIDEBAR ============
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(210, 720));
        sidebar.setBackground(new Color(30, 55, 95));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        // Logo area
        JPanel logoPanel = new JPanel();
        logoPanel.setBackground(new Color(20, 40, 80));
        logoPanel.setMaximumSize(new Dimension(210, 75));
        logoPanel.setMinimumSize(new Dimension(210, 75));
        logoPanel.setPreferredSize(new Dimension(210, 75));
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblAppName = new JLabel("🛒 SRMS");
        lblAppName.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblAppName.setForeground(Color.WHITE);
        lblAppName.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblAppDesc = new JLabel("Smart Retail System");
        lblAppDesc.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblAppDesc.setForeground(new Color(170, 200, 230));
        lblAppDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        logoPanel.add(lblAppName);
        logoPanel.add(Box.createVerticalStrut(2));
        logoPanel.add(lblAppDesc);
        sidebar.add(logoPanel);

        // Menu group label
        sidebar.add(createMenuLabel("UTAMA"));
        btnDashboard  = createMenuButton("📊  Dashboard");
        btnPenjualan  = createMenuButton("🛒  Penjualan (POS)");
        btnPembelian  = createMenuButton("📦  Pembelian");
        btnStok       = createMenuButton("🏭  Manajemen Stok");
        sidebar.add(btnDashboard);
        sidebar.add(btnPenjualan);
        sidebar.add(btnPembelian);
        sidebar.add(btnStok);

        sidebar.add(createMenuLabel("MASTER DATA"));
        btnBarang    = createMenuButton("🏷️  Data Barang");
        btnKategori  = createMenuButton("📂  Kategori");
        btnSupplier  = createMenuButton("🚚  Supplier");
        btnCustomer  = createMenuButton("👥  Customer");
        sidebar.add(btnBarang);
        sidebar.add(btnKategori);
        sidebar.add(btnSupplier);
        sidebar.add(btnCustomer);

        sidebar.add(createMenuLabel("LAPORAN"));
        btnLaporan = createMenuButton("📄  Laporan & Grafik");
        sidebar.add(btnLaporan);

        sidebar.add(createMenuLabel("PENGATURAN"));
        btnUser = createMenuButton("👤  Kelola User");
        sidebar.add(btnUser);

        sidebar.add(Box.createVerticalGlue());

        // User info bottom
        JPanel userPanel = new JPanel();
        userPanel.setBackground(new Color(20, 40, 80));
        userPanel.setMaximumSize(new Dimension(210, 70));
        userPanel.setMinimumSize(new Dimension(210, 70));
        userPanel.setLayout(new BorderLayout());
        userPanel.setBorder(new EmptyBorder(10, 15, 10, 10));

        JLabel lblUser = new JLabel(Session.currentUser.getNamaLengkap());
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblUser.setForeground(Color.WHITE);

        JLabel lblRole = new JLabel(Session.currentUser.getRole());
        lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblRole.setForeground(new Color(170, 200, 230));

        JPanel userInfo = new JPanel(new GridLayout(2, 1));
        userInfo.setOpaque(false);
        userInfo.add(lblUser);
        userInfo.add(lblRole);

        btnLogout = new JButton("Keluar");
        btnLogout.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btnLogout.setForeground(new Color(255, 120, 120));
        btnLogout.setBackground(new Color(20, 40, 80));
        btnLogout.setBorderPainted(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));

        userPanel.add(userInfo, BorderLayout.CENTER);
        userPanel.add(btnLogout, BorderLayout.EAST);
        sidebar.add(userPanel);

        // ============ CONTENT AREA ============
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(245, 247, 250));
        contentPanel.add(buildDashboardPanel(), BorderLayout.CENTER);

        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        // Role-based menu visibility
        applyRoleAccess();

        // Action listeners
        btnDashboard.addActionListener(e -> showContent(buildDashboardPanel()));
        btnBarang.addActionListener(e -> showContent(new BarangForm()));
        btnKategori.addActionListener(e -> showContent(new KategoriForm()));
        btnSupplier.addActionListener(e -> showContent(new SupplierForm()));
        btnCustomer.addActionListener(e -> showContent(new CustomerForm()));
        btnPenjualan.addActionListener(e -> showContent(new PenjualanForm()));
        btnPembelian.addActionListener(e -> showContent(new PembelianForm()));
        btnStok.addActionListener(e -> showContent(new StokForm()));
        btnLaporan.addActionListener(e -> showContent(new ReportForm()));
        btnUser.addActionListener(e -> showContent(new UserForm()));
        btnLogout.addActionListener(e -> doLogout());
    }

    private void applyRoleAccess() {
        // KASIR: tidak bisa akses master, user management
        if (Session.isKasir()) {
            btnKategori.setVisible(false);
            btnSupplier.setVisible(false);
            btnCustomer.setVisible(false);
            btnPembelian.setVisible(false);
            btnStok.setVisible(false);
            btnUser.setVisible(false);
            btnLaporan.setVisible(false);
        }
        // STAFF GUDANG: fokus ke stok & pembelian
        if (Session.isStaffGudang()) {
            btnPenjualan.setVisible(false);
            btnUser.setVisible(false);
            btnLaporan.setVisible(false);
        }
        // SUPERVISOR: read-only di transaksi, bisa laporan
        if (Session.isSupervisor()) {
            btnUser.setVisible(false);
        }
        // ADMIN: akses penuh
    }

    private void showContent(Component comp) {
        contentPanel.removeAll();
        if (comp instanceof JPanel) {
            contentPanel.add(comp, BorderLayout.CENTER);
        } else {
            JScrollPane sp = new JScrollPane(comp);
            sp.setBorder(BorderFactory.createEmptyBorder());
            contentPanel.add(sp, BorderLayout.CENTER);
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel buildDashboardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(245, 247, 250));
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Page title
        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(30, 55, 95));
        panel.add(title, BorderLayout.NORTH);

        // Summary cards
        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        cardsPanel.setOpaque(false);

        lblOmzetHariIni    = new JLabel("Rp 0");
        lblTransaksiHariIni = new JLabel("0");
        lblTotalBarang     = new JLabel("0");
        lblStokRendah      = new JLabel("0");

        cardsPanel.add(createCard("💰 Omzet Hari Ini",     lblOmzetHariIni,    new Color(52, 152, 219)));
        cardsPanel.add(createCard("🛒 Transaksi Hari Ini", lblTransaksiHariIni, new Color(46, 204, 113)));
        cardsPanel.add(createCard("📦 Total Barang",       lblTotalBarang,      new Color(155, 89, 182)));
        cardsPanel.add(createCard("⚠️ Stok Rendah",        lblStokRendah,       new Color(231, 76, 60)));

        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);
        centerPanel.add(cardsPanel, BorderLayout.NORTH);

        // Stok rendah table
        JLabel lblTblTitle = new JLabel("⚠️ Barang Stok Rendah");
        lblTblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTblTitle.setForeground(new Color(231, 76, 60));

        String[] cols = {"Kode", "Nama Barang", "Kategori", "Stok", "Stok Min", "Satuan"};
        modelStokRendah = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblStokRendah = new JTable(modelStokRendah);
        tblStokRendah.setRowHeight(28);
        tblStokRendah.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblStokRendah.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblStokRendah.getTableHeader().setBackground(new Color(30, 55, 95));
        tblStokRendah.getTableHeader().setForeground(Color.WHITE);
        tblStokRendah.setSelectionBackground(new Color(210, 230, 255));

        JScrollPane scrollStok = new JScrollPane(tblStokRendah);
        scrollStok.setPreferredSize(new Dimension(0, 200));
        scrollStok.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        JPanel tablePanel = new JPanel(new BorderLayout(0, 8));
        tablePanel.setOpaque(false);
        tablePanel.add(lblTblTitle, BorderLayout.NORTH);
        tablePanel.add(scrollStok, BorderLayout.CENTER);

        centerPanel.add(tablePanel, BorderLayout.CENTER);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTitle.setForeground(new Color(220, 235, 255));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(Color.WHITE);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private void loadDashboardData() {
        SwingWorker<Map<String, Object>, Void> worker = new SwingWorker<>() {
            protected Map<String, Object> doInBackground() {
                return controller.getSummary();
            }
            protected void done() {
                try {
                    Map<String, Object> data = get();
                    lblOmzetHariIni.setText(FormatUtil.formatRupiah((double) data.getOrDefault("omzetHariIni", 0.0)));
                    lblTransaksiHariIni.setText(String.valueOf(data.getOrDefault("transaksiHariIni", 0)));
                    lblTotalBarang.setText(String.valueOf(data.getOrDefault("totalBarang", 0)));
                    int sr = (int) data.getOrDefault("stokRendah", 0);
                    lblStokRendah.setText(String.valueOf(sr));

                    // Load stok rendah table
                    List<Barang> stokRendahList = controller.getStokRendah();
                    modelStokRendah.setRowCount(0);
                    for (Barang b : stokRendahList) {
                        modelStokRendah.addRow(new Object[]{
                            b.getKodeBarang(), b.getNamaBarang(), b.getNamaKategori(),
                            b.getStok(), b.getStokMinimum(), b.getSatuan()
                        });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void checkStokRendah() {
        SwingWorker<List<Barang>, Void> worker = new SwingWorker<>() {
            protected List<Barang> doInBackground() {
                return controller.getStokRendah();
            }
            protected void done() {
                try {
                    List<Barang> list = get();
                    if (!list.isEmpty()) {
                        AlertUtil.showWarning(DashboardForm.this,
                            "⚠️ Perhatian!\n" + list.size() + " barang memiliki stok di bawah minimum.\n" +
                            "Segera lakukan pembelian/restok.");
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        };
        worker.execute();
    }

    private void doLogout() {
        if (AlertUtil.showConfirm(this, "Apakah Anda yakin ingin keluar?")) {
            Session.logout();
            dispose();
            new LoginForm().setVisible(true);
        }
    }

    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(new Color(200, 220, 250));
        btn.setBackground(new Color(30, 55, 95));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(10, 20, 10, 10));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(210, 40));
        btn.setMinimumSize(new Dimension(210, 40));
        btn.setPreferredSize(new Dimension(210, 40));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(50, 80, 130)); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(new Color(30, 55, 95)); }
        });
        return btn;
    }

    private JLabel createMenuLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lbl.setForeground(new Color(130, 160, 200));
        lbl.setBorder(new EmptyBorder(12, 15, 4, 0));
        lbl.setMaximumSize(new Dimension(210, 30));
        return lbl;
    }
}
