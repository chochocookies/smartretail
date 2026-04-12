package com.app.smartretail.view.master;

import com.app.smartretail.config.DatabaseConnection;
import com.app.smartretail.utils.*;
import com.app.smartretail.view.component.Icons;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class CustomerForm extends JPanel {

    private JTable table; private DefaultTableModel mdl;
    private JTextField txtKode, txtNama, txtTlp, txtEmail, txtAlamat;
    private JButton btnSimpan, btnHapus, btnBatal, btnNew;
    private int selId = -1;

    public CustomerForm() {
        setLayout(new BorderLayout()); setBackground(UITheme.BG_SURFACE);
        setBorder(new EmptyBorder(22,24,22,24));
        build(); load();
    }

    private void build() {
        // Header with tabs (Customers | Employees | Attendance)
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false); hdr.setBorder(new EmptyBorder(0,0,0,0));
        JPanel ht = new JPanel(); ht.setOpaque(false); ht.setLayout(new BoxLayout(ht, BoxLayout.Y_AXIS));
        JLabel title = UITheme.pageTitle("Customers & HR");
        JLabel sub = new JLabel("Kelola data pelanggan dan SDM");
        sub.setFont(UITheme.FONT_BODY); sub.setForeground(UITheme.TEXT_SECONDARY);
        ht.add(title); ht.add(sub);

        JPanel tabRow = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        tabRow.setOpaque(false);
        for (String t : new String[]{"Customers","Employees","Attendance Tracking"}) {
            boolean a = "Customers".equals(t);
            JButton b = new JButton(t){
                @Override protected void paintComponent(Graphics g){
                    Graphics2D g2=(Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if(a){g2.setColor(UITheme.TEXT_PRIMARY);g2.setStroke(new BasicStroke(2));g2.drawLine(0,getHeight()-2,getWidth(),getHeight()-2);}
                    g2.dispose(); super.paintComponent(g);
                }
            };
            b.setFont(a?new Font("Segoe UI",Font.BOLD,12):UITheme.FONT_BODY);
            b.setForeground(a?UITheme.TEXT_PRIMARY:UITheme.TEXT_MUTED);
            b.setOpaque(false); b.setContentAreaFilled(false);
            b.setBorderPainted(false); b.setFocusPainted(false);
            b.setBorder(new EmptyBorder(8,12,8,12));
            tabRow.add(b);
        }

        hdr.add(ht, BorderLayout.NORTH);
        hdr.add(tabRow, BorderLayout.SOUTH);
        add(hdr, BorderLayout.NORTH);

        JPanel main = new JPanel(new BorderLayout(16,0)); main.setOpaque(false);
        main.setBorder(new EmptyBorder(12,0,0,0));

        // Search + new
        JPanel toolbar = new JPanel(new BorderLayout(8,0)); toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(0,0,10,0));
        JLabel listTitle = new JLabel("Customers List");
        listTitle.setFont(UITheme.FONT_H2); listTitle.setForeground(UITheme.TEXT_PRIMARY);
        JPanel tbRight = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); tbRight.setOpaque(false);
        JTextField txtS = UITheme.styledField("Cari customer…");
        txtS.setPreferredSize(new Dimension(220,34));
        JComboBox<String> cmbFilter = UITheme.styledCombo(new String[]{"Membership Status","Silver","Gold","Platinum"});
        cmbFilter.setPreferredSize(new Dimension(160,34));
        tbRight.add(txtS); tbRight.add(cmbFilter);
        toolbar.add(listTitle, BorderLayout.WEST);
        toolbar.add(tbRight, BorderLayout.EAST);

        // Table inside card
        JPanel tblCard = UITheme.card();
        tblCard.setLayout(new BorderLayout(0,0));
        String[] cols = {"Customer ID","Customer Name","Phone","Email","Total Amount","Membership","Action"};
        mdl = new DefaultTableModel(cols,0){ public boolean isCellEditable(int r,int c){return false;} };
        table = new JTable(mdl); UITheme.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setMaxWidth(100);
        table.getColumnModel().getColumn(6).setMaxWidth(80);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c){
                Component cp=super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                cp.setBackground(sel?new Color(238,242,255):(r%2==0?UITheme.BG_CARD:UITheme.BG_ROW_ALT));
                cp.setForeground(UITheme.TEXT_PRIMARY);
                if(c==5&&v!=null){ String s=v.toString(); cp.setForeground("Gold".equals(s)?UITheme.ACCENT_AMBER:"Platinum".equals(s)?UITheme.ACCENT_PURPLE:UITheme.TEXT_SECONDARY); }
                ((JLabel)cp).setBorder(new EmptyBorder(0,14,0,14));
                return cp;
            }
        });
        tblCard.add(toolbar, BorderLayout.NORTH);
        tblCard.add(UITheme.styledScroll(table), BorderLayout.CENTER);
        tblCard.add(buildPagination(), BorderLayout.SOUTH);

        // Form card
        JPanel fc = UITheme.card();
        fc.setLayout(new BoxLayout(fc, BoxLayout.Y_AXIS));
        fc.setPreferredSize(new Dimension(270,0));
        JLabel lf = new JLabel("Form Customer"); lf.setFont(UITheme.FONT_H2); lf.setForeground(UITheme.TEXT_PRIMARY); lf.setAlignmentX(LEFT_ALIGNMENT);
        txtKode  = f2("Kode Customer", fc); txtNama  = f2("Nama Customer *", fc);
        txtTlp   = f2("Telepon", fc); txtEmail = f2("Email", fc); txtAlamat= f2("Alamat", fc);
        JPanel br=new JPanel(new GridLayout(1,2,8,0)); br.setOpaque(false); br.setMaximumSize(new Dimension(Integer.MAX_VALUE,34)); br.setAlignmentX(LEFT_ALIGNMENT);
        btnNew=UITheme.ghostButton("+ Baru",UITheme.ACCENT_BLUE); btnSimpan=UITheme.primaryButton("Simpan",UITheme.ACCENT_LIME);
        btnHapus=UITheme.dangerButton("Hapus"); btnBatal=UITheme.ghostButton("Batal",UITheme.TEXT_SECONDARY);
        br.add(btnBatal); br.add(btnSimpan);
        JPanel br2=new JPanel(new GridLayout(1,2,8,0)); br2.setOpaque(false); br2.setMaximumSize(new Dimension(Integer.MAX_VALUE,34)); br2.setAlignmentX(LEFT_ALIGNMENT);
        br2.add(btnNew); br2.add(btnHapus);
        fc.add(lf); fc.add(Box.createVerticalStrut(8)); fc.add(UITheme.separator()); fc.add(Box.createVerticalStrut(10));
        fc.add(Box.createVerticalGlue()); fc.add(br2); fc.add(Box.createVerticalStrut(6)); fc.add(br);

        main.add(tblCard, BorderLayout.CENTER);
        main.add(fc, BorderLayout.EAST);
        add(main, BorderLayout.CENTER);

        btnNew.addActionListener(e->clear()); btnSimpan.addActionListener(e->simpan());
        btnHapus.addActionListener(e->hapus()); btnBatal.addActionListener(e->clear());
        table.getSelectionModel().addListSelectionListener(e->{
            int row=table.getSelectedRow();
            if(row>=0){ selId=(int)mdl.getValueAt(row,0); txtKode.setText(s(mdl.getValueAt(row,0)));
                txtNama.setText(s(mdl.getValueAt(row,1))); txtTlp.setText(s(mdl.getValueAt(row,2))); txtEmail.setText(s(mdl.getValueAt(row,3))); }
        });
    }

    private JPanel buildPagination() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER,6,6));
        p.setOpaque(false); p.setBorder(BorderFactory.createMatteBorder(1,0,0,0,UITheme.BORDER_DEFAULT));
        for (int i=0;i<5;i++){
            JButton b=new JButton(i==0?"1":String.valueOf(i+1));
            b.setFont(UITheme.FONT_SMALL);
            b.setPreferredSize(new Dimension(30,30));
            if(i==0){b.setBackground(UITheme.ACCENT_LIME);b.setForeground(UITheme.TEXT_ON_LIME);b.setOpaque(true);}
            else{b.setOpaque(false);b.setForeground(UITheme.TEXT_MUTED);}
            b.setContentAreaFilled(i==0); b.setBorderPainted(false); b.setFocusPainted(false);
            p.add(b);
        }
        return p;
    }

    private void simpan(){
        if(txtNama.getText().isBlank()){AlertUtil.showWarning(this,"Nama customer wajib diisi!");return;}
        Connection conn=DatabaseConnection.getInstance();
        try{
            if(selId==-1){
                PreparedStatement ps=conn.prepareStatement("INSERT INTO customer(kode_customer,nama_customer,telepon,email,alamat,poin) VALUES(?,?,?,?,?,0)");
                ps.setString(1,txtKode.getText().trim());ps.setString(2,txtNama.getText().trim());ps.setString(3,txtTlp.getText().trim());ps.setString(4,txtEmail.getText().trim());ps.setString(5,txtAlamat.getText().trim());
                ps.executeUpdate();
            } else {
                PreparedStatement ps=conn.prepareStatement("UPDATE customer SET kode_customer=?,nama_customer=?,telepon=?,email=?,alamat=? WHERE id=?");
                ps.setString(1,txtKode.getText().trim());ps.setString(2,txtNama.getText().trim());ps.setString(3,txtTlp.getText().trim());ps.setString(4,txtEmail.getText().trim());ps.setString(5,txtAlamat.getText().trim());ps.setInt(6,selId);
                ps.executeUpdate();
            }
            AlertUtil.showInfo(this,"Customer berhasil disimpan!"); load(); clear();
        }catch(SQLException ex){AlertUtil.showError(this,"Error: "+ex.getMessage());}
    }
    private void hapus(){
        if(selId==-1){AlertUtil.showWarning(this,"Pilih customer dahulu!");return;}
        if(!AlertUtil.showConfirm(this,"Hapus customer ini?")) return;
        Connection conn=DatabaseConnection.getInstance();
        try{PreparedStatement ps=conn.prepareStatement("DELETE FROM customer WHERE id=?");ps.setInt(1,selId);ps.executeUpdate();AlertUtil.showInfo(this,"Customer dihapus.");load();clear();}
        catch(SQLException ex){AlertUtil.showError(this,"Error: "+ex.getMessage());}
    }
    private void load(){
        mdl.setRowCount(0);
        Connection conn=DatabaseConnection.getInstance();
        try(Statement st=conn.createStatement();ResultSet rs=st.executeQuery("SELECT * FROM customer ORDER BY nama_customer")){
            while(rs.next()) mdl.addRow(new Object[]{rs.getInt("id"),rs.getString("nama_customer"),rs.getString("telepon"),rs.getString("email"),FormatUtil.formatRupiah(0),"Silver","\uD83D\uDC41 \u270F"});
        }catch(SQLException ex){ex.printStackTrace();}
    }
    private void clear(){selId=-1;txtKode.setText("");txtNama.setText("");txtTlp.setText("");txtEmail.setText("");txtAlamat.setText("");}
    private JTextField f2(String label, JPanel p){JLabel l=UITheme.fieldLabel(label);l.setAlignmentX(LEFT_ALIGNMENT);JTextField f=UITheme.styledField("");f.setMaximumSize(new Dimension(Integer.MAX_VALUE,34));f.setAlignmentX(LEFT_ALIGNMENT);p.add(l);p.add(Box.createVerticalStrut(4));p.add(f);p.add(Box.createVerticalStrut(8));return f;}
    private String s(Object o){return o==null?"":o.toString();}
}
