package com.app.smartretail.view.transaksi;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.app.smartretail.controller.BarangController;
import com.app.smartretail.controller.TransaksiController;
import com.app.smartretail.dao.SupplierDAO;
import com.app.smartretail.model.Barang;
import com.app.smartretail.model.Supplier;
import com.app.smartretail.model.Transaksi;
import com.app.smartretail.model.TransaksiDetail;
import com.app.smartretail.utils.AlertUtil;
import com.app.smartretail.utils.FormatUtil;
import com.app.smartretail.utils.UITheme;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.engine.design.JRDesignLine;
import net.sf.jasperreports.engine.design.JRDesignRectangle;
import net.sf.jasperreports.engine.design.JRDesignSection;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;
import net.sf.jasperreports.engine.type.ModeEnum;
import net.sf.jasperreports.engine.type.SplitTypeEnum;
import net.sf.jasperreports.engine.type.VerticalTextAlignEnum;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfReportConfiguration;

/**
 * PurchaseForm — Combined Purchase + Supplier management.
 * Tabs: New Purchase | Purchase History | Suppliers
 *
 * FIX #3A: Saat supplier dipilih, tabel barang supplier otomatis muncul
 * FIX #3B: Tombol simpan pakai teks biasa (tanpa Unicode ✓ yang bisa blank)
 * FIX #3C: supplier_id diset ke currentTrx sebelum simpan
 */
public class PurchaseForm extends JPanel {

    private final TransaksiController trxCtrl  = new TransaksiController();
    private final BarangController    barangCtrl= new BarangController();
    private final SupplierDAO         supDAO    = new SupplierDAO();

    // New Purchase state
    private JTextField txtKode, txtBayar;
    private JLabel lblNo, lblTotal, lblGrand;
    private JTable cartTable;     private DefaultTableModel cartMdl;
    private JTable supplierTable; private DefaultTableModel supplierMdl;  // FIX #3A
    private JComboBox<String> cmbSupplier;
    private Transaksi currentTrx;

    // FIX #3A: simpan daftar supplier ID sejajar dengan combo item
    private final List<Integer> supplierIdList = new ArrayList<>();

    // History & supplier tab
    private JTable histTable;   private DefaultTableModel histMdl;
    private JTable supTable;    private DefaultTableModel supMdl;
    private JTextField sKode,sNama,sAlamat,sTlp,sEmail,sCP;
    private int selSupId = -1;

    private JPanel tabContent;

    public PurchaseForm() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_SURFACE);
        setBorder(new EmptyBorder(22, 24, 22, 24));
        build();
    }

    private void build() {
        // Header
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false); hdr.setBorder(new EmptyBorder(0,0,16,0));
        JPanel ht = new JPanel(); ht.setOpaque(false);
        ht.setLayout(new BoxLayout(ht, BoxLayout.Y_AXIS));
        ht.add(UITheme.pageTitle("Purchase"));
        JLabel sub = new JLabel("Pembelian barang & manajemen supplier");
        sub.setFont(UITheme.FONT_BODY); sub.setForeground(UITheme.TEXT_SECONDARY); ht.add(sub);
        hdr.add(ht, BorderLayout.WEST);
        add(hdr, BorderLayout.NORTH);

        // Tab strip
        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabs.setBackground(UITheme.BG_CARD);
        tabs.setBorder(BorderFactory.createCompoundBorder(
            new UITheme.RoundedBorder(10, UITheme.BORDER_DEFAULT, null),
            new EmptyBorder(0,4,0,4)));

        tabContent = new JPanel(new CardLayout());
        tabContent.setOpaque(false);

        JPanel pNewPurchase = buildNewPurchase();
        JPanel pHistory     = buildHistory();
        JPanel pSuppliers   = buildSuppliers();

        tabContent.add(pNewPurchase, "new");
        tabContent.add(pHistory,     "history");
        tabContent.add(pSuppliers,   "suppliers");

        String[] tabNames = {"New Purchase", "Purchase History", "Suppliers"};
        for (int i = 0; i < tabNames.length; i++) {
            final String card = i==0?"new":i==1?"history":"suppliers";
            final boolean first = i==0;
            JButton tb = new JButton(tabNames[i]) {
                boolean active = first;
                {
                    addActionListener(e -> {
                        ((CardLayout)tabContent.getLayout()).show(tabContent, card);
                        for (Component c : tabs.getComponents()) if(c instanceof JButton) ((JButton)c).putClientProperty("active",false);
                        putClientProperty("active", true);
                        tabs.repaint();
                        if("history".equals(card)) loadHistory();
                        if("suppliers".equals(card)) loadSuppliers();
                    });
                }
                @Override protected void paintComponent(Graphics g) {
                    boolean a = Boolean.TRUE.equals(getClientProperty("active")) || active;
                    Graphics2D g2=(Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if(a){ g2.setColor(UITheme.ACCENT_LIME); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8); setForeground(UITheme.TEXT_ON_LIME); setFont(new Font("Segoe UI",Font.BOLD,12)); }
                    else { setForeground(UITheme.TEXT_SECONDARY); setFont(UITheme.FONT_BODY); }
                    g2.dispose(); super.paintComponent(g);
                }
            };
            tb.putClientProperty("active", first);
            tb.setOpaque(false); tb.setContentAreaFilled(false);
            tb.setBorderPainted(false); tb.setFocusPainted(false);
            tb.setBorder(new EmptyBorder(6,14,6,14));
            tb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            tabs.add(tb);
        }

        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setOpaque(false);
        wrapper.add(tabs, BorderLayout.NORTH);
        wrapper.add(tabContent, BorderLayout.CENTER);
        add(wrapper, BorderLayout.CENTER);

        resetPurchase();
    }

    // ── New Purchase tab ──────────────────────────────────────────
    private JPanel buildNewPurchase() {
        JPanel p = new JPanel(new BorderLayout(16, 0));
        p.setOpaque(false);

        // Left: supplier + tabel barang supplier + cart
        JPanel left = new JPanel(new BorderLayout(0, 10));
        left.setOpaque(false);

        // ── Baris atas: pilih supplier ──
        JPanel supPan = new JPanel(new BorderLayout(0, 5));
        supPan.setOpaque(false);
        supPan.add(UITheme.fieldLabel("Pilih Supplier — item akan muncul otomatis"), BorderLayout.NORTH);

        cmbSupplier = UITheme.styledCombo(new String[]{"— Pilih Supplier —"});
        supplierIdList.clear();
        for (Supplier s : supDAO.getAll()) {
            cmbSupplier.addItem(s.getNamaSupplier());
            supplierIdList.add(s.getId());
        }
        supPan.add(cmbSupplier, BorderLayout.CENTER);

        // ── Tabel barang supplier (FIX #3A) ──
        String[] supCols = {"Kode", "Nama Barang", "Harga Beli", "Stok"};
        supplierMdl = new DefaultTableModel(supCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        supplierTable = new JTable(supplierMdl);
        UITheme.styleTable(supplierTable);
        supplierTable.setRowHeight(30);
        supplierTable.getColumnModel().getColumn(0).setMaxWidth(90);
        supplierTable.getColumnModel().getColumn(2).setMaxWidth(100);
        supplierTable.getColumnModel().getColumn(3).setMaxWidth(60);
        supplierTable.setDefaultRenderer(Object.class, tblRenderer(-1, null));
        supplierTable.setToolTipText("Double-klik untuk menambahkan ke cart");

        JScrollPane supplierScroll = UITheme.styledScroll(supplierTable);
        supplierScroll.setPreferredSize(new Dimension(0, 180));

        JLabel lblSupHint = new JLabel("Double-klik baris atau pilih + klik Tambah ke Cart");
        lblSupHint.setFont(UITheme.FONT_SMALL);
        lblSupHint.setForeground(UITheme.TEXT_MUTED);

        JPanel supItemPanel = new JPanel(new BorderLayout(0, 4));
        supItemPanel.setOpaque(false);
        supItemPanel.add(UITheme.fieldLabel("Barang dari Supplier:"), BorderLayout.NORTH);
        supItemPanel.add(supplierScroll, BorderLayout.CENTER);
        supItemPanel.add(lblSupHint, BorderLayout.SOUTH);

        // ── Atau input kode manual (fallback) ──
        JPanel kodePan = new JPanel(new BorderLayout(0, 5));
        kodePan.setOpaque(false);
        kodePan.add(UITheme.fieldLabel("Atau: Input Kode Manual"), BorderLayout.NORTH);
        JPanel kodeRow = new JPanel(new BorderLayout(8, 0)); kodeRow.setOpaque(false);
        txtKode = UITheme.styledField("Ketik kode barang → Enter");
        txtKode.setPreferredSize(new Dimension(0, 36));
        JButton btnAdd = UITheme.primaryButton("+ Tambah", UITheme.ACCENT_LIME);
        kodeRow.add(txtKode, BorderLayout.CENTER); kodeRow.add(btnAdd, BorderLayout.EAST);
        kodePan.add(kodeRow, BorderLayout.CENTER);

        // ── Cart table ──
        String[] cols = {"#","Kode","Nama Barang","Harga Beli","Qty","Subtotal"};
        cartMdl = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c){ return c==4; } };
        cartTable = new JTable(cartMdl); UITheme.styleTable(cartTable);
        cartTable.setRowHeight(38);
        cartTable.getColumnModel().getColumn(0).setMaxWidth(36);
        cartTable.getColumnModel().getColumn(1).setMaxWidth(90);
        cartTable.getColumnModel().getColumn(4).setMaxWidth(60);
        cartTable.setDefaultRenderer(Object.class, tblRenderer(5, UITheme.ACCENT_PURPLE));

        JButton btnDel = UITheme.dangerButton("Hapus Item");
        JPanel cartTools = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        cartTools.setOpaque(false); cartTools.add(btnDel);

        JPanel cartPanel = new JPanel(new BorderLayout(0, 6));
        cartPanel.setOpaque(false);
        cartPanel.add(UITheme.fieldLabel("Cart Pembelian:"), BorderLayout.NORTH);
        cartPanel.add(UITheme.styledScroll(cartTable), BorderLayout.CENTER);
        cartPanel.add(cartTools, BorderLayout.SOUTH);

        // Susun layout kiri dengan semua komponen
        left.add(supPan,        BorderLayout.NORTH);

        JPanel leftMid = new JPanel(new BorderLayout(0, 10));
        leftMid.setOpaque(false);
        leftMid.add(supItemPanel, BorderLayout.NORTH);
        leftMid.add(kodePan,      BorderLayout.CENTER);

        JSplitPane splitLeft = new JSplitPane(JSplitPane.VERTICAL_SPLIT, leftMid, cartPanel);
        splitLeft.setOpaque(false);
        splitLeft.setBorder(null);
        splitLeft.setDividerSize(6);
        splitLeft.setResizeWeight(0.45);

        left.add(splitLeft, BorderLayout.CENTER);

        // Right: summary
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setPreferredSize(new Dimension(260, 0));

        JPanel sumCard = UITheme.card();
        sumCard.setLayout(new BoxLayout(sumCard, BoxLayout.Y_AXIS));
        sumCard.setAlignmentX(LEFT_ALIGNMENT);

        lblNo = new JLabel("Purchase Order: —");
        lblNo.setFont(new Font("Segoe UI",Font.BOLD,12)); lblNo.setForeground(UITheme.TEXT_PRIMARY); lblNo.setAlignmentX(LEFT_ALIGNMENT);
        JLabel lSum = new JLabel("Order Summary"); lSum.setFont(UITheme.FONT_H2); lSum.setForeground(UITheme.TEXT_PRIMARY); lSum.setAlignmentX(LEFT_ALIGNMENT);
        lblTotal = new JLabel("Rp 0"); lblTotal.setFont(UITheme.FONT_BODY); lblTotal.setForeground(UITheme.TEXT_SECONDARY); lblTotal.setAlignmentX(LEFT_ALIGNMENT);
        lblGrand = new JLabel("Rp 0"); lblGrand.setFont(new Font("Segoe UI",Font.BOLD,20)); lblGrand.setForeground(UITheme.TEXT_PRIMARY); lblGrand.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lByr = UITheme.fieldLabel("Jumlah Dibayar"); lByr.setAlignmentX(LEFT_ALIGNMENT);
        txtBayar = UITheme.styledField("0");
        txtBayar.setFont(new Font("Segoe UI",Font.BOLD,14));
        txtBayar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40)); txtBayar.setAlignmentX(LEFT_ALIGNMENT);

        // FIX #3B: Tombol simpan tanpa icon Unicode ✓ agar selalu tampil
        JButton btnProses = UITheme.primaryButton("Simpan Pembelian", UITheme.ACCENT_LIME);
        btnProses.setFont(new Font("Segoe UI",Font.BOLD,13));
        btnProses.setMaximumSize(new Dimension(Integer.MAX_VALUE,44)); btnProses.setAlignmentX(LEFT_ALIGNMENT);

        JButton btnReset = UITheme.ghostButton("Reset", UITheme.TEXT_SECONDARY);
        btnReset.setMaximumSize(new Dimension(Integer.MAX_VALUE,34)); btnReset.setAlignmentX(LEFT_ALIGNMENT);

        sumCard.add(lblNo); sumCard.add(Box.createVerticalStrut(6));
        sumCard.add(UITheme.separator()); sumCard.add(Box.createVerticalStrut(8));
        sumCard.add(lSum); sumCard.add(Box.createVerticalStrut(6));
        sumRow(sumCard,"Subtotal",lblTotal); sumRow(sumCard,"Grand Total",lblGrand);
        sumCard.add(Box.createVerticalStrut(10));
        sumCard.add(lByr); sumCard.add(Box.createVerticalStrut(5));
        sumCard.add(txtBayar); sumCard.add(Box.createVerticalStrut(14));
        sumCard.add(btnProses); sumCard.add(Box.createVerticalStrut(6)); sumCard.add(btnReset);

        right.add(sumCard);

        p.add(left, BorderLayout.CENTER);
        p.add(right, BorderLayout.EAST);

        // ── Events ──
        btnAdd.addActionListener(e -> addItemByKode());
        txtKode.addActionListener(e -> addItemByKode());
        btnDel.addActionListener(e -> delItem());
        btnProses.addActionListener(e -> proses());
        btnReset.addActionListener(e -> resetPurchase());

        // FIX #3A: Ketika supplier dipilih → load barang supplier
        cmbSupplier.addActionListener(e -> {
            int idx = cmbSupplier.getSelectedIndex();
            if (idx > 0 && idx - 1 < supplierIdList.size()) {
                int supId = supplierIdList.get(idx - 1);
                loadBarangBySupplier(supId);
            } else {
                supplierMdl.setRowCount(0);
            }
        });

        // Double-klik baris barang supplier → tambah ke cart
        supplierTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    addItemFromSupplierTable();
                }
            }
        });

        return p;
    }

    /**
     * FIX #3A: Load barang berdasarkan supplier_id ke tabel supplierTable.
     * Gunakan BarangController.searchBarang atau getAllBarang lalu filter.
     */
    private void loadBarangBySupplier(int supplierId) {
        supplierMdl.setRowCount(0);
        List<Barang> all = barangCtrl.getAllBarang();
        for (Barang b : all) {
            if (b.getSupplierId() == supplierId) {
                supplierMdl.addRow(new Object[]{
                    b.getKodeBarang(),
                    b.getNamaBarang(),
                    FormatUtil.formatRupiah(b.getHargaBeli()),
                    b.getStok()
                });
            }
        }
        if (supplierMdl.getRowCount() == 0) {
            // Jika tidak ada barang dengan supplier_id, tampilkan semua barang
            // sebagai fallback (tergantung apakah model Barang punya getSupplierId)
            AlertUtil.showWarning(this,
                "Tidak ada barang yang terdaftar untuk supplier ini.\n" +
                "Gunakan input kode manual untuk menambah item.");
        }
    }

    /**
     * FIX #3A: Tambah barang dari tabel supplier ke cart.
     */
    private void addItemFromSupplierTable() {
        int row = supplierTable.getSelectedRow();
        if (row < 0) return;
        String kode = supplierMdl.getValueAt(row, 0).toString();
        Barang b = barangCtrl.getByKode(kode);
        if (b == null) {
            AlertUtil.showWarning(this, "Barang tidak ditemukan: " + kode);
            return;
        }
        addItemToCart(b);
    }

    private void addItemByKode() {
        String kode = txtKode.getText().trim();
        if (kode.isEmpty()) return;
        Barang b = barangCtrl.getByKode(kode);
        if (b == null) {
            AlertUtil.showWarning(this, "Barang tidak ditemukan: " + kode);
            txtKode.setText(""); return;
        }
        addItemToCart(b);
        txtKode.setText("");
    }

    private void addItemToCart(Barang b) {
        // Cek sudah ada di cart?
        for (int i = 0; i < cartMdl.getRowCount(); i++) {
            if (cartMdl.getValueAt(i, 1).equals(b.getKodeBarang())) {
                int q = Integer.parseInt(cartMdl.getValueAt(i, 4).toString()) + 1;
                cartMdl.setValueAt(q, i, 4);
                cartMdl.setValueAt(FormatUtil.formatRupiah(q * b.getHargaBeli()), i, 5);
                hitungTotal(); return;
            }
        }
        cartMdl.addRow(new Object[]{
            cartMdl.getRowCount() + 1,
            b.getKodeBarang(), b.getNamaBarang(),
            FormatUtil.formatRupiah(b.getHargaBeli()), 1,
            FormatUtil.formatRupiah(b.getHargaBeli())
        });
        hitungTotal();
    }

    private void delItem(){
        int r=cartTable.getSelectedRow();
        if(r<0){AlertUtil.showWarning(this,"Pilih item!");return;}
        cartMdl.removeRow(r);
        for(int i=0;i<cartMdl.getRowCount();i++) cartMdl.setValueAt(i+1,i,0);
        hitungTotal();
    }

    private void hitungTotal(){
        double t=0;
        for(int i=0;i<cartMdl.getRowCount();i++){
            String v=cartMdl.getValueAt(i,5).toString().replaceAll("[^\\d]","");
            t+=v.isEmpty()?0:Double.parseDouble(v);
        }
        lblTotal.setText(FormatUtil.formatRupiah(t));
        lblGrand.setText(FormatUtil.formatRupiah(t));
    }

    private void proses(){
        if(cartMdl.getRowCount()==0){AlertUtil.showWarning(this,"Keranjang kosong!");return;}

        // FIX #3C: Set supplier_id ke Transaksi sebelum simpan
        int supIdx = cmbSupplier.getSelectedIndex();
        if (supIdx > 0 && supIdx - 1 < supplierIdList.size()) {
            currentTrx.setSupplierId(supplierIdList.get(supIdx - 1));
        }

        for(int i=0;i<cartMdl.getRowCount();i++){
            Barang b=barangCtrl.getByKode(cartMdl.getValueAt(i,1).toString());
            if(b==null) continue;
            int q=FormatUtil.parseInt(cartMdl.getValueAt(i,4).toString());
            TransaksiDetail d=new TransaksiDetail(b.getId(),b.getKodeBarang(),b.getNamaBarang(),q,b.getHargaBeli());
            d.hitungSubtotal(); currentTrx.addDetail(d);
        }
        currentTrx.setBayar(FormatUtil.parseDouble(txtBayar.getText()));
        if(new TransaksiController().simpanPembelian(currentTrx)){
            AlertUtil.showInfo(this,"Pembelian berhasil!\nNo: "+currentTrx.getNoTransaksi());
            resetPurchase();
        } else AlertUtil.showError(this,"Gagal menyimpan.");
    }

    private void resetPurchase(){
        currentTrx=new Transaksi();
        cartMdl.setRowCount(0);
        if (supplierMdl != null) supplierMdl.setRowCount(0);
        lblNo.setText("PO: "+new TransaksiController().generateNoTransaksi("PBL"));
        lblTotal.setText("Rp 0"); lblGrand.setText("Rp 0");
        txtKode.setText(""); txtBayar.setText("0");
        cmbSupplier.setSelectedIndex(0);
    }

    // ── Purchase History tab ──────────────────────────────────────
    private JPanel buildHistory() {
        JPanel p = new JPanel(new BorderLayout(0, 10)); p.setOpaque(false);

        // Toolbar
        JPanel tbr = new JPanel(new BorderLayout(8, 0)); tbr.setOpaque(false);

        JTextField sField = UITheme.styledField("Cari nomor PO, supplier…");
        sField.setPreferredSize(new Dimension(240, 34));

        JPanel rightBtns = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 6, 0));
        rightBtns.setOpaque(false);
        JButton btnRefreshHist = UITheme.ghostButton("Refresh",    UITheme.ACCENT_BLUE);
        JButton btnExportCSV   = UITheme.ghostButton("Export CSV", UITheme.ACCENT_TEAL);
        JButton btnExportPDF   = UITheme.primaryButton("Export PDF", UITheme.ACCENT_AMBER);
        rightBtns.add(btnRefreshHist);
        rightBtns.add(btnExportCSV);
        rightBtns.add(btnExportPDF);

        tbr.add(sField,     BorderLayout.WEST);
        tbr.add(rightBtns,  BorderLayout.EAST);

        // Table
        String[] cols = {"No PO","Tanggal","Supplier","Jumlah Item","Total","Status"};
        histMdl = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        histTable = new JTable(histMdl);
        UITheme.styleTable(histTable);
        histTable.setRowHeight(34);
        histTable.setDefaultRenderer(Object.class, tblRenderer(4, UITheme.ACCENT_BLUE));

        // Sort
        TableRowSorter<DefaultTableModel> histSorter = new TableRowSorter<>(histMdl);
        histTable.setRowSorter(histSorter);

        // Real-time search
        sField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { doFilter(); }
            public void removeUpdate(DocumentEvent e) { doFilter(); }
            public void changedUpdate(DocumentEvent e) { doFilter(); }
            void doFilter() {
                String kw = sField.getText().trim();
                histSorter.setRowFilter(kw.isEmpty() ? null
                    : RowFilter.regexFilter("(?i)" + kw, 0, 1, 2));
            }
        });

        // ── Events ──
        btnRefreshHist.addActionListener(e -> loadHistory());
        btnExportCSV.addActionListener(e -> exportHistoryCSV());
        btnExportPDF.addActionListener(e -> exportHistoryPDF());   // ← FIX: sekarang terhubung

        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));

        JPanel hdrRow = new JPanel(new java.awt.BorderLayout()); hdrRow.setOpaque(false);
        JLabel ttl = new JLabel("Riwayat Pembelian");
        ttl.setFont(UITheme.FONT_H2); ttl.setForeground(UITheme.TEXT_PRIMARY);
        JLabel hint = new JLabel("Klik header kolom untuk mengurutkan  •  Ketik di kolom cari untuk filter");
        hint.setFont(UITheme.FONT_SMALL); hint.setForeground(UITheme.TEXT_MUTED);
        hdrRow.add(ttl, java.awt.BorderLayout.WEST); hdrRow.add(hint, java.awt.BorderLayout.EAST);

        card.add(tbr,                              java.awt.BorderLayout.NORTH);
        card.add(hdrRow,                           java.awt.BorderLayout.CENTER);
        card.add(UITheme.styledScroll(histTable),  java.awt.BorderLayout.SOUTH);

        // Override layout agar tabel bisa grow
        card.setLayout(new java.awt.BorderLayout(0, 8));
        card.add(tbr,   java.awt.BorderLayout.NORTH);
        card.add(UITheme.styledScroll(histTable), java.awt.BorderLayout.CENTER);

        p.add(card, java.awt.BorderLayout.CENTER);
        return p;
    }

    // ── Suppliers tab ─────────────────────────────────────────────
    private JPanel buildSuppliers() {
        JPanel p = new JPanel(new BorderLayout(16, 0)); p.setOpaque(false);
        String[] cols = {"Kode","Nama Supplier","Telepon","Email","Contact Person"};
        supMdl = new DefaultTableModel(cols,0){ public boolean isCellEditable(int r,int c){return false;} };
        supTable = new JTable(supMdl); UITheme.styleTable(supTable);
        supTable.setDefaultRenderer(Object.class, tblRenderer(-1,null));

        JPanel fc = UITheme.card();
        fc.setLayout(new BoxLayout(fc, BoxLayout.Y_AXIS));
        fc.setPreferredSize(new Dimension(270,0));
        JLabel lf = new JLabel("Supplier Form"); lf.setFont(UITheme.FONT_H2); lf.setForeground(UITheme.TEXT_PRIMARY); lf.setAlignmentX(LEFT_ALIGNMENT);
        sKode=fld("Kode Supplier",fc); sNama=fld("Nama Supplier *",fc); sTlp=fld("Telepon",fc); sEmail=fld("Email",fc); sAlamat=fld("Alamat",fc); sCP=fld("Contact Person",fc);
        JPanel br=new JPanel(new GridLayout(1,2,8,0)); br.setOpaque(false); br.setMaximumSize(new Dimension(Integer.MAX_VALUE,34)); br.setAlignmentX(LEFT_ALIGNMENT);
        JButton btnS=UITheme.primaryButton("Simpan",UITheme.ACCENT_LIME);
        JButton btnH=UITheme.dangerButton("Hapus");
        JButton btnN=UITheme.ghostButton("+ Baru",UITheme.ACCENT_BLUE);
        JButton btnB=UITheme.ghostButton("Batal",UITheme.TEXT_SECONDARY);
        br.add(btnB); br.add(btnS);
        JPanel br2=new JPanel(new GridLayout(1,2,8,0)); br2.setOpaque(false); br2.setMaximumSize(new Dimension(Integer.MAX_VALUE,34)); br2.setAlignmentX(LEFT_ALIGNMENT);
        br2.add(btnN); br2.add(btnH);
        fc.add(lf); fc.add(Box.createVerticalStrut(8)); fc.add(UITheme.separator()); fc.add(Box.createVerticalStrut(8)); fc.add(Box.createVerticalGlue()); fc.add(br2); fc.add(Box.createVerticalStrut(6)); fc.add(br);

        JPanel leftCard=UITheme.card(); leftCard.setLayout(new BorderLayout(0,10));
        JPanel tbr=new JPanel(new BorderLayout(8,0)); tbr.setOpaque(false);
        JTextField sf=UITheme.styledField("Cari supplier…"); sf.setPreferredSize(new Dimension(220,34));
        JButton btnAdd2=UITheme.primaryButton("+ Tambah",UITheme.ACCENT_LIME);
        tbr.add(sf,BorderLayout.WEST); tbr.add(btnAdd2,BorderLayout.EAST);
        leftCard.add(tbr,BorderLayout.NORTH);
        leftCard.add(UITheme.styledScroll(supTable),BorderLayout.CENTER);

        p.add(leftCard,BorderLayout.CENTER); p.add(fc,BorderLayout.EAST);

        btnN.addActionListener(e->clearSup()); btnB.addActionListener(e->clearSup());
        btnS.addActionListener(e->saveSup()); btnH.addActionListener(e->delSup());
        supTable.getSelectionModel().addListSelectionListener(e->{
            int row=supTable.getSelectedRow();
            if(row>=0){ selSupId=-1; sKode.setText(s(supMdl.getValueAt(row,0))); sNama.setText(s(supMdl.getValueAt(row,1))); sTlp.setText(s(supMdl.getValueAt(row,2))); sEmail.setText(s(supMdl.getValueAt(row,3))); sCP.setText(s(supMdl.getValueAt(row,4))); }
        });
        return p;
    }

    // ── Supplier CRUD ─────────────────────────────────────────────
    private void loadSuppliers(){
        supMdl.setRowCount(0);
        for(Supplier s:supDAO.getAll())
            supMdl.addRow(new Object[]{s.getKodeSupplier(),s.getNamaSupplier(),s.getTelepon(),s.getEmail(),s.getContactPerson()});
    }
    private void saveSup(){
        if(sNama.getText().isBlank()){AlertUtil.showWarning(this,"Nama supplier wajib!");return;}
        Supplier s=new Supplier();s.setId(selSupId);s.setKodeSupplier(sKode.getText().trim());s.setNamaSupplier(sNama.getText().trim());s.setAlamat(sAlamat.getText().trim());s.setTelepon(sTlp.getText().trim());s.setEmail(sEmail.getText().trim());s.setContactPerson(sCP.getText().trim());
        boolean ok=(selSupId==-1)?supDAO.insert(s):supDAO.update(s);
        if(ok){AlertUtil.showInfo(this,"Supplier disimpan!");loadSuppliers();clearSup();}else AlertUtil.showError(this,"Gagal.");
    }
    private void delSup(){
        if(selSupId==-1){AlertUtil.showWarning(this,"Pilih supplier!");return;}
        if(!AlertUtil.showConfirm(this,"Hapus supplier?"))return;
        if(supDAO.delete(selSupId)){AlertUtil.showInfo(this,"Dihapus.");loadSuppliers();clearSup();}else AlertUtil.showError(this,"Gagal.");
    }
    private void clearSup(){selSupId=-1;sKode.setText("");sNama.setText("");sTlp.setText("");sEmail.setText("");sAlamat.setText("");sCP.setText("");}

    // ── Helpers ───────────────────────────────────────────────────
    private void sumRow(JPanel p, String label, Component val){
        JPanel r=new JPanel(new BorderLayout());r.setOpaque(false);r.setMaximumSize(new Dimension(Integer.MAX_VALUE,32));r.setAlignmentX(LEFT_ALIGNMENT);
        r.add(UITheme.fieldLabel(label),BorderLayout.WEST);r.add(val,BorderLayout.EAST);p.add(r);p.add(Box.createVerticalStrut(4));
    }
    private JTextField fld(String label, JPanel p){
        JLabel l=UITheme.fieldLabel(label);l.setAlignmentX(LEFT_ALIGNMENT);
        JTextField f=UITheme.styledField("");f.setMaximumSize(new Dimension(Integer.MAX_VALUE,34));f.setAlignmentX(LEFT_ALIGNMENT);
        p.add(l);p.add(Box.createVerticalStrut(4));p.add(f);p.add(Box.createVerticalStrut(8));return f;
    }
    private DefaultTableCellRenderer tblRenderer(int accentCol, Color accent){
        return new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int r,int c){
                Component cp=super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                cp.setBackground(sel?new Color(238,242,255):(r%2==0?UITheme.BG_CARD:UITheme.BG_ROW_ALT));
                cp.setForeground(c==accentCol&&accent!=null?accent:UITheme.TEXT_PRIMARY);
                ((JLabel)cp).setBorder(new EmptyBorder(0,12,0,12));
                return cp;
            }
        };
    }
    private String s(Object o){return o==null?"":o.toString();}

    // ════════════════════════════════════════════════════════════════
    // LOAD HISTORY
    // ════════════════════════════════════════════════════════════════
    private void loadHistory() {
        histMdl.setRowCount(0);
        try {
            List<Transaksi> list = trxCtrl.getRiwayatPembelian();
            if (list == null || list.isEmpty()) {
                histMdl.addRow(new Object[]{"— Tidak ada data —", "", "", "", "", ""});
                return;
            }
            for (Transaksi t : list) {
                histMdl.addRow(new Object[]{
                    t.getNoTransaksi(),
                    FormatUtil.formatDateTime(t.getTanggal()),
                    t.getNamaSupplier() != null ? t.getNamaSupplier() : "-",
                    t.getDetails() != null ? t.getDetails().size() : "-",
                    FormatUtil.formatRupiah(t.getGrandTotal()),
                    t.getStatus() != null ? t.getStatus() : "SELESAI"
                });
            }
        } catch (Exception ex) {
            showDetailedError("Gagal memuat riwayat pembelian", ex);
        }
    }

    // ════════════════════════════════════════════════════════════════
    // EXPORT PDF — JasperReports Programatik
    // ════════════════════════════════════════════════════════════════
    private void exportHistoryPDF() {
        if (histMdl.getRowCount() == 0 ||
            (histMdl.getRowCount() == 1 && histMdl.getValueAt(0,0).toString().startsWith("—"))) {
            AlertUtil.showWarning(this, "Tidak ada data untuk diekspor.\nKlik Refresh terlebih dahulu.");
            return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Simpan Laporan Pembelian PDF");
        fc.setFileFilter(new FileNameExtensionFilter("PDF Files (*.pdf)", "pdf"));
        String fname = "Laporan_Pembelian_" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".pdf";
        fc.setSelectedFile(new java.io.File(System.getProperty("user.home"), fname));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        java.io.File pdfFile = fc.getSelectedFile();
        if (!pdfFile.getName().toLowerCase().endsWith(".pdf"))
            pdfFile = new java.io.File(pdfFile.getAbsolutePath() + ".pdf");

        final java.io.File finalFile = pdfFile;
        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));

        // Progress dialog
        JDialog dlg = new JDialog((java.awt.Frame)
            javax.swing.SwingUtilities.getWindowAncestor(this), false);
        dlg.setUndecorated(true);
        JPanel pp = new JPanel(new java.awt.BorderLayout(10, 10));
        pp.setBorder(new EmptyBorder(18, 24, 18, 24));
        pp.setBackground(UITheme.BG_CARD);
        JProgressBar bar = new JProgressBar(); bar.setIndeterminate(true);
        bar.setPreferredSize(new java.awt.Dimension(220, 8));
        pp.add(new JLabel("Membuat PDF laporan pembelian..."), java.awt.BorderLayout.NORTH);
        pp.add(bar, java.awt.BorderLayout.CENTER);
        dlg.add(pp); dlg.pack(); dlg.setLocationRelativeTo(this);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            String errMsg = null;
            @Override
            protected Void doInBackground() {
                try {
                    JasperPrint print = buildPurchaseJasperPrint();
                    JRPdfExporter exporter = new JRPdfExporter();
                    exporter.setExporterInput(new SimpleExporterInput(print));
                    exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(finalFile));
                    SimplePdfReportConfiguration cfg = new SimplePdfReportConfiguration();
                    cfg.setSizePageToContent(false);
                    exporter.setConfiguration(cfg);
                    exporter.exportReport();
                } catch (Exception ex) {
                    StringWriter sw = new StringWriter();
                    ex.printStackTrace(new PrintWriter(sw));
                    errMsg = ex.getMessage() + "\n\n" + sw.toString();
                }
                return null;
            }
            @Override
            protected void done() {
                dlg.dispose();
                setCursor(java.awt.Cursor.getDefaultCursor());
                if (errMsg != null) {
                    showDetailedError("Gagal export PDF", new RuntimeException(errMsg));
                } else {
                    int choice = JOptionPane.showConfirmDialog(PurchaseForm.this,
                        "PDF berhasil disimpan!\n" + finalFile.getAbsolutePath() +
                        "\n\nBuka file sekarang?",
                        "Export Berhasil", JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE);
                    if (choice == JOptionPane.YES_OPTION) {
                        try { Desktop.getDesktop().open(finalFile); }
                        catch (Exception ex) {
                            AlertUtil.showWarning(PurchaseForm.this,
                                "Tidak bisa membuka file otomatis.\nBuka manual di: " +
                                finalFile.getAbsolutePath());
                        }
                    }
                }
            }
        };
        dlg.setVisible(true);
        worker.execute();
    }

    private JasperPrint buildPurchaseJasperPrint() throws JRException {
        final int PAGE_W    = 842;   // A4 landscape lebar
        final int PAGE_H    = 595;
        final int MARGIN     = 30;
        final int CONTENT_W  = PAGE_W - MARGIN * 2;

        // ── Snapshot data dari histMdl ──
        String[] hdrs = new String[histMdl.getColumnCount()];
        for (int i = 0; i < hdrs.length; i++) hdrs[i] = histMdl.getColumnName(i);

        List<Object[]> rows = new ArrayList<>();
        for (int r = 0; r < histMdl.getRowCount(); r++) {
            Object[] row = new Object[hdrs.length];
            for (int c = 0; c < hdrs.length; c++) {
                Object v = histMdl.getValueAt(r, c);
                row[c] = v != null ? v.toString() : "-";
            }
            rows.add(row);
        }

        // ── Warna ──
        java.awt.Color C_TITLE_BG  = new java.awt.Color(15, 23, 42);
        java.awt.Color C_ACCENT    = new java.awt.Color(251, 191, 36);   // amber
        java.awt.Color C_HDR_BG    = new java.awt.Color(30, 41, 59);
        java.awt.Color C_HDR_FG    = java.awt.Color.WHITE;
        java.awt.Color C_ALT       = new java.awt.Color(248, 250, 252);
        java.awt.Color C_BORDER    = new java.awt.Color(226, 232, 240);
        java.awt.Color C_MUTED     = new java.awt.Color(148, 163, 184);

        // ── Design ──
        JasperDesign design = new JasperDesign();
        design.setName("PurchaseReport");
        design.setPageWidth(PAGE_W);   design.setPageHeight(PAGE_H);
        design.setLeftMargin(MARGIN);  design.setRightMargin(MARGIN);
        design.setTopMargin(MARGIN);   design.setBottomMargin(MARGIN);
        design.setColumnWidth(CONTENT_W);

        // Register fields
        for (int i = 0; i < hdrs.length; i++) {
            JRDesignField f = new JRDesignField();
            f.setName("F" + i); f.setValueClass(String.class);
            design.addField(f);
        }

        // Lebar kolom
        int[] cw = calcColWidths(hdrs, CONTENT_W);

        // ── TITLE BAND ──
        JRDesignBand title = new JRDesignBand(); title.setHeight(80);
        JRDesignRectangle bgR = rect(0, 0, CONTENT_W, 80, C_TITLE_BG);
        JRDesignRectangle acc = rect(0, 0, 5, 80, C_ACCENT);
        title.addElement(bgR); title.addElement(acc);
        title.addElement(st("LAPORAN PEMBELIAN", 15, 14, 10, CONTENT_W-20, 26,
            HorizontalTextAlignEnum.LEFT, true, C_HDR_FG));
        title.addElement(st("Dicetak: " +
            LocalDateTime.now().format(DateTimeFormatter.ofPattern(
                "dd MMMM yyyy HH:mm", new Locale("id","ID"))) +
            "   |   Total: " + rows.size() + " transaksi",
            9, 14, 38, CONTENT_W-20, 18,
            HorizontalTextAlignEnum.LEFT, false, C_MUTED));
        design.setTitle(title);

        // ── COLUMN HEADER BAND ──
        JRDesignBand colHdr = new JRDesignBand(); colHdr.setHeight(26);
        colHdr.addElement(rect(0, 0, CONTENT_W, 26, C_HDR_BG));
        int cx = 0;
        for (int i = 0; i < hdrs.length; i++) {
            colHdr.addElement(st(hdrs[i].toUpperCase(), 8, cx+4, 3, cw[i]-8, 20,
                HorizontalTextAlignEnum.LEFT, true, C_HDR_FG));
            cx += cw[i];
        }
        design.setColumnHeader(colHdr);

        // ── DETAIL BAND ──
        JRDesignBand detail = new JRDesignBand(); detail.setHeight(22);
        detail.setSplitType(SplitTypeEnum.STRETCH);

        // Alt row background
        JRDesignRectangle altBg = rect(0, 0, CONTENT_W, 22, C_ALT);
        JRDesignExpression altExpr = new JRDesignExpression();
        altExpr.setText("$V{REPORT_COUNT} % 2 == 0");
        altBg.setPrintWhenExpression(altExpr);
        detail.addElement(altBg);

        // Bottom line
        JRDesignLine ln = new JRDesignLine();
        ln.setX(0); ln.setY(21); ln.setWidth(CONTENT_W); ln.setHeight(1);
        ln.getLinePen().setLineWidth(0.4f); ln.getLinePen().setLineColor(C_BORDER);
        detail.addElement(ln);

        cx = 0;
        for (int i = 0; i < hdrs.length; i++) {
            JRDesignTextField tf = new JRDesignTextField();
            tf.setX(cx+4); tf.setY(2); tf.setWidth(cw[i]-8); tf.setHeight(18);
            tf.setBlankWhenNull(true); tf.setStretchWithOverflow(true);
            JRDesignExpression ex = new JRDesignExpression();
            ex.setText("$F{F" + i + "}");
            tf.setExpression(ex);
            boolean isMoney  = hdrs[i].toLowerCase().contains("total");
            boolean isStatus = hdrs[i].equalsIgnoreCase("Status");
            tf.setHorizontalTextAlign(isMoney ? HorizontalTextAlignEnum.RIGHT : HorizontalTextAlignEnum.LEFT);
            tf.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
            tf.setFontSize(9f);
            tf.setForecolor(isMoney  ? new java.awt.Color(5, 150, 105)
                          : isStatus ? new java.awt.Color(7, 89, 133)
                          : java.awt.Color.DARK_GRAY);
            detail.addElement(tf);
            cx += cw[i];
        }
        ((JRDesignSection) design.getDetailSection()).addBand(detail);

        // ── SUMMARY BAND ──
        JRDesignBand sum = new JRDesignBand(); sum.setHeight(28);
        sum.addElement(rect(0, 0, CONTENT_W, 28, new java.awt.Color(241, 245, 249)));
        sum.addElement(st("Total " + rows.size() + " transaksi pembelian",
            10, 8, 6, CONTENT_W-16, 18, HorizontalTextAlignEnum.LEFT, true,
            new java.awt.Color(30,41,59)));
        design.setSummary(sum);

        // ── PAGE FOOTER ──
        JRDesignBand footer = new JRDesignBand(); footer.setHeight(18);
        footer.addElement(st("SRMS — Smart Retail Management System",
            8, 0, 3, CONTENT_W/2, 14, HorizontalTextAlignEnum.LEFT, false, C_MUTED));
        JRDesignTextField pgNum = new JRDesignTextField();
        pgNum.setX(CONTENT_W/2); pgNum.setY(3); pgNum.setWidth(CONTENT_W/2); pgNum.setHeight(14);
        pgNum.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
        pgNum.setFontSize(8f); pgNum.setForecolor(C_MUTED);
        JRDesignExpression pgExpr = new JRDesignExpression();
        pgExpr.setText("\"Halaman \" + $V{PAGE_NUMBER}");
        pgNum.setExpression(pgExpr);
        footer.addElement(pgNum);
        design.setPageFooter(footer);

        // ── Compile + fill ──
        JasperReport report = JasperCompileManager.compileReport(design);

        DefaultTableModel dm = new DefaultTableModel(hdrs, 0);
        for (Object[] r : rows) dm.addRow(r);

        java.util.Map<String,Object> params = new java.util.HashMap<>();
        params.put(JRParameter.REPORT_LOCALE, new Locale("id","ID"));

        return JasperFillManager.fillReport(report, params, new JRTableModelDataSource(dm));
    }

    // ── Helper: static text element ──
    private JRDesignStaticText st(String text, int fontSize, int x, int y, int w, int h,
                                   HorizontalTextAlignEnum align, boolean bold, java.awt.Color fg) {
        JRDesignStaticText s = new JRDesignStaticText();
        s.setText(text); s.setX(x); s.setY(y); s.setWidth(w); s.setHeight(h);
        s.setHorizontalTextAlign(align); s.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        s.setForecolor(fg); s.setFontSize((float) fontSize); s.setBold(bold);
        return s;
    }

    // ── Helper: filled rectangle ──
    private JRDesignRectangle rect(int x, int y, int w, int h, java.awt.Color bg) {
        JRDesignRectangle r = new JRDesignRectangle();
        r.setX(x); r.setY(y); r.setWidth(w); r.setHeight(h);
        r.getLinePen().setLineWidth(0f); r.setBackcolor(bg); r.setMode(ModeEnum.OPAQUE);
        return r;
    }

    // ── Helper: lebar kolom proporsional ──
    private int[] calcColWidths(String[] headers, int total) {
        int[] w = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            String h = headers[i].toLowerCase();
            if (h.contains("no") || h.contains("#"))         w[i] = 7;
            else if (h.contains("tanggal"))                  w[i] = 9;
            else if (h.contains("supplier"))                 w[i] = 12;
            else if (h.contains("total") || h.contains("grand")) w[i] = 9;
            else if (h.contains("item") || h.contains("qty"))w[i] = 5;
            else if (h.contains("status"))                   w[i] = 6;
            else                                             w[i] = 7;
        }
        int sumW = Arrays.stream(w).sum();
        int[] res = new int[headers.length]; int used = 0;
        for (int i = 0; i < headers.length - 1; i++) {
            res[i] = (int)((double) w[i] / sumW * total); used += res[i];
        }
        res[headers.length-1] = total - used;
        return res;
    }

    // ════════════════════════════════════════════════════════════════
    // EXPORT CSV — PURCHASE HISTORY
    // ════════════════════════════════════════════════════════════════
    private void exportHistoryCSV() {
        if (histMdl.getRowCount() == 0) {
            AlertUtil.showWarning(this, "Tidak ada data untuk diekspor!"); return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Simpan CSV Riwayat Pembelian");
        fc.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));
        String fname = "Pembelian_" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".csv";
        fc.setSelectedFile(new java.io.File(System.getProperty("user.home"), fname));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        java.io.File file = fc.getSelectedFile();
        if (!file.getName().endsWith(".csv")) file = new java.io.File(file.getAbsolutePath()+".csv");
        try (PrintWriter pw = new PrintWriter(
                new java.io.OutputStreamWriter(new java.io.FileOutputStream(file), "UTF-8"))) {
            pw.print('\uFEFF'); // BOM for Excel
            StringBuilder hdr = new StringBuilder();
            for (int i = 0; i < histMdl.getColumnCount(); i++) {
                if (i > 0) hdr.append(",");
                hdr.append("\"").append(histMdl.getColumnName(i)).append("\"");
            }
            pw.println(hdr);
            for (int r = 0; r < histMdl.getRowCount(); r++) {
                StringBuilder row = new StringBuilder();
                for (int c = 0; c < histMdl.getColumnCount(); c++) {
                    if (c > 0) row.append(",");
                    Object v = histMdl.getValueAt(r, c);
                    String val = v == null ? "" : v.toString().replace("\"", "\"\"\"");
                    row.append("\"").append(val).append("\"");
                }
                pw.println(row);
            }
            AlertUtil.showInfo(this, "CSV berhasil disimpan!\n" + file.getAbsolutePath());
        } catch (Exception ex) {
            showDetailedError("Gagal menyimpan CSV", ex);
        }
    }

    // ════════════════════════════════════════════════════════════════
    // ERROR DIALOG — tampilkan pesan + stack trace agar bisa debug
    // ════════════════════════════════════════════════════════════════
    private void showDetailedError(String title, Exception ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        String trace = sw.toString();

        JTextArea ta = new JTextArea(trace);
        ta.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 11));
        ta.setEditable(false); ta.setRows(12);
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new java.awt.Dimension(600, 220));

        JPanel panel = new JPanel(new java.awt.BorderLayout(0, 8));
        panel.add(new JLabel("<html><b>" + title + "</b><br>"
            + "<font color=red>" + ex.getMessage() + "</font></html>"),
            java.awt.BorderLayout.NORTH);
        panel.add(sp, java.awt.BorderLayout.CENTER);
        panel.add(new JLabel("Salin teks di atas dan kirim ke developer untuk analisis."),
            java.awt.BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, panel, "Error Detail", JOptionPane.ERROR_MESSAGE);
    }


}