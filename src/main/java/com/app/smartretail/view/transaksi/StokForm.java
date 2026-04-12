package com.app.smartretail.view.transaksi;

import com.app.smartretail.controller.BarangController;
import com.app.smartretail.model.Barang;
import com.app.smartretail.utils.AlertUtil;
import com.app.smartretail.utils.FormatUtil;
import com.app.smartretail.utils.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StokForm extends JPanel {

    private final BarangController ctrl = new BarangController();
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch, txtJumlah, txtKet;
    private JComboBox<String> cmbTipe;
    private JButton btnAdjust, btnRefresh;
    private int selectedId = -1;
    private JLabel lblSelected;

    public StokForm() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.BG_SURFACE);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        build();
        load();
    }

    private void build() {
        // Header
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.setBorder(new EmptyBorder(0, 0, 20, 0));
        JLabel title = UITheme.pageTitle("Manajemen Stok");
        JLabel sub = new JLabel("Monitor & sesuaikan stok barang");
        sub.setFont(UITheme.FONT_BODY); sub.setForeground(UITheme.TEXT_SECONDARY);
        JPanel ht = new JPanel(); ht.setOpaque(false);
        ht.setLayout(new BoxLayout(ht, BoxLayout.Y_AXIS));
        ht.add(title); ht.add(sub);

        JPanel stats = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        stats.setOpaque(false);
        btnRefresh = UITheme.ghostButton("↻ Refresh", UITheme.ACCENT_BLUE);
        stats.add(btnRefresh);

        hdr.add(ht, BorderLayout.WEST);
        hdr.add(stats, BorderLayout.EAST);
        add(hdr, BorderLayout.NORTH);

        JPanel main = new JPanel(new BorderLayout(16, 0));
        main.setOpaque(false);

        // Left: search + table
        JPanel left = new JPanel(new BorderLayout(0, 12));
        left.setOpaque(false);

        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setOpaque(false);
        txtSearch = UITheme.styledField("Cari barang…");
        txtSearch.setPreferredSize(new Dimension(0, 38));
        JButton btnS = UITheme.primaryButton("Cari", UITheme.ACCENT_BLUE);
        searchRow.add(txtSearch, BorderLayout.CENTER);
        searchRow.add(btnS, BorderLayout.EAST);

        String[] cols = {"#", "Kode", "Nama Barang", "Kategori", "Stok", "Min", "Satuan", "Status"};
        tableModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r,int c){return false;} };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(36);
        table.getColumnModel().getColumn(1).setMaxWidth(90);
        table.getColumnModel().getColumn(4).setMaxWidth(60);
        table.getColumnModel().getColumn(5).setMaxWidth(60);
        table.getColumnModel().getColumn(7).setMaxWidth(100);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component cp = super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                cp.setBackground(sel ? new Color(82,130,255,50) : (r%2==0 ? UITheme.BG_CARD : UITheme.BG_ROW_ALT));
                cp.setForeground(UITheme.TEXT_PRIMARY);
                if (c==7 && v!=null) {
                    if (v.toString().contains("Rendah")) cp.setForeground(UITheme.ACCENT_CORAL);
                    else cp.setForeground(UITheme.ACCENT_TEAL);
                }
                ((JLabel)cp).setBorder(new EmptyBorder(0,10,0,10));
                return cp;
            }
        });

        left.add(searchRow, BorderLayout.NORTH);
        left.add(UITheme.styledScroll(table), BorderLayout.CENTER);

        // Right: adjust card
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setPreferredSize(new Dimension(270, 0));

        JPanel adjCard = UITheme.card();
        adjCard.setLayout(new BoxLayout(adjCard, BoxLayout.Y_AXIS));
        adjCard.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lAdj = new JLabel("Penyesuaian Stok");
        lAdj.setFont(UITheme.FONT_H2); lAdj.setForeground(UITheme.TEXT_PRIMARY);
        lAdj.setAlignmentX(LEFT_ALIGNMENT);

        lblSelected = new JLabel("— Pilih barang dari tabel —");
        lblSelected.setFont(UITheme.FONT_SMALL); lblSelected.setForeground(UITheme.TEXT_MUTED);
        lblSelected.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lTipe = UITheme.fieldLabel("Tipe Penyesuaian");
        lTipe.setAlignmentX(LEFT_ALIGNMENT);
        cmbTipe = UITheme.styledCombo(new String[]{"TAMBAH (+)", "KURANGI (-)"});
        cmbTipe.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cmbTipe.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lJml = UITheme.fieldLabel("Jumlah");
        lJml.setAlignmentX(LEFT_ALIGNMENT);
        txtJumlah = UITheme.styledField("0");
        txtJumlah.setFont(new Font("Segoe UI", Font.BOLD, 15));
        txtJumlah.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtJumlah.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lKet = UITheme.fieldLabel("Keterangan (opsional)");
        lKet.setAlignmentX(LEFT_ALIGNMENT);
        txtKet = UITheme.styledField("Contoh: Stok opname, retur…");
        txtKet.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtKet.setAlignmentX(LEFT_ALIGNMENT);

        btnAdjust = UITheme.primaryButton("Simpan Perubahan", UITheme.ACCENT_AMBER);
        btnAdjust.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAdjust.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnAdjust.setAlignmentX(LEFT_ALIGNMENT);

        adjCard.add(lAdj);
        adjCard.add(Box.createVerticalStrut(6));
        adjCard.add(lblSelected);
        adjCard.add(Box.createVerticalStrut(14));
        adjCard.add(UITheme.separator());
        adjCard.add(Box.createVerticalStrut(12));
        adjCard.add(lTipe);
        adjCard.add(Box.createVerticalStrut(6));
        adjCard.add(cmbTipe);
        adjCard.add(Box.createVerticalStrut(12));
        adjCard.add(lJml);
        adjCard.add(Box.createVerticalStrut(6));
        adjCard.add(txtJumlah);
        adjCard.add(Box.createVerticalStrut(12));
        adjCard.add(lKet);
        adjCard.add(Box.createVerticalStrut(6));
        adjCard.add(txtKet);
        adjCard.add(Box.createVerticalStrut(16));
        adjCard.add(btnAdjust);

        right.add(adjCard);

        main.add(left, BorderLayout.CENTER);
        main.add(right, BorderLayout.EAST);
        add(main, BorderLayout.CENTER);

        // Events
        btnS.addActionListener(e -> { String kw=txtSearch.getText().trim(); fillTable(kw.isEmpty()?ctrl.getAllBarang():ctrl.searchBarang(kw)); });
        txtSearch.addActionListener(e -> btnS.doClick());
        btnRefresh.addActionListener(e -> load());
        btnAdjust.addActionListener(e -> adjust());
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                selectedId = (int) tableModel.getValueAt(row, 0);
                lblSelected.setText(tableModel.getValueAt(row, 2).toString() +
                    "  (Stok: " + tableModel.getValueAt(row, 4) + ")");
                lblSelected.setForeground(UITheme.ACCENT_BLUE);
            }
        });
    }

    private void adjust() {
        if (selectedId == -1) { AlertUtil.showWarning(this, "Pilih barang terlebih dahulu!"); return; }
        int jml = FormatUtil.parseInt(txtJumlah.getText());
        if (jml <= 0) { AlertUtil.showWarning(this, "Jumlah harus lebih dari 0!"); return; }
        boolean tambah = cmbTipe.getSelectedIndex() == 0;
        if (ctrl.updateStok(selectedId, tambah ? jml : -jml)) {
            AlertUtil.showInfo(this, (tambah?"Stok ditambah ":"Stok dikurangi ") + jml + " unit.");
            load(); selectedId=-1; lblSelected.setText("— Pilih barang dari tabel —");
            lblSelected.setForeground(UITheme.TEXT_MUTED);
            txtJumlah.setText("0"); txtKet.setText("");
        } else AlertUtil.showError(this, "Gagal memperbarui stok.");
    }

    private void load() { fillTable(ctrl.getAllBarang()); }
    private void fillTable(List<Barang> list) {
        tableModel.setRowCount(0);
        int n=1;
        for (Barang b : list)
            tableModel.addRow(new Object[]{n++, b.getKodeBarang(), b.getNamaBarang(), b.getNamaKategori(),
                b.getStok(), b.getStokMinimum(), b.getSatuan(),
                b.isStokRendah() ? "⚠ Rendah" : "✓ OK"});
    }
}
