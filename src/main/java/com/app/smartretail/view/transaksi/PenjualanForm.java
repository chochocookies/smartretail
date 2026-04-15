package com.app.smartretail.view.transaksi;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.app.smartretail.controller.BarangController;
import com.app.smartretail.controller.TransaksiController;
import com.app.smartretail.model.Barang;
import com.app.smartretail.model.Transaksi;
import com.app.smartretail.model.TransaksiDetail;
import com.app.smartretail.utils.AlertUtil;
import com.app.smartretail.utils.FormatUtil;
import com.app.smartretail.utils.UITheme;
import com.app.smartretail.view.component.Icons;

public class PenjualanForm extends JPanel {

    private final TransaksiController trxCtrl  = new TransaksiController();
    private final BarangController    barangCtrl = new BarangController();

    private JTextField txtScan, txtBayar, txtDiskon;
    private JLabel lblNo, lblSub, lblGrand, lblKembalian;
    private JTable cart; private DefaultTableModel cartMdl;
    private JComboBox<String> cmbMetode;
    private JButton btnProses, btnReset;
    private Transaksi trx;

    public PenjualanForm() {
        setLayout(new BorderLayout()); setBackground(UITheme.BG_SURFACE);
        setBorder(new EmptyBorder(22,24,22,24));
        build(); reset();
    }

    private void build() {
        // Header
        JPanel hdr = pageHeader("POS  —  Point of Sale",
            "Transaksi penjualan kasir", null, "Riwayat");
        add(hdr, BorderLayout.NORTH);

        JPanel main = new JPanel(new BorderLayout(16,0));
        main.setOpaque(false);

        // LEFT: tabs + scan + cart
        JPanel left = new JPanel(new BorderLayout(0,12));
        left.setOpaque(false);

        // Tab strip (POS Machine | POS Dashboard)
        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        tabs.setOpaque(false);
        tabs.add(tabBtn("POS Machine", false));
        tabs.add(tabBtn("POS Dashboard", true));

        // Scan row
        JPanel scanRow = new JPanel(new BorderLayout(8,0));
        scanRow.setOpaque(false);
        JButton btnView = UITheme.ghostButton("View All Orders", UITheme.ACCENT_BLUE);

        JPanel scanRight = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
        scanRight.setOpaque(false);
        JButton btnSearch = new JButton(Icons.SEARCH);
        btnSearch.setPreferredSize(new Dimension(34,34));
        btnSearch.setOpaque(false); btnSearch.setContentAreaFilled(false);
        btnSearch.setBorderPainted(false); btnSearch.setFocusPainted(false);
        txtScan = UITheme.styledField("Scan Barcode / PLU / Kode…");
        txtScan.setPreferredSize(new Dimension(220,38));
        scanRight.add(btnSearch); scanRight.add(txtScan);

        JPanel scanFull = new JPanel(new BorderLayout(10,0));
        scanFull.setOpaque(false);
        scanFull.add(btnView, BorderLayout.WEST);
        scanFull.add(scanRight, BorderLayout.EAST);

        // Product category chips
        JPanel chips = buildCategoryChips();

        // Cart table
        String[] cols = {"#","Kode","Nama Barang","Harga","Qty","Subtotal"};
        cartMdl = new DefaultTableModel(cols,0){ public boolean isCellEditable(int r,int c){return c==4;} };
        cart = new JTable(cartMdl);
        UITheme.styleTable(cart);
        cart.setRowHeight(42);
        cart.getColumnModel().getColumn(0).setMaxWidth(36);
        cart.getColumnModel().getColumn(1).setMaxWidth(90);
        cart.getColumnModel().getColumn(4).setMaxWidth(60);
        cart.setDefaultRenderer(Object.class, cartRenderer());

        JPanel cartTools = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        cartTools.setOpaque(false);
        JButton btnDel = UITheme.dangerButton("Hapus Item");
        JButton btnClr = UITheme.ghostButton("Kosongkan", UITheme.TEXT_SECONDARY);
        cartTools.add(btnDel); cartTools.add(btnClr);

        left.add(tabs, BorderLayout.NORTH);
        JPanel leftContent = new JPanel(new BorderLayout(0,10));
        leftContent.setOpaque(false);
        leftContent.add(scanFull, BorderLayout.NORTH);
        JPanel leftMid = new JPanel(new BorderLayout(0,10));
        leftMid.setOpaque(false);
        leftMid.add(chips, BorderLayout.NORTH);
        leftMid.add(UITheme.styledScroll(cart), BorderLayout.CENTER);
        leftMid.add(cartTools, BorderLayout.SOUTH);
        leftContent.add(leftMid, BorderLayout.CENTER);
        left.add(leftContent, BorderLayout.CENTER);

        // RIGHT: Order summary
        JPanel right = buildOrderPanel();

        main.add(left, BorderLayout.CENTER);
        main.add(right, BorderLayout.EAST);
        add(main, BorderLayout.CENTER);

        // Events
        txtScan.addActionListener(e -> addItem());
        btnView.addActionListener(e -> addItem());
        btnDel.addActionListener(e -> delItem());
        btnClr.addActionListener(e -> { if(AlertUtil.showConfirm(this,"Kosongkan keranjang?")) {cartMdl.setRowCount(0);hitungTotal();} });
        btnProses.addActionListener(e -> proses());
        btnReset.addActionListener(e -> { if(AlertUtil.showConfirm(this,"Reset transaksi?")) reset(); });
        txtBayar.addKeyListener(new KeyAdapter(){ public void keyReleased(KeyEvent e){hitungKembalian();} });
        txtDiskon.addKeyListener(new KeyAdapter(){ public void keyReleased(KeyEvent e){hitungTotal();} });
    }

    private JPanel buildOrderPanel() {
        JPanel orderCard = UITheme.card();
        orderCard.setLayout(new BoxLayout(orderCard, BoxLayout.Y_AXIS));
        orderCard.setPreferredSize(new Dimension(290,0));
        orderCard.setMaximumSize(new Dimension(290,Integer.MAX_VALUE));

        lblNo = new JLabel("Order No: —");
        lblNo.setFont(new Font("Segoe UI",Font.BOLD,13));
        lblNo.setForeground(UITheme.TEXT_PRIMARY);
        lblNo.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lCart = new JLabel("Cart Items");
        lCart.setFont(UITheme.FONT_H2); lCart.setForeground(UITheme.TEXT_PRIMARY);
        lCart.setAlignmentX(LEFT_ALIGNMENT);

        // Mini cart summary lines area
        JPanel miniCart = new JPanel(); miniCart.setOpaque(false);
        miniCart.setLayout(new BoxLayout(miniCart, BoxLayout.Y_AXIS));
        miniCart.setAlignmentX(LEFT_ALIGNMENT);
        miniCart.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        // Pricing
        JSeparator s1 = UITheme.separator(); s1.setAlignmentX(LEFT_ALIGNMENT);
        lblSub = totalRow("Subtotal", "Rp 0", UITheme.TEXT_SECONDARY);
        JPanel diskonRow = new JPanel(new BorderLayout()); diskonRow.setOpaque(false);
        diskonRow.setMaximumSize(new Dimension(Integer.MAX_VALUE,34));
        diskonRow.setAlignmentX(LEFT_ALIGNMENT);
        JLabel lDis = UITheme.fieldLabel("Diskon (Rp)");
        txtDiskon = UITheme.styledField("0");
        txtDiskon.setFont(UITheme.FONT_BODY);
        txtDiskon.setMaximumSize(new Dimension(100,32));
        diskonRow.add(lDis, BorderLayout.WEST); diskonRow.add(txtDiskon, BorderLayout.EAST);
        JSeparator s2 = UITheme.separator(); s2.setAlignmentX(LEFT_ALIGNMENT);
        lblGrand = totalRow("Total Amount", "Rp 0", UITheme.TEXT_PRIMARY);
        lblGrand.setFont(new Font("Segoe UI",Font.BOLD,18));

        // Metode
        JLabel lMet = UITheme.fieldLabel("Metode Pembayaran"); lMet.setAlignmentX(LEFT_ALIGNMENT);
        cmbMetode = UITheme.styledCombo(new String[]{"TUNAI","KARTU DEBIT","KARTU KREDIT","TRANSFER"});
        cmbMetode.setMaximumSize(new Dimension(Integer.MAX_VALUE,34));
        cmbMetode.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lByr = UITheme.fieldLabel("Jumlah Bayar"); lByr.setAlignmentX(LEFT_ALIGNMENT);
        txtBayar = UITheme.styledField("0");
        txtBayar.setFont(new Font("Segoe UI",Font.BOLD,15));
        txtBayar.setMaximumSize(new Dimension(Integer.MAX_VALUE,40));
        txtBayar.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lKem = UITheme.fieldLabel("Kembalian"); lKem.setAlignmentX(LEFT_ALIGNMENT);
        lblKembalian = new JLabel("Rp 0");
        lblKembalian.setFont(new Font("Segoe UI",Font.BOLD,22));
        lblKembalian.setForeground(UITheme.ACCENT_TEAL);
        lblKembalian.setAlignmentX(LEFT_ALIGNMENT);

        // Numpad (like reference image)
        JPanel numpad = buildNumpad();
        numpad.setAlignmentX(LEFT_ALIGNMENT);

        // Action btns
        btnProses = UITheme.primaryButton("Proses Transaksi", UITheme.ACCENT_LIME);
        btnProses.setFont(new Font("Segoe UI",Font.BOLD,13));
        btnProses.setMaximumSize(new Dimension(Integer.MAX_VALUE,42));
        btnProses.setAlignmentX(LEFT_ALIGNMENT);
        btnReset = UITheme.ghostButton("Reset / Batal", UITheme.ACCENT_CORAL);
        btnReset.setMaximumSize(new Dimension(Integer.MAX_VALUE,36));
        btnReset.setAlignmentX(LEFT_ALIGNMENT);

        orderCard.add(lblNo); orderCard.add(Box.createVerticalStrut(4));
        orderCard.add(UITheme.separator()); orderCard.add(Box.createVerticalStrut(8));
        orderCard.add(lCart); orderCard.add(Box.createVerticalStrut(6));
        orderCard.add(miniCart); orderCard.add(Box.createVerticalStrut(8));
        orderCard.add(s1); orderCard.add(Box.createVerticalStrut(6));
        orderCard.add(lblSub); orderCard.add(Box.createVerticalStrut(6));
        orderCard.add(diskonRow); orderCard.add(Box.createVerticalStrut(4));
        orderCard.add(s2); orderCard.add(Box.createVerticalStrut(4));
        orderCard.add(lblGrand); orderCard.add(Box.createVerticalStrut(10));
        orderCard.add(lMet); orderCard.add(Box.createVerticalStrut(5));
        orderCard.add(cmbMetode); orderCard.add(Box.createVerticalStrut(8));
        orderCard.add(lByr); orderCard.add(Box.createVerticalStrut(4));
        orderCard.add(txtBayar); orderCard.add(Box.createVerticalStrut(6));
        orderCard.add(lKem); orderCard.add(Box.createVerticalStrut(4));
        orderCard.add(lblKembalian); orderCard.add(Box.createVerticalStrut(10));
        orderCard.add(numpad); orderCard.add(Box.createVerticalStrut(10));
        orderCard.add(btnProses); orderCard.add(Box.createVerticalStrut(6));
        orderCard.add(btnReset);
        return orderCard;
    }

    // ── Numpad — 4×3, compact height ─────────────────────────────────────────
    private JPanel buildNumpad() {
        JPanel np = new JPanel(new GridLayout(4, 3, 6, 6));
        np.setOpaque(false);
        np.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));  // ← dikompres dari 240
        np.setAlignmentX(LEFT_ALIGNMENT);

        String[] keys = {"7","8","9","4","5","6","1","2","3","C","0","del"};

        for (String k : keys) {
            JButton b = new JButton(k) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color bg;
                    if (k.equals("C"))
                        bg = getModel().isPressed()  ? new Color(255,200,200)
                           : getModel().isRollover() ? new Color(255,235,235) : UITheme.BG_CARD;
                    else if (k.equals("⌫"))
                        bg = getModel().isPressed()  ? new Color(255,220,150)
                           : getModel().isRollover() ? new Color(255,245,230) : UITheme.BG_CARD;
                    else
                        bg = getModel().isRollover() ? UITheme.BG_HOVER : UITheme.BG_CARD;

                    g2.setColor(bg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                    if (k.equals("C"))       g2.setColor(new Color(255,150,150));
                    else if (k.equals("⌫")) g2.setColor(new Color(255,180,100));
                    else                     g2.setColor(UITheme.BORDER_DEFAULT);

                    g2.setStroke(new BasicStroke(1.1f));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            if (k.equals("C"))       b.setForeground(new Color(200,0,0));
            else if (k.equals("⌫")) b.setForeground(new Color(200,100,0));
            else                     b.setForeground(UITheme.TEXT_PRIMARY);

            b.setFont(new Font("Segoe UI", Font.BOLD, 15));  // ← dikompres dari 18
            b.setMargin(new Insets(0, 0, 0, 0));
            b.setOpaque(false); b.setContentAreaFilled(false);
            b.setBorderPainted(false); b.setFocusPainted(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.addActionListener(e -> handleNumpad(k));
            np.add(b);
        }
        return np;
    }

    private void handleNumpad(String key) {
        String cur = txtBayar.getText();
        switch (key) {
            case "C": txtBayar.setText("0"); break;
            case "⌫": txtBayar.setText(cur.length()>1?cur.substring(0,cur.length()-1):"0"); break;
            default:
                txtBayar.setText(("0".equals(cur)?""  : cur) + key);
        }
        hitungKembalian();
    }

    private JPanel buildCategoryChips() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        p.setOpaque(false);
        String[] cats = {"All","Makanan","Minuman","Kebersihan","Rokok","Lainnya"};
        for (int i=0; i<cats.length; i++) {
            final int idx = i;
            JButton b = new JButton(cats[i]){
                @Override protected void paintComponent(Graphics g){
                    Graphics2D g2=(Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if(idx==0){ g2.setColor(UITheme.TEXT_PRIMARY); g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20); setForeground(Color.WHITE); }
                    else { g2.setColor(UITheme.BG_CARD); g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20); g2.setColor(UITheme.BORDER_DEFAULT); g2.setStroke(new BasicStroke(0.8f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,20,20); setForeground(UITheme.TEXT_SECONDARY); }
                    g2.dispose(); super.paintComponent(g);
                }
            };
            b.setFont(new Font("Segoe UI",Font.BOLD,11));
            b.setOpaque(false); b.setContentAreaFilled(false);
            b.setBorderPainted(false); b.setFocusPainted(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            p.add(b);
        }
        return p;
    }

    private JLabel totalRow(String label, String val, Color valColor) {
        JLabel combined = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2=(Graphics2D)g.create();
                g2.setFont(UITheme.FONT_SMALL); g2.setColor(UITheme.TEXT_SECONDARY);
                g2.drawString(label, 0, 14);
                g2.dispose();
            }
        };
        JLabel valLbl = new JLabel(val);
        valLbl.setFont(UITheme.FONT_BODY); valLbl.setForeground(valColor);
        return valLbl;
    }

    private JButton tabBtn(String text, boolean active) {
        JButton b = new JButton(text){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_CARD); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                if(active){ g2.setColor(UITheme.TEXT_PRIMARY); g2.fillRoundRect(2,getHeight()-4,getWidth()-4,3,2,2); }
                g2.dispose(); super.paintComponent(g);
            }
        };
        b.setFont(active?new Font("Segoe UI",Font.BOLD,12):UITheme.FONT_BODY);
        b.setForeground(active?UITheme.TEXT_PRIMARY:UITheme.TEXT_MUTED);
        b.setOpaque(false); b.setContentAreaFilled(false);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(8,14,8,14));
        return b;
    }

    private void addItem() {
        String kode = txtScan.getText().trim(); if(kode.isEmpty()) return;
        Barang b = barangCtrl.getByKode(kode);
        if (b == null) {
            List<Barang> r = barangCtrl.searchBarang(kode);
            if(r.isEmpty()){AlertUtil.showWarning(this,"Barang tidak ditemukan: "+kode);txtScan.setText("");return;}
            b=r.get(0);
        }
        if(b.getStok()<=0){AlertUtil.showWarning(this,"Stok habis!");txtScan.setText("");return;}
        for(int i=0;i<cartMdl.getRowCount();i++){
            if(cartMdl.getValueAt(i,1).equals(b.getKodeBarang())){
                int q=Integer.parseInt(cartMdl.getValueAt(i,4).toString())+1;
                if(q>b.getStok()){AlertUtil.showWarning(this,"Stok tidak mencukupi!");return;}
                cartMdl.setValueAt(q,i,4);
                cartMdl.setValueAt(FormatUtil.formatRupiah(q*b.getHargaJual()),i,5);
                hitungTotal();txtScan.setText("");return;
            }
        }
        cartMdl.addRow(new Object[]{cartMdl.getRowCount()+1,b.getKodeBarang(),b.getNamaBarang(),FormatUtil.formatRupiah(b.getHargaJual()),1,FormatUtil.formatRupiah(b.getHargaJual())});
        hitungTotal();txtScan.setText("");
    }

    private void delItem() {
        int row=cart.getSelectedRow();
        if(row<0){AlertUtil.showWarning(this,"Pilih item dahulu!");return;}
        cartMdl.removeRow(row);
        for(int i=0;i<cartMdl.getRowCount();i++) cartMdl.setValueAt(i+1,i,0);
        hitungTotal();
    }

    private void hitungTotal() {
        double sub=0;
        for(int i=0;i<cartMdl.getRowCount();i++){String s=cartMdl.getValueAt(i,5).toString().replaceAll("[^\\d]","");sub+=s.isEmpty()?0:Double.parseDouble(s);}
        double dis=FormatUtil.parseDouble(txtDiskon.getText());
        double grand=Math.max(0,sub-dis);
        lblSub.setText(FormatUtil.formatRupiah(sub));
        lblGrand.setText(FormatUtil.formatRupiah(grand));
        hitungKembalian();
    }

    private void hitungKembalian(){
        double g=FormatUtil.parseDouble(lblGrand.getText());
        double b=FormatUtil.parseDouble(txtBayar.getText());
        double k=b-g;
        lblKembalian.setText(FormatUtil.formatRupiah(k));
        lblKembalian.setForeground(k>=0?UITheme.ACCENT_TEAL:UITheme.ACCENT_CORAL);
    }

    private void proses(){
        if(cartMdl.getRowCount()==0){AlertUtil.showWarning(this,"Keranjang kosong!");return;}
        double g=FormatUtil.parseDouble(lblGrand.getText());
        double b=FormatUtil.parseDouble(txtBayar.getText());
        if(b<g){AlertUtil.showWarning(this,"Jumlah bayar kurang dari total!");return;}
        trx.setDiskon(FormatUtil.parseDouble(txtDiskon.getText()));
        trx.setMetode(cmbMetode.getSelectedItem().toString().split(" ")[0]);
        trx.setBayar(b);
        for(int i=0;i<cartMdl.getRowCount();i++){
            Barang bar=barangCtrl.getByKode(cartMdl.getValueAt(i,1).toString());
            if(bar==null) continue;
            int q=FormatUtil.parseInt(cartMdl.getValueAt(i,4).toString());
            TransaksiDetail d=new TransaksiDetail(bar.getId(),bar.getKodeBarang(),bar.getNamaBarang(),q,bar.getHargaJual());
            d.hitungSubtotal(); trx.addDetail(d);
        }
        if(new TransaksiController().simpanPenjualan(trx)){
            AlertUtil.showInfo(this,"Transaksi Berhasil!\nNo: "+trx.getNoTransaksi()+"\nKembalian: "+FormatUtil.formatRupiah(b-g));
            reset();
        } else AlertUtil.showError(this,"Gagal menyimpan transaksi!");
    }

    private void reset(){
        trx=new Transaksi();
        cartMdl.setRowCount(0);
        lblNo.setText("Order No: "+new TransaksiController().generateNoTransaksi("TRX"));
        lblSub.setText("Rp 0"); lblGrand.setText("Rp 0");
        lblKembalian.setText("Rp 0"); lblKembalian.setForeground(UITheme.ACCENT_TEAL);
        txtBayar.setText("0"); txtDiskon.setText("0"); txtScan.setText("");
        cmbMetode.setSelectedIndex(0);
    }

    private DefaultTableCellRenderer cartRenderer(){
        return new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c){
                Component cp=super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                cp.setBackground(sel?new Color(238,242,255):(r%2==0?UITheme.BG_CARD:UITheme.BG_ROW_ALT));
                cp.setForeground(c==5?UITheme.ACCENT_TEAL:UITheme.TEXT_PRIMARY);
                if(c==4)((JLabel)cp).setHorizontalAlignment(SwingConstants.CENTER);
                ((JLabel)cp).setBorder(new EmptyBorder(0,10,0,10));
                return cp;
            }
        };
    }

    private JPanel pageHeader(String title, String sub, JButton btnLeft, String rightLabel) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false); p.setBorder(new EmptyBorder(0,0,16,0));
        JPanel ht = new JPanel(); ht.setOpaque(false);
        ht.setLayout(new BoxLayout(ht, BoxLayout.Y_AXIS));
        JLabel t = UITheme.pageTitle(title); t.setAlignmentX(LEFT_ALIGNMENT);
        JLabel s = new JLabel(sub); s.setFont(UITheme.FONT_BODY); s.setForeground(UITheme.TEXT_SECONDARY); s.setAlignmentX(LEFT_ALIGNMENT);
        ht.add(t); ht.add(s);
        p.add(ht, BorderLayout.WEST);
        if (rightLabel != null) p.add(UITheme.ghostButton(rightLabel, UITheme.ACCENT_BLUE), BorderLayout.EAST);
        return p;
    }
}
