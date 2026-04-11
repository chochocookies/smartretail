package com.app.smartretail.view.master;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.app.smartretail.dao.SupplierDAO;
import com.app.smartretail.model.Supplier;
import com.app.smartretail.utils.AlertUtil;

public class SupplierForm extends JPanel {

    private SupplierDAO dao;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtKode, txtNama, txtAlamat, txtTlp, txtEmail, txtCP;
    private JButton btnSimpan, btnHapus, btnBatal, btnTambah;
    private int selectedId = -1;

    public SupplierForm() {
        dao = new SupplierDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 247, 250));
        initComponents();
        loadData();
    }

    private void initComponents() {
        JLabel title = new JLabel("🚚 Data Supplier");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(30, 55, 95));
        add(title, BorderLayout.NORTH);

        String[] cols = {"ID","Kode","Nama Supplier","Telepon","Email","Contact Person"};
        tableModel = new DefaultTableModel(cols,0){ public boolean isCellEditable(int r,int c){return false;} };
        table = new JTable(tableModel); table.setRowHeight(28);
        table.setFont(new Font("Segoe UI",Font.PLAIN,12));
        table.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,12));
        table.getTableHeader().setBackground(new Color(30,55,95));
        table.getTableHeader().setForeground(Color.WHITE);

        JPanel form = new JPanel(); form.setLayout(new BoxLayout(form,BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE); form.setPreferredSize(new Dimension(260,0));
        form.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220,220,220)),new EmptyBorder(15,15,15,15)));

        JLabel lf = new JLabel("Form Supplier"); lf.setFont(new Font("Segoe UI",Font.BOLD,14)); lf.setForeground(new Color(30,55,95)); lf.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtKode  = field("Kode Supplier", form);
        txtNama  = field("Nama Supplier *", form);
        txtAlamat= field("Alamat", form);
        txtTlp   = field("Telepon", form);
        txtEmail = field("Email", form);
        txtCP    = field("Contact Person", form);

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.LEFT,5,0)); bp.setOpaque(false); bp.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnTambah= new JButton("➕"); btnSimpan=new JButton("💾 Simpan"); btnHapus=new JButton("🗑️"); btnBatal=new JButton("✖");
        styleBtn(btnTambah,new Color(46,204,113)); styleBtn(btnSimpan,new Color(30,55,95)); styleBtn(btnHapus,new Color(231,76,60)); styleBtn(btnBatal,new Color(149,165,166));
        bp.add(btnTambah); bp.add(btnSimpan); bp.add(btnHapus); bp.add(btnBatal);

        form.add(lf); form.add(Box.createVerticalStrut(10)); form.add(Box.createVerticalStrut(10)); form.add(bp);

        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(table), form);
        sp.setDividerLocation(600); sp.setBorder(null);
        add(sp, BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                selectedId = (int) tableModel.getValueAt(row, 0);
                txtKode.setText(nullSafe(tableModel.getValueAt(row,1)));
                txtNama.setText(nullSafe(tableModel.getValueAt(row,2)));
                txtTlp.setText(nullSafe(tableModel.getValueAt(row,3)));
                txtEmail.setText(nullSafe(tableModel.getValueAt(row,4)));
                txtCP.setText(nullSafe(tableModel.getValueAt(row,5)));
            }
        });
        btnTambah.addActionListener(e -> { clearForm(); selectedId = -1; });
        btnSimpan.addActionListener(e -> simpan());
        btnHapus.addActionListener(e -> hapus());
        btnBatal.addActionListener(e -> clearForm());
    }

    private void simpan() {
        if (txtNama.getText().isBlank()) { AlertUtil.showWarning(this,"Nama supplier wajib diisi!"); return; }
        Supplier s = new Supplier();
        s.setId(selectedId); s.setKodeSupplier(txtKode.getText().trim()); s.setNamaSupplier(txtNama.getText().trim());
        s.setAlamat(txtAlamat.getText().trim()); s.setTelepon(txtTlp.getText().trim());
        s.setEmail(txtEmail.getText().trim()); s.setContactPerson(txtCP.getText().trim());
        boolean ok = (selectedId == -1) ? dao.insert(s) : dao.update(s);
        if (ok) { AlertUtil.showInfo(this,"Supplier disimpan!"); loadData(); clearForm(); }
        else AlertUtil.showError(this,"Gagal menyimpan supplier.");
    }

    private void hapus() {
        if (selectedId == -1) { AlertUtil.showWarning(this,"Pilih supplier dahulu!"); return; }
        if (AlertUtil.showConfirm(this,"Hapus supplier ini?")) {
            if (dao.delete(selectedId)) { AlertUtil.showInfo(this,"Supplier dihapus."); loadData(); clearForm(); }
            else AlertUtil.showError(this,"Gagal menghapus.");
        }
    }

    private void loadData() {
        tableModel.setRowCount(0);
        for (Supplier s : dao.getAll())
            tableModel.addRow(new Object[]{s.getId(),s.getKodeSupplier(),s.getNamaSupplier(),s.getTelepon(),s.getEmail(),s.getContactPerson()});
    }

    private void clearForm() { selectedId=-1; txtKode.setText(""); txtNama.setText(""); txtAlamat.setText(""); txtTlp.setText(""); txtEmail.setText(""); txtCP.setText(""); }

    private JTextField field(String label, JPanel p) {
        JLabel l = new JLabel(label); l.setFont(new Font("Segoe UI",Font.BOLD,11)); l.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextField f = new JTextField(); f.setMaximumSize(new Dimension(Integer.MAX_VALUE,32)); f.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l); p.add(f); p.add(Box.createVerticalStrut(5));
        return f;
    }

    private void styleBtn(JButton b, Color c) {
        b.setBackground(c); b.setForeground(Color.WHITE); b.setFont(new Font("Segoe UI",Font.BOLD,11));
        b.setFocusPainted(false); b.setBorderPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private String nullSafe(Object o) { return o == null ? "" : o.toString(); }
}
