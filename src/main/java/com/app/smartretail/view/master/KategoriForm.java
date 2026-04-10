package com.app.smartretail.view.master;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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

import com.app.smartretail.config.DatabaseConnection;
import com.app.smartretail.utils.AlertUtil;

public class KategoriForm extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtNama, txtDeskripsi;
    private JButton btnTambah, btnSimpan, btnHapus, btnBatal;
    private int selectedId = -1;

    public KategoriForm() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 247, 250));
        initComponents();
        loadData();
    }

    private void initComponents() {
        JLabel title = new JLabel("📂 Kategori Barang");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(30, 55, 95));
        add(title, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Nama Kategori", "Deskripsi"};
        tableModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(30, 55, 95));
        table.getTableHeader().setForeground(Color.WHITE);

        // Form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            new EmptyBorder(15, 15, 15, 15)));
        formPanel.setPreferredSize(new Dimension(250, 0));

        JLabel lblF = new JLabel("Form Kategori");
        lblF.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblF.setForeground(new Color(30, 55, 95));
        lblF.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel l1 = new JLabel("Nama Kategori *");
        l1.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l1.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtNama = new JTextField(); txtNama.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        txtNama.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel l2 = new JLabel("Deskripsi");
        l2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l2.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtDeskripsi = new JTextField(); txtDeskripsi.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        txtDeskripsi.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        btnP.setOpaque(false); btnP.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSimpan = new JButton("💾 Simpan"); btnHapus = new JButton("🗑️ Hapus"); btnBatal = new JButton("✖ Batal");
        styleBtn(btnSimpan, new Color(30,55,95)); styleBtn(btnHapus, new Color(231,76,60)); styleBtn(btnBatal, new Color(149,165,166));
        btnP.add(btnSimpan); btnP.add(btnHapus); btnP.add(btnBatal);

        formPanel.add(lblF); formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(l1); formPanel.add(txtNama); formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(l2); formPanel.add(txtDeskripsi); formPanel.add(Box.createVerticalStrut(12));
        formPanel.add(btnP);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(table), formPanel);
        split.setDividerLocation(600); split.setBorder(null);
        add(split, BorderLayout.CENTER);

        // Events
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                selectedId = (int) tableModel.getValueAt(row, 0);
                txtNama.setText(tableModel.getValueAt(row, 1).toString());
                Object d = tableModel.getValueAt(row, 2);
                txtDeskripsi.setText(d != null ? d.toString() : "");
            }
        });
        btnSimpan.addActionListener(e -> simpan());
        btnHapus.addActionListener(e -> hapus());
        btnBatal.addActionListener(e -> { selectedId = -1; txtNama.setText(""); txtDeskripsi.setText(""); });
    }

    private void simpan() {
        if (txtNama.getText().isBlank()) { AlertUtil.showWarning(this, "Nama kategori wajib diisi!"); return; }
        Connection conn = DatabaseConnection.getInstance();
        try {
            if (selectedId == -1) {
                PreparedStatement ps = conn.prepareStatement("INSERT INTO kategori (nama_kategori, deskripsi) VALUES(?,?)");
                ps.setString(1, txtNama.getText().trim()); ps.setString(2, txtDeskripsi.getText().trim());
                ps.executeUpdate();
            } else {
                PreparedStatement ps = conn.prepareStatement("UPDATE kategori SET nama_kategori=?, deskripsi=? WHERE id=?");
                ps.setString(1, txtNama.getText().trim()); ps.setString(2, txtDeskripsi.getText().trim()); ps.setInt(3, selectedId);
                ps.executeUpdate();
            }
            AlertUtil.showInfo(this, "Kategori berhasil disimpan!"); loadData();
            selectedId = -1; txtNama.setText(""); txtDeskripsi.setText("");
        } catch (SQLException ex) { AlertUtil.showError(this, "Error: " + ex.getMessage()); }
    }

    private void hapus() {
        if (selectedId == -1) { AlertUtil.showWarning(this, "Pilih kategori dahulu!"); return; }
        if (!AlertUtil.showConfirm(this, "Hapus kategori ini?")) return;
        Connection conn = DatabaseConnection.getInstance();
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM kategori WHERE id=?");
            ps.setInt(1, selectedId); ps.executeUpdate();
            AlertUtil.showInfo(this, "Kategori dihapus!"); loadData();
            selectedId = -1; txtNama.setText(""); txtDeskripsi.setText("");
        } catch (SQLException ex) { AlertUtil.showError(this, "Error: " + ex.getMessage()); }
    }

    private void loadData() {
        tableModel.setRowCount(0);
        Connection conn = DatabaseConnection.getInstance();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM kategori ORDER BY nama_kategori")) {
            while (rs.next()) tableModel.addRow(new Object[]{rs.getInt("id"), rs.getString("nama_kategori"), rs.getString("deskripsi")});
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void styleBtn(JButton b, Color c) {
        b.setBackground(c); b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 11));
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}
