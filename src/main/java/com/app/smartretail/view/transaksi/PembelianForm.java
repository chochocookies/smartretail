package com.app.smartretail.view.transaksi;

import com.app.smartretail.controller.BarangController;
import com.app.smartretail.controller.TransaksiController;
import com.app.smartretail.dao.SupplierDAO;
import com.app.smartretail.model.*;
import com.app.smartretail.utils.AlertUtil;
import com.app.smartretail.utils.FormatUtil;
import com.app.smartretail.utils.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class PembelianForm extends JPanel {

    private final TransaksiController transaksiCtrl = new TransaksiController();
    private final BarangController barangCtrl       = new BarangController();
    private final SupplierDAO supplierDAO           = new SupplierDAO();

    private JTextField txtKode, txtBayar;
    private JLabel lblNo, lblTotal, lblGrandTotal;
    private JTable cartTable;
    private DefaultTableModel cartModel;
    private JComboBox<String> cmbSupplier;
    private JButton btnProses, btnReset;
    private Transaksi currentTrx;

    public PembelianForm() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.BG_SURFACE);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        build();
        resetTrx();
    }

    private void build() {
        // Header
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.setBorder(new EmptyBorder(0, 0, 20, 0));
        JLabel title = UITheme.pageTitle("Pembelian Barang");
        JLabel sub = new JLabel("Input barang masuk dari supplier");
        sub.setFont(UITheme.FONT_BODY); sub.setForeground(UITheme.TEXT_SECONDARY);
        JPanel ht = new JPanel(); ht.setOpaque(false);
        ht.setLayout(new BoxLayout(ht, BoxLayout.Y_AXIS));
        ht.add(title); ht.add(sub);
        lblNo = new JLabel("No: —");
        lblNo.setFont(UITheme.FONT_MONO); lblNo.setForeground(UITheme.ACCENT_PURPLE);
        hdr.add(ht, BorderLayout.WEST);
        hdr.add(lblNo, BorderLayout.EAST);
        add(hdr, BorderLayout.NORTH);

        JPanel main = new JPanel(new BorderLayout(20, 0));
        main.setOpaque(false);

        // Left
        JPanel left = new JPanel(new BorderLayout(0, 12));
        left.setOpaque(false);

        // Supplier + kode row
        JPanel topRow = new JPanel(new GridLayout(1, 2, 12, 0));
        topRow.setOpaque(false);

        JPanel supPanel = new JPanel(new BorderLayout(0, 6));
        supPanel.setOpaque(false);
        supPanel.add(UITheme.fieldLabel("Supplier"), BorderLayout.NORTH);
        cmbSupplier = UITheme.styledCombo(new String[]{"— Pilih Supplier —"});
        for (Supplier s : supplierDAO.getAll()) cmbSupplier.addItem(s.getNamaSupplier());
        supPanel.add(cmbSupplier, BorderLayout.CENTER);

        JPanel kodePanel = new JPanel(new BorderLayout(0, 6));
        kodePanel.setOpaque(false);
        kodePanel.add(UITheme.fieldLabel("Kode Barang"), BorderLayout.NORTH);
        JPanel kodeRow = new JPanel(new BorderLayout(8, 0));
        kodeRow.setOpaque(false);
        txtKode = UITheme.styledField("Scan / kode barang → Enter");
        JButton btnAdd = UITheme.primaryButton("+ Tambah", UITheme.ACCENT_PURPLE);
        kodeRow.add(txtKode, BorderLayout.CENTER);
        kodeRow.add(btnAdd, BorderLayout.EAST);
        kodePanel.add(kodeRow, BorderLayout.CENTER);

        topRow.add(supPanel);
        topRow.add(kodePanel);

        // Cart
        String[] cols = {"#", "Kode", "Nama Barang", "Harga Beli", "Qty", "Subtotal"};
        cartModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return c == 4; }
        };
        cartTable = new JTable(cartModel);
        UITheme.styleTable(cartTable);
        cartTable.setRowHeight(38);
        cartTable.getColumnModel().getColumn(0).setMaxWidth(36);
        cartTable.getColumnModel().getColumn(1).setMaxWidth(90);
        cartTable.getColumnModel().getColumn(4).setMaxWidth(60);
        cartTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component cp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                cp.setBackground(sel ? new Color(130,82,255,50) : (r%2==0 ? UITheme.BG_CARD : UITheme.BG_ROW_ALT));
                cp.setForeground(c==5 ? UITheme.ACCENT_PURPLE : UITheme.TEXT_PRIMARY);
                ((JLabel)cp).setBorder(new EmptyBorder(0,10,0,10));
                return cp;
            }
        });

        JButton btnHapus = UITheme.dangerButton("Hapus Item");
        JPanel cartTools = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        cartTools.setOpaque(false); cartTools.add(btnHapus);

        left.add(topRow, BorderLayout.NORTH);
        left.add(UITheme.styledScroll(cartTable), BorderLayout.CENTER);
        left.add(cartTools, BorderLayout.SOUTH);

        // Right: summary
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setPreferredSize(new Dimension(280, 0));

        JPanel sumCard = UITheme.card();
        sumCard.setLayout(new BoxLayout(sumCard, BoxLayout.Y_AXIS));
        sumCard.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lSum = new JLabel("Ringkasan Pembelian");
        lSum.setFont(UITheme.FONT_H2); lSum.setForeground(UITheme.TEXT_PRIMARY);
        lSum.setAlignmentX(LEFT_ALIGNMENT);

        lblTotal     = new JLabel("Rp 0"); lblTotal.setFont(UITheme.FONT_BODY); lblTotal.setForeground(UITheme.TEXT_SECONDARY);
        lblGrandTotal= new JLabel("Rp 0"); lblGrandTotal.setFont(new Font("Segoe UI",Font.BOLD,20)); lblGrandTotal.setForeground(UITheme.ACCENT_PURPLE);

        JLabel lBayar = UITheme.fieldLabel("Jumlah Dibayar");
        lBayar.setAlignmentX(LEFT_ALIGNMENT);
        txtBayar = UITheme.styledField("0");
        txtBayar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        txtBayar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtBayar.setAlignmentX(LEFT_ALIGNMENT);

        sumCard.add(lSum);
        sumCard.add(Box.createVerticalStrut(10));
        sumCard.add(UITheme.separator());
        sumCard.add(Box.createVerticalStrut(10));
        addRow2(sumCard, "Total Barang", lblTotal);
        addRow2(sumCard, "Grand Total", lblGrandTotal);
        sumCard.add(Box.createVerticalStrut(12));
        sumCard.add(lBayar);
        sumCard.add(Box.createVerticalStrut(6));
        sumCard.add(txtBayar);

        btnProses = UITheme.primaryButton("✓  Simpan Pembelian", UITheme.ACCENT_PURPLE);
        btnProses.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnProses.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btnProses.setAlignmentX(LEFT_ALIGNMENT);

        btnReset = UITheme.ghostButton("Reset", UITheme.TEXT_MUTED);
        btnReset.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnReset.setAlignmentX(LEFT_ALIGNMENT);

        right.add(sumCard);
        right.add(Box.createVerticalStrut(12));
        right.add(btnProses);
        right.add(Box.createVerticalStrut(8));
        right.add(btnReset);

        main.add(left, BorderLayout.CENTER);
        main.add(right, BorderLayout.EAST);
        add(main, BorderLayout.CENTER);

        // Events
        btnAdd.addActionListener(e -> addItem());
        txtKode.addActionListener(e -> addItem());
        btnHapus.addActionListener(e -> hapus());
        btnProses.addActionListener(e -> proses());
        btnReset.addActionListener(e -> resetTrx());
    }

    private void addItem() {
        String kode = txtKode.getText().trim();
        if (kode.isEmpty()) return;
        Barang b = barangCtrl.getByKode(kode);
        if (b == null) {
            AlertUtil.showWarning(this, "Barang tidak ditemukan: " + kode);
            txtKode.setText(""); return;
        }
        for (int i=0;i<cartModel.getRowCount();i++) {
            if (cartModel.getValueAt(i,1).equals(b.getKodeBarang())) {
                int q = Integer.parseInt(cartModel.getValueAt(i,4).toString()) + 1;
                cartModel.setValueAt(q, i, 4);
                cartModel.setValueAt(FormatUtil.formatRupiah(q*b.getHargaBeli()), i, 5);
                hitungTotal(); txtKode.setText(""); return;
            }
        }
        cartModel.addRow(new Object[]{
            cartModel.getRowCount()+1, b.getKodeBarang(), b.getNamaBarang(),
            FormatUtil.formatRupiah(b.getHargaBeli()), 1, FormatUtil.formatRupiah(b.getHargaBeli())
        });
        hitungTotal(); txtKode.setText("");
    }

    private void hapus() {
        int row = cartTable.getSelectedRow();
        if (row < 0) { AlertUtil.showWarning(this,"Pilih item dahulu!"); return; }
        cartModel.removeRow(row);
        for (int i=0;i<cartModel.getRowCount();i++) cartModel.setValueAt(i+1,i,0);
        hitungTotal();
    }

    private void hitungTotal() {
        double t = 0;
        for (int i=0;i<cartModel.getRowCount();i++) {
            String s = cartModel.getValueAt(i,5).toString().replaceAll("[^\\d]","");
            t += s.isEmpty() ? 0 : Double.parseDouble(s);
        }
        lblTotal.setText(FormatUtil.formatRupiah(t));
        lblGrandTotal.setText(FormatUtil.formatRupiah(t));
    }

    private void proses() {
        if (cartModel.getRowCount() == 0) { AlertUtil.showWarning(this,"Keranjang kosong!"); return; }
        for (int i=0;i<cartModel.getRowCount();i++) {
            Barang b = barangCtrl.getByKode(cartModel.getValueAt(i,1).toString());
            if (b==null) continue;
            int q = FormatUtil.parseInt(cartModel.getValueAt(i,4).toString());
            TransaksiDetail d = new TransaksiDetail(b.getId(), b.getKodeBarang(), b.getNamaBarang(), q, b.getHargaBeli());
            d.hitungSubtotal(); currentTrx.addDetail(d);
        }
        currentTrx.setBayar(FormatUtil.parseDouble(txtBayar.getText()));
        if (transaksiCtrl.simpanPembelian(currentTrx)) {
            AlertUtil.showInfo(this, "✓ Pembelian berhasil!\nNo: " + currentTrx.getNoTransaksi());
            resetTrx();
        } else AlertUtil.showError(this,"Gagal menyimpan pembelian!");
    }

    private void resetTrx() {
        currentTrx = new Transaksi();
        cartModel.setRowCount(0);
        lblNo.setText("No: " + transaksiCtrl.generateNoTransaksi("PBL"));
        lblTotal.setText("Rp 0"); lblGrandTotal.setText("Rp 0");
        txtKode.setText(""); txtBayar.setText("0");
        cmbSupplier.setSelectedIndex(0);
    }

    private void addRow2(JPanel p, String l, Component v) {
        JPanel r = new JPanel(new BorderLayout()); r.setOpaque(false);
        r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        r.add(UITheme.fieldLabel(l), BorderLayout.WEST);
        r.add(v, BorderLayout.EAST);
        p.add(r); p.add(Box.createVerticalStrut(6));
    }
}
