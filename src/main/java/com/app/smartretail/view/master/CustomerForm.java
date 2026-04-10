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

public class CustomerForm extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtKode, txtNama, txtTlp, txtEmail, txtAlamat;
    private JButton btnSimpan, btnHapus, btnBatal;
    private int selectedId = -1;

    public CustomerForm() {
        setLayout(new BorderLayout(10,10));
        setBorder(new EmptyBorder(15,15,15,15));
        setBackground(new Color(245,247,250));
        build(); loadData();
    }

    private void build() {
        JLabel title = new JLabel("👥 Data Customer");
        title.setFont(new Font("Segoe UI",Font.BOLD,20)); title.setForeground(new Color(30,55,95));
        add(title, BorderLayout.NORTH);

        String[] cols = {"ID","Kode","Nama Customer","Telepon","Email","Poin"};
        tableModel = new DefaultTableModel(cols,0){ public boolean isCellEditable(int r,int c){return false;} };
        table = new JTable(tableModel); table.setRowHeight(28);
        table.getTableHeader().setBackground(new Color(30,55,95)); table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,12));
        table.setFont(new Font("Segoe UI",Font.PLAIN,12));

        JPanel form = new JPanel(); form.setLayout(new BoxLayout(form,BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE); form.setPreferredSize(new Dimension(260,0));
        form.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220,220,220)),new EmptyBorder(15,15,15,15)));

        JLabel lf = new JLabel("Form Customer"); lf.setFont(new Font("Segoe UI",Font.BOLD,14)); lf.setForeground(new Color(30,55,95)); lf.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtKode  = fld("Kode Customer",form); txtNama  = fld("Nama Customer *",form);
        txtTlp   = fld("Telepon",form); txtEmail = fld("Email",form); txtAlamat= fld("Alamat",form);

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.LEFT,5,0)); bp.setOpaque(false); bp.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSimpan=new JButton("💾 Simpan"); btnHapus=new JButton("🗑️ Hapus"); btnBatal=new JButton("✖ Batal");
        sb(btnSimpan,new Color(30,55,95)); sb(btnHapus,new Color(231,76,60)); sb(btnBatal,new Color(149,165,166));
        bp.add(btnSimpan); bp.add(btnHapus); bp.add(btnBatal);

        form.add(lf); form.add(Box.createVerticalStrut(10)); form.add(bp);
        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(table), form);
        sp.setDividerLocation(600); sp.setBorder(null);
        add(sp, BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row>=0) { selectedId=(int)tableModel.getValueAt(row,0); txtKode.setText(s(tableModel.getValueAt(row,1))); txtNama.setText(s(tableModel.getValueAt(row,2))); txtTlp.setText(s(tableModel.getValueAt(row,3))); txtEmail.setText(s(tableModel.getValueAt(row,4))); }
        });
        btnSimpan.addActionListener(e->simpan()); btnHapus.addActionListener(e->hapus()); btnBatal.addActionListener(e->clear());
    }

    private void simpan() {
        if (txtNama.getText().isBlank()) { AlertUtil.showWarning(this,"Nama customer wajib diisi!"); return; }
        Connection conn = DatabaseConnection.getInstance();
        try {
            if (selectedId==-1) {
                PreparedStatement ps = conn.prepareStatement("INSERT INTO customer(kode_customer,nama_customer,telepon,email,alamat,poin) VALUES(?,?,?,?,?,0)");
                ps.setString(1,txtKode.getText().trim()); ps.setString(2,txtNama.getText().trim()); ps.setString(3,txtTlp.getText().trim()); ps.setString(4,txtEmail.getText().trim()); ps.setString(5,txtAlamat.getText().trim());
                ps.executeUpdate();
            } else {
                PreparedStatement ps = conn.prepareStatement("UPDATE customer SET kode_customer=?,nama_customer=?,telepon=?,email=?,alamat=? WHERE id=?");
                ps.setString(1,txtKode.getText().trim()); ps.setString(2,txtNama.getText().trim()); ps.setString(3,txtTlp.getText().trim()); ps.setString(4,txtEmail.getText().trim()); ps.setString(5,txtAlamat.getText().trim()); ps.setInt(6,selectedId);
                ps.executeUpdate();
            }
            AlertUtil.showInfo(this,"Customer disimpan!"); loadData(); clear();
        } catch(SQLException ex) { AlertUtil.showError(this,"Error: "+ex.getMessage()); }
    }

    private void hapus() {
        if (selectedId==-1) { AlertUtil.showWarning(this,"Pilih customer dahulu!"); return; }
        if (!AlertUtil.showConfirm(this,"Hapus customer ini?")) return;
        Connection conn = DatabaseConnection.getInstance();
        try { PreparedStatement ps=conn.prepareStatement("DELETE FROM customer WHERE id=?"); ps.setInt(1,selectedId); ps.executeUpdate(); AlertUtil.showInfo(this,"Dihapus."); loadData(); clear(); }
        catch(SQLException ex) { AlertUtil.showError(this,"Error: "+ex.getMessage()); }
    }

    private void loadData() {
        tableModel.setRowCount(0);
        Connection conn = DatabaseConnection.getInstance();
        try (Statement st=conn.createStatement(); ResultSet rs=st.executeQuery("SELECT * FROM customer ORDER BY nama_customer")) {
            while(rs.next()) tableModel.addRow(new Object[]{rs.getInt("id"),rs.getString("kode_customer"),rs.getString("nama_customer"),rs.getString("telepon"),rs.getString("email"),rs.getInt("poin")});
        } catch(SQLException ex) { ex.printStackTrace(); }
    }

    private void clear() { selectedId=-1; txtKode.setText(""); txtNama.setText(""); txtTlp.setText(""); txtEmail.setText(""); txtAlamat.setText(""); }
    private JTextField fld(String label, JPanel p) { JLabel l=new JLabel(label); l.setFont(new Font("Segoe UI",Font.BOLD,11)); l.setAlignmentX(Component.LEFT_ALIGNMENT); JTextField f=new JTextField(); f.setMaximumSize(new Dimension(Integer.MAX_VALUE,32)); f.setAlignmentX(Component.LEFT_ALIGNMENT); p.add(l); p.add(f); p.add(Box.createVerticalStrut(5)); return f; }
    private void sb(JButton b,Color c){b.setBackground(c);b.setForeground(Color.WHITE);b.setFont(new Font("Segoe UI",Font.BOLD,11));b.setFocusPainted(false);b.setBorderPainted(false);b.setCursor(new Cursor(Cursor.HAND_CURSOR));}
    private String s(Object o){return o==null?"":o.toString();}
}
