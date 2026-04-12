package com.app.smartretail.view.master;
import com.app.smartretail.utils.UITheme;

import com.app.smartretail.dao.UserDAO;
import com.app.smartretail.model.User;
import com.app.smartretail.utils.AlertUtil;
import com.app.smartretail.utils.PasswordHasher;
import com.app.smartretail.utils.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UserForm extends JPanel {

    private UserDAO dao;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtUsername, txtNama, txtEmail;
    private JPasswordField txtPassword;
    private JComboBox<String> cmbRole;
    private JCheckBox chkAktif;
    private JButton btnSimpan, btnHapus, btnBatal, btnTambah;
    private int selectedId = -1;

    public UserForm() {
        if (!Session.isAdmin()) {
            setLayout(new BorderLayout());
            add(new JLabel("⛔ Akses Ditolak - Hanya Admin", SwingConstants.CENTER), BorderLayout.CENTER);
            return;
        }
        dao = new UserDAO();
        setLayout(new BorderLayout(10,10));
        setBorder(new EmptyBorder(15,15,15,15));
        setBackground(new Color(245,247,250));
        build(); loadData();
    }

    private void build() {
        JLabel title = new JLabel("👤 Kelola User");
        title.setFont(new Font("Segoe UI",Font.BOLD,20)); title.setForeground(UITheme.ACCENT_BLUE);
        add(title, BorderLayout.NORTH);

        String[] cols = {"ID","Username","Nama Lengkap","Email","Role","Status"};
        tableModel = new DefaultTableModel(cols,0){ public boolean isCellEditable(int r,int c){return false;} };
        table = new JTable(tableModel); table.setRowHeight(28);
        table.getTableHeader().setBackground(UITheme.ACCENT_BLUE); table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,12));
        table.setFont(new Font("Segoe UI",Font.PLAIN,12));

        JPanel form = new JPanel(); form.setLayout(new BoxLayout(form,BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE); form.setPreferredSize(new Dimension(270,0));
        form.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220,220,220)),new EmptyBorder(15,15,15,15)));

        JLabel lf = new JLabel("Form User"); lf.setFont(new Font("Segoe UI",Font.BOLD,14)); lf.setForeground(UITheme.ACCENT_BLUE); lf.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lf); form.add(Box.createVerticalStrut(10));

        JLabel lu=new JLabel("Username *"); lu.setFont(new Font("Segoe UI",Font.BOLD,11)); lu.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtUsername=new JTextField(); txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE,32)); txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lu); form.add(txtUsername); form.add(Box.createVerticalStrut(5));

        JLabel lp=new JLabel("Password"); lp.setFont(new Font("Segoe UI",Font.BOLD,11)); lp.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtPassword=new JPasswordField(); txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE,32)); txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lp); form.add(txtPassword); form.add(Box.createVerticalStrut(5));

        JLabel ln=new JLabel("Nama Lengkap *"); ln.setFont(new Font("Segoe UI",Font.BOLD,11)); ln.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtNama=new JTextField(); txtNama.setMaximumSize(new Dimension(Integer.MAX_VALUE,32)); txtNama.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(ln); form.add(txtNama); form.add(Box.createVerticalStrut(5));

        JLabel le=new JLabel("Email"); le.setFont(new Font("Segoe UI",Font.BOLD,11)); le.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtEmail=new JTextField(); txtEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE,32)); txtEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(le); form.add(txtEmail); form.add(Box.createVerticalStrut(5));

        JLabel lr=new JLabel("Role *"); lr.setFont(new Font("Segoe UI",Font.BOLD,11)); lr.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbRole=new JComboBox<>(new String[]{"ADMIN","KASIR","STAFF_GUDANG","SUPERVISOR"});
        cmbRole.setMaximumSize(new Dimension(Integer.MAX_VALUE,32)); cmbRole.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lr); form.add(cmbRole); form.add(Box.createVerticalStrut(8));

        chkAktif=new JCheckBox("Aktif"); chkAktif.setSelected(true); chkAktif.setOpaque(false); chkAktif.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(chkAktif); form.add(Box.createVerticalStrut(12));

        JPanel bp=new JPanel(new FlowLayout(FlowLayout.LEFT,5,0)); bp.setOpaque(false); bp.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnTambah=new JButton("➕"); btnSimpan=new JButton("💾 Simpan"); btnHapus=new JButton("🗑️ Hapus"); btnBatal=new JButton("✖");
        sb(btnTambah,new Color(46,204,113)); sb(btnSimpan,UITheme.ACCENT_BLUE); sb(btnHapus,new Color(231,76,60)); sb(btnBatal,new Color(149,165,166));
        bp.add(btnTambah); bp.add(btnSimpan); bp.add(btnHapus); bp.add(btnBatal);
        form.add(bp);

        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(table), form);
        sp.setDividerLocation(580); sp.setBorder(null);
        add(sp, BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            int row=table.getSelectedRow();
            if(row>=0){ selectedId=(int)tableModel.getValueAt(row,0); txtUsername.setText(s(tableModel.getValueAt(row,1))); txtNama.setText(s(tableModel.getValueAt(row,2))); txtEmail.setText(s(tableModel.getValueAt(row,3))); cmbRole.setSelectedItem(tableModel.getValueAt(row,4)); chkAktif.setSelected("Aktif".equals(tableModel.getValueAt(row,5))); txtPassword.setText(""); }
        });
        btnTambah.addActionListener(e->clear());
        btnSimpan.addActionListener(e->simpan());
        btnHapus.addActionListener(e->hapus());
        btnBatal.addActionListener(e->clear());
    }

    private void simpan() {
        if(txtUsername.getText().isBlank()||txtNama.getText().isBlank()){AlertUtil.showWarning(this,"Username dan nama wajib diisi!");return;}
        User u=new User(); u.setId(selectedId); u.setUsername(txtUsername.getText().trim()); u.setNamaLengkap(txtNama.getText().trim()); u.setEmail(txtEmail.getText().trim()); u.setRole((String)cmbRole.getSelectedItem()); u.setAktif(chkAktif.isSelected());
        String pw=new String(txtPassword.getPassword());
        if(selectedId==-1){
            if(pw.isBlank()){AlertUtil.showWarning(this,"Password wajib diisi untuk user baru!");return;}
            u.setPassword(PasswordHasher.hash(pw));
            if(dao.insert(u)){AlertUtil.showInfo(this,"User berhasil ditambah!");loadData();clear();}else AlertUtil.showError(this,"Gagal menambah user.");
        } else {
            if(dao.update(u)){
                if(!pw.isBlank()) dao.updatePassword(selectedId,PasswordHasher.hash(pw));
                AlertUtil.showInfo(this,"User berhasil diupdate!");loadData();clear();
            } else AlertUtil.showError(this,"Gagal update user.");
        }
    }

    private void hapus(){
        if(selectedId==-1){AlertUtil.showWarning(this,"Pilih user dahulu!");return;}
        if(selectedId==Session.currentUser.getId()){AlertUtil.showWarning(this,"Tidak bisa hapus user sendiri!");return;}
        if(AlertUtil.showConfirm(this,"Hapus user ini?")){if(dao.delete(selectedId)){AlertUtil.showInfo(this,"User dihapus.");loadData();clear();}else AlertUtil.showError(this,"Gagal.");}
    }

    private void loadData(){
        tableModel.setRowCount(0);
        for(User u:dao.getAll()) tableModel.addRow(new Object[]{u.getId(),u.getUsername(),u.getNamaLengkap(),u.getEmail(),u.getRole(),u.isAktif()?"Aktif":"Non-Aktif"});
    }

    private void clear(){selectedId=-1;txtUsername.setText("");txtPassword.setText("");txtNama.setText("");txtEmail.setText("");cmbRole.setSelectedIndex(0);chkAktif.setSelected(true);}
    private void sb(JButton b,Color c){b.setBackground(c);b.setForeground(Color.WHITE);b.setFont(new Font("Segoe UI",Font.BOLD,11));b.setFocusPainted(false);b.setBorderPainted(false);b.setCursor(new Cursor(Cursor.HAND_CURSOR));}
    private String s(Object o){return o==null?"":o.toString();}
}
