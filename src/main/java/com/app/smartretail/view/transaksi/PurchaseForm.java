package com.app.smartretail.view.transaksi;

import com.app.smartretail.controller.BarangController;
import com.app.smartretail.controller.TransaksiController;
import com.app.smartretail.dao.SupplierDAO;
import com.app.smartretail.model.*;
import com.app.smartretail.utils.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * PurchaseForm — Combined Purchase + Supplier management.
 * Tabs: New Purchase | Purchase History | Suppliers
 */
public class PurchaseForm extends JPanel {

    private final TransaksiController trxCtrl  = new TransaksiController();
    private final BarangController    barangCtrl= new BarangController();
    private final SupplierDAO         supDAO    = new SupplierDAO();

    // New Purchase state
    private JTextField txtKode, txtBayar;
    private JLabel lblNo, lblTotal, lblGrand;
    private JTable cartTable; private DefaultTableModel cartMdl;
    private JComboBox<String> cmbSupplier;
    private Transaksi currentTrx;

    // History table
    private JTable histTable; private DefaultTableModel histMdl;
    // Supplier table
    private JTable supTable;  private DefaultTableModel supMdl;
    private JTextField sKode,sNama,sAlamat,sTlp,sEmail,sCP;
    private int selSupId = -1;

    private JPanel tabContent;

    public PurchaseForm() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_SURFACE);
        setBorder(new EmptyBorder(22, 24, 22, 24));
        build();
    }

    private void build() {
        // Header
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false); hdr.setBorder(new EmptyBorder(0,0,16,0));
        JPanel ht = new JPanel(); ht.setOpaque(false);
        ht.setLayout(new BoxLayout(ht, BoxLayout.Y_AXIS));
        ht.add(UITheme.pageTitle("Purchase"));
        JLabel sub = new JLabel("Pembelian barang & manajemen supplier");
        sub.setFont(UITheme.FONT_BODY); sub.setForeground(UITheme.TEXT_SECONDARY); ht.add(sub);
        hdr.add(ht, BorderLayout.WEST);
        add(hdr, BorderLayout.NORTH);

        // Tab strip
        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabs.setBackground(UITheme.BG_CARD);
        tabs.setBorder(BorderFactory.createCompoundBorder(
            new UITheme.RoundedBorder(10, UITheme.BORDER_DEFAULT, null),
            new EmptyBorder(0,4,0,4)));

        tabContent = new JPanel(new CardLayout());
        tabContent.setOpaque(false);

        JPanel pNewPurchase = buildNewPurchase();
        JPanel pHistory     = buildHistory();
        JPanel pSuppliers   = buildSuppliers();

        tabContent.add(pNewPurchase, "new");
        tabContent.add(pHistory,     "history");
        tabContent.add(pSuppliers,   "suppliers");

        String[] tabNames = {"New Purchase", "Purchase History", "Suppliers"};
        for (int i = 0; i < tabNames.length; i++) {
            final String card = i==0?"new":i==1?"history":"suppliers";
            final boolean first = i==0;
            JButton tb = new JButton(tabNames[i]) {
                boolean active = first;
                {
                    addActionListener(e -> {
                        ((CardLayout)tabContent.getLayout()).show(tabContent, card);
                        for (Component c : tabs.getComponents()) if(c instanceof JButton) ((JButton)c).putClientProperty("active",false);
                        putClientProperty("active", true);
                        tabs.repaint();
                        if("history".equals(card)) loadHistory();
                        if("suppliers".equals(card)) loadSuppliers();
                    });
                }
                @Override protected void paintComponent(Graphics g) {
                    boolean a = Boolean.TRUE.equals(getClientProperty("active")) || active;
                    Graphics2D g2=(Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if(a){ g2.setColor(UITheme.ACCENT_LIME); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8); setForeground(UITheme.TEXT_ON_LIME); setFont(new Font("Segoe UI",Font.BOLD,12)); }
                    else { setForeground(UITheme.TEXT_SECONDARY); setFont(UITheme.FONT_BODY); }
                    g2.dispose(); super.paintComponent(g);
                }
            };
            tb.putClientProperty("active", first);
            tb.setOpaque(false); tb.setContentAreaFilled(false);
            tb.setBorderPainted(false); tb.setFocusPainted(false);
            tb.setBorder(new EmptyBorder(6,14,6,14));
            tb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            tabs.add(tb);
        }

        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setOpaque(false);
        wrapper.add(tabs, BorderLayout.NORTH);
        wrapper.add(tabContent, BorderLayout.CENTER);
        add(wrapper, BorderLayout.CENTER);

        resetPurchase();
    }

    // ── New Purchase tab ──────────────────────────────────────────
    private JPanel buildNewPurchase() {
        JPanel p = new JPanel(new BorderLayout(16, 0));
        p.setOpaque(false);

        // Left: supplier + kode + cart
        JPanel left = new JPanel(new BorderLayout(0, 10));
        left.setOpaque(false);

        JPanel topRow = new JPanel(new GridLayout(1, 2, 12, 0));
        topRow.setOpaque(false);
        JPanel supPan = new JPanel(new BorderLayout(0,5)); supPan.setOpaque(false);
        supPan.add(UITheme.fieldLabel("Supplier"), BorderLayout.NORTH);
        cmbSupplier = UITheme.styledCombo(new String[]{"— Pilih Supplier —"});
        for (Supplier s : supDAO.getAll()) cmbSupplier.addItem(s.getNamaSupplier());
        supPan.add(cmbSupplier, BorderLayout.CENTER);

        JPanel kodePan = new JPanel(new BorderLayout(0,5)); kodePan.setOpaque(false);
        kodePan.add(UITheme.fieldLabel("Kode / PLU Barang"), BorderLayout.NORTH);
        JPanel kodeRow = new JPanel(new BorderLayout(8, 0)); kodeRow.setOpaque(false);
        txtKode = UITheme.styledField("Scan atau ketik kode → Enter");
        txtKode.setPreferredSize(new Dimension(0, 36));
        JButton btnAdd = UITheme.primaryButton("+ Tambah", UITheme.ACCENT_LIME);
        kodeRow.add(txtKode, BorderLayout.CENTER); kodeRow.add(btnAdd, BorderLayout.EAST);
        kodePan.add(kodeRow, BorderLayout.CENTER);

        topRow.add(supPan); topRow.add(kodePan);

        String[] cols = {"#","Kode","Nama Barang","Harga Beli","Qty","Subtotal"};
        cartMdl = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c){ return c==4; } };
        cartTable = new JTable(cartMdl); UITheme.styleTable(cartTable);
        cartTable.setRowHeight(38);
        cartTable.getColumnModel().getColumn(0).setMaxWidth(36);
        cartTable.getColumnModel().getColumn(1).setMaxWidth(90);
        cartTable.getColumnModel().getColumn(4).setMaxWidth(60);
        cartTable.setDefaultRenderer(Object.class, tblRenderer(5, UITheme.ACCENT_PURPLE));

        JButton btnDel = UITheme.dangerButton("Hapus Item");
        JPanel cartTools = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        cartTools.setOpaque(false); cartTools.add(btnDel);

        left.add(topRow, BorderLayout.NORTH);
        left.add(UITheme.styledScroll(cartTable), BorderLayout.CENTER);
        left.add(cartTools, BorderLayout.SOUTH);

        // Right: summary
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setPreferredSize(new Dimension(260, 0));

        JPanel sumCard = UITheme.card();
        sumCard.setLayout(new BoxLayout(sumCard, BoxLayout.Y_AXIS));
        sumCard.setAlignmentX(LEFT_ALIGNMENT);

        lblNo = new JLabel("Purchase Order: —");
        lblNo.setFont(new Font("Segoe UI",Font.BOLD,12)); lblNo.setForeground(UITheme.TEXT_PRIMARY); lblNo.setAlignmentX(LEFT_ALIGNMENT);
        JLabel lSum = new JLabel("Order Summary"); lSum.setFont(UITheme.FONT_H2); lSum.setForeground(UITheme.TEXT_PRIMARY); lSum.setAlignmentX(LEFT_ALIGNMENT);
        lblTotal = new JLabel("Rp 0"); lblTotal.setFont(UITheme.FONT_BODY); lblTotal.setForeground(UITheme.TEXT_SECONDARY); lblTotal.setAlignmentX(LEFT_ALIGNMENT);
        lblGrand = new JLabel("Rp 0"); lblGrand.setFont(new Font("Segoe UI",Font.BOLD,20)); lblGrand.setForeground(UITheme.TEXT_PRIMARY); lblGrand.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lByr = UITheme.fieldLabel("Jumlah Dibayar"); lByr.setAlignmentX(LEFT_ALIGNMENT);
        txtBayar = UITheme.styledField("0");
        txtBayar.setFont(new Font("Segoe UI",Font.BOLD,14));
        txtBayar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40)); txtBayar.setAlignmentX(LEFT_ALIGNMENT);

        JButton btnProses = UITheme.primaryButton("✓ Simpan Pembelian", UITheme.ACCENT_LIME);
        btnProses.setFont(new Font("Segoe UI",Font.BOLD,13));
        btnProses.setMaximumSize(new Dimension(Integer.MAX_VALUE,44)); btnProses.setAlignmentX(LEFT_ALIGNMENT);

        JButton btnReset = UITheme.ghostButton("Reset", UITheme.TEXT_SECONDARY);
        btnReset.setMaximumSize(new Dimension(Integer.MAX_VALUE,34)); btnReset.setAlignmentX(LEFT_ALIGNMENT);

        sumCard.add(lblNo); sumCard.add(Box.createVerticalStrut(6));
        sumCard.add(UITheme.separator()); sumCard.add(Box.createVerticalStrut(8));
        sumCard.add(lSum); sumCard.add(Box.createVerticalStrut(6));
        sumRow(sumCard,"Subtotal",lblTotal); sumRow(sumCard,"Grand Total",lblGrand);
        sumCard.add(Box.createVerticalStrut(10));
        sumCard.add(lByr); sumCard.add(Box.createVerticalStrut(5));
        sumCard.add(txtBayar); sumCard.add(Box.createVerticalStrut(14));
        sumCard.add(btnProses); sumCard.add(Box.createVerticalStrut(6)); sumCard.add(btnReset);

        right.add(sumCard);

        p.add(left, BorderLayout.CENTER); p.add(right, BorderLayout.EAST);

        btnAdd.addActionListener(e -> addItem());
        txtKode.addActionListener(e -> addItem());
        btnDel.addActionListener(e -> delItem());
        btnProses.addActionListener(e -> proses());
        btnReset.addActionListener(e -> resetPurchase());
        return p;
    }

    // ── Purchase History tab ──────────────────────────────────────
    private JPanel buildHistory() {
        JPanel p = new JPanel(new BorderLayout(0,10)); p.setOpaque(false);
        JPanel tbr = new JPanel(new BorderLayout(8,0)); tbr.setOpaque(false);
        JTextField sField = UITheme.styledField("Cari nomor PO, supplier…");
        sField.setPreferredSize(new Dimension(240,34));
        tbr.add(sField, BorderLayout.WEST);
        tbr.add(UITheme.ghostButton("Export PDF", UITheme.ACCENT_AMBER), BorderLayout.EAST);
        String[] cols = {"No PO","Tanggal","Supplier","Jumlah Item","Total","Status"};
        histMdl = new DefaultTableModel(cols,0){ public boolean isCellEditable(int r,int c){return false;} };
        histTable = new JTable(histMdl); UITheme.styleTable(histTable);
        histTable.setDefaultRenderer(Object.class, tblRenderer(-1, null));
        JPanel card = UITheme.card(); card.setLayout(new BorderLayout(0,10));
        card.add(tbr, BorderLayout.NORTH);
        card.add(UITheme.styledScroll(histTable), BorderLayout.CENTER);
        p.add(card, BorderLayout.CENTER);
        return p;
    }

    // ── Suppliers tab ─────────────────────────────────────────────
    private JPanel buildSuppliers() {
        JPanel p = new JPanel(new BorderLayout(16, 0)); p.setOpaque(false);
        String[] cols = {"Kode","Nama Supplier","Telepon","Email","Contact Person"};
        supMdl = new DefaultTableModel(cols,0){ public boolean isCellEditable(int r,int c){return false;} };
        supTable = new JTable(supMdl); UITheme.styleTable(supTable);
        supTable.setDefaultRenderer(Object.class, tblRenderer(-1,null));

        JPanel fc = UITheme.card();
        fc.setLayout(new BoxLayout(fc, BoxLayout.Y_AXIS));
        fc.setPreferredSize(new Dimension(270,0));
        JLabel lf = new JLabel("Supplier Form"); lf.setFont(UITheme.FONT_H2); lf.setForeground(UITheme.TEXT_PRIMARY); lf.setAlignmentX(LEFT_ALIGNMENT);
        sKode=fld("Kode Supplier",fc); sNama=fld("Nama Supplier *",fc); sTlp=fld("Telepon",fc); sEmail=fld("Email",fc); sAlamat=fld("Alamat",fc); sCP=fld("Contact Person",fc);
        JPanel br=new JPanel(new GridLayout(1,2,8,0)); br.setOpaque(false); br.setMaximumSize(new Dimension(Integer.MAX_VALUE,34)); br.setAlignmentX(LEFT_ALIGNMENT);
        JButton btnS=UITheme.primaryButton("Simpan",UITheme.ACCENT_LIME); JButton btnH=UITheme.dangerButton("Hapus"); JButton btnN=UITheme.ghostButton("+ Baru",UITheme.ACCENT_BLUE); JButton btnB=UITheme.ghostButton("Batal",UITheme.TEXT_SECONDARY);
        br.add(btnB); br.add(btnS);
        JPanel br2=new JPanel(new GridLayout(1,2,8,0)); br2.setOpaque(false); br2.setMaximumSize(new Dimension(Integer.MAX_VALUE,34)); br2.setAlignmentX(LEFT_ALIGNMENT);
        br2.add(btnN); br2.add(btnH);
        fc.add(lf); fc.add(Box.createVerticalStrut(8)); fc.add(UITheme.separator()); fc.add(Box.createVerticalStrut(8)); fc.add(Box.createVerticalGlue()); fc.add(br2); fc.add(Box.createVerticalStrut(6)); fc.add(br);

        JPanel leftCard=UITheme.card(); leftCard.setLayout(new BorderLayout(0,10));
        JPanel tbr=new JPanel(new BorderLayout(8,0)); tbr.setOpaque(false);
        JTextField sf=UITheme.styledField("Cari supplier…"); sf.setPreferredSize(new Dimension(220,34));
        JButton btnAdd2=UITheme.primaryButton("+ Tambah",UITheme.ACCENT_LIME);
        tbr.add(sf,BorderLayout.WEST); tbr.add(btnAdd2,BorderLayout.EAST);
        leftCard.add(tbr,BorderLayout.NORTH);
        leftCard.add(UITheme.styledScroll(supTable),BorderLayout.CENTER);

        p.add(leftCard,BorderLayout.CENTER); p.add(fc,BorderLayout.EAST);

        btnN.addActionListener(e->clearSup()); btnB.addActionListener(e->clearSup());
        btnS.addActionListener(e->saveSup()); btnH.addActionListener(e->delSup());
        supTable.getSelectionModel().addListSelectionListener(e->{
            int row=supTable.getSelectedRow();
            if(row>=0){ selSupId=-1; sKode.setText(s(supMdl.getValueAt(row,0))); sNama.setText(s(supMdl.getValueAt(row,1))); sTlp.setText(s(supMdl.getValueAt(row,2))); sEmail.setText(s(supMdl.getValueAt(row,3))); sCP.setText(s(supMdl.getValueAt(row,4))); }
        });
        return p;
    }

    // ── Cart logic ────────────────────────────────────────────────
    private void addItem() {
        String kode=txtKode.getText().trim(); if(kode.isEmpty()) return;
        Barang b=barangCtrl.getByKode(kode);
        if(b==null){AlertUtil.showWarning(this,"Barang tidak ditemukan: "+kode);txtKode.setText("");return;}
        for(int i=0;i<cartMdl.getRowCount();i++){
            if(cartMdl.getValueAt(i,1).equals(b.getKodeBarang())){int q=Integer.parseInt(cartMdl.getValueAt(i,4).toString())+1;cartMdl.setValueAt(q,i,4);cartMdl.setValueAt(FormatUtil.formatRupiah(q*b.getHargaBeli()),i,5);hitungTotal();txtKode.setText("");return;}
        }
        cartMdl.addRow(new Object[]{cartMdl.getRowCount()+1,b.getKodeBarang(),b.getNamaBarang(),FormatUtil.formatRupiah(b.getHargaBeli()),1,FormatUtil.formatRupiah(b.getHargaBeli())});
        hitungTotal(); txtKode.setText("");
    }
    private void delItem(){int r=cartTable.getSelectedRow();if(r<0){AlertUtil.showWarning(this,"Pilih item!");return;}cartMdl.removeRow(r);for(int i=0;i<cartMdl.getRowCount();i++)cartMdl.setValueAt(i+1,i,0);hitungTotal();}
    private void hitungTotal(){double t=0;for(int i=0;i<cartMdl.getRowCount();i++){String v=cartMdl.getValueAt(i,5).toString().replaceAll("[^\\d]","");t+=v.isEmpty()?0:Double.parseDouble(v);}lblTotal.setText(FormatUtil.formatRupiah(t));lblGrand.setText(FormatUtil.formatRupiah(t));}
    private void proses(){
        if(cartMdl.getRowCount()==0){AlertUtil.showWarning(this,"Keranjang kosong!");return;}
        for(int i=0;i<cartMdl.getRowCount();i++){
            Barang b=barangCtrl.getByKode(cartMdl.getValueAt(i,1).toString());if(b==null)continue;
            int q=FormatUtil.parseInt(cartMdl.getValueAt(i,4).toString());
            TransaksiDetail d=new TransaksiDetail(b.getId(),b.getKodeBarang(),b.getNamaBarang(),q,b.getHargaBeli());d.hitungSubtotal();currentTrx.addDetail(d);
        }
        currentTrx.setBayar(FormatUtil.parseDouble(txtBayar.getText()));
        if(new TransaksiController().simpanPembelian(currentTrx)){AlertUtil.showInfo(this,"Pembelian berhasil!\nNo: "+currentTrx.getNoTransaksi());resetPurchase();}
        else AlertUtil.showError(this,"Gagal menyimpan.");
    }
    private void resetPurchase(){currentTrx=new Transaksi();cartMdl.setRowCount(0);lblNo.setText("PO: "+new TransaksiController().generateNoTransaksi("PBL"));lblTotal.setText("Rp 0");lblGrand.setText("Rp 0");txtKode.setText("");txtBayar.setText("0");cmbSupplier.setSelectedIndex(0);}

    // ── Supplier logic ────────────────────────────────────────────
    private void loadHistory(){
        histMdl.setRowCount(0);
        for(Transaksi t:trxCtrl.getRiwayatPembelian())
            histMdl.addRow(new Object[]{t.getNoTransaksi(),FormatUtil.formatDateTime(t.getTanggal()),t.getNamaSupplier(),"—",FormatUtil.formatRupiah(t.getGrandTotal()),"SELESAI"});
    }
    private void loadSuppliers(){
        supMdl.setRowCount(0);
        for(Supplier s:supDAO.getAll())
            supMdl.addRow(new Object[]{s.getKodeSupplier(),s.getNamaSupplier(),s.getTelepon(),s.getEmail(),s.getContactPerson()});
    }
    private void saveSup(){
        if(sNama.getText().isBlank()){AlertUtil.showWarning(this,"Nama supplier wajib!");return;}
        Supplier s=new Supplier();s.setId(selSupId);s.setKodeSupplier(sKode.getText().trim());s.setNamaSupplier(sNama.getText().trim());s.setAlamat(sAlamat.getText().trim());s.setTelepon(sTlp.getText().trim());s.setEmail(sEmail.getText().trim());s.setContactPerson(sCP.getText().trim());
        boolean ok=(selSupId==-1)?supDAO.insert(s):supDAO.update(s);
        if(ok){AlertUtil.showInfo(this,"Supplier disimpan!");loadSuppliers();clearSup();}else AlertUtil.showError(this,"Gagal.");
    }
    private void delSup(){
        if(selSupId==-1){AlertUtil.showWarning(this,"Pilih supplier!");return;}
        if(!AlertUtil.showConfirm(this,"Hapus supplier?"))return;
        if(supDAO.delete(selSupId)){AlertUtil.showInfo(this,"Dihapus.");loadSuppliers();clearSup();}else AlertUtil.showError(this,"Gagal.");
    }
    private void clearSup(){selSupId=-1;sKode.setText("");sNama.setText("");sTlp.setText("");sEmail.setText("");sAlamat.setText("");sCP.setText("");}

    private void sumRow(JPanel p, String label, Component val){
        JPanel r=new JPanel(new BorderLayout());r.setOpaque(false);r.setMaximumSize(new Dimension(Integer.MAX_VALUE,32));r.setAlignmentX(LEFT_ALIGNMENT);
        r.add(UITheme.fieldLabel(label),BorderLayout.WEST);r.add(val,BorderLayout.EAST);p.add(r);p.add(Box.createVerticalStrut(4));
    }
    private JTextField fld(String label, JPanel p){JLabel l=UITheme.fieldLabel(label);l.setAlignmentX(LEFT_ALIGNMENT);JTextField f=UITheme.styledField("");f.setMaximumSize(new Dimension(Integer.MAX_VALUE,34));f.setAlignmentX(LEFT_ALIGNMENT);p.add(l);p.add(Box.createVerticalStrut(4));p.add(f);p.add(Box.createVerticalStrut(8));return f;}
    private DefaultTableCellRenderer tblRenderer(int accentCol, Color accent){
        return new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int r,int c){
                Component cp=super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                cp.setBackground(sel?new Color(238,242,255):(r%2==0?UITheme.BG_CARD:UITheme.BG_ROW_ALT));
                cp.setForeground(c==accentCol&&accent!=null?accent:UITheme.TEXT_PRIMARY);
                ((JLabel)cp).setBorder(new EmptyBorder(0,12,0,12));
                return cp;
            }
        };
    }
    private String s(Object o){return o==null?"":o.toString();}
}
