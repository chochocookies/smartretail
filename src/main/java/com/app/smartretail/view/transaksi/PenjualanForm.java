package com.app.smartretail.view.transaksi;

import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;

import com.app.smartretail.controller.BarangController;
import com.app.smartretail.controller.TransaksiController;
import com.app.smartretail.model.Barang;
import com.app.smartretail.model.Transaksi;
import com.app.smartretail.model.TransaksiDetail;
import com.app.smartretail.utils.AlertUtil;
import com.app.smartretail.utils.FormatUtil;
import com.app.smartretail.utils.UITheme;
import com.app.smartretail.view.component.Icons;

public class PenjualanForm extends JPanel {

    private final TransaksiController trxCtrl   = new TransaksiController();
    private final BarangController    barangCtrl = new BarangController();

    // ── Komponen utama ─────────────────────────────────────────────
    private JTextField txtSearch, txtBayar, txtDiskon;
    private JLabel lblNo, lblSub, lblGrand, lblKembalian;
    private JTable  cart;  private DefaultTableModel cartMdl;
    private JComboBox<String> cmbMetode;
    private JButton btnProses, btnReset;
    private Transaksi trx;

    // ── Live-search dropdown ────────────────────────────────────────
    private JWindow  searchPopup;
    private JList<String> searchList;
    private DefaultListModel<String> searchListModel;
    private List<Barang> lastSearchResult;

    // ── Riwayat ────────────────────────────────────────────────────
    private JTable  histTable; private DefaultTableModel histMdl;
    private JPanel  pnlMachine, pnlRiwayat;
    private JButton btnTabMachine, btnTabRiwayat;

    private static final int DEFAULT_CUSTOMER_ID = 1;

    public PenjualanForm() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_SURFACE);
        setBorder(new EmptyBorder(22, 24, 22, 24));
        build();
        reset();
    }

    // ═══════════════════════════════════════════════════════════════
    // BUILD
    // ═══════════════════════════════════════════════════════════════
    private void build() {
        // ── Header ──
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.setBorder(new EmptyBorder(0, 0, 14, 0));
        JPanel ht = new JPanel(); ht.setOpaque(false);
        ht.setLayout(new BoxLayout(ht, BoxLayout.Y_AXIS));
        JLabel title = UITheme.pageTitle("POS — Point of Sale");
        title.setAlignmentX(LEFT_ALIGNMENT);
        JLabel sub = new JLabel("Transaksi penjualan kasir");
        sub.setFont(UITheme.FONT_BODY); sub.setForeground(UITheme.TEXT_SECONDARY);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        ht.add(title); ht.add(sub);
        hdr.add(ht, BorderLayout.WEST);
        add(hdr, BorderLayout.NORTH);

        // ── Tab strip ──
        JPanel tabStrip = buildTabStrip();
        add(tabStrip, BorderLayout.SOUTH); // sementara, dipindah ke center

        // ── Card layout: POS Machine | Riwayat ──
        JPanel cardArea = new JPanel(new CardLayout());
        cardArea.setOpaque(false);

        pnlMachine = buildMachinePanel();
        pnlRiwayat = buildRiwayatPanel();

        cardArea.add(pnlMachine, "machine");
        cardArea.add(pnlRiwayat, "riwayat");

        // Susun: tab strip di atas, card di bawah
        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(tabStrip, BorderLayout.NORTH);
        center.add(cardArea, BorderLayout.CENTER);

        remove(tabStrip); // hapus dari SOUTH
        add(center, BorderLayout.CENTER);

        // Tab events
        btnTabMachine.addActionListener(e -> {
            ((CardLayout) cardArea.getLayout()).show(cardArea, "machine");
            setTabActive(btnTabMachine, btnTabRiwayat);
        });
        btnTabRiwayat.addActionListener(e -> {
            ((CardLayout) cardArea.getLayout()).show(cardArea, "riwayat");
            setTabActive(btnTabRiwayat, btnTabMachine);
            loadRiwayat();
        });
    }

    // ═══════════════════════════════════════════════════════════════
    // TAB STRIP
    // ═══════════════════════════════════════════════════════════════
    private JPanel buildTabStrip() {
        JPanel strip = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        strip.setOpaque(false);

        btnTabMachine = makeTabButton("POS Machine",  true);
        btnTabRiwayat = makeTabButton("Riwayat Transaksi", false);
        strip.add(btnTabMachine);
        strip.add(btnTabRiwayat);
        return strip;
    }

    private JButton makeTabButton(String text, boolean active) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                boolean a = Boolean.TRUE.equals(getClientProperty("active"));
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (a) {
                    g2.setColor(UITheme.BG_CARD);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.setColor(UITheme.ACCENT_BLUE);
                    g2.fillRect(4, getHeight() - 3, getWidth() - 8, 3);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.putClientProperty("active", active);
        b.setFont(active ? new Font("Segoe UI", Font.BOLD, 12) : UITheme.FONT_BODY);
        b.setForeground(active ? UITheme.TEXT_PRIMARY : UITheme.TEXT_MUTED);
        b.setOpaque(false); b.setContentAreaFilled(false);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(8, 18, 8, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void setTabActive(JButton on, JButton off) {
        on.putClientProperty("active", true);
        on.setFont(new Font("Segoe UI", Font.BOLD, 12));
        on.setForeground(UITheme.TEXT_PRIMARY);
        off.putClientProperty("active", false);
        off.setFont(UITheme.FONT_BODY);
        off.setForeground(UITheme.TEXT_MUTED);
        on.repaint(); off.repaint();
    }

    // ═══════════════════════════════════════════════════════════════
    // POS MACHINE PANEL
    // ═══════════════════════════════════════════════════════════════
    private JPanel buildMachinePanel() {
        JPanel p = new JPanel(new BorderLayout(16, 0));
        p.setOpaque(false);

        // ── Kiri: search + cart ──
        JPanel left = new JPanel(new BorderLayout(0, 10));
        left.setOpaque(false);

        // Search bar (lebar penuh, tombol lebih lebar)
        JPanel searchWrap = buildSearchBar();
        left.add(searchWrap, BorderLayout.NORTH);

        // Cart table
        String[] cols = {"#", "Kode", "Nama Barang", "Harga Satuan", "Qty", "Subtotal"};
        cartMdl = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return c == 4; }
        };
        cart = new JTable(cartMdl);
        UITheme.styleTable(cart);
        cart.setRowHeight(42);
        cart.getColumnModel().getColumn(0).setMaxWidth(36);
        cart.getColumnModel().getColumn(1).setMaxWidth(100);
        cart.getColumnModel().getColumn(4).setMaxWidth(60);
        cart.setDefaultRenderer(Object.class, cartRenderer());

        // Qty edit listener: recalc subtotal on change
        cart.getModel().addTableModelListener(e -> {
            if (e.getColumn() == 4) {
                int row = e.getFirstRow();
                try {
                    int qty = Integer.parseInt(cartMdl.getValueAt(row, 4).toString());
                    double harga = FormatUtil.parseDouble(cartMdl.getValueAt(row, 3).toString());
                    cartMdl.setValueAt(FormatUtil.formatRupiah(qty * harga), row, 5);
                    hitungTotal();
                } catch (Exception ignored) {}
            }
        });

        JPanel cartTools = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        cartTools.setOpaque(false);
        JButton btnDel = UITheme.dangerButton("Hapus Item");
        JButton btnClr = UITheme.ghostButton("Kosongkan Cart", UITheme.TEXT_SECONDARY);
        cartTools.add(btnDel); cartTools.add(btnClr);

        JPanel cartCard = UITheme.card();
        cartCard.setLayout(new BorderLayout(0, 0));
        JLabel lCart = new JLabel("Keranjang Belanja");
        lCart.setFont(UITheme.FONT_H2); lCart.setForeground(UITheme.TEXT_PRIMARY);
        lCart.setBorder(new EmptyBorder(0, 0, 6, 0));
        cartCard.add(lCart, BorderLayout.NORTH);
        cartCard.add(UITheme.styledScroll(cart), BorderLayout.CENTER);
        cartCard.add(cartTools, BorderLayout.SOUTH);

        left.add(cartCard, BorderLayout.CENTER);

        // ── Kanan: order summary ──
        JPanel right = buildOrderPanel();

        p.add(left, BorderLayout.CENTER);
        p.add(right, BorderLayout.EAST);

        // ── Events ──
        btnDel.addActionListener(e -> delItem());
        btnClr.addActionListener(e -> {
            if (AlertUtil.showConfirm(this, "Kosongkan keranjang?")) {
                cartMdl.setRowCount(0); hitungTotal();
            }
        });
        btnProses.addActionListener(e -> proses());
        btnReset.addActionListener(e -> { if (AlertUtil.showConfirm(this, "Reset transaksi?")) reset(); });
        txtBayar.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { hitungKembalian(); }
        });
        txtDiskon.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { hitungTotal(); }
        });

        return p;
    }

    // ═══════════════════════════════════════════════════════════════
    // LIVE SEARCH BAR + DROPDOWN POPUP
    // ═══════════════════════════════════════════════════════════════
    private JPanel buildSearchBar() {
        JPanel wrap = new JPanel(new BorderLayout(8, 0));
        wrap.setOpaque(false);

        // Field pencarian
        txtSearch = UITheme.styledField("Ketik nama / kode barang untuk mencari...");
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setPreferredSize(new Dimension(0, 42));

        // Tombol Cari — lebih lebar (fix request)
        JButton btnCari = new JButton("CARI / TAMBAH") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed()  ? UITheme.ACCENT_BLUE.darker()
                         : getModel().isRollover() ? UITheme.ACCENT_BLUE.brighter()
                         : UITheme.ACCENT_BLUE;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnCari.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCari.setForeground(Color.WHITE);
        btnCari.setOpaque(false); btnCari.setContentAreaFilled(false);
        btnCari.setBorderPainted(false); btnCari.setFocusPainted(false);
        btnCari.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCari.setPreferredSize(new Dimension(160, 42)); // LEBIH LEBAR

        wrap.add(txtSearch, BorderLayout.CENTER);
        wrap.add(btnCari,   BorderLayout.EAST);

        // ── Popup dropdown live search ──
        searchListModel = new DefaultListModel<>();
        searchList = new JList<>(searchListModel);
        searchList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        searchList.setFixedCellHeight(40);
        searchList.setBorder(new EmptyBorder(4, 8, 4, 8));
        searchList.setCellRenderer(new SearchItemRenderer());
        searchList.setBackground(UITheme.BG_CARD);

        JScrollPane popupScroll = new JScrollPane(searchList);
        popupScroll.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230), 1));
        popupScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        searchPopup = new JWindow();
        searchPopup.add(popupScroll);
        searchPopup.setBackground(new Color(0, 0, 0, 0));

        // ── Live search listener ──
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { doLiveSearch(); }
            public void removeUpdate(DocumentEvent e) { doLiveSearch(); }
            public void changedUpdate(DocumentEvent e) { doLiveSearch(); }
        });

        // Enter di field → ambil item pertama / item yang dipilih
        txtSearch.addActionListener(e -> {
            int idx = searchList.getSelectedIndex();
            if (idx < 0 && !searchListModel.isEmpty()) idx = 0;
            if (idx >= 0 && lastSearchResult != null && idx < lastSearchResult.size()) {
                addItemToCart(lastSearchResult.get(idx));
            } else {
                addItemByKode(txtSearch.getText().trim());
            }
            hideSearchPopup();
        });

        // Klik tombol Cari
        btnCari.addActionListener(e -> {
            int idx = searchList.getSelectedIndex();
            if (idx >= 0 && lastSearchResult != null && idx < lastSearchResult.size()) {
                addItemToCart(lastSearchResult.get(idx));
            } else {
                addItemByKode(txtSearch.getText().trim());
            }
            hideSearchPopup();
        });

        // Klik item di dropdown → tambah ke cart
        searchList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int idx = searchList.locationToIndex(e.getPoint());
                if (idx >= 0 && lastSearchResult != null && idx < lastSearchResult.size()) {
                    addItemToCart(lastSearchResult.get(idx));
                    hideSearchPopup();
                }
            }
        });

        // Arrow key: navigasi dropdown dari field
        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (searchPopup.isVisible()) {
                        searchList.requestFocus();
                        int idx = Math.max(0, searchList.getSelectedIndex());
                        searchList.setSelectedIndex(idx);
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    hideSearchPopup();
                }
            }
        });

        // Enter di dropdown list → add
        searchList.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    int idx = searchList.getSelectedIndex();
                    if (idx >= 0 && lastSearchResult != null && idx < lastSearchResult.size()) {
                        addItemToCart(lastSearchResult.get(idx));
                        hideSearchPopup();
                        txtSearch.requestFocus();
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    hideSearchPopup();
                    txtSearch.requestFocus();
                }
            }
        });

        return wrap;
    }

    /** Jalankan live search saat user mengetik */
    private void doLiveSearch() {
        String kw = txtSearch.getText().trim();
        if (kw.length() < 1) { hideSearchPopup(); return; }

        SwingUtilities.invokeLater(() -> {
            lastSearchResult = barangCtrl.searchBarang(kw);
            searchListModel.clear();
            if (lastSearchResult.isEmpty()) { hideSearchPopup(); return; }

            for (Barang b : lastSearchResult) {
                // Format: "KODE  —  Nama Barang                  Rp 13.000  (stok: 25)"
                searchListModel.addElement(
                    b.getKodeBarang() + "||" +
                    b.getNamaBarang() + "||" +
                    FormatUtil.formatRupiah(b.getHargaJual()) + "||" +
                    b.getStok()
                );
            }
            showSearchPopup();
        });
    }

    private void showSearchPopup() {
        if (!txtSearch.isShowing()) return;
        Point loc = txtSearch.getLocationOnScreen();
        int popupH = Math.min(lastSearchResult.size() * 40 + 10, 240);
        searchPopup.setBounds(loc.x, loc.y + txtSearch.getHeight() + 2,
                              txtSearch.getWidth() + 168, // sama lebar dengan field + tombol
                              popupH);
        searchPopup.setVisible(true);
    }

    private void hideSearchPopup() {
        searchPopup.setVisible(false);
        searchListModel.clear();
    }

    /** Custom renderer untuk dropdown — tampilkan nama + harga */
    private static class SearchItemRenderer extends JPanel implements ListCellRenderer<String> {
        private final JLabel lblKode, lblNama, lblHarga, lblStok;

        SearchItemRenderer() {
            setLayout(new BorderLayout(10, 0));
            setOpaque(true);

            lblKode  = new JLabel(); lblKode.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lblKode.setForeground(new Color(100, 120, 150));
            lblKode.setPreferredSize(new Dimension(70, 0));

            lblNama  = new JLabel(); lblNama.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblHarga = new JLabel(); lblHarga.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblHarga.setForeground(new Color(30, 130, 90));
            lblHarga.setPreferredSize(new Dimension(100, 0));
            lblHarga.setHorizontalAlignment(SwingConstants.RIGHT);

            lblStok  = new JLabel(); lblStok.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            lblStok.setForeground(new Color(140, 140, 140));
            lblStok.setPreferredSize(new Dimension(70, 0));
            lblStok.setHorizontalAlignment(SwingConstants.RIGHT);

            setBorder(new EmptyBorder(0, 8, 0, 8));
            add(lblKode,  BorderLayout.WEST);
            add(lblNama,  BorderLayout.CENTER);

            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            right.setOpaque(false);
            right.add(lblHarga); right.add(lblStok);
            add(right, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list,
                String value, int index, boolean isSelected, boolean cellHasFocus) {
            String[] parts = value.split("\\|\\|");
            lblKode.setText(parts.length > 0 ? parts[0] : "");
            lblNama.setText(parts.length > 1 ? parts[1] : "");
            lblHarga.setText(parts.length > 2 ? parts[2] : "");
            lblStok.setText(parts.length > 3 ? "stok: " + parts[3] : "");
            setBackground(isSelected ? new Color(230, 240, 255) : Color.WHITE);
            setForeground(isSelected ? UITheme.TEXT_PRIMARY : UITheme.TEXT_PRIMARY);
            lblNama.setForeground(isSelected ? UITheme.TEXT_PRIMARY : UITheme.TEXT_PRIMARY);
            return this;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // ORDER PANEL (kanan)
    // ═══════════════════════════════════════════════════════════════
    private JPanel buildOrderPanel() {
        JPanel card = UITheme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(300, 0));

        lblNo = lbl("Order No: —", new Font("Segoe UI", Font.BOLD, 12), UITheme.TEXT_PRIMARY);
        JLabel lSum = lbl("Ringkasan Order", UITheme.FONT_H2, UITheme.TEXT_PRIMARY);

        JSeparator sep1 = UITheme.separator(); sep1.setAlignmentX(LEFT_ALIGNMENT);

        // Subtotal
        JPanel rowSub = sumRow2("Subtotal:");
        lblSub = (JLabel) rowSub.getClientProperty("val");

        // Diskon
        JPanel rowDis = new JPanel(new BorderLayout(6, 0));
        rowDis.setOpaque(false); rowDis.setAlignmentX(LEFT_ALIGNMENT);
        rowDis.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        JLabel lDis = UITheme.fieldLabel("Diskon (Rp):");
        txtDiskon = UITheme.styledField("0");
        txtDiskon.setFont(UITheme.FONT_BODY);
        txtDiskon.setPreferredSize(new Dimension(110, 30));
        rowDis.add(lDis, BorderLayout.WEST); rowDis.add(txtDiskon, BorderLayout.EAST);

        JSeparator sep2 = UITheme.separator(); sep2.setAlignmentX(LEFT_ALIGNMENT);

        // Grand total
        JPanel rowGrand = sumRow2("TOTAL:");
        lblGrand = (JLabel) rowGrand.getClientProperty("val");
        lblGrand.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblGrand.setForeground(UITheme.TEXT_PRIMARY);

        // Metode
        JLabel lMet = UITheme.fieldLabel("Metode Pembayaran");
        lMet.setAlignmentX(LEFT_ALIGNMENT);
        cmbMetode = UITheme.styledCombo(new String[]{"TUNAI","KARTU DEBIT","KARTU KREDIT","TRANSFER"});
        cmbMetode.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cmbMetode.setAlignmentX(LEFT_ALIGNMENT);

        // Bayar
        JLabel lByr = UITheme.fieldLabel("Jumlah Bayar (Rp)");
        lByr.setAlignmentX(LEFT_ALIGNMENT);
        txtBayar = UITheme.styledField("0");
        txtBayar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        txtBayar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        txtBayar.setAlignmentX(LEFT_ALIGNMENT);

        // Kembalian
        JLabel lKem = UITheme.fieldLabel("Kembalian");
        lKem.setAlignmentX(LEFT_ALIGNMENT);
        lblKembalian = lbl("Rp 0", new Font("Segoe UI", Font.BOLD, 24), UITheme.ACCENT_TEAL);

        // Numpad
        JPanel numpad = buildNumpad();
        numpad.setAlignmentX(LEFT_ALIGNMENT);

        // Tombol aksi
        btnProses = UITheme.primaryButton("Proses Transaksi", UITheme.ACCENT_LIME);
        btnProses.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnProses.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btnProses.setAlignmentX(LEFT_ALIGNMENT);

        btnReset = UITheme.ghostButton("Batal / Reset", UITheme.ACCENT_CORAL);
        btnReset.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnReset.setAlignmentX(LEFT_ALIGNMENT);

        card.add(lblNo); card.add(Box.createVerticalStrut(6));
        card.add(sep1);  card.add(Box.createVerticalStrut(8));
        card.add(lSum);  card.add(Box.createVerticalStrut(8));
        card.add(rowSub); card.add(Box.createVerticalStrut(6));
        card.add(rowDis); card.add(Box.createVerticalStrut(4));
        card.add(sep2);   card.add(Box.createVerticalStrut(6));
        card.add(rowGrand); card.add(Box.createVerticalStrut(12));
        card.add(lMet); card.add(Box.createVerticalStrut(5));
        card.add(cmbMetode); card.add(Box.createVerticalStrut(10));
        card.add(lByr); card.add(Box.createVerticalStrut(5));
        card.add(txtBayar); card.add(Box.createVerticalStrut(8));
        card.add(lKem); card.add(Box.createVerticalStrut(4));
        card.add(lblKembalian); card.add(Box.createVerticalStrut(10));
        card.add(numpad); card.add(Box.createVerticalStrut(12));
        card.add(btnProses); card.add(Box.createVerticalStrut(6));
        card.add(btnReset);

        return card;
    }

    // ── Helper: baris subtotal/total ──
    private JPanel sumRow2(String labelText) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false); row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        JLabel key = UITheme.fieldLabel(labelText);
        JLabel val = new JLabel("Rp 0");
        val.setFont(UITheme.FONT_BODY); val.setForeground(UITheme.TEXT_SECONDARY);
        val.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(key, BorderLayout.WEST); row.add(val, BorderLayout.EAST);
        row.putClientProperty("val", val);
        return row;
    }

    // ═══════════════════════════════════════════════════════════════
    // NUMPAD
    // ═══════════════════════════════════════════════════════════════
    private JPanel buildNumpad() {
        JPanel np = new JPanel(new GridLayout(4, 3, 6, 6));
        np.setOpaque(false);
        np.setMaximumSize(new Dimension(Integer.MAX_VALUE, 168));

        String[] keys = {"7","8","9","4","5","6","1","2","3","C","0","⌫"};
        for (String k : keys) {
            JButton b = new JButton(k) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color bg;
                    if ("C".equals(k))
                        bg = getModel().isPressed() ? new Color(255,200,200) :
                             getModel().isRollover() ? new Color(255,235,235) : UITheme.BG_CARD;
                    else if ("⌫".equals(k))
                        bg = getModel().isPressed() ? new Color(255,220,150) :
                             getModel().isRollover() ? new Color(255,245,210) : UITheme.BG_CARD;
                    else
                        bg = getModel().isRollover() ? UITheme.BG_HOVER : UITheme.BG_CARD;
                    g2.setColor(bg);
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                    g2.setColor("C".equals(k) ? new Color(255,150,150) :
                                "⌫".equals(k) ? new Color(255,180,80) :
                                UITheme.BORDER_DEFAULT);
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            b.setFont(new Font("Segoe UI", Font.BOLD, "⌫".equals(k) ? 16 : 15));
            b.setForeground("C".equals(k) ? new Color(200,0,0) :
                            "⌫".equals(k) ? new Color(180,90,0) : UITheme.TEXT_PRIMARY);
            b.setMargin(new Insets(0,0,0,0));
            b.setOpaque(false); b.setContentAreaFilled(false);
            b.setBorderPainted(false); b.setFocusPainted(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.addActionListener(e -> handleNumpad(k));
            np.add(b);
        }
        return np;
    }

    private void handleNumpad(String key) {
        String cur = txtBayar.getText();
        switch (key) {
            case "C":  txtBayar.setText("0"); break;
            case "⌫":  txtBayar.setText(cur.length() > 1 ? cur.substring(0, cur.length()-1) : "0"); break;
            default:   txtBayar.setText("0".equals(cur) ? key : cur + key);
        }
        hitungKembalian();
    }

    // ═══════════════════════════════════════════════════════════════
    // RIWAYAT PANEL
    // ═══════════════════════════════════════════════════════════════
    private JPanel buildRiwayatPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);

        // Toolbar riwayat
        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.setOpaque(false);

        JTextField txtCariRiwayat = UITheme.styledField("Cari no. transaksi, kasir...");
        txtCariRiwayat.setPreferredSize(new Dimension(260, 36));
        JButton btnRefreshRiwayat = UITheme.ghostButton("Refresh", UITheme.ACCENT_BLUE);

        JPanel toolLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolLeft.setOpaque(false);
        toolLeft.add(txtCariRiwayat); toolLeft.add(btnRefreshRiwayat);
        toolbar.add(toolLeft, BorderLayout.WEST);

        // Tabel riwayat
        String[] cols = {"No. Transaksi","Tanggal & Waktu","Kasir","Customer","Grand Total","Bayar","Kembalian","Metode","Status"};
        histMdl = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        histTable = new JTable(histMdl);
        UITheme.styleTable(histTable);
        histTable.setRowHeight(34);
        histTable.getColumnModel().getColumn(0).setPreferredWidth(130);
        histTable.getColumnModel().getColumn(1).setPreferredWidth(130);
        histTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        histTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        histTable.getColumnModel().getColumn(4).setPreferredWidth(110);
        histTable.getColumnModel().getColumn(5).setPreferredWidth(110);
        histTable.getColumnModel().getColumn(6).setPreferredWidth(110);
        histTable.getColumnModel().getColumn(7).setPreferredWidth(80);
        histTable.getColumnModel().getColumn(8).setPreferredWidth(80);

        // Warnai kolom Status
        histTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component cp = super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                cp.setBackground(sel ? new Color(230,240,255) : (r%2==0 ? UITheme.BG_CARD : UITheme.BG_ROW_ALT));
                cp.setForeground(UITheme.TEXT_PRIMARY);
                if (c == 8 && v != null && !sel) { // Status
                    cp.setForeground("SELESAI".equals(v.toString()) ?
                        new Color(21,87,36) : new Color(133,100,4));
                }
                if (c == 4 || c == 5 || c == 6) // uang
                    ((JLabel)cp).setHorizontalAlignment(SwingConstants.RIGHT);
                ((JLabel)cp).setBorder(new EmptyBorder(0,10,0,10));
                return cp;
            }
        });

        // Filter real-time
        TableRowSorter<DefaultTableModel> riwayatSorter = new TableRowSorter<>(histMdl);
        histTable.setRowSorter(riwayatSorter);
        txtCariRiwayat.getDocument().addDocumentListener(new DocumentListener() {
            void doFilter() {
                String kw = txtCariRiwayat.getText().trim();
                riwayatSorter.setRowFilter(kw.isEmpty() ? null :
                    RowFilter.regexFilter("(?i)" + kw, 0, 1, 2, 3));
            }
            public void insertUpdate(DocumentEvent e) { doFilter(); }
            public void removeUpdate(DocumentEvent e) { doFilter(); }
            public void changedUpdate(DocumentEvent e) { doFilter(); }
        });

        btnRefreshRiwayat.addActionListener(e -> loadRiwayat());

        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));
        JPanel hdr = new JPanel(new BorderLayout()); hdr.setOpaque(false);
        JLabel ttl = new JLabel("Riwayat Transaksi Penjualan");
        ttl.setFont(UITheme.FONT_H2); ttl.setForeground(UITheme.TEXT_PRIMARY);
        JLabel hint = new JLabel("Klik header kolom untuk mengurutkan");
        hint.setFont(UITheme.FONT_SMALL); hint.setForeground(UITheme.TEXT_MUTED);
        hdr.add(ttl, BorderLayout.WEST); hdr.add(hint, BorderLayout.EAST);

        card.add(toolbar, BorderLayout.NORTH);
        card.add(hdr, BorderLayout.CENTER);   // sementara, akan ditimpa oleh CENTER
        // Susun ulang
        JPanel cardInner = new JPanel(new BorderLayout(0,8));
        cardInner.setOpaque(false);
        cardInner.add(toolbar, BorderLayout.NORTH);
        cardInner.add(hdr,     BorderLayout.CENTER);

        JPanel cardFinal = UITheme.card();
        cardFinal.setLayout(new BorderLayout(0, 10));
        cardFinal.add(cardInner,                   BorderLayout.NORTH);
        cardFinal.add(UITheme.styledScroll(histTable), BorderLayout.CENTER);

        p.add(cardFinal, BorderLayout.CENTER);
        return p;
    }

    private void loadRiwayat() {
        histMdl.setRowCount(0);
        try {
            List<Transaksi> list = trxCtrl.getRiwayatPenjualan();
            if (list == null || list.isEmpty()) {
                histMdl.addRow(new Object[]{"— Tidak ada data —","","","","","","","",""});
                return;
            }
            for (Transaksi t : list) {
                histMdl.addRow(new Object[]{
                    t.getNoTransaksi(),
                    FormatUtil.formatDateTime(t.getTanggal()),
                    t.getNamaUser()    != null ? t.getNamaUser()    : "-",
                    t.getNamaCustomer()!= null ? t.getNamaCustomer(): "Umum",
                    FormatUtil.formatRupiah(t.getGrandTotal()),
                    FormatUtil.formatRupiah(t.getBayar()),
                    FormatUtil.formatRupiah(t.getKembalian()),
                    t.getMetode()  != null ? t.getMetode()  : "-",
                    t.getStatus()  != null ? t.getStatus()  : "SELESAI"
                });
            }
        } catch (Exception ex) {
            histMdl.addRow(new Object[]{"Error: " + ex.getMessage(),"","","","","","","",""});
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // CART LOGIC
    // ═══════════════════════════════════════════════════════════════
    private void addItemByKode(String kode) {
        if (kode == null || kode.isEmpty()) return;
        Barang b = barangCtrl.getByKode(kode);
        if (b == null) {
            List<Barang> results = barangCtrl.searchBarang(kode);
            if (results.isEmpty()) { AlertUtil.showWarning(this, "Barang tidak ditemukan: " + kode); return; }
            b = results.get(0);
        }
        addItemToCart(b);
        txtSearch.setText("");
    }

    private void addItemToCart(Barang b) {
        if (b.getStok() <= 0) { AlertUtil.showWarning(this, "Stok barang '" + b.getNamaBarang() + "' habis!"); return; }
        // Cek sudah ada di cart?
        for (int i = 0; i < cartMdl.getRowCount(); i++) {
            if (cartMdl.getValueAt(i, 1).equals(b.getKodeBarang())) {
                int q = Integer.parseInt(cartMdl.getValueAt(i, 4).toString()) + 1;
                if (q > b.getStok()) { AlertUtil.showWarning(this, "Stok tidak mencukupi!"); return; }
                cartMdl.setValueAt(q, i, 4);
                cartMdl.setValueAt(FormatUtil.formatRupiah(q * b.getHargaJual()), i, 5);
                hitungTotal(); txtSearch.setText(""); return;
            }
        }
        cartMdl.addRow(new Object[]{
            cartMdl.getRowCount() + 1,
            b.getKodeBarang(),
            b.getNamaBarang(),
            FormatUtil.formatRupiah(b.getHargaJual()),
            1,
            FormatUtil.formatRupiah(b.getHargaJual())
        });
        hitungTotal();
        txtSearch.setText("");
    }

    private void delItem() {
        int row = cart.getSelectedRow();
        if (row < 0) { AlertUtil.showWarning(this, "Pilih item yang ingin dihapus!"); return; }
        cartMdl.removeRow(row);
        for (int i = 0; i < cartMdl.getRowCount(); i++) cartMdl.setValueAt(i+1, i, 0);
        hitungTotal();
    }

    // ═══════════════════════════════════════════════════════════════
    // HITUNG TOTAL & KEMBALIAN
    // ═══════════════════════════════════════════════════════════════
    private void hitungTotal() {
        long sub = 0;
        for (int i = 0; i < cartMdl.getRowCount(); i++) {
            // FIX: parseDouble sekarang buang SEMUA non-digit → "Rp 13.000" = 13000
            sub += (long) FormatUtil.parseDouble(cartMdl.getValueAt(i, 5).toString());
        }
        long dis   = (long) FormatUtil.parseDouble(txtDiskon.getText());
        long grand = Math.max(0, sub - dis);

        lblSub.setText(FormatUtil.formatRupiah(sub));
        lblGrand.setText(FormatUtil.formatRupiah(grand));
        hitungKembalian();
    }

    /**
     * FIX KEMBALIAN:
     *   Gunakan long (bilangan bulat) karena Rupiah tidak punya desimal.
     *   BigDecimal.valueOf dipakai agar pengurangan sempurna tanpa floating-point error.
     *
     *   Contoh sebelumnya (bug):
     *     grand = parseDouble("Rp 13.000") = 13.0   (titik dikira desimal!)
     *     bayar = parseDouble("15000")    = 15000
     *     kembalian = 15000 - 13.0        = 14987   SALAH
     *
     *   Sekarang (fix):
     *     grand = parseDouble("Rp 13.000") = 13000  (titik dibuang, hanya digit)
     *     bayar = parseDouble("15000")    = 15000
     *     kembalian = 15000 - 13000        = 2000   BENAR
     */
    private void hitungKembalian() {
        long grand = (long) FormatUtil.parseDouble(lblGrand.getText());
        long bayar = (long) FormatUtil.parseDouble(txtBayar.getText());

        long kembalian = bayar - grand;

        if (kembalian >= 0) {
            lblKembalian.setText(FormatUtil.formatRupiah(kembalian));
            lblKembalian.setForeground(UITheme.ACCENT_TEAL);
        } else {
            lblKembalian.setText("Kurang: " + FormatUtil.formatRupiah(-kembalian));
            lblKembalian.setForeground(UITheme.ACCENT_CORAL);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // PROSES TRANSAKSI
    // ═══════════════════════════════════════════════════════════════
    private void proses() {
        if (cartMdl.getRowCount() == 0) {
            AlertUtil.showWarning(this, "Keranjang kosong! Tambahkan barang dulu."); return;
        }
        long grand = (long) FormatUtil.parseDouble(lblGrand.getText());
        long bayar = (long) FormatUtil.parseDouble(txtBayar.getText());
        if (bayar < grand) {
            AlertUtil.showWarning(this,
                "Jumlah bayar kurang!\nTotal:  " + FormatUtil.formatRupiah(grand) +
                "\nBayar:  " + FormatUtil.formatRupiah(bayar) +
                "\nKurang: " + FormatUtil.formatRupiah(grand - bayar));
            return;
        }

        trx.setDiskon(FormatUtil.parseDouble(txtDiskon.getText()));
        trx.setGrandTotal(grand);
        trx.setBayar(bayar);
        trx.setKembalian(bayar - grand);
        trx.setMetode(cmbMetode.getSelectedItem().toString().split(" ")[0]);
        if (trx.getCustomerId() <= 0) trx.setCustomerId(DEFAULT_CUSTOMER_ID);

        for (int i = 0; i < cartMdl.getRowCount(); i++) {
            Barang bar = barangCtrl.getByKode(cartMdl.getValueAt(i, 1).toString());
            if (bar == null) continue;
            int qty = FormatUtil.parseInt(cartMdl.getValueAt(i, 4).toString());
            TransaksiDetail d = new TransaksiDetail(
                bar.getId(), bar.getKodeBarang(), bar.getNamaBarang(), qty, bar.getHargaJual());
            d.hitungSubtotal();
            trx.addDetail(d);
        }

        if (new TransaksiController().simpanPenjualan(trx)) {
            AlertUtil.showInfo(this,
                "Transaksi Berhasil!\n" +
                "No: " + trx.getNoTransaksi() + "\n" +
                "Total:      " + FormatUtil.formatRupiah(grand) + "\n" +
                "Bayar:      " + FormatUtil.formatRupiah(bayar) + "\n" +
                "Kembalian:  " + FormatUtil.formatRupiah(bayar - grand));
            reset();
        } else {
            AlertUtil.showError(this, "Gagal menyimpan transaksi!");
        }
    }

    private void reset() {
        trx = new Transaksi();
        cartMdl.setRowCount(0);
        lblNo.setText("Order No: " + new TransaksiController().generateNoTransaksi("TRX"));
        lblSub.setText("Rp 0"); lblGrand.setText("Rp 0");
        lblKembalian.setText("Rp 0"); lblKembalian.setForeground(UITheme.ACCENT_TEAL);
        txtBayar.setText("0"); txtDiskon.setText("0"); txtSearch.setText("");
        cmbMetode.setSelectedIndex(0);
        hideSearchPopup();
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════
    private JLabel lbl(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font); l.setForeground(color); l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private DefaultTableCellRenderer cartRenderer() {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component cp = super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                cp.setBackground(sel ? new Color(230,242,255) : (r%2==0 ? UITheme.BG_CARD : UITheme.BG_ROW_ALT));
                cp.setForeground(c == 5 ? UITheme.ACCENT_TEAL : UITheme.TEXT_PRIMARY);
                if (c == 3 || c == 5) ((JLabel)cp).setHorizontalAlignment(SwingConstants.RIGHT);
                if (c == 4) ((JLabel)cp).setHorizontalAlignment(SwingConstants.CENTER);
                ((JLabel)cp).setBorder(new EmptyBorder(0,10,0,10));
                return cp;
            }
        };
    }
}
