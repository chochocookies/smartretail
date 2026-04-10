package com.app.smartretail.view.transaksi;

import com.app.smartretail.controller.BarangController;
import com.app.smartretail.controller.TransaksiController;
import com.app.smartretail.dao.SupplierDAO;
import com.app.smartretail.model.*;
import com.app.smartretail.utils.AlertUtil;
import com.app.smartretail.utils.FormatUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PembelianForm extends JPanel {

    private TransaksiController transaksiCtrl;
    private BarangController barangCtrl;
    private SupplierDAO supplierDAO;

    private JTextField txtKodeBarang, txtJumlahBayar;
    private JLabel lblNoTransaksi, lblTotal, lblGrandTotal;
    private JTable tblCart;
    private DefaultTableModel cartModel;
    private JComboBox<String> cmbSupplier;
    private JButton btnTambah, btnHapus, btnProses, btnReset;

    private Transaksi currentTransaksi;

    public PembelianForm() {
        transaksiCtrl = new TransaksiController();
        barangCtrl    = new BarangController();
        supplierDAO   = new SupplierDAO();
        setLayout(new BorderLayout(10,10));
        setBorder(new EmptyBorder(15,15,15,15));
        setBackground(new Color(245,247,250));
        build(); reset();
    }

    private void build() {
        JLabel title = new JLabel("📦 Pembelian Barang");
        title.setFont(new Font("Segoe UI",Font.BOLD,20)); title.setForeground(new Color(30,55,95));

        JPanel hdr = new JPanel(new BorderLayout()); hdr.setOpaque(false);
        lblNoTransaksi = new JLabel("No: -"); lblNoTransaksi.setForeground(Color.GRAY);
        hdr.add(title, BorderLayout.WEST); hdr.add(lblNoTransaksi, BorderLayout.EAST);
        add(hdr, BorderLayout.NORTH);

        String[] cols = {"#","Kode","Nama Barang","Harga Beli","Qty","Subtotal"};
        cartModel = new DefaultTableModel(cols,0){ public boolean isCellEditable(int r,int c){return c==4;} };
        tblCart = new JTable(cartModel); tblCart.setRowHeight(30);
        tblCart.getTableHeader().setBackground(new Color(30,55,95)); tblCart.getTableHeader().setForeground(Color.WHITE);
        tblCart.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,12));
        tblCart.setFont(new Font("Segoe UI",Font.PLAIN,12));
        add(new JScrollPane(tblCart), BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(10,0)); south.setOpaque(false);

        JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.LEFT,8,5)); inputRow.setOpaque(false);
        inputRow.setBorder(BorderFactory.createTitledBorder("Tambah Barang"));

        // Supplier
        cmbSupplier = new JComboBox<>();
        cmbSupplier.addItem("-- Pilih Supplier --");
        for (Supplier s : supplierDAO.getAll()) cmbSupplier.addItem(s.getNamaSupplier());
        cmbSupplier.setPreferredSize(new Dimension(180,28));

        txtKodeBarang = new JTextField(14); txtKodeBarang.putClientProperty("JTextField.placeholderText","Kode barang...");
        btnTambah = new JButton("➕ Tambah"); sb(btnTambah, new Color(46,204,113));
        btnHapus  = new JButton("🗑️ Hapus Item"); sb(btnHapus, new Color(231,76,60));
        inputRow.add(new JLabel("Supplier:")); inputRow.add(cmbSupplier);
        inputRow.add(new JLabel("Kode:")); inputRow.add(txtKodeBarang);
        inputRow.add(btnTambah); inputRow.add(btnHapus);

        JPanel sumPanel = new JPanel(new GridLayout(0,2,8,6));
        sumPanel.setBackground(Color.WHITE);
        sumPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200,200,200)),new EmptyBorder(12,15,12,15)));
        sumPanel.setPreferredSize(new Dimension(320,130));

        lblTotal = new JLabel("Rp 0"); lblTotal.setFont(new Font("Segoe UI",Font.BOLD,16));
        lblGrandTotal = new JLabel("Rp 0"); lblGrandTotal.setFont(new Font("Segoe UI",Font.BOLD,18)); lblGrandTotal.setForeground(new Color(30,55,95));
        txtJumlahBayar = new JTextField("0"); txtJumlahBayar.setFont(new Font("Segoe UI",Font.BOLD,14));

        ar(sumPanel,"Total:",lblTotal); ar(sumPanel,"Grand Total:",lblGrandTotal); ar(sumPanel,"Jumlah Bayar:",txtJumlahBayar);

        JPanel bp = new JPanel(new GridLayout(1,2,8,0)); bp.setOpaque(false);
        btnReset  = new JButton("🔄 Reset"); btnProses = new JButton("✅ Proses Pembelian");
        sb(btnReset,new Color(149,165,166)); sb(btnProses,new Color(30,55,95)); btnProses.setFont(new Font("Segoe UI",Font.BOLD,13));
        bp.add(btnReset); bp.add(btnProses);

        JPanel rightS = new JPanel(new BorderLayout(0,8)); rightS.setOpaque(false);
        rightS.add(sumPanel,BorderLayout.CENTER); rightS.add(bp,BorderLayout.SOUTH);

        south.add(inputRow,BorderLayout.CENTER); south.add(rightS,BorderLayout.EAST);
        add(south,BorderLayout.SOUTH);

        btnTambah.addActionListener(e->tambah()); txtKodeBarang.addActionListener(e->tambah());
        btnHapus.addActionListener(e->hapus());
        btnProses.addActionListener(e->proses());
        btnReset.addActionListener(e->reset());
    }

    private void tambah() {
        String kode = txtKodeBarang.getText().trim();
        if (kode.isEmpty()) return;
        Barang b = barangCtrl.getByKode(kode);
        if (b == null) { AlertUtil.showWarning(this,"Barang tidak ditemukan!"); return; }

        for (int i=0;i<cartModel.getRowCount();i++) {
            if (cartModel.getValueAt(i,1).equals(b.getKodeBarang())) {
                int qty = Integer.parseInt(cartModel.getValueAt(i,4).toString())+1;
                cartModel.setValueAt(qty,i,4);
                cartModel.setValueAt(FormatUtil.formatRupiah(qty*b.getHargaBeli()),i,5);
                hitungTotal(); txtKodeBarang.setText(""); return;
            }
        }
        cartModel.addRow(new Object[]{cartModel.getRowCount()+1,b.getKodeBarang(),b.getNamaBarang(),FormatUtil.formatRupiah(b.getHargaBeli()),1,FormatUtil.formatRupiah(b.getHargaBeli())});
        hitungTotal(); txtKodeBarang.setText("");
    }

    private void hapus() {
        int row=tblCart.getSelectedRow();
        if(row<0){AlertUtil.showWarning(this,"Pilih item dahulu!");return;}
        cartModel.removeRow(row);
        for(int i=0;i<cartModel.getRowCount();i++) cartModel.setValueAt(i+1,i,0);
        hitungTotal();
    }

    private void hitungTotal() {
        double total=0;
        for(int i=0;i<cartModel.getRowCount();i++){String sub=cartModel.getValueAt(i,5).toString().replaceAll("[^\\d]","");total+=sub.isEmpty()?0:Double.parseDouble(sub);}
        lblTotal.setText(FormatUtil.formatRupiah(total)); lblGrandTotal.setText(FormatUtil.formatRupiah(total));
    }

    private void proses() {
        if(cartModel.getRowCount()==0){AlertUtil.showWarning(this,"Keranjang kosong!");return;}
        for(int i=0;i<cartModel.getRowCount();i++){
            Barang b=barangCtrl.getByKode(cartModel.getValueAt(i,1).toString());
            if(b==null) continue;
            int qty=Integer.parseInt(cartModel.getValueAt(i,4).toString());
            TransaksiDetail d=new TransaksiDetail(b.getId(),b.getKodeBarang(),b.getNamaBarang(),qty,b.getHargaBeli());
            d.hitungSubtotal(); currentTransaksi.addDetail(d);
        }
        currentTransaksi.setBayar(FormatUtil.parseDouble(txtJumlahBayar.getText()));
        if(transaksiCtrl.simpanPembelian(currentTransaksi)){AlertUtil.showInfo(this,"✅ Pembelian berhasil!\nNo: "+currentTransaksi.getNoTransaksi());reset();}
        else AlertUtil.showError(this,"Gagal menyimpan pembelian!");
    }

    private void reset() {
        currentTransaksi=new Transaksi();
        cartModel.setRowCount(0);
        lblNoTransaksi.setText("No: "+transaksiCtrl.generateNoTransaksi("PBL"));
        lblTotal.setText("Rp 0"); lblGrandTotal.setText("Rp 0");
        txtKodeBarang.setText(""); txtJumlahBayar.setText("0");
        cmbSupplier.setSelectedIndex(0);
    }

    private void ar(JPanel p,String l,Component c){JLabel lb=new JLabel(l);lb.setFont(new Font("Segoe UI",Font.BOLD,12));p.add(lb);p.add(c);}
    private void sb(JButton b,Color c){b.setBackground(c);b.setForeground(Color.WHITE);b.setFont(new Font("Segoe UI",Font.BOLD,11));b.setFocusPainted(false);b.setBorderPainted(false);b.setCursor(new Cursor(Cursor.HAND_CURSOR));}
}
