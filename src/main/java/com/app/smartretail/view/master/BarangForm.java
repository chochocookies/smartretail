package com.app.smartretail.view.master;

import com.app.smartretail.controller.BarangController;
import com.app.smartretail.model.Barang;
import com.app.smartretail.utils.*;
import com.app.smartretail.view.component.Icons;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class BarangForm extends JPanel {

    private final BarangController ctrl = new BarangController();

    // Table view
    private JTable table; private DefaultTableModel mdl;

    // Grid view
    private JPanel gridPanel;
    private JScrollPane gridScroll;

    // Shared
    private JTextField txtSearch;
    private JPanel viewContainer; // CardLayout switching table/grid
    private boolean isGridView = false;

    // Form fields
    private JTextField txtKode, txtNama, txtHargaBeli, txtHargaJual,
                       txtStok, txtStokMin, txtSatuan, txtImgUrl;
    private JTextArea txtDesk;
    private JButton btnSimpan, btnBatal, btnTambah, btnEdit, btnHapus;
    private JLabel lblFormTitle, lblPreview;
    private int selId = -1;

    public BarangForm() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_SURFACE);
        setBorder(new EmptyBorder(22, 24, 22, 24));
        build();
        load();
    }

    private void build() {
        // Header
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false); hdr.setBorder(new EmptyBorder(0, 0, 16, 0));
        JPanel ht = new JPanel(); ht.setOpaque(false); ht.setLayout(new BoxLayout(ht, BoxLayout.Y_AXIS));
        ht.add(UITheme.pageTitle("Products"));
        JLabel sub = new JLabel("Kelola data produk & inventaris");
        sub.setFont(UITheme.FONT_BODY); sub.setForeground(UITheme.TEXT_SECONDARY); ht.add(sub);

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); acts.setOpaque(false);

        // List / Grid toggle buttons
        JButton btnList = viewToggleBtn("≡ List", false);
        JButton btnGrid = viewToggleBtn("\u2589\u2589 Grid", false);
        btnList.putClientProperty("active", true);
        btnList.addActionListener(e -> { isGridView=false; btnList.putClientProperty("active",true); btnGrid.putClientProperty("active",false); btnList.repaint(); btnGrid.repaint(); ((CardLayout)viewContainer.getLayout()).show(viewContainer,"list"); });
        btnGrid.addActionListener(e -> { isGridView=true;  btnGrid.putClientProperty("active",true); btnList.putClientProperty("active",false); btnList.repaint(); btnGrid.repaint(); ((CardLayout)viewContainer.getLayout()).show(viewContainer,"grid"); refreshGrid(); });

        JPanel toggleGroup = new JPanel(new FlowLayout(FlowLayout.LEFT,1,0)); toggleGroup.setOpaque(false);
        toggleGroup.add(btnList); toggleGroup.add(btnGrid);

        JButton btnRef = UITheme.ghostButton("↻", UITheme.TEXT_MUTED);
        btnHapus = UITheme.dangerButton("Hapus");
        btnEdit  = UITheme.ghostButton("Edit", UITheme.ACCENT_AMBER);
        btnTambah= UITheme.primaryButton("+ Tambah", UITheme.ACCENT_LIME);
        acts.add(toggleGroup); acts.add(btnRef); acts.add(btnHapus); acts.add(btnEdit); acts.add(btnTambah);
        hdr.add(ht, BorderLayout.WEST); hdr.add(acts, BorderLayout.EAST);
        add(hdr, BorderLayout.NORTH);

        JPanel main = new JPanel(new BorderLayout(16, 0)); main.setOpaque(false);

        // Search row
        JPanel searchRow = new JPanel(new BorderLayout(8, 0)); searchRow.setOpaque(false);
        txtSearch = UITheme.styledField("Cari kode, nama, PLU, barcode…");
        txtSearch.setPreferredSize(new Dimension(0, 38));
        JButton btnS = UITheme.primaryButton("Cari", UITheme.ACCENT_BLUE);
        searchRow.add(txtSearch, BorderLayout.CENTER); searchRow.add(btnS, BorderLayout.EAST);

        // List view table
        String[] cols = {"#","Kode","PLU","Nama Barang","Harga Jual","Stok","Status"};
        mdl = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c){ return false; } };
        table = new JTable(mdl); UITheme.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(38);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(2).setPreferredWidth(75);
        table.getColumnModel().getColumn(5).setMaxWidth(60);
        table.getColumnModel().getColumn(6).setMaxWidth(80);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c){
                Component cp=super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                cp.setBackground(sel?new Color(238,242,255):(r%2==0?UITheme.BG_CARD:UITheme.BG_ROW_ALT));
                cp.setForeground(c==6?(v!=null&&"Rendah".equals(v.toString())?UITheme.ACCENT_CORAL:UITheme.ACCENT_GREEN):UITheme.TEXT_PRIMARY);
                if(c==4) cp.setForeground(UITheme.ACCENT_BLUE);
                ((JLabel)cp).setBorder(new EmptyBorder(0,12,0,12));
                return cp;
            }
        });

        // Grid view
        gridPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 12, 12));
        gridPanel.setBackground(UITheme.BG_SURFACE);
        gridScroll = UITheme.styledScroll(gridPanel);

        viewContainer = new JPanel(new CardLayout()); viewContainer.setOpaque(false);
        viewContainer.add(UITheme.styledScroll(table), "list");
        viewContainer.add(gridScroll, "grid");

        JPanel leftP = new JPanel(new BorderLayout(0, 10)); leftP.setOpaque(false);
        leftP.add(searchRow, BorderLayout.NORTH);
        leftP.add(viewContainer, BorderLayout.CENTER);

        // Form card
        JPanel fc = UITheme.card();
        fc.setLayout(new BoxLayout(fc, BoxLayout.Y_AXIS));
        fc.setPreferredSize(new Dimension(292, 0)); fc.setMaximumSize(new Dimension(292, Integer.MAX_VALUE));

        lblFormTitle = new JLabel("Detail Produk");
        lblFormTitle.setFont(UITheme.FONT_H2); lblFormTitle.setForeground(UITheme.TEXT_PRIMARY); lblFormTitle.setAlignmentX(LEFT_ALIGNMENT);

        lblPreview = new JLabel(ImageLoader.getPlaceholder());
        lblPreview.setPreferredSize(new Dimension(254,110)); lblPreview.setMaximumSize(new Dimension(Integer.MAX_VALUE,110));
        lblPreview.setHorizontalAlignment(SwingConstants.CENTER);
        lblPreview.setBorder(BorderFactory.createCompoundBorder(new UITheme.RoundedBorder(8,UITheme.BORDER_DEFAULT,UITheme.BG_INPUT),new EmptyBorder(4,4,4,4)));
        lblPreview.setAlignmentX(LEFT_ALIGNMENT);

        txtKode      = fld("Kode Barang",          fc);
        txtNama      = fld("Nama Barang *",         fc);
        txtHargaBeli = fld("Harga Beli (Rp)",       fc);
        txtHargaJual = fld("Harga Jual (Rp) *",     fc);
        txtStok      = fld("Stok Awal",             fc);
        txtStokMin   = fld("Stok Minimum",          fc);
        txtSatuan    = fld("Satuan",                fc);
        txtImgUrl    = fld("URL Gambar Produk",     fc);

        JButton btnPrev = UITheme.ghostButton("Muat Gambar", UITheme.ACCENT_TEAL);
        btnPrev.setMaximumSize(new Dimension(Integer.MAX_VALUE,28)); btnPrev.setAlignmentX(LEFT_ALIGNMENT);
        btnPrev.addActionListener(e -> { String u=txtImgUrl.getText().trim(); if(!u.isEmpty()) ImageLoader.loadAsync(u,254,110,ic->{lblPreview.setIcon(ic);lblPreview.setText(null);}); });

        JLabel lD = UITheme.fieldLabel("Deskripsi"); lD.setAlignmentX(LEFT_ALIGNMENT);
        txtDesk = new JTextArea(2,0); txtDesk.setFont(UITheme.FONT_BODY); txtDesk.setForeground(UITheme.TEXT_PRIMARY);
        txtDesk.setBackground(UITheme.BG_INPUT); txtDesk.setCaretColor(UITheme.ACCENT_BLUE);
        txtDesk.setLineWrap(true); txtDesk.setBorder(new EmptyBorder(7,10,7,10));
        JScrollPane spD = new JScrollPane(txtDesk); spD.setBorder(new UITheme.RoundedBorder(8,UITheme.BORDER_DEFAULT,null));
        spD.setBackground(UITheme.BG_INPUT); spD.getViewport().setBackground(UITheme.BG_INPUT);
        spD.setMaximumSize(new Dimension(Integer.MAX_VALUE,55)); spD.setAlignmentX(LEFT_ALIGNMENT);

        JPanel btnRow = new JPanel(new GridLayout(1,2,8,0)); btnRow.setOpaque(false);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE,34)); btnRow.setAlignmentX(LEFT_ALIGNMENT);
        btnSimpan = UITheme.primaryButton("Simpan", UITheme.ACCENT_LIME);
        btnBatal  = UITheme.ghostButton("Batal", UITheme.TEXT_SECONDARY);
        btnRow.add(btnBatal); btnRow.add(btnSimpan);

        fc.add(lblFormTitle); fc.add(Box.createVerticalStrut(8));
        fc.add(UITheme.separator()); fc.add(Box.createVerticalStrut(8));
        fc.add(lblPreview); fc.add(Box.createVerticalStrut(4));
        fc.add(btnPrev); fc.add(Box.createVerticalStrut(8));
        fc.add(lD); fc.add(Box.createVerticalStrut(4));
        fc.add(spD); fc.add(Box.createVerticalStrut(10));
        fc.add(Box.createVerticalGlue()); fc.add(btnRow);

        main.add(leftP, BorderLayout.CENTER); main.add(fc, BorderLayout.EAST);
        add(main, BorderLayout.CENTER);

        setFE(false);
        btnS.addActionListener(e -> { String kw=txtSearch.getText().trim(); fill(kw.isEmpty()?ctrl.getAllBarang():ctrl.searchBarang(kw)); });
        txtSearch.addActionListener(e -> btnS.doClick());
        btnRef.addActionListener(e -> load());
        btnTambah.addActionListener(e -> startNew());
        btnEdit.addActionListener(e -> { if(table.getSelectedRow()<0){AlertUtil.showWarning(this,"Pilih barang!");return;} setFE(true); lblFormTitle.setText("Edit: "+txtNama.getText()); });
        btnHapus.addActionListener(e -> hapus());
        btnSimpan.addActionListener(e -> simpan());
        btnBatal.addActionListener(e -> { clearF(); setFE(false); lblFormTitle.setText("Detail Produk"); });
        table.getSelectionModel().addListSelectionListener(e -> { if(!e.getValueIsAdjusting()) fillForm(); });
    }

    private JButton viewToggleBtn(String text, boolean active) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                boolean a = Boolean.TRUE.equals(getClientProperty("active"));
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                if(a){ g2.setColor(UITheme.ACCENT_LIME); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8); setForeground(UITheme.TEXT_ON_LIME); }
                else if(getModel().isRollover()){ g2.setColor(UITheme.BG_HOVER); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8); setForeground(UITheme.TEXT_PRIMARY); }
                else { g2.setColor(UITheme.BG_CARD); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8); g2.setColor(UITheme.BORDER_DEFAULT); g2.setStroke(new BasicStroke(0.8f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8); setForeground(UITheme.TEXT_SECONDARY); }
                g2.dispose(); super.paintComponent(g);
            }
        };
        b.setFont(new Font("Segoe UI",Font.BOLD,11)); b.setOpaque(false); b.setContentAreaFilled(false);
        b.setBorderPainted(false); b.setFocusPainted(false); b.setBorder(new EmptyBorder(5,12,5,12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.putClientProperty("active", active);
        return b;
    }

    private void load() { fill(ctrl.getAllBarang()); }
    private void fill(List<Barang> list) {
        mdl.setRowCount(0); int n = 1;
        for (Barang b : list)
            mdl.addRow(new Object[]{n++,b.getKodeBarang(),b.getPlu()!=null?b.getPlu():"-",b.getNamaBarang(),FormatUtil.formatRupiah(b.getHargaJual()),b.getStok(),b.isStokRendah()?"Rendah":"OK"});
        if (isGridView) refreshGrid();
    }

    private void refreshGrid() {
        gridPanel.removeAll();
        List<Barang> list = ctrl.getAllBarang();
        for (Barang b : list) {
            JPanel card = buildProductCard(b);
            gridPanel.add(card);
        }
        gridPanel.revalidate(); gridPanel.repaint();
    }

    private JPanel buildProductCard(Barang b) {
        JPanel card = new JPanel(new BorderLayout(0, 4)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_CARD);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.setColor(UITheme.BORDER_DEFAULT);
                g2.setStroke(new BasicStroke(0.7f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(160, 200));
        card.setBorder(new EmptyBorder(10,10,10,10));

        // Image
        JLabel imgLbl = new JLabel(ImageLoader.getPlaceholder()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_INPUT);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.dispose(); super.paintComponent(g);
            }
        };
        imgLbl.setPreferredSize(new Dimension(140, 100));
        imgLbl.setHorizontalAlignment(SwingConstants.CENTER);
        imgLbl.setOpaque(false);
        if (b.getImageUrl() != null && !b.getImageUrl().isEmpty())
            ImageLoader.loadAsync(b.getImageUrl(), 140, 100, ic -> { imgLbl.setIcon(ic); imgLbl.setText(null); });

        // Name
        String name = b.getNamaBarang().length() > 20 ? b.getNamaBarang().substring(0,19)+"…" : b.getNamaBarang();
        JLabel lName = new JLabel(name);
        lName.setFont(new Font("Segoe UI",Font.BOLD,11)); lName.setForeground(UITheme.TEXT_PRIMARY);

        // Code & price row
        JPanel info = new JPanel(new BorderLayout()); info.setOpaque(false);
        JLabel lCode = new JLabel(b.getKodeBarang());
        lCode.setFont(UITheme.FONT_LABEL); lCode.setForeground(UITheme.TEXT_MUTED);
        JLabel lPrice = new JLabel(FormatUtil.formatRupiah(b.getHargaJual()));
        lPrice.setFont(new Font("Segoe UI",Font.BOLD,11)); lPrice.setForeground(UITheme.ACCENT_BLUE);
        info.add(lCode, BorderLayout.WEST); info.add(lPrice, BorderLayout.EAST);

        // Stock badge
        JLabel stockBadge = b.isStokRendah()
            ? UITheme.badge("Stok "+b.getStok(), UITheme.ACCENT_CORAL, UITheme.ACCENT_CORAL)
            : UITheme.badge(String.valueOf(b.getStok())+" "+b.getSatuan(), UITheme.ACCENT_GREEN, UITheme.ACCENT_GREEN);

        // Add button
        JButton btnAdd = UITheme.primaryButton("+ Tambah", UITheme.ACCENT_LIME);
        btnAdd.setFont(UITheme.FONT_LABEL);
        btnAdd.setPreferredSize(new Dimension(140, 24));

        JPanel bottom = new JPanel(new BorderLayout(0,4)); bottom.setOpaque(false);
        bottom.add(lName, BorderLayout.NORTH);
        bottom.add(info, BorderLayout.CENTER);
        bottom.add(stockBadge, BorderLayout.SOUTH);

        card.add(imgLbl, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    private void fillForm() {
        int row = table.getSelectedRow(); if(row<0) return;
        String kode = mdl.getValueAt(row,1).toString();
        for (Barang b : ctrl.getAllBarang()) {
            if (b.getKodeBarang().equals(kode)) {
                selId=b.getId(); txtKode.setText(b.getKodeBarang()); txtNama.setText(b.getNamaBarang());
                txtHargaBeli.setText(String.valueOf((long)b.getHargaBeli())); txtHargaJual.setText(String.valueOf((long)b.getHargaJual()));
                txtStok.setText(String.valueOf(b.getStok())); txtStokMin.setText(String.valueOf(b.getStokMinimum()));
                txtSatuan.setText(b.getSatuan()!=null?b.getSatuan():"");
                txtDesk.setText(b.getDeskripsi()!=null?b.getDeskripsi():"");
                txtImgUrl.setText(b.getImageUrl()!=null?b.getImageUrl():"");
                lblFormTitle.setText(b.getNamaBarang().length()>22?b.getNamaBarang().substring(0,21)+"…":b.getNamaBarang());
                if(b.getImageUrl()!=null&&!b.getImageUrl().isEmpty()) ImageLoader.loadAsync(b.getImageUrl(),254,110,ic->{lblPreview.setIcon(ic);lblPreview.setText(null);});
                else lblPreview.setIcon(ImageLoader.getPlaceholder());
                break;
            }
        }
    }

    private void startNew(){clearF();setFE(true);selId=-1;lblFormTitle.setText("Tambah Produk Baru");txtKode.setText(ctrl.generateKode());}
    private void hapus(){
        if(table.getSelectedRow()<0){AlertUtil.showWarning(this,"Pilih barang!");return;}
        if(!Session.isAdmin()){AlertUtil.showWarning(this,"Hanya Admin.");return;}
        if(!AlertUtil.showConfirm(this,"Hapus barang ini?"))return;
        if(ctrl.hapusBarang(selId)){AlertUtil.showInfo(this,"Barang dihapus.");load();clearF();setFE(false);}else AlertUtil.showError(this,"Gagal.");
    }
    private void simpan(){
        if(txtNama.getText().isBlank()||txtHargaJual.getText().isBlank()){AlertUtil.showWarning(this,"Nama dan harga jual wajib!");return;}
        Barang b=new Barang(); b.setId(selId); b.setKodeBarang(txtKode.getText().trim());
        b.setNamaBarang(txtNama.getText().trim()); b.setHargaBeli(FormatUtil.parseDouble(txtHargaBeli.getText()));
        b.setHargaJual(FormatUtil.parseDouble(txtHargaJual.getText())); b.setStok(FormatUtil.parseInt(txtStok.getText()));
        b.setStokMinimum(FormatUtil.parseInt(txtStokMin.getText())); b.setSatuan(txtSatuan.getText().trim());
        b.setDeskripsi(txtDesk.getText().trim()); b.setImageUrl(txtImgUrl.getText().trim());
        boolean ok=(selId==-1)?ctrl.tambahBarang(b):ctrl.updateBarang(b);
        if(ok){AlertUtil.showInfo(this,"Disimpan!");load();clearF();setFE(false);}else AlertUtil.showError(this,"Gagal.");
    }
    private void clearF(){selId=-1;txtKode.setText("");txtNama.setText("");txtHargaBeli.setText("");txtHargaJual.setText("");txtStok.setText("");txtStokMin.setText("");txtSatuan.setText("");txtDesk.setText("");txtImgUrl.setText("");lblPreview.setIcon(ImageLoader.getPlaceholder());}
    private void setFE(boolean e){txtKode.setEnabled(e);txtNama.setEnabled(e);txtHargaBeli.setEnabled(e);txtHargaJual.setEnabled(e);txtStok.setEnabled(e);txtStokMin.setEnabled(e);txtSatuan.setEnabled(e);txtDesk.setEnabled(e);txtImgUrl.setEnabled(e);btnSimpan.setEnabled(e);btnBatal.setEnabled(e);}
    private JTextField fld(String label, JPanel p){JLabel l=UITheme.fieldLabel(label);l.setAlignmentX(LEFT_ALIGNMENT);JTextField f=UITheme.styledField("");f.setMaximumSize(new Dimension(Integer.MAX_VALUE,34));f.setAlignmentX(LEFT_ALIGNMENT);p.add(l);p.add(Box.createVerticalStrut(4));p.add(f);p.add(Box.createVerticalStrut(8));return f;}

    // WrapLayout helper
    static class WrapLayout extends FlowLayout {
        WrapLayout(int a, int h, int v){super(a,h,v);}
        @Override public Dimension preferredLayoutSize(Container c){return layoutSize(c, true);}
        @Override public Dimension minimumLayoutSize(Container c){return layoutSize(c, false);}
        private Dimension layoutSize(Container c, boolean preferred) {
            int W = c.getWidth(); if(W==0) W=800;
            Insets ins = c.getInsets(); W -= ins.left+ins.right+getHgap()*2;
            int rowW=0,rowH=0,totalH=ins.top+ins.bottom;
            for(Component comp:c.getComponents()){
                if(!comp.isVisible()) continue;
                Dimension d = preferred?comp.getPreferredSize():comp.getMinimumSize();
                if(rowW>0&&rowW+getHgap()+d.width>W){totalH+=rowH+getVgap();rowW=0;rowH=0;}
                rowW+=getHgap()+d.width; rowH=Math.max(rowH,d.height);
            }
            totalH+=rowH;
            return new Dimension(W,totalH);
        }
    }
}
