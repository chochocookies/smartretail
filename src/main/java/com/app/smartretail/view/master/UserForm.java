package com.app.smartretail.view.master;

import com.app.smartretail.dao.UserDAO;
import com.app.smartretail.model.User;
import com.app.smartretail.utils.*;
import com.app.smartretail.view.component.Icons;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class UserForm extends JPanel {

    private final UserDAO dao = new UserDAO();
    private JTable table;
    private DefaultTableModel mdl;
    private JTextField txtUsername, txtNama, txtEmail;
    private JPasswordField txtPassword;
    private JComboBox<String> cmbRole;
    private JCheckBox chkAktif;
    private JButton btnSimpan, btnHapus, btnBatal, btnNew;
    private JLabel lblTitle, lblAvatar;
    private int selId = -1;

    public UserForm() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_SURFACE);
        setBorder(new EmptyBorder(22, 24, 22, 24));
        if (!Session.isAdmin()) {
            add(noAccess(), BorderLayout.CENTER); return;
        }
        build();
        load();
    }

    private JPanel noAccess() {
        JPanel p = new JPanel(new GridBagLayout()); p.setBackground(UITheme.BG_SURFACE);
        JLabel l = new JLabel("<html><center>\u26D4 Akses Ditolak<br><small>Hanya Admin yang dapat mengelola user.</small></center></html>");
        l.setFont(UITheme.FONT_H2); l.setForeground(UITheme.TEXT_MUTED);
        l.setHorizontalAlignment(SwingConstants.CENTER); p.add(l); return p;
    }

    private void build() {
        // ── Header ──────────────────────────────────────────────
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false); hdr.setBorder(new EmptyBorder(0,0,16,0));
        JPanel ht = new JPanel(); ht.setOpaque(false);
        ht.setLayout(new BoxLayout(ht, BoxLayout.Y_AXIS));
        ht.add(UITheme.pageTitle("User Management"));
        JLabel sub = new JLabel("Kelola akun pengguna & hak akses sistem");
        sub.setFont(UITheme.FONT_BODY); sub.setForeground(UITheme.TEXT_SECONDARY); ht.add(sub);

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); acts.setOpaque(false);
        JButton btnRef = UITheme.ghostButton("\u21BB", UITheme.TEXT_MUTED);
        btnHapus = UITheme.dangerButton("Hapus User");
        btnNew   = UITheme.primaryButton("+ User Baru", UITheme.ACCENT_LIME);
        acts.add(btnRef); acts.add(btnHapus); acts.add(btnNew);
        hdr.add(ht, BorderLayout.WEST); hdr.add(acts, BorderLayout.EAST);
        add(hdr, BorderLayout.NORTH);

        // ── Main split ───────────────────────────────────────────
        JPanel main = new JPanel(new BorderLayout(16, 0)); main.setOpaque(false);

        // LEFT: table card
        JPanel tableCard = UITheme.card(); tableCard.setLayout(new BorderLayout(0,10));

        JPanel tbr = new JPanel(new BorderLayout(8,0)); tbr.setOpaque(false);
        JTextField search = UITheme.styledField("Cari username, nama\u2026");
        search.setPreferredSize(new Dimension(0,34));
        JComboBox<String> cmbFilter = UITheme.styledCombo(new String[]{"Semua Role","ADMIN","KASIR","STAFF_GUDANG","SUPERVISOR"});
        cmbFilter.setPreferredSize(new Dimension(150,34));
        tbr.add(search, BorderLayout.CENTER);
        tbr.add(cmbFilter, BorderLayout.EAST);

        String[] cols = {"#","Username","Nama Lengkap","Email","Role","Status"};
        mdl = new DefaultTableModel(cols,0){ public boolean isCellEditable(int r,int c){return false;} };
        table = new JTable(mdl); UITheme.styleTable(table);
        table.setRowHeight(40);
        table.getColumnModel().getColumn(0).setMaxWidth(38);
        table.getColumnModel().getColumn(4).setMaxWidth(110);
        table.getColumnModel().getColumn(5).setMaxWidth(80);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c){
                Component cp=super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                cp.setBackground(sel?new Color(238,242,255):(r%2==0?UITheme.BG_CARD:UITheme.BG_ROW_ALT));
                if(c==4&&v!=null) cp.setForeground(roleColor(v.toString()));
                else if(c==5&&v!=null) cp.setForeground("Aktif".equals(v.toString())?UITheme.ACCENT_TEAL:UITheme.ACCENT_CORAL);
                else cp.setForeground(UITheme.TEXT_PRIMARY);
                ((JLabel)cp).setBorder(new EmptyBorder(0,12,0,12));
                if(c==0)((JLabel)cp).setHorizontalAlignment(SwingConstants.CENTER);
                return cp;
            }
        });
        tableCard.add(tbr, BorderLayout.NORTH);
        tableCard.add(UITheme.styledScroll(table), BorderLayout.CENTER);

        // RIGHT: form card
        JPanel fc = UITheme.card(); fc.setLayout(new BoxLayout(fc,BoxLayout.Y_AXIS));
        fc.setPreferredSize(new Dimension(284,0)); fc.setMaximumSize(new Dimension(284,Integer.MAX_VALUE));

        lblTitle = new JLabel("Detail User");
        lblTitle.setFont(UITheme.FONT_H2); lblTitle.setForeground(UITheme.TEXT_PRIMARY); lblTitle.setAlignmentX(LEFT_ALIGNMENT);

        // Avatar preview
        JPanel avRow = new JPanel(new FlowLayout(FlowLayout.CENTER,0,0)); avRow.setOpaque(false);
        avRow.setAlignmentX(LEFT_ALIGNMENT); avRow.setMaximumSize(new Dimension(Integer.MAX_VALUE,70));
        lblAvatar = new JLabel(Icons.avatarIcon("?", UITheme.ACCENT_BLUE, 54)); avRow.add(lblAvatar);

        // Role badge row
        JPanel roleRow = new JPanel(new FlowLayout(FlowLayout.CENTER,6,0)); roleRow.setOpaque(false);
        roleRow.setAlignmentX(LEFT_ALIGNMENT); roleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE,28));
        for(String r:new String[]{"ADMIN","KASIR","GUDANG","SPV"}) {
            JLabel b = UITheme.badge(r, roleColorFor(r), roleColorFor(r)); roleRow.add(b);
        }

        txtUsername = fld("Username *", fc);
        txtNama     = fld("Nama Lengkap *", fc);
        txtEmail    = fld("Email", fc);

        JLabel lP = UITheme.fieldLabel("Password"); lP.setAlignmentX(LEFT_ALIGNMENT);
        txtPassword = UITheme.styledPassword(); txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE,36)); txtPassword.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lR = UITheme.fieldLabel("Role *"); lR.setAlignmentX(LEFT_ALIGNMENT);
        cmbRole = UITheme.styledCombo(new String[]{"KASIR","ADMIN","STAFF_GUDANG","SUPERVISOR"});
        cmbRole.setMaximumSize(new Dimension(Integer.MAX_VALUE,34)); cmbRole.setAlignmentX(LEFT_ALIGNMENT);
        cmbRole.addActionListener(e -> refreshAvatar());

        chkAktif = new JCheckBox("User Aktif"); chkAktif.setSelected(true); chkAktif.setOpaque(false);
        chkAktif.setFont(UITheme.FONT_BODY); chkAktif.setForeground(UITheme.TEXT_PRIMARY);
        chkAktif.setAlignmentX(LEFT_ALIGNMENT);

        JPanel br1=gr(UITheme.ghostButton("+ Baru",UITheme.ACCENT_BLUE), UITheme.dangerButton("Hapus"));
        JPanel br2=gr(UITheme.ghostButton("Batal",UITheme.TEXT_SECONDARY), UITheme.primaryButton("Simpan",UITheme.ACCENT_LIME));
        JButton btnNew2  = extractBtn(br1,0); JButton btnH2   = extractBtn(br1,1);
        JButton btnBatal = extractBtn(br2,0);      btnSimpan  = extractBtn(br2,1);

        fc.add(lblTitle); fc.add(Box.createVerticalStrut(6));
        fc.add(UITheme.separator()); fc.add(Box.createVerticalStrut(8));
        fc.add(avRow); fc.add(Box.createVerticalStrut(4)); fc.add(roleRow);
        fc.add(Box.createVerticalStrut(12));
        fc.add(lP); fc.add(Box.createVerticalStrut(4)); fc.add(txtPassword); fc.add(Box.createVerticalStrut(8));
        fc.add(lR); fc.add(Box.createVerticalStrut(4)); fc.add(cmbRole); fc.add(Box.createVerticalStrut(8));
        fc.add(chkAktif); fc.add(Box.createVerticalGlue());
        fc.add(Box.createVerticalStrut(12));
        fc.add(br1); fc.add(Box.createVerticalStrut(6)); fc.add(br2);

        main.add(tableCard, BorderLayout.CENTER); main.add(fc, BorderLayout.EAST);
        add(main, BorderLayout.CENTER);

        // Events
        search.addActionListener(e -> filterTable(search.getText(), cmbFilter.getSelectedItem().toString()));
        cmbFilter.addActionListener(e -> filterTable(search.getText(), cmbFilter.getSelectedItem().toString()));
        btnRef.addActionListener(e->load());
        btnNew.addActionListener(e->clearF()); btnNew2.addActionListener(e->clearF());
        btnSimpan.addActionListener(e->simpan());
        btnBatal.addActionListener(e->clearF());
        btnHapus.addActionListener(e->hapus()); btnH2.addActionListener(e->hapus());
        table.getSelectionModel().addListSelectionListener(e->{ if(!e.getValueIsAdjusting()) fillForm(); });
    }

    private void load() { fillTable(dao.getAll()); }

    private void filterTable(String kw, String role) {
        List<User> all = dao.getAll();
        mdl.setRowCount(0); int n=1;
        for(User u:all){
            boolean matchRole = "Semua Role".equals(role) || u.getRole().equals(role);
            boolean matchKw   = kw.isBlank() || u.getUsername().contains(kw) || u.getNamaLengkap().contains(kw);
            if(matchRole && matchKw)
                mdl.addRow(new Object[]{n++,u.getUsername(),u.getNamaLengkap(),u.getEmail(),u.getRole(),u.isAktif()?"Aktif":"Non-Aktif"});
        }
    }

    private void fillTable(List<User> list) {
        mdl.setRowCount(0); int n=1;
        for(User u:list)
            mdl.addRow(new Object[]{n++,u.getUsername(),u.getNamaLengkap(),u.getEmail(),u.getRole(),u.isAktif()?"Aktif":"Non-Aktif"});
    }

    private void fillForm(){
        int row=table.getSelectedRow(); if(row<0)return;
        String uname = mdl.getValueAt(row,1).toString();
        // load from dao by username
        dao.getAll().stream().filter(u->u.getUsername().equals(uname)).findFirst().ifPresent(u->{
            selId=u.getId(); txtUsername.setText(u.getUsername()); txtNama.setText(u.getNamaLengkap());
            txtEmail.setText(u.getEmail()!=null?u.getEmail():""); txtPassword.setText("");
            cmbRole.setSelectedItem(u.getRole()); chkAktif.setSelected(u.isAktif());
            lblTitle.setText(u.getNamaLengkap().length()>20?u.getNamaLengkap().substring(0,19)+"\u2026":u.getNamaLengkap());
            refreshAvatar();
        });
    }

    private void refreshAvatar(){
        String role = cmbRole.getSelectedItem() != null ? cmbRole.getSelectedItem().toString() : "";
        Color c = roleColor(role);
        String init = txtNama.getText().isBlank() ? "?" : getInit(txtNama.getText());
        lblAvatar.setIcon(Icons.avatarIcon(init, c, 54));
    }

    private void simpan(){
        if(txtUsername.getText().isBlank()||txtNama.getText().isBlank()){
            AlertUtil.showWarning(this,"Username dan nama lengkap wajib diisi!"); return;
        }
        User u=new User(); u.setId(selId); u.setUsername(txtUsername.getText().trim());
        u.setNamaLengkap(txtNama.getText().trim()); u.setEmail(txtEmail.getText().trim());
        u.setRole(cmbRole.getSelectedItem().toString()); u.setAktif(chkAktif.isSelected());
        String pw = new String(txtPassword.getPassword());
        if(selId==-1){
            if(pw.isBlank()){AlertUtil.showWarning(this,"Password wajib untuk user baru!");return;}
            u.setPassword(PasswordHasher.hash(pw));
            if(dao.insert(u)){AlertUtil.showInfo(this,"User berhasil ditambahkan!");load();clearF();}
            else AlertUtil.showError(this,"Gagal menambah user.");
        } else {
            if(dao.update(u)){
                if(!pw.isBlank()) dao.updatePassword(selId, PasswordHasher.hash(pw));
                AlertUtil.showInfo(this,"User berhasil diupdate!"); load(); clearF();
            } else AlertUtil.showError(this,"Gagal update user.");
        }
    }

    private void hapus(){
        if(selId==-1){AlertUtil.showWarning(this,"Pilih user terlebih dahulu!");return;}
        if(selId==Session.currentUser.getId()){AlertUtil.showWarning(this,"Tidak bisa menghapus akun sendiri!");return;}
        if(!AlertUtil.showConfirm(this,"Hapus user ini?\nTindakan tidak dapat dibatalkan."))return;
        if(dao.delete(selId)){AlertUtil.showInfo(this,"User dihapus.");load();clearF();}
        else AlertUtil.showError(this,"Gagal menghapus.");
    }

    private void clearF(){
        selId=-1; txtUsername.setText(""); txtNama.setText(""); txtEmail.setText("");
        txtPassword.setText(""); cmbRole.setSelectedIndex(0); chkAktif.setSelected(true);
        lblTitle.setText("Detail User"); lblAvatar.setIcon(Icons.avatarIcon("?",UITheme.ACCENT_BLUE,54));
        table.clearSelection();
    }

    // ── UI helpers ────────────────────────────────────────────────
    private JTextField fld(String label, JPanel p){
        JLabel l=UITheme.fieldLabel(label);l.setAlignmentX(LEFT_ALIGNMENT);
        JTextField f=UITheme.styledField("");f.setMaximumSize(new Dimension(Integer.MAX_VALUE,36));f.setAlignmentX(LEFT_ALIGNMENT);
        f.addFocusListener(new java.awt.event.FocusAdapter(){ public void focusLost(java.awt.event.FocusEvent e){ refreshAvatar(); } });
        p.add(l);p.add(Box.createVerticalStrut(4));p.add(f);p.add(Box.createVerticalStrut(8));return f;
    }
    private JPanel gr(JButton l, JButton r){
        JPanel p=new JPanel(new GridLayout(1,2,8,0));p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE,34));p.setAlignmentX(LEFT_ALIGNMENT);
        JPanel wL=new JPanel(new BorderLayout());wL.setOpaque(false);wL.add(l);
        JPanel wR=new JPanel(new BorderLayout());wR.setOpaque(false);wR.add(r);
        p.add(wL);p.add(wR);return p;
    }
    private JButton extractBtn(JPanel gr, int idx){return(JButton)((JPanel)gr.getComponent(idx)).getComponent(0);}
    private Color roleColor(String r){switch(r==null?"":r){case "ADMIN":return UITheme.ACCENT_CORAL;case "SUPERVISOR":return UITheme.ACCENT_AMBER;case "STAFF_GUDANG":return UITheme.ACCENT_TEAL;default:return UITheme.ACCENT_BLUE;}}
    private Color roleColorFor(String r){switch(r){case "ADMIN":return UITheme.ACCENT_CORAL;case "SPV":return UITheme.ACCENT_AMBER;case "GUDANG":return UITheme.ACCENT_TEAL;default:return UITheme.ACCENT_BLUE;}}
    private String getInit(String n){String[]p=n.trim().split(" ");return p.length==1?p[0].substring(0,Math.min(2,p[0].length())).toUpperCase():(""+p[0].charAt(0)+p[p.length-1].charAt(0)).toUpperCase();}
}
