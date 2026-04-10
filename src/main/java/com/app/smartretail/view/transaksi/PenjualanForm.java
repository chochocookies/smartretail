package com.app.smartretail.view.transaksi;

import com.app.smartretail.controller.BarangController;
import com.app.smartretail.controller.TransaksiController;
import com.app.smartretail.model.Barang;
import com.app.smartretail.model.Transaksi;
import com.app.smartretail.model.TransaksiDetail;
import com.app.smartretail.utils.AlertUtil;
import com.app.smartretail.utils.FormatUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * PenjualanForm - Point of Sale (POS)
 */
public class PenjualanForm extends JPanel {

    private TransaksiController transaksiCtrl;
    private BarangController barangCtrl;

    private JTextField txtKodeBarang, txtBayar, txtDiskon;
    private JLabel lblNoTransaksi, lblTotal, lblKembalian, lblGrandTotal;
    private JTable tblCart;
    private DefaultTableModel cartModel;
    private JComboBox<String> cmbMetode;
    private JButton btnCariBarang, btnHapusItem, btnProses, btnReset;

    private Transaksi currentTransaksi;

    public PenjualanForm() {
        transaksiCtrl = new TransaksiController();
        barangCtrl    = new BarangController();
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 247, 250));
        initComponents();
        resetTransaksi();
    }

    private void initComponents() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("🛒 Penjualan (POS)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(30, 55, 95));
        lblNoTransaksi = new JLabel("No: -");
        lblNoTransaksi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNoTransaksi.setForeground(Color.GRAY);
        header.add(title, BorderLayout.WEST);
        header.add(lblNoTransaksi, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ---- CENTER: Cart table ----
        String[] cols = {"#", "Kode", "Nama Barang", "Harga Satuan", "Qty", "Subtotal"};
        cartModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return c == 4; } // qty editable
        };
        tblCart = new JTable(cartModel);
        tblCart.setRowHeight(30);
        tblCart.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblCart.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblCart.getTableHeader().setBackground(new Color(30, 55, 95));
        tblCart.getTableHeader().setForeground(Color.WHITE);
        tblCart.getColumnModel().getColumn(0).setMaxWidth(35);

        add(new JScrollPane(tblCart), BorderLayout.CENTER);

        // ---- SOUTH: Input + Summary ----
        JPanel south = new JPanel(new BorderLayout(10, 0));
        south.setOpaque(false);

        // Input row
        JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        inputRow.setOpaque(false);
        inputRow.setBorder(BorderFactory.createTitledBorder("Tambah Barang"));

        txtKodeBarang = new JTextField(14);
        txtKodeBarang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtKodeBarang.putClientProperty("JTextField.placeholderText", "Kode / scan barcode...");
        btnCariBarang = new JButton("➕ Tambah");
        styleBtn(btnCariBarang, new Color(46, 204, 113));

        inputRow.add(new JLabel("Kode Barang:"));
        inputRow.add(txtKodeBarang);
        inputRow.add(btnCariBarang);
        inputRow.add(Box.createHorizontalStrut(10));

        btnHapusItem = new JButton("🗑️ Hapus Item");
        styleBtn(btnHapusItem, new Color(231, 76, 60));
        inputRow.add(btnHapusItem);

        // Summary panel
        JPanel summaryPanel = new JPanel(new GridLayout(0, 2, 8, 6));
        summaryPanel.setBackground(Color.WHITE);
        summaryPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            new EmptyBorder(12, 15, 12, 15)));
        summaryPanel.setPreferredSize(new Dimension(380, 180));

        lblTotal = new JLabel("Rp 0");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));

        txtDiskon = new JTextField("0");
        txtDiskon.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        lblGrandTotal = new JLabel("Rp 0");
        lblGrandTotal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblGrandTotal.setForeground(new Color(30, 55, 95));

        txtBayar = new JTextField("0");
        txtBayar.setFont(new Font("Segoe UI", Font.BOLD, 14));

        lblKembalian = new JLabel("Rp 0");
        lblKembalian.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblKembalian.setForeground(new Color(46, 204, 113));

        cmbMetode = new JComboBox<>(new String[]{"TUNAI", "KARTU", "TRANSFER"});
        cmbMetode.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        addRow(summaryPanel, "Total Harga:", lblTotal);
        addRow(summaryPanel, "Diskon (Rp):", txtDiskon);
        addRow(summaryPanel, "Grand Total:", lblGrandTotal);
        addRow(summaryPanel, "Metode Bayar:", cmbMetode);
        addRow(summaryPanel, "Jumlah Bayar:", txtBayar);
        addRow(summaryPanel, "Kembalian:", lblKembalian);

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        btnPanel.setOpaque(false);
        btnProses = new JButton("✅ PROSES TRANSAKSI");
        btnReset  = new JButton("🔄 RESET");
        styleBtn(btnProses, new Color(30, 55, 95));
        styleBtn(btnReset,  new Color(149, 165, 166));
        btnProses.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnPanel.add(btnReset);
        btnPanel.add(btnProses);

        JPanel rightSouth = new JPanel(new BorderLayout(0, 8));
        rightSouth.setOpaque(false);
        rightSouth.add(summaryPanel, BorderLayout.CENTER);
        rightSouth.add(btnPanel, BorderLayout.SOUTH);

        south.add(inputRow, BorderLayout.CENTER);
        south.add(rightSouth, BorderLayout.EAST);
        add(south, BorderLayout.SOUTH);

        // Events
        btnCariBarang.addActionListener(e -> tambahBarang());
        txtKodeBarang.addActionListener(e -> tambahBarang());
        btnHapusItem.addActionListener(e -> hapusItem());
        btnProses.addActionListener(e -> prosesTransaksi());
        btnReset.addActionListener(e -> resetTransaksi());
        txtBayar.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { hitungKembalian(); }
        });
        txtDiskon.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { hitungTotal(); }
        });
    }

    private void tambahBarang() {
        String kode = txtKodeBarang.getText().trim();
        if (kode.isEmpty()) {
            // Open search dialog
            String input = JOptionPane.showInputDialog(this, "Masukkan kode atau nama barang:");
            if (input == null || input.isBlank()) return;
            kode = input.trim();
        }
        Barang b = barangCtrl.getByKode(kode);
        if (b == null) {
            // Try searching by name
            List<Barang> list = barangCtrl.searchBarang(kode);
            if (list.isEmpty()) { AlertUtil.showWarning(this, "Barang tidak ditemukan: " + kode); return; }
            if (list.size() == 1) { b = list.get(0); }
            else {
                String[] opts = list.stream().map(Barang::toString).toArray(String[]::new);
                String sel = (String) JOptionPane.showInputDialog(this, "Pilih barang:", "Pilih",
                    JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]);
                if (sel == null) return;
                b = list.stream().filter(x -> x.toString().equals(sel)).findFirst().orElse(null);
            }
        }
        if (b == null) return;
        if (b.getStok() <= 0) { AlertUtil.showWarning(this, "Stok barang habis!"); return; }

        // Check if already in cart
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            if (cartModel.getValueAt(i, 1).equals(b.getKodeBarang())) {
                int qty = Integer.parseInt(cartModel.getValueAt(i, 4).toString()) + 1;
                if (qty > b.getStok()) { AlertUtil.showWarning(this, "Stok tidak mencukupi!"); return; }
                cartModel.setValueAt(qty, i, 4);
                cartModel.setValueAt(FormatUtil.formatRupiah(qty * b.getHargaJual()), i, 5);
                hitungTotal(); txtKodeBarang.setText(""); return;
            }
        }

        // Add new row
        int row = cartModel.getRowCount() + 1;
        cartModel.addRow(new Object[]{
            row, b.getKodeBarang(), b.getNamaBarang(),
            FormatUtil.formatRupiah(b.getHargaJual()), 1,
            FormatUtil.formatRupiah(b.getHargaJual())
        });
        hitungTotal();
        txtKodeBarang.setText("");
    }

    private void hapusItem() {
        int row = tblCart.getSelectedRow();
        if (row < 0) { AlertUtil.showWarning(this, "Pilih item yang akan dihapus!"); return; }
        cartModel.removeRow(row);
        // Renumber
        for (int i = 0; i < cartModel.getRowCount(); i++) cartModel.setValueAt(i + 1, i, 0);
        hitungTotal();
    }

    private void hitungTotal() {
        double total = 0;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            String sub = cartModel.getValueAt(i, 5).toString().replaceAll("[^\\d]", "");
            total += sub.isEmpty() ? 0 : Double.parseDouble(sub);
        }
        double diskon = FormatUtil.parseDouble(txtDiskon.getText());
        double grand  = total - diskon;
        if (grand < 0) grand = 0;
        lblTotal.setText(FormatUtil.formatRupiah(total));
        lblGrandTotal.setText(FormatUtil.formatRupiah(grand));
        hitungKembalian();
    }

    private void hitungKembalian() {
        double grand = FormatUtil.parseDouble(lblGrandTotal.getText());
        double bayar = FormatUtil.parseDouble(txtBayar.getText());
        double kembalian = bayar - grand;
        lblKembalian.setText(FormatUtil.formatRupiah(kembalian));
        lblKembalian.setForeground(kembalian >= 0 ? new Color(46, 204, 113) : Color.RED);
    }

    private void prosesTransaksi() {
        if (cartModel.getRowCount() == 0) { AlertUtil.showWarning(this, "Keranjang masih kosong!"); return; }
        double grand = FormatUtil.parseDouble(lblGrandTotal.getText());
        double bayar = FormatUtil.parseDouble(txtBayar.getText());
        if (bayar < grand) { AlertUtil.showWarning(this, "Jumlah bayar kurang dari grand total!"); return; }

        currentTransaksi.setDiskon(FormatUtil.parseDouble(txtDiskon.getText()));
        currentTransaksi.setMetode((String) cmbMetode.getSelectedItem());
        currentTransaksi.setBayar(bayar);

        for (int i = 0; i < cartModel.getRowCount(); i++) {
            Barang b = barangCtrl.getByKode(cartModel.getValueAt(i, 1).toString());
            if (b == null) continue;
            int qty = Integer.parseInt(cartModel.getValueAt(i, 4).toString());
            TransaksiDetail d = new TransaksiDetail(b.getId(), b.getKodeBarang(), b.getNamaBarang(), qty, b.getHargaJual());
            d.hitungSubtotal();
            currentTransaksi.addDetail(d);
        }

        if (transaksiCtrl.simpanPenjualan(currentTransaksi)) {
            AlertUtil.showInfo(this, "✅ Transaksi berhasil!\nNo: " + currentTransaksi.getNoTransaksi() +
                "\nKembalian: " + FormatUtil.formatRupiah(bayar - grand));
            resetTransaksi();
        } else {
            AlertUtil.showError(this, "Gagal menyimpan transaksi!");
        }
    }

    private void resetTransaksi() {
        currentTransaksi = new Transaksi();
        cartModel.setRowCount(0);
        lblNoTransaksi.setText("No: " + transaksiCtrl.generateNoTransaksi("TRX"));
        lblTotal.setText("Rp 0"); lblGrandTotal.setText("Rp 0"); lblKembalian.setText("Rp 0");
        txtBayar.setText("0"); txtDiskon.setText("0"); txtKodeBarang.setText("");
        cmbMetode.setSelectedIndex(0);
    }

    private void addRow(JPanel p, String label, Component comp) {
        JLabel l = new JLabel(label); l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        p.add(l); p.add(comp);
    }

    private void styleBtn(JButton b, Color c) {
        b.setBackground(c); b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 11));
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}
