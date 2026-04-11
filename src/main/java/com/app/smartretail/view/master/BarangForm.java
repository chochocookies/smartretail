package com.app.smartretail.view.master;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.app.smartretail.controller.BarangController;
import com.app.smartretail.model.Barang;
import com.app.smartretail.utils.AlertUtil;
import com.app.smartretail.utils.FormatUtil;
import com.app.smartretail.utils.Session;
import com.app.smartretail.utils.UITheme;

public class BarangForm extends JPanel {

    private final BarangController ctrl = new BarangController();
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch, txtKode, txtNama, txtHargaBeli, txtHargaJual, txtStok, txtStokMin, txtSatuan;
    private JTextArea txtDeskripsi;
    private JButton btnTambah, btnEdit, btnHapus, btnSimpan, btnBatal, btnRefresh;
    private JLabel lblFormTitle;
    private int selectedId = -1;
    private boolean editMode = false;

    public BarangForm() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.BG_DARK);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        build();
        loadData();
    }

    private void build() {
        // Header
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.setBorder(new EmptyBorder(0, 0, 20, 0));
        JLabel title = UITheme.pageTitle("Data Barang");
        JLabel sub = new JLabel("Kelola semua data produk & inventaris");
        sub.setFont(UITheme.FONT_BODY); sub.setForeground(UITheme.TEXT_SECONDARY);
        JPanel ht = new JPanel(); ht.setOpaque(false);
        ht.setLayout(new BoxLayout(ht, BoxLayout.Y_AXIS));
        ht.add(title); ht.add(sub);
        hdr.add(ht, BorderLayout.WEST);

        JPanel actBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actBtns.setOpaque(false);
        btnTambah  = UITheme.primaryButton("+ Tambah", UITheme.ACCENT_BLUE);
        btnEdit    = UITheme.ghostButton("Edit", UITheme.ACCENT_AMBER);
        btnHapus   = UITheme.dangerButton("Hapus");
        btnRefresh = UITheme.ghostButton("↻", UITheme.TEXT_MUTED);
        actBtns.add(btnRefresh); actBtns.add(btnHapus); actBtns.add(btnEdit); actBtns.add(btnTambah);
        hdr.add(actBtns, BorderLayout.EAST);
        add(hdr, BorderLayout.NORTH);

        // ── MAIN: table + form split ──────────────────────────────────
        JPanel main = new JPanel(new BorderLayout(16, 0));
        main.setOpaque(false);

        // Left: search + table
        JPanel leftPanel = new JPanel(new BorderLayout(0, 12));
        leftPanel.setOpaque(false);

        // Search bar
        JPanel searchBar = new JPanel(new BorderLayout(8, 0));
        searchBar.setOpaque(false);
        txtSearch = UITheme.styledField("Cari kode atau nama barang…");
        txtSearch.setPreferredSize(new Dimension(0, 38));
        JButton btnSearch = UITheme.primaryButton("Cari", UITheme.ACCENT_BLUE);
        searchBar.add(txtSearch, BorderLayout.CENTER);
        searchBar.add(btnSearch, BorderLayout.EAST);

        // Table
        String[] cols = {"", "Kode", "Nama Barang", "Kategori", "Harga Jual", "Stok", "Satuan", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(5).setMaxWidth(60);
        table.getColumnModel().getColumn(7).setMaxWidth(100);
        table.setDefaultRenderer(Object.class, new StatusRowRenderer());

        leftPanel.add(searchBar, BorderLayout.NORTH);
        leftPanel.add(UITheme.styledScroll(table), BorderLayout.CENTER);

        // Right: form panel
        JPanel formCard = UITheme.card();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setPreferredSize(new Dimension(290, 0));
        formCard.setMaximumSize(new Dimension(290, Integer.MAX_VALUE));

        lblFormTitle = new JLabel("Detail Barang");
        lblFormTitle.setFont(UITheme.FONT_H2);
        lblFormTitle.setForeground(UITheme.TEXT_PRIMARY);
        lblFormTitle.setAlignmentX(LEFT_ALIGNMENT);

        JSeparator sep = UITheme.separator();
        sep.setAlignmentX(LEFT_ALIGNMENT);

        txtKode      = addFormField(formCard, "Kode Barang");
        txtNama      = addFormField(formCard, "Nama Barang *");
        txtHargaBeli = addFormField(formCard, "Harga Beli");
        txtHargaJual = addFormField(formCard, "Harga Jual *");
        txtStok      = addFormField(formCard, "Stok Awal");
        txtStokMin   = addFormField(formCard, "Stok Minimum");
        txtSatuan    = addFormField(formCard, "Satuan (pcs / kg / dll)");

        JLabel lDesk = UITheme.fieldLabel("Deskripsi");
        lDesk.setAlignmentX(LEFT_ALIGNMENT);
        txtDeskripsi = new JTextArea(3, 0);
        txtDeskripsi.setFont(UITheme.FONT_BODY);
        txtDeskripsi.setForeground(UITheme.TEXT_PRIMARY);
        txtDeskripsi.setBackground(UITheme.BG_INPUT);
        txtDeskripsi.setCaretColor(UITheme.ACCENT_BLUE);
        txtDeskripsi.setLineWrap(true);
        txtDeskripsi.setBorder(BorderFactory.createCompoundBorder(
            new UITheme.RoundedBorder(8, UITheme.BORDER_DEFAULT),
            new EmptyBorder(8, 10, 8, 10)));
        JScrollPane spDesk = new JScrollPane(txtDeskripsi);
        spDesk.setBorder(BorderFactory.createEmptyBorder());
        spDesk.setBackground(UITheme.BG_INPUT);
        spDesk.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        spDesk.setAlignmentX(LEFT_ALIGNMENT);

        // Form action buttons
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnRow.setAlignmentX(LEFT_ALIGNMENT);
        btnSimpan = UITheme.primaryButton("Simpan", UITheme.ACCENT_BLUE);
        btnBatal  = UITheme.ghostButton("Batal", UITheme.TEXT_MUTED);
        btnRow.add(btnBatal); btnRow.add(btnSimpan);

        formCard.add(lblFormTitle);
        formCard.add(Box.createVerticalStrut(8));
        formCard.add(sep);
        formCard.add(Box.createVerticalStrut(12));
        formCard.add(lDesk);
        formCard.add(Box.createVerticalStrut(5));
        formCard.add(spDesk);
        formCard.add(Box.createVerticalStrut(14));
        formCard.add(Box.createVerticalGlue());
        formCard.add(btnRow);

        main.add(leftPanel, BorderLayout.CENTER);
        main.add(formCard, BorderLayout.EAST);
        add(main, BorderLayout.CENTER);

        // ── Events ───────────────────────────────────────────────────
        setFormEnabled(false);
        btnSearch.addActionListener(e -> search());
        txtSearch.addActionListener(e -> search());
        btnRefresh.addActionListener(e -> loadData());
        btnTambah.addActionListener(e -> startNew());
        btnEdit.addActionListener(e -> startEdit());
        btnHapus.addActionListener(e -> hapus());
        btnSimpan.addActionListener(e -> simpan());
        btnBatal.addActionListener(e -> cancelForm());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillForm();
        });
    }

    private JTextField addFormField(JPanel parent, String label) {
        JLabel lbl = UITheme.fieldLabel(label);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        JTextField f = UITheme.styledField("");
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        f.setAlignmentX(LEFT_ALIGNMENT);
        parent.add(lbl);
        parent.add(Box.createVerticalStrut(5));
        parent.add(f);
        parent.add(Box.createVerticalStrut(10));
        return f;
    }

    private void loadData() { fillTable(ctrl.getAllBarang()); }
    private void search() {
        String kw = txtSearch.getText().trim();
        fillTable(kw.isEmpty() ? ctrl.getAllBarang() : ctrl.searchBarang(kw));
    }

    private void fillTable(List<Barang> list) {
        tableModel.setRowCount(0);
        int no = 1;
        for (Barang b : list) {
            tableModel.addRow(new Object[]{
                no++, b.getKodeBarang(), b.getNamaBarang(), b.getNamaKategori(),
                FormatUtil.formatRupiah(b.getHargaJual()), b.getStok(), b.getSatuan(),
                b.isStokRendah() ? "Rendah" : "OK"
            });
        }
    }

    private void fillForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedId = (int) tableModel.getValueAt(row, 0);
        txtKode.setText(tableModel.getValueAt(row, 1).toString());
        txtNama.setText(tableModel.getValueAt(row, 2).toString());
        txtHargaJual.setText(tableModel.getValueAt(row, 4).toString().replaceAll("[^\\d]", ""));
        String status = tableModel.getValueAt(row, 7).toString();
        lblFormTitle.setText(tableModel.getValueAt(row, 2).toString());
    }

    private void startNew() {
        clearForm(); setFormEnabled(true); editMode = false;
        selectedId = -1;
        lblFormTitle.setText("Tambah Barang Baru");
        txtKode.setText(ctrl.generateKode());
    }

    private void startEdit() {
        if (table.getSelectedRow() < 0) { AlertUtil.showWarning(this, "Pilih barang terlebih dahulu!"); return; }
        setFormEnabled(true); editMode = true;
        lblFormTitle.setText("Edit: " + txtNama.getText());
    }

    private void hapus() {
        if (table.getSelectedRow() < 0) { AlertUtil.showWarning(this, "Pilih barang terlebih dahulu!"); return; }
        if (!Session.isAdmin()) { AlertUtil.showWarning(this, "Hanya Admin yang dapat menghapus data."); return; }
        if (!AlertUtil.showConfirm(this, "Hapus barang ini?")) return;
        if (ctrl.hapusBarang(selectedId)) {
            AlertUtil.showInfo(this, "Barang berhasil dihapus.");
            loadData(); clearForm(); setFormEnabled(false);
        } else AlertUtil.showError(this, "Gagal menghapus barang.");
    }

    private void simpan() {
        if (txtNama.getText().isBlank() || txtHargaJual.getText().isBlank()) {
            AlertUtil.showWarning(this, "Nama barang dan harga jual wajib diisi!"); return;
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

        boolean ok = (selectedId == -1) ? ctrl.tambahBarang(b) : ctrl.updateBarang(b);
        if (ok) {
            AlertUtil.showInfo(this, "Data barang berhasil disimpan!");
            loadData(); clearForm(); setFormEnabled(false);
        } else AlertUtil.showError(this, "Gagal menyimpan data.");
    }

    private void cancelForm() { clearForm(); setFormEnabled(false); lblFormTitle.setText("Detail Barang"); }

    private void clearForm() {
        selectedId = -1;
        txtKode.setText(""); txtNama.setText(""); txtHargaBeli.setText("");
        txtHargaJual.setText(""); txtStok.setText(""); txtStokMin.setText("");
        txtSatuan.setText(""); txtDeskripsi.setText("");
    }

    private void setFormEnabled(boolean e) {
        txtKode.setEnabled(e); txtNama.setEnabled(e); txtHargaBeli.setEnabled(e);
        txtHargaJual.setEnabled(e); txtStok.setEnabled(e); txtStokMin.setEnabled(e);
        txtSatuan.setEnabled(e); txtDeskripsi.setEnabled(e);
        btnSimpan.setEnabled(e); btnBatal.setEnabled(e);
    }

    // Custom row renderer for status column
    private class StatusRowRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            comp.setBackground(sel ? new Color(82,130,255,40) :
                (r%2==0 ? UITheme.BG_CARD : UITheme.BG_ROW_ALT));
            comp.setForeground(UITheme.TEXT_PRIMARY);
            if (c == 7 && v != null) {
                if ("Rendah".equals(v.toString())) comp.setForeground(UITheme.ACCENT_CORAL);
                else comp.setForeground(UITheme.ACCENT_TEAL);
            }
            setBorder(new EmptyBorder(0,10,0,10));
            return comp;
        }
    }
}
