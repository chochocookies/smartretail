package com.app.smartretail.view.transaksi;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.app.smartretail.controller.BarangController;
import com.app.smartretail.controller.TransaksiController;
import com.app.smartretail.model.Barang;
import com.app.smartretail.model.Transaksi;
import com.app.smartretail.model.TransaksiDetail;
import com.app.smartretail.utils.AlertUtil;
import com.app.smartretail.utils.FormatUtil;
import com.app.smartretail.utils.UITheme;

public class PenjualanForm extends JPanel {

    private final TransaksiController transaksiCtrl = new TransaksiController();
    private final BarangController barangCtrl       = new BarangController();

    private JTextField txtScan, txtBayar, txtDiskon;
    private JLabel lblNo, lblSubtotal, lblDiskonAmt, lblTotal, lblKembalian;
    private JTable cartTable;
    private DefaultTableModel cartModel;
    private JComboBox<String> cmbMetode;
    private JButton btnProses, btnReset, btnHapusItem;

    private Transaksi currentTrx;

    public PenjualanForm() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.BG_DARK);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        build();
        resetTrx();
    }

    private void build() {
        // Header
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.setBorder(new EmptyBorder(0, 0, 20, 0));
        JLabel title = UITheme.pageTitle("Point of Sale");
        JLabel sub = new JLabel("Transaksi penjualan kasir");
        sub.setFont(UITheme.FONT_BODY); sub.setForeground(UITheme.TEXT_SECONDARY);
        JPanel ht = new JPanel(); ht.setOpaque(false);
        ht.setLayout(new BoxLayout(ht, BoxLayout.Y_AXIS));
        ht.add(title); ht.add(sub);
        lblNo = new JLabel("No: —");
        lblNo.setFont(UITheme.FONT_MONO); lblNo.setForeground(UITheme.ACCENT_BLUE);
        hdr.add(ht, BorderLayout.WEST);
        hdr.add(lblNo, BorderLayout.EAST);
        add(hdr, BorderLayout.NORTH);

        // ── MAIN area ─────────────────────────────────────────────────
        JPanel main = new JPanel(new BorderLayout(20, 0));
        main.setOpaque(false);

        // ── LEFT: Scan + Cart ─────────────────────────────────────────
        JPanel leftPanel = new JPanel(new BorderLayout(0, 12));
        leftPanel.setOpaque(false);

        // Scan input
        JPanel scanRow = new JPanel(new BorderLayout(10, 0));
        scanRow.setOpaque(false);
        txtScan = UITheme.styledField("Scan / ketik kode barang → Enter");
        txtScan.setPreferredSize(new Dimension(0, 44));
        txtScan.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JButton btnAdd = UITheme.primaryButton("+ Tambah", UITheme.ACCENT_BLUE);
        btnAdd.setPreferredSize(new Dimension(110, 44));
        scanRow.add(txtScan, BorderLayout.CENTER);
        scanRow.add(btnAdd, BorderLayout.EAST);

        // Cart table
        String[] cols = {"#", "Kode", "Nama Barang", "Harga", "Qty", "Subtotal", ""};
        cartModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return c == 4; }
        };
        cartTable = new JTable(cartModel);
        UITheme.styleTable(cartTable);
        cartTable.setRowHeight(40);
        cartTable.getColumnModel().getColumn(0).setMaxWidth(36);
        cartTable.getColumnModel().getColumn(1).setMaxWidth(90);
        cartTable.getColumnModel().getColumn(4).setMaxWidth(60);
        cartTable.getColumnModel().getColumn(6).setMaxWidth(36);
        cartTable.setDefaultRenderer(Object.class, cartRenderer());

        // Qty cell editor — spinner style
        JTextField qtyEditor = UITheme.styledField("");
        qtyEditor.setHorizontalAlignment(SwingConstants.CENTER);
        cartTable.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(qtyEditor) {
            public boolean stopCellEditing() {
                boolean ok = super.stopCellEditing();
                if (ok) recalcCart();
                return ok;
            }
        });

        JPanel cartTools = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        cartTools.setOpaque(false);
        btnHapusItem = UITheme.dangerButton("Hapus Item");
        JButton btnClear = UITheme.ghostButton("Kosongkan", UITheme.TEXT_MUTED);
        cartTools.add(btnHapusItem); cartTools.add(btnClear);

        leftPanel.add(scanRow, BorderLayout.NORTH);
        leftPanel.add(UITheme.styledScroll(cartTable), BorderLayout.CENTER);
        leftPanel.add(cartTools, BorderLayout.SOUTH);

        // ── RIGHT: Summary + Payment ──────────────────────────────────
        JPanel rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(290, 0));
        rightPanel.setMaximumSize(new Dimension(290, Integer.MAX_VALUE));

        // Summary card
        JPanel sumCard = UITheme.card();
        sumCard.setLayout(new BoxLayout(sumCard, BoxLayout.Y_AXIS));
        sumCard.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lSum = new JLabel("Ringkasan");
        lSum.setFont(UITheme.FONT_H2); lSum.setForeground(UITheme.TEXT_PRIMARY);
        lSum.setAlignmentX(LEFT_ALIGNMENT);

        lblSubtotal = sumLabel("Subtotal", "Rp 0", UITheme.TEXT_SECONDARY);
        lblDiskonAmt= sumLabel("Diskon", "Rp 0", UITheme.ACCENT_AMBER);
        lblTotal    = sumLabel("Total", "Rp 0", UITheme.TEXT_PRIMARY);

        sumCard.add(lSum);
        sumCard.add(Box.createVerticalStrut(12));
        sumCard.add(UITheme.separator());
        sumCard.add(Box.createVerticalStrut(10));
        addSumRow(sumCard, "Subtotal", lblSubtotal);
        addSumRow(sumCard, "Diskon (Rp)", makeDiskonField());
        sumCard.add(UITheme.separator());
        addSumRow(sumCard, "Grand Total", lblTotal);

        // Payment card
        JPanel payCard = UITheme.card();
        payCard.setLayout(new BoxLayout(payCard, BoxLayout.Y_AXIS));
        payCard.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lPay = new JLabel("Pembayaran");
        lPay.setFont(UITheme.FONT_H2); lPay.setForeground(UITheme.TEXT_PRIMARY);
        lPay.setAlignmentX(LEFT_ALIGNMENT);

        cmbMetode = UITheme.styledCombo(new String[]{"TUNAI", "KARTU DEBIT", "KARTU KREDIT", "TRANSFER"});
        cmbMetode.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cmbMetode.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lBayar = UITheme.fieldLabel("Jumlah Diterima");
        lBayar.setAlignmentX(LEFT_ALIGNMENT);
        txtBayar = UITheme.styledField("0");
        txtBayar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        txtBayar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        txtBayar.setAlignmentX(LEFT_ALIGNMENT);

        lblKembalian = new JLabel("Rp 0");
        lblKembalian.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblKembalian.setForeground(UITheme.ACCENT_TEAL);
        lblKembalian.setAlignmentX(LEFT_ALIGNMENT);

        payCard.add(lPay);
        payCard.add(Box.createVerticalStrut(10));
        payCard.add(UITheme.fieldLabel("Metode Pembayaran"));
        payCard.add(Box.createVerticalStrut(6));
        payCard.add(cmbMetode);
        payCard.add(Box.createVerticalStrut(12));
        payCard.add(lBayar);
        payCard.add(Box.createVerticalStrut(6));
        payCard.add(txtBayar);
        payCard.add(Box.createVerticalStrut(12));
        payCard.add(UITheme.fieldLabel("Kembalian"));
        payCard.add(Box.createVerticalStrut(6));
        payCard.add(lblKembalian);

        // Action buttons
        btnProses = UITheme.primaryButton("✓  Proses Transaksi", UITheme.ACCENT_TEAL);
        btnProses.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnProses.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        btnProses.setAlignmentX(LEFT_ALIGNMENT);

        btnReset = UITheme.ghostButton("Reset / Batal", UITheme.ACCENT_CORAL);
        btnReset.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnReset.setAlignmentX(LEFT_ALIGNMENT);

        rightPanel.add(sumCard);
        rightPanel.add(Box.createVerticalStrut(12));
        rightPanel.add(payCard);
        rightPanel.add(Box.createVerticalStrut(14));
        rightPanel.add(btnProses);
        rightPanel.add(Box.createVerticalStrut(8));
        rightPanel.add(btnReset);

        main.add(leftPanel, BorderLayout.CENTER);
        main.add(rightPanel, BorderLayout.EAST);
        add(main, BorderLayout.CENTER);

        // Events
        btnAdd.addActionListener(e -> addItem());
        txtScan.addActionListener(e -> addItem());
        btnHapusItem.addActionListener(e -> hapusItem());
        btnClear.addActionListener(e -> { if (AlertUtil.showConfirm(this,"Kosongkan keranjang?")) { cartModel.setRowCount(0); hitungTotal(); } });
        btnProses.addActionListener(e -> proses());
        btnReset.addActionListener(e -> { if (AlertUtil.showConfirm(this,"Reset transaksi?")) resetTrx(); });
        txtBayar.addKeyListener(new KeyAdapter() { public void keyReleased(KeyEvent e) { hitungKembalian(); } });
    }

    private JPanel makeDiskonField() {
        txtDiskon = UITheme.styledField("0");
        txtDiskon.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        txtDiskon.addKeyListener(new KeyAdapter() { public void keyReleased(KeyEvent e) { hitungTotal(); } });
        return wrapField(txtDiskon);
    }
    private JPanel wrapField(JTextField f) {
        JPanel p = new JPanel(new BorderLayout()); p.setOpaque(false); p.add(f); return p;
    }

    private JLabel sumLabel(String id, String val, Color c) {
        JLabel l = new JLabel(val);
        l.setFont(id.equals("Total") ? new Font("Segoe UI",Font.BOLD,18) : UITheme.FONT_BODY);
        l.setForeground(c);
        return l;
    }

    private void addSumRow(JPanel p, String label, Component val) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        JLabel lbl = UITheme.fieldLabel(label);
        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        p.add(row);
        p.add(Box.createVerticalStrut(6));
    }

    // ─── Cart Logic ───────────────────────────────────────────────────
    private void addItem() {
        String kode = txtScan.getText().trim();
        if (kode.isEmpty()) return;
        Barang b = barangCtrl.getByKode(kode);
        if (b == null) {
            List<Barang> res = barangCtrl.searchBarang(kode);
            if (res.isEmpty()) { AlertUtil.showWarning(this, "Barang \"" + kode + "\" tidak ditemukan!"); txtScan.setText(""); return; }
            if (res.size() == 1) { b = res.get(0); }
            else {
                String[] opts = res.stream().map(x -> x.getKodeBarang() + " — " + x.getNamaBarang()).toArray(String[]::new);
                String sel = (String) JOptionPane.showInputDialog(this, "Pilih barang:", "Pilih",
                    JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]);
                if (sel == null) { txtScan.setText(""); return; }
                b = res.get(java.util.Arrays.asList(opts).indexOf(sel));
            }
        }
        if (b.getStok() <= 0) { AlertUtil.showWarning(this, "Stok habis!"); txtScan.setText(""); return; }

        for (int i = 0; i < cartModel.getRowCount(); i++) {
            if (cartModel.getValueAt(i,1).equals(b.getKodeBarang())) {
                int q = Integer.parseInt(cartModel.getValueAt(i,4).toString()) + 1;
                if (q > b.getStok()) { AlertUtil.showWarning(this,"Stok tidak mencukupi!"); return; }
                cartModel.setValueAt(q, i, 4);
                cartModel.setValueAt(FormatUtil.formatRupiah(q * b.getHargaJual()), i, 5);
                hitungTotal(); txtScan.setText(""); return;
            }
        }
        final Barang fb = b;
        cartModel.addRow(new Object[]{
            cartModel.getRowCount()+1, fb.getKodeBarang(), fb.getNamaBarang(),
            FormatUtil.formatRupiah(fb.getHargaJual()), 1,
            FormatUtil.formatRupiah(fb.getHargaJual()), "✕"
        });
        hitungTotal(); txtScan.setText("");
    }

    private void hapusItem() {
        int row = cartTable.getSelectedRow();
        if (row < 0) { AlertUtil.showWarning(this,"Pilih item terlebih dahulu!"); return; }
        cartModel.removeRow(row);
        for (int i=0;i<cartModel.getRowCount();i++) cartModel.setValueAt(i+1,i,0);
        hitungTotal();
    }

    private void recalcCart() {
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            String hStr = cartModel.getValueAt(i,3).toString().replaceAll("[^\\d]","");
            double h = hStr.isEmpty() ? 0 : Double.parseDouble(hStr);
            int q = FormatUtil.parseInt(cartModel.getValueAt(i,4).toString());
            cartModel.setValueAt(FormatUtil.formatRupiah(h*q), i, 5);
        }
        hitungTotal();
    }

    private void hitungTotal() {
        double sub = 0;
        for (int i=0;i<cartModel.getRowCount();i++) {
            String s = cartModel.getValueAt(i,5).toString().replaceAll("[^\\d]","");
            sub += s.isEmpty() ? 0 : Double.parseDouble(s);
        }
        double diskon = FormatUtil.parseDouble(txtDiskon.getText());
        double grand  = Math.max(0, sub - diskon);
        lblSubtotal.setText(FormatUtil.formatRupiah(sub));
        lblDiskonAmt.setText(FormatUtil.formatRupiah(diskon));
        lblTotal.setText(FormatUtil.formatRupiah(grand));
        hitungKembalian();
    }

    private void hitungKembalian() {
        double grand = FormatUtil.parseDouble(lblTotal.getText());
        double bayar = FormatUtil.parseDouble(txtBayar.getText());
        double kem   = bayar - grand;
        lblKembalian.setText(FormatUtil.formatRupiah(kem));
        lblKembalian.setForeground(kem >= 0 ? UITheme.ACCENT_TEAL : UITheme.ACCENT_CORAL);
    }

    private void proses() {
        if (cartModel.getRowCount() == 0) { AlertUtil.showWarning(this,"Keranjang kosong!"); return; }
        double grand = FormatUtil.parseDouble(lblTotal.getText());
        double bayar = FormatUtil.parseDouble(txtBayar.getText());
        if (bayar < grand) { AlertUtil.showWarning(this,"Jumlah bayar kurang dari total!"); return; }

        currentTrx.setDiskon(FormatUtil.parseDouble(txtDiskon.getText()));
        currentTrx.setMetode(cmbMetode.getSelectedItem().toString());
        currentTrx.setBayar(bayar);

        for (int i=0;i<cartModel.getRowCount();i++) {
            Barang b = barangCtrl.getByKode(cartModel.getValueAt(i,1).toString());
            if (b == null) continue;
            int q = FormatUtil.parseInt(cartModel.getValueAt(i,4).toString());
            TransaksiDetail d = new TransaksiDetail(b.getId(), b.getKodeBarang(), b.getNamaBarang(), q, b.getHargaJual());
            d.hitungSubtotal();
            currentTrx.addDetail(d);
        }

        if (transaksiCtrl.simpanPenjualan(currentTrx)) {
            String kemStr = FormatUtil.formatRupiah(bayar - grand);
            JOptionPane.showMessageDialog(this,
                "<html><div style='font-family:Segoe UI;padding:10px'>" +
                "<p style='font-size:14px;font-weight:bold;color:#34D399'>✓ Transaksi Berhasil!</p>" +
                "<p>No: <b>" + currentTrx.getNoTransaksi() + "</b></p>" +
                "<p>Total: <b>" + FormatUtil.formatRupiah(grand) + "</b></p>" +
                "<p>Kembalian: <b>" + kemStr + "</b></p>" +
                "</div></html>",
                "Transaksi Selesai", JOptionPane.PLAIN_MESSAGE);
            resetTrx();
        } else AlertUtil.showError(this, "Gagal menyimpan transaksi!");
    }

    private void resetTrx() {
        currentTrx = new Transaksi();
        cartModel.setRowCount(0);
        lblNo.setText("No: " + transaksiCtrl.generateNoTransaksi("TRX"));
        lblSubtotal.setText("Rp 0"); lblDiskonAmt.setText("Rp 0");
        lblTotal.setText("Rp 0"); lblKembalian.setText("Rp 0");
        txtBayar.setText(""); txtDiskon.setText("0"); txtScan.setText("");
        cmbMetode.setSelectedIndex(0);
        lblKembalian.setForeground(UITheme.ACCENT_TEAL);
    }

    private DefaultTableCellRenderer cartRenderer() {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                comp.setBackground(sel ? new Color(82,130,255,50) :
                    (r%2==0 ? UITheme.BG_CARD : UITheme.BG_ROW_ALT));
                comp.setForeground(c==6 ? UITheme.ACCENT_CORAL : UITheme.TEXT_PRIMARY);
                ((JLabel)comp).setBorder(new EmptyBorder(0,10,0,10));
                if (c==4) ((JLabel)comp).setHorizontalAlignment(SwingConstants.CENTER);
                if (c==5) comp.setForeground(UITheme.ACCENT_TEAL);
                return comp;
            }
        };
    }
}
