package com.app.smartretail.view.master;

import com.app.smartretail.controller.BarangController;
import com.app.smartretail.model.Barang;
import com.app.smartretail.utils.AlertUtil;
import com.app.smartretail.utils.FormatUtil;
import com.app.smartretail.utils.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class BarangForm extends JPanel {

    private BarangController controller;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch, txtKode, txtNama, txtHargaBeli, txtHargaJual, txtStok, txtStokMin, txtSatuan;
    private JTextArea txtDeskripsi;
    private JButton btnTambah, btnEdit, btnHapus, btnSimpan, btnBatal, btnRefresh;
    private int selectedId = -1;

    public BarangForm() {
        this.controller = new BarangController();
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 247, 250));
        initComponents();
        loadData();
    }

    private void initComponents() {
        // Header
        JLabel title = new JLabel("📦 Data Barang");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(30, 55, 95));
        add(title, BorderLayout.NORTH);

        // Split pane
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(650);
        split.setBorder(null);

        // ---- LEFT: Table ----
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setOpaque(false);

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        toolbar.setOpaque(false);

        txtSearch = new JTextField(20);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSearch.putClientProperty("JTextField.placeholderText", "Cari kode / nama barang...");

        JButton btnSearch = new JButton("🔍 Cari");
        btnTambah  = new JButton("➕ Tambah");
        btnEdit    = new JButton("✏️ Edit");
        btnHapus   = new JButton("🗑️ Hapus");
        btnRefresh = new JButton("🔄 Refresh");

        styleButton(btnSearch,  new Color(70, 130, 180));
        styleButton(btnTambah,  new Color(46, 204, 113));
        styleButton(btnEdit,    new Color(241, 196, 15));
        styleButton(btnHapus,   new Color(231, 76, 60));
        styleButton(btnRefresh, new Color(149, 165, 166));

        toolbar.add(txtSearch);
        toolbar.add(btnSearch);
        toolbar.add(btnTambah);
        toolbar.add(btnEdit);
        toolbar.add(btnHapus);
        toolbar.add(btnRefresh);

        // Table
        String[] cols = {"ID", "Kode", "Nama Barang", "Kategori", "Harga Jual", "Stok", "Satuan", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(30, 55, 95));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(210, 230, 255));
        table.getColumnModel().getColumn(0).setMaxWidth(40);

        leftPanel.add(toolbar, BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        // ---- RIGHT: Form ----
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            new EmptyBorder(15, 15, 15, 15)));

        JLabel lblForm = new JLabel("Form Barang");
        lblForm.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblForm.setForeground(new Color(30, 55, 95));
        lblForm.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtKode      = createField("Kode Barang *", rightPanel);
        txtNama      = createField("Nama Barang *", rightPanel);
        txtHargaBeli = createField("Harga Beli", rightPanel);
        txtHargaJual = createField("Harga Jual *", rightPanel);
        txtStok      = createField("Stok Awal", rightPanel);
        txtStokMin   = createField("Stok Minimum", rightPanel);
        txtSatuan    = createField("Satuan (pcs/kg/dll)", rightPanel);

        JLabel lblDesk = new JLabel("Deskripsi");
        lblDesk.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblDesk.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtDeskripsi = new JTextArea(3, 0);
        txtDeskripsi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDeskripsi.setLineWrap(true);
        JScrollPane spDesk = new JScrollPane(txtDeskripsi);
        spDesk.setAlignmentX(Component.LEFT_ALIGNMENT);
        spDesk.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        rightPanel.add(Box.createVerticalStrut(5));
        rightPanel.add(lblForm);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(lblDesk);
        rightPanel.add(spDesk);
        rightPanel.add(Box.createVerticalStrut(12));

        // Form buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        btnPanel.setOpaque(false);
        btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSimpan = new JButton("💾 Simpan");
        btnBatal  = new JButton("✖ Batal");
        styleButton(btnSimpan, new Color(30, 55, 95));
        styleButton(btnBatal,  new Color(149, 165, 166));
        btnPanel.add(btnSimpan);
        btnPanel.add(btnBatal);
        rightPanel.add(btnPanel);

        split.setLeftComponent(leftPanel);
        split.setRightComponent(rightPanel);
        add(split, BorderLayout.CENTER);

        setFormEnabled(false);

        // Events
        btnSearch.addActionListener(e -> {
            String kw = txtSearch.getText().trim();
            List<Barang> list = kw.isEmpty() ? controller.getAllBarang() : controller.searchBarang(kw);
            fillTable(list);
        });
        btnRefresh.addActionListener(e -> loadData());
        btnTambah.addActionListener(e -> { clearForm(); setFormEnabled(true); selectedId = -1; txtKode.setText(controller.generateKode()); });
        btnEdit.addActionListener(e -> editSelected());
        btnHapus.addActionListener(e -> hapusSelected());
        btnSimpan.addActionListener(e -> simpan());
        btnBatal.addActionListener(e -> { clearForm(); setFormEnabled(false); });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillFormFromTable();
        });
    }

    private JTextField createField(String label, JPanel parent) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(lbl);
        parent.add(field);
        parent.add(Box.createVerticalStrut(5));
        return field;
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void loadData() {
        fillTable(controller.getAllBarang());
    }

    private void fillTable(List<Barang> list) {
        tableModel.setRowCount(0);
        for (Barang b : list) {
            tableModel.addRow(new Object[]{
                b.getId(), b.getKodeBarang(), b.getNamaBarang(), b.getNamaKategori(),
                FormatUtil.formatRupiah(b.getHargaJual()), b.getStok(), b.getSatuan(),
                b.isStokRendah() ? "⚠️ Rendah" : "✅ OK"
            });
        }
    }

    private void fillFormFromTable() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedId = (int) tableModel.getValueAt(row, 0);
        txtKode.setText(tableModel.getValueAt(row, 1).toString());
        txtNama.setText(tableModel.getValueAt(row, 2).toString());
        txtHargaJual.setText(tableModel.getValueAt(row, 4).toString().replaceAll("[^\\d]", ""));
    }

    private void editSelected() {
        if (table.getSelectedRow() < 0) { AlertUtil.showWarning(this, "Pilih barang terlebih dahulu!"); return; }
        setFormEnabled(true);
    }

    private void hapusSelected() {
        if (table.getSelectedRow() < 0) { AlertUtil.showWarning(this, "Pilih barang terlebih dahulu!"); return; }
        if (!Session.isAdmin()) { AlertUtil.showWarning(this, "Hanya Admin yang bisa menghapus data!"); return; }
        if (AlertUtil.showConfirm(this, "Yakin hapus barang ini?")) {
            if (controller.hapusBarang(selectedId)) {
                AlertUtil.showInfo(this, "Barang berhasil dihapus.");
                loadData(); clearForm();
            } else {
                AlertUtil.showError(this, "Gagal menghapus barang.");
            }
        }
    }

    private void simpan() {
        if (txtNama.getText().isBlank() || txtHargaJual.getText().isBlank()) {
            AlertUtil.showWarning(this, "Nama barang dan harga jual wajib diisi!");
            return;
        }
        Barang b = new Barang();
        b.setId(selectedId);
        b.setKodeBarang(txtKode.getText().trim());
        b.setNamaBarang(txtNama.getText().trim());
        b.setHargaBeli(FormatUtil.parseDouble(txtHargaBeli.getText()));
        b.setHargaJual(FormatUtil.parseDouble(txtHargaJual.getText()));
        b.setStok(FormatUtil.parseInt(txtStok.getText()));
        b.setStokMinimum(FormatUtil.parseInt(txtStokMin.getText()));
        b.setSatuan(txtSatuan.getText().trim());
        b.setDeskripsi(txtDeskripsi.getText().trim());

        boolean ok = (selectedId == -1) ? controller.tambahBarang(b) : controller.updateBarang(b);
        if (ok) {
            AlertUtil.showInfo(this, "Data barang berhasil disimpan!");
            loadData(); clearForm(); setFormEnabled(false);
        } else {
            AlertUtil.showError(this, "Gagal menyimpan data barang.");
        }
    }

    private void clearForm() {
        selectedId = -1;
        txtKode.setText(""); txtNama.setText(""); txtHargaBeli.setText("");
        txtHargaJual.setText(""); txtStok.setText(""); txtStokMin.setText("");
        txtSatuan.setText(""); txtDeskripsi.setText("");
    }

    private void setFormEnabled(boolean enabled) {
        txtKode.setEnabled(enabled); txtNama.setEnabled(enabled);
        txtHargaBeli.setEnabled(enabled); txtHargaJual.setEnabled(enabled);
        txtStok.setEnabled(enabled); txtStokMin.setEnabled(enabled);
        txtSatuan.setEnabled(enabled); txtDeskripsi.setEnabled(enabled);
        btnSimpan.setEnabled(enabled); btnBatal.setEnabled(enabled);
    }
}
