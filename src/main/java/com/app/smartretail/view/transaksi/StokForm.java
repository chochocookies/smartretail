package com.app.smartretail.view.transaksi;

import com.app.smartretail.controller.BarangController;
import com.app.smartretail.model.Barang;
import com.app.smartretail.utils.AlertUtil;
import com.app.smartretail.utils.FormatUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StokForm extends JPanel {

    private BarangController ctrl;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch, txtAdjust, txtKeterangan;
    private JComboBox<String> cmbTipe;
    private JButton btnCari, btnAdjust, btnRefresh;
    private int selectedBarangId = -1;

    public StokForm() {
        ctrl = new BarangController();
        setLayout(new BorderLayout(10,10));
        setBorder(new EmptyBorder(15,15,15,15));
        setBackground(new Color(245,247,250));
        build(); load();
    }

    private void build() {
        JLabel title = new JLabel("🏭 Manajemen Stok");
        title.setFont(new Font("Segoe UI",Font.BOLD,20)); title.setForeground(new Color(30,55,95));
        add(title, BorderLayout.NORTH);

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT,8,5)); toolbar.setOpaque(false);
        txtSearch = new JTextField(18); txtSearch.putClientProperty("JTextField.placeholderText","Cari barang...");
        btnCari = new JButton("🔍 Cari"); sb(btnCari, new Color(70,130,180));
        btnRefresh = new JButton("🔄"); sb(btnRefresh, new Color(149,165,166));
        toolbar.add(txtSearch); toolbar.add(btnCari); toolbar.add(btnRefresh);

        // Table
        String[] cols = {"ID","Kode","Nama Barang","Kategori","Stok","Stok Min","Satuan","Status"};
        tableModel = new DefaultTableModel(cols,0){ public boolean isCellEditable(int r,int c){return false;} };
        table = new JTable(tableModel); table.setRowHeight(28);
        table.getTableHeader().setBackground(new Color(30,55,95)); table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,12));
        table.setFont(new Font("Segoe UI",Font.PLAIN,12));

        // Adjust panel
        JPanel adjustPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,8,8));
        adjustPanel.setBackground(Color.WHITE);
        adjustPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200,200,200)),new EmptyBorder(8,10,8,10)));

        cmbTipe = new JComboBox<>(new String[]{"TAMBAH","KURANGI"});
        cmbTipe.setFont(new Font("Segoe UI",Font.PLAIN,12));
        txtAdjust = new JTextField(8); txtAdjust.setFont(new Font("Segoe UI",Font.PLAIN,12));
        txtKeterangan = new JTextField(20); txtKeterangan.setFont(new Font("Segoe UI",Font.PLAIN,12));
        txtKeterangan.putClientProperty("JTextField.placeholderText","Keterangan (opsional)...");
        btnAdjust = new JButton("💾 Simpan Perubahan"); sb(btnAdjust, new Color(30,55,95));

        adjustPanel.add(new JLabel("Tipe:")); adjustPanel.add(cmbTipe);
        adjustPanel.add(new JLabel("Jumlah:")); adjustPanel.add(txtAdjust);
        adjustPanel.add(new JLabel("Keterangan:")); adjustPanel.add(txtKeterangan);
        adjustPanel.add(btnAdjust);

        JPanel center = new JPanel(new BorderLayout(0,8)); center.setOpaque(false);
        center.add(toolbar, BorderLayout.NORTH);
        center.add(new JScrollPane(table), BorderLayout.CENTER);
        center.add(adjustPanel, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        // Events
        btnCari.addActionListener(e -> {
            String kw = txtSearch.getText().trim();
            fillTable(kw.isEmpty() ? ctrl.getAllBarang() : ctrl.searchBarang(kw));
        });
        btnRefresh.addActionListener(e -> load());
        txtSearch.addActionListener(e -> btnCari.doClick());
        btnAdjust.addActionListener(e -> adjustStok());
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) selectedBarangId = (int) tableModel.getValueAt(row, 0);
        });
    }

    private void adjustStok() {
        if (selectedBarangId == -1) { AlertUtil.showWarning(this,"Pilih barang dahulu!"); return; }
        String jumlahStr = txtAdjust.getText().trim();
        if (jumlahStr.isEmpty()) { AlertUtil.showWarning(this,"Masukkan jumlah!"); return; }
        int jumlah = FormatUtil.parseInt(jumlahStr);
        if (jumlah <= 0) { AlertUtil.showWarning(this,"Jumlah harus > 0"); return; }
        boolean tambah = "TAMBAH".equals(cmbTipe.getSelectedItem());
        int change = tambah ? jumlah : -jumlah;
        if (ctrl.updateStok(selectedBarangId, change)) {
            AlertUtil.showInfo(this,"Stok berhasil diperbarui!");
            load(); txtAdjust.setText(""); txtKeterangan.setText(""); selectedBarangId = -1;
        } else {
            AlertUtil.showError(this,"Gagal memperbarui stok.");
        }
    }

    private void load() { fillTable(ctrl.getAllBarang()); }

    private void fillTable(List<Barang> list) {
        tableModel.setRowCount(0);
        for (Barang b : list) {
            tableModel.addRow(new Object[]{
                b.getId(), b.getKodeBarang(), b.getNamaBarang(), b.getNamaKategori(),
                b.getStok(), b.getStokMinimum(), b.getSatuan(),
                b.isStokRendah() ? "⚠️ Rendah" : "✅ OK"
            });
        }
    }

    private void sb(JButton b,Color c){b.setBackground(c);b.setForeground(Color.WHITE);b.setFont(new Font("Segoe UI",Font.BOLD,11));b.setFocusPainted(false);b.setBorderPainted(false);b.setCursor(new Cursor(Cursor.HAND_CURSOR));}
}
