package com.app.smartretail.view.pegawai;

import com.app.smartretail.utils.*;
import com.app.smartretail.view.component.Icons;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

public class PegawaiForm extends JPanel {

    private JTable table; private DefaultTableModel mdl;
    private JTextField fNama, fJabatan, fTlp, fEmail, fAlamat, fTglMasuk;
    private JComboBox<String> cmbStatus;
    private JButton btnSimpan, btnBatal, btnNew, btnHapus;
    private JLabel lblAvatar;
    private int selId = -1;

    // Sample data (in production: load from DB table `pegawai`)
    private static final Object[][] SAMPLE = {
        {1,"Budi Santoso","Kasir","0812-3456-7890","budi@srms.com","AKTIF","01/03/2022"},
        {2,"Dewi Rahayu","Staff Gudang","0856-1234-5678","dewi@srms.com","AKTIF","15/06/2021"},
        {3,"Ahmad Fauzi","Supervisor","0821-9876-5432","ahmad@srms.com","AKTIF","10/01/2020"},
        {4,"Siti Nurhaliza","Kasir","0877-4567-8901","siti@srms.com","CUTI","05/09/2023"},
        {5,"Riko Pratama","Staff Gudang","0813-2345-6789","riko@srms.com","AKTIF","20/11/2022"},
    };

    public PegawaiForm() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_SURFACE);
        setBorder(new EmptyBorder(22, 24, 22, 24));
        build();
        loadSample();
    }

    private void build() {
        // Header
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false); hdr.setBorder(new EmptyBorder(0,0,16,0));
        JPanel ht = new JPanel(); ht.setOpaque(false); ht.setLayout(new BoxLayout(ht, BoxLayout.Y_AXIS));
        ht.add(UITheme.pageTitle("Employees"));
        JLabel sub = new JLabel("Daftar pegawai & manajemen SDM");
        sub.setFont(UITheme.FONT_BODY); sub.setForeground(UITheme.TEXT_SECONDARY); ht.add(sub);

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); acts.setOpaque(false);
        btnNew    = UITheme.primaryButton("+ Pegawai Baru", UITheme.ACCENT_LIME);
        JButton btnReport = UITheme.ghostButton("Export Report", UITheme.ACCENT_AMBER);
        acts.add(btnReport); acts.add(btnNew);
        hdr.add(ht, BorderLayout.WEST); hdr.add(acts, BorderLayout.EAST);
        add(hdr, BorderLayout.NORTH);

        JPanel main = new JPanel(new BorderLayout(16,0)); main.setOpaque(false);

        // Search + filter toolbar
        JPanel tbr = new JPanel(new BorderLayout(8,0)); tbr.setOpaque(false);
        tbr.setBorder(new EmptyBorder(0,0,10,0));
        JTextField search = UITheme.styledField("Cari nama, jabatan…");
        search.setPreferredSize(new Dimension(240,34));
        JComboBox<String> cmbFil = UITheme.styledCombo(new String[]{"Semua Status","AKTIF","CUTI","RESIGN"});
        cmbFil.setPreferredSize(new Dimension(150,34));
        JPanel tbRight = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); tbRight.setOpaque(false);
        tbRight.add(search); tbRight.add(cmbFil);
        tbr.add(new JLabel("Employees List — " + SAMPLE.length + " orang"), BorderLayout.WEST);
        tbr.add(tbRight, BorderLayout.EAST);
        ((JLabel)tbr.getComponent(0)).setFont(UITheme.FONT_H2);
        ((JLabel)tbr.getComponent(0)).setForeground(UITheme.TEXT_PRIMARY);

        // Table
        String[] cols = {"ID","Nama Lengkap","Jabatan","Telepon","Email","Status","Tgl Masuk","Aksi"};
        mdl = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c){ return false; } };
        table = new JTable(mdl); UITheme.styleTable(table);
        table.setRowHeight(40);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(5).setMaxWidth(80);
        table.getColumnModel().getColumn(6).setMaxWidth(100);
        table.getColumnModel().getColumn(7).setMaxWidth(80);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c){
                Component cp=super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                cp.setBackground(sel?new Color(238,242,255):(r%2==0?UITheme.BG_CARD:UITheme.BG_ROW_ALT));
                if(c==5&&v!=null){
                    String st=v.toString();
                    cp.setForeground("AKTIF".equals(st)?UITheme.ACCENT_TEAL:"CUTI".equals(st)?UITheme.ACCENT_AMBER:UITheme.ACCENT_CORAL);
                } else if(c==7) { cp.setForeground(UITheme.ACCENT_BLUE); }
                else cp.setForeground(UITheme.TEXT_PRIMARY);
                ((JLabel)cp).setBorder(new EmptyBorder(0,12,0,12));
                if(c==0)((JLabel)cp).setHorizontalAlignment(SwingConstants.CENTER);
                return cp;
            }
        });

        JPanel tableCard = UITheme.card();
        tableCard.setLayout(new BorderLayout(0,10));
        tableCard.add(tbr, BorderLayout.NORTH);
        tableCard.add(UITheme.styledScroll(table), BorderLayout.CENTER);

        // Form card
        JPanel fc = UITheme.card();
        fc.setLayout(new BoxLayout(fc, BoxLayout.Y_AXIS));
        fc.setPreferredSize(new Dimension(272,0));

        JLabel lf = new JLabel("Detail Pegawai"); lf.setFont(UITheme.FONT_H2); lf.setForeground(UITheme.TEXT_PRIMARY); lf.setAlignmentX(LEFT_ALIGNMENT);

        lblAvatar = new JLabel(Icons.avatarIcon("?", UITheme.ACCENT_BLUE, 56));
        lblAvatar.setHorizontalAlignment(SwingConstants.CENTER);
        lblAvatar.setAlignmentX(LEFT_ALIGNMENT);
        JPanel avRow = new JPanel(new FlowLayout(FlowLayout.CENTER)); avRow.setOpaque(false); avRow.setAlignmentX(LEFT_ALIGNMENT);
        avRow.setMaximumSize(new Dimension(Integer.MAX_VALUE,70)); avRow.add(lblAvatar);

        fNama     = fld("Nama Lengkap *", fc);
        fJabatan  = fld("Jabatan",        fc);
        fTlp      = fld("Telepon",        fc);
        fEmail    = fld("Email",          fc);
        fAlamat   = fld("Alamat",         fc);
        fTglMasuk = fld("Tanggal Masuk",  fc);

        JLabel lStat = UITheme.fieldLabel("Status"); lStat.setAlignmentX(LEFT_ALIGNMENT);
        cmbStatus = UITheme.styledCombo(new String[]{"AKTIF","CUTI","RESIGN"});
        cmbStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE,34)); cmbStatus.setAlignmentX(LEFT_ALIGNMENT);

        JPanel br1 = new JPanel(new GridLayout(1,2,8,0)); br1.setOpaque(false);
        br1.setMaximumSize(new Dimension(Integer.MAX_VALUE,34)); br1.setAlignmentX(LEFT_ALIGNMENT);
        btnNew  = UITheme.ghostButton("+ Baru", UITheme.ACCENT_BLUE);
        btnHapus= UITheme.dangerButton("Hapus");
        br1.add(btnNew); br1.add(btnHapus);

        JPanel br2 = new JPanel(new GridLayout(1,2,8,0)); br2.setOpaque(false);
        br2.setMaximumSize(new Dimension(Integer.MAX_VALUE,34)); br2.setAlignmentX(LEFT_ALIGNMENT);
        btnBatal  = UITheme.ghostButton("Batal", UITheme.TEXT_SECONDARY);
        btnSimpan = UITheme.primaryButton("Simpan", UITheme.ACCENT_LIME);
        br2.add(btnBatal); br2.add(btnSimpan);

        fc.add(lf); fc.add(Box.createVerticalStrut(8)); fc.add(UITheme.separator()); fc.add(Box.createVerticalStrut(8));
        fc.add(avRow); fc.add(Box.createVerticalStrut(8));
        fc.add(lStat); fc.add(Box.createVerticalStrut(4)); fc.add(cmbStatus); fc.add(Box.createVerticalStrut(8));
        fc.add(Box.createVerticalGlue());
        fc.add(br1); fc.add(Box.createVerticalStrut(6)); fc.add(br2);

        main.add(tableCard, BorderLayout.CENTER); main.add(fc, BorderLayout.EAST);
        add(main, BorderLayout.CENTER);

        // Events
        btnNew.addActionListener(e -> clearF());
        btnBatal.addActionListener(e -> clearF());
        btnSimpan.addActionListener(e -> AlertUtil.showInfo(this, "Data pegawai disimpan!\n(Fitur terhubung ke DB pada implementasi penuh)"));
        btnHapus.addActionListener(e -> { if(selId==-1){AlertUtil.showWarning(this,"Pilih pegawai!");return;} if(AlertUtil.showConfirm(this,"Hapus pegawai ini?")) AlertUtil.showInfo(this,"Dihapus."); });
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow(); if(row<0) return;
            selId = (int)mdl.getValueAt(row,0);
            fNama.setText(s(mdl.getValueAt(row,1))); fJabatan.setText(s(mdl.getValueAt(row,2)));
            fTlp.setText(s(mdl.getValueAt(row,3))); fEmail.setText(s(mdl.getValueAt(row,4)));
            fTglMasuk.setText(s(mdl.getValueAt(row,6)));
            cmbStatus.setSelectedItem(s(mdl.getValueAt(row,5)));
            String initials = getInitials(s(mdl.getValueAt(row,1)));
            Color ac = "AKTIF".equals(s(mdl.getValueAt(row,5))) ? UITheme.ACCENT_TEAL :
                       "CUTI".equals(s(mdl.getValueAt(row,5)))  ? UITheme.ACCENT_AMBER : UITheme.TEXT_MUTED;
            lblAvatar.setIcon(Icons.avatarIcon(initials, ac, 56));
        });
    }

    private void loadSample() {
        mdl.setRowCount(0);
        for (Object[] row : SAMPLE)
            mdl.addRow(new Object[]{row[0],row[1],row[2],row[3],row[4],row[5],row[6],"Detail"});
    }

    private void clearF(){ selId=-1; fNama.setText(""); fJabatan.setText(""); fTlp.setText(""); fEmail.setText(""); fAlamat.setText(""); fTglMasuk.setText(""); cmbStatus.setSelectedIndex(0); lblAvatar.setIcon(Icons.avatarIcon("?",UITheme.ACCENT_BLUE,56)); }
    private JTextField fld(String label, JPanel p){JLabel l=UITheme.fieldLabel(label);l.setAlignmentX(LEFT_ALIGNMENT);JTextField f=UITheme.styledField("");f.setMaximumSize(new Dimension(Integer.MAX_VALUE,34));f.setAlignmentX(LEFT_ALIGNMENT);p.add(l);p.add(Box.createVerticalStrut(4));p.add(f);p.add(Box.createVerticalStrut(8));return f;}
    private String s(Object o){return o==null?"":o.toString();}
    private String getInitials(String n){if(n==null||n.isEmpty())return"?";String[]p=n.trim().split(" ");return p.length==1?p[0].substring(0,Math.min(2,p[0].length())).toUpperCase():(""+p[0].charAt(0)+p[p.length-1].charAt(0)).toUpperCase();}
}
