package com.app.smartretail.view.master;

import com.app.smartretail.controller.BarangController;
import com.app.smartretail.model.Barang;
import com.app.smartretail.utils.*;
import com.app.smartretail.view.component.Icons;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class BarangForm extends JPanel {

    private final BarangController ctrl = new BarangController();
    private JTable table; private DefaultTableModel mdl;
    private JTextField txtSearch, txtKode, txtNama, txtHargaBeli, txtHargaJual,
                       txtStok, txtStokMin, txtSatuan, txtImgUrl;
    private JTextArea txtDesk;
    private JButton btnTambah, btnEdit, btnHapus, btnSimpan, btnBatal, btnRefresh;
    private JLabel lblFormTitle, lblPreview;
    private int selId = -1;

    public BarangForm() {
        setLayout(new BorderLayout()); setBackground(UITheme.BG_SURFACE);
        setBorder(new EmptyBorder(22,24,22,24));
        build(); load();
    }

    private void build() {
        // Header
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false); hdr.setBorder(new EmptyBorder(0,0,16,0));
        JPanel ht = new JPanel(); ht.setOpaque(false); ht.setLayout(new BoxLayout(ht, BoxLayout.Y_AXIS));
        ht.add(UITheme.pageTitle("Products")); JLabel sub=new JLabel("Kelola data produk & inventaris");
        sub.setFont(UITheme.FONT_BODY); sub.setForeground(UITheme.TEXT_SECONDARY); ht.add(sub);
        hdr.add(ht, BorderLayout.WEST);
        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); acts.setOpaque(false);
        btnRefresh = UITheme.ghostButton("↻", UITheme.TEXT_MUTED);
        btnHapus   = UITheme.dangerButton("Hapus");
        btnEdit    = UITheme.ghostButton("Edit", UITheme.ACCENT_AMBER);
        btnTambah  = UITheme.primaryButton("+ Tambah", UITheme.ACCENT_LIME);
        acts.add(btnRefresh); acts.add(btnHapus); acts.add(btnEdit); acts.add(btnTambah);
        hdr.add(acts, BorderLayout.EAST);
        add(hdr, BorderLayout.NORTH);

        // Main split
        JPanel main = new JPanel(new BorderLayout(16,0)); main.setOpaque(false);

        // Search
        JPanel searchRow = new JPanel(new BorderLayout(8,0)); searchRow.setOpaque(false);
        txtSearch = UITheme.styledField("Cari kode, nama, PLU, barcode…");
        txtSearch.setPreferredSize(new Dimension(0,38));
        JButton btnS = UITheme.primaryButton("Cari", UITheme.ACCENT_BLUE);
        searchRow.add(txtSearch, BorderLayout.CENTER); searchRow.add(btnS, BorderLayout.EAST);

        // Table
        String[] cols = {"#","Kode","PLU","Nama Barang","Harga Jual","Stok","Status"};
        mdl = new DefaultTableModel(cols,0){ public boolean isCellEditable(int r,int c){return false;} };
        table = new JTable(mdl); UITheme.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(38);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setMaxWidth(60);
        table.getColumnModel().getColumn(6).setMaxWidth(90);
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

        JPanel leftP = new JPanel(new BorderLayout(0,10)); leftP.setOpaque(false);
        leftP.add(searchRow, BorderLayout.NORTH);
        leftP.add(UITheme.styledScroll(table), BorderLayout.CENTER);

        // Form card
        JPanel fc = UITheme.card();
        fc.setLayout(new BoxLayout(fc, BoxLayout.Y_AXIS));
        fc.setPreferredSize(new Dimension(296,0)); fc.setMaximumSize(new Dimension(296,Integer.MAX_VALUE));

        lblFormTitle = new JLabel("Detail Produk");
        lblFormTitle.setFont(UITheme.FONT_H2); lblFormTitle.setForeground(UITheme.TEXT_PRIMARY); lblFormTitle.setAlignmentX(LEFT_ALIGNMENT);

        lblPreview = new JLabel(ImageLoader.getPlaceholder());
        lblPreview.setPreferredSize(new Dimension(258,120)); lblPreview.setMaximumSize(new Dimension(Integer.MAX_VALUE,120));
        lblPreview.setHorizontalAlignment(SwingConstants.CENTER);
        lblPreview.setBorder(BorderFactory.createCompoundBorder(new UITheme.RoundedBorder(8,UITheme.BORDER_DEFAULT,UITheme.BG_INPUT),new EmptyBorder(4,4,4,4)));
        lblPreview.setAlignmentX(LEFT_ALIGNMENT);

        txtKode      = fld("Kode Barang", fc);
        txtNama      = fld("Nama Barang *", fc);
        txtHargaBeli = fld("Harga Beli (Rp)", fc);
        txtHargaJual = fld("Harga Jual (Rp) *", fc);
        txtStok      = fld("Stok Awal", fc);
        txtStokMin   = fld("Stok Minimum", fc);
        txtSatuan    = fld("Satuan (pcs/kg/btl)", fc);
        txtImgUrl    = fld("URL Gambar", fc);

        JButton btnPrev = UITheme.ghostButton("Muat Gambar", UITheme.ACCENT_TEAL);
        btnPrev.setMaximumSize(new Dimension(Integer.MAX_VALUE,30)); btnPrev.setAlignmentX(LEFT_ALIGNMENT);
        btnPrev.addActionListener(e -> { String u=txtImgUrl.getText().trim(); if(!u.isEmpty()) ImageLoader.loadAsync(u,258,120,icon->{lblPreview.setIcon(icon);lblPreview.setText(null);}); });

        JLabel lD=UITheme.fieldLabel("Deskripsi"); lD.setAlignmentX(LEFT_ALIGNMENT);
        txtDesk = new JTextArea(2,0); txtDesk.setFont(UITheme.FONT_BODY); txtDesk.setForeground(UITheme.TEXT_PRIMARY);
        txtDesk.setBackground(UITheme.BG_INPUT); txtDesk.setCaretColor(UITheme.ACCENT_BLUE);
        txtDesk.setLineWrap(true); txtDesk.setBorder(new EmptyBorder(7,10,7,10));
        JScrollPane spD=new JScrollPane(txtDesk); spD.setBorder(new UITheme.RoundedBorder(8,UITheme.BORDER_DEFAULT,null));
        spD.setBackground(UITheme.BG_INPUT); spD.getViewport().setBackground(UITheme.BG_INPUT);
        spD.setMaximumSize(new Dimension(Integer.MAX_VALUE,60)); spD.setAlignmentX(LEFT_ALIGNMENT);

        JPanel btnRow=new JPanel(new GridLayout(1,2,8,0)); btnRow.setOpaque(false);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE,34)); btnRow.setAlignmentX(LEFT_ALIGNMENT);
        btnSimpan=UITheme.primaryButton("Simpan",UITheme.ACCENT_LIME);
        btnBatal=UITheme.ghostButton("Batal",UITheme.TEXT_SECONDARY);
        btnRow.add(btnBatal); btnRow.add(btnSimpan);

        fc.add(lblFormTitle); fc.add(Box.createVerticalStrut(8));
        fc.add(UITheme.separator()); fc.add(Box.createVerticalStrut(8));
        fc.add(lblPreview); fc.add(Box.createVerticalStrut(4));
        fc.add(btnPrev); fc.add(Box.createVerticalStrut(8));
        fc.add(lD); fc.add(Box.createVerticalStrut(4));
        fc.add(spD); fc.add(Box.createVerticalStrut(10));
        fc.add(Box.createVerticalGlue()); fc.add(btnRow);

        main.add(leftP, BorderLayout.CENTER);
        main.add(fc, BorderLayout.EAST);
        add(main, BorderLayout.CENTER);

        setFE(false);
        btnS.addActionListener(e->{String kw=txtSearch.getText().trim();fill(kw.isEmpty()?ctrl.getAllBarang():ctrl.searchBarang(kw));});
        txtSearch.addActionListener(e->btnS.doClick());
        btnRefresh.addActionListener(e->load());
        btnTambah.addActionListener(e->startNew());
        btnEdit.addActionListener(e->{if(table.getSelectedRow()<0){AlertUtil.showWarning(this,"Pilih barang!");return;}setFE(true);lblFormTitle.setText("Edit: "+txtNama.getText());});
        btnHapus.addActionListener(e->hapus());
        btnSimpan.addActionListener(e->simpan());
        btnBatal.addActionListener(e->{clearF();setFE(false);lblFormTitle.setText("Detail Produk");});
        table.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting())fillForm();});
    }

    private JTextField fld(String label, JPanel p) {
        JLabel lbl=UITheme.fieldLabel(label); lbl.setAlignmentX(LEFT_ALIGNMENT);
        JTextField f=UITheme.styledField(""); f.setMaximumSize(new Dimension(Integer.MAX_VALUE,34)); f.setAlignmentX(LEFT_ALIGNMENT);
        p.add(lbl); p.add(Box.createVerticalStrut(4)); p.add(f); p.add(Box.createVerticalStrut(8));
        return f;
    }

    private void load(){fill(ctrl.getAllBarang());}
    private void fill(List<Barang> list){
        mdl.setRowCount(0); int n=1;
        for(Barang b:list) mdl.addRow(new Object[]{n++,b.getKodeBarang(),b.getPlu()!=null?b.getPlu():"-",b.getNamaBarang(),FormatUtil.formatRupiah(b.getHargaJual()),b.getStok(),b.isStokRendah()?"Rendah":"OK"});
    }
    private void fillForm(){
        int row=table.getSelectedRow(); if(row<0)return;
        String kode=mdl.getValueAt(row,1).toString();
        List<Barang> all=ctrl.getAllBarang();
        for(Barang b:all) if(b.getKodeBarang().equals(kode)){
            selId=b.getId(); txtKode.setText(b.getKodeBarang()); txtNama.setText(b.getNamaBarang());
            txtHargaBeli.setText(String.valueOf((long)b.getHargaBeli())); txtHargaJual.setText(String.valueOf((long)b.getHargaJual()));
            txtStok.setText(String.valueOf(b.getStok())); txtStokMin.setText(String.valueOf(b.getStokMinimum()));
            txtSatuan.setText(b.getSatuan()!=null?b.getSatuan():"");
            txtDesk.setText(b.getDeskripsi()!=null?b.getDeskripsi():"");
            txtImgUrl.setText(b.getImageUrl()!=null?b.getImageUrl():"");
            lblFormTitle.setText(b.getNamaBarang().length()>20?b.getNamaBarang().substring(0,19)+"…":b.getNamaBarang());
            if(b.getImageUrl()!=null&&!b.getImageUrl().isEmpty()) ImageLoader.loadAsync(b.getImageUrl(),258,120,ic->{lblPreview.setIcon(ic);lblPreview.setText(null);});
            else lblPreview.setIcon(ImageLoader.getPlaceholder());
            break;
        }
    }
    private void startNew(){clearF();setFE(true);selId=-1;lblFormTitle.setText("Tambah Produk Baru");txtKode.setText(ctrl.generateKode());}
    private void hapus(){
        if(table.getSelectedRow()<0){AlertUtil.showWarning(this,"Pilih barang!");return;}
        if(!Session.isAdmin()){AlertUtil.showWarning(this,"Hanya Admin yang dapat menghapus.");return;}
        if(!AlertUtil.showConfirm(this,"Hapus barang ini?")) return;
        if(ctrl.hapusBarang(selId)){AlertUtil.showInfo(this,"Barang berhasil dihapus.");load();clearF();setFE(false);}
        else AlertUtil.showError(this,"Gagal menghapus.");
    }
    private void simpan(){
        if(txtNama.getText().isBlank()||txtHargaJual.getText().isBlank()){AlertUtil.showWarning(this,"Nama dan harga jual wajib diisi!");return;}
        Barang b=new Barang(); b.setId(selId); b.setKodeBarang(txtKode.getText().trim());
        b.setNamaBarang(txtNama.getText().trim()); b.setHargaBeli(FormatUtil.parseDouble(txtHargaBeli.getText()));
        b.setHargaJual(FormatUtil.parseDouble(txtHargaJual.getText())); b.setStok(FormatUtil.parseInt(txtStok.getText()));
        b.setStokMinimum(FormatUtil.parseInt(txtStokMin.getText())); b.setSatuan(txtSatuan.getText().trim());
        b.setDeskripsi(txtDesk.getText().trim()); b.setImageUrl(txtImgUrl.getText().trim());
        boolean ok=(selId==-1)?ctrl.tambahBarang(b):ctrl.updateBarang(b);
        if(ok){AlertUtil.showInfo(this,"Data barang berhasil disimpan!");load();clearF();setFE(false);}
        else AlertUtil.showError(this,"Gagal menyimpan.");
    }
    private void clearF(){selId=-1;txtKode.setText("");txtNama.setText("");txtHargaBeli.setText("");txtHargaJual.setText("");txtStok.setText("");txtStokMin.setText("");txtSatuan.setText("");txtDesk.setText("");txtImgUrl.setText("");lblPreview.setIcon(ImageLoader.getPlaceholder());}
    private void setFE(boolean e){txtKode.setEnabled(e);txtNama.setEnabled(e);txtHargaBeli.setEnabled(e);txtHargaJual.setEnabled(e);txtStok.setEnabled(e);txtStokMin.setEnabled(e);txtSatuan.setEnabled(e);txtDesk.setEnabled(e);txtImgUrl.setEnabled(e);btnSimpan.setEnabled(e);btnBatal.setEnabled(e);}
}
