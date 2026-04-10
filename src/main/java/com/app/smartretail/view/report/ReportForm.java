package com.app.smartretail.view.report;

import com.app.smartretail.controller.TransaksiController;
import com.app.smartretail.controller.DashboardController;
import com.app.smartretail.model.Transaksi;
import com.app.smartretail.utils.FormatUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ReportForm extends JPanel {

    private TransaksiController transaksiCtrl;
    private DashboardController dashboardCtrl;
    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> cmbJenis;
    private JButton btnLoad, btnExport;
    private JLabel lblTotalOmzet, lblTotalTrx;
    private JSpinner spDari, spSampai;

    public ReportForm() {
        transaksiCtrl = new TransaksiController();
        dashboardCtrl = new DashboardController();
        setLayout(new BorderLayout(10,10));
        setBorder(new EmptyBorder(15,15,15,15));
        setBackground(new Color(245,247,250));
        build();
    }

    private void build() {
        JLabel title = new JLabel("📄 Laporan");
        title.setFont(new Font("Segoe UI",Font.BOLD,20)); title.setForeground(new Color(30,55,95));
        add(title, BorderLayout.NORTH);

        // Filter panel
        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT,10,8));
        filter.setBackground(Color.WHITE);
        filter.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220,220,220)),new EmptyBorder(8,10,8,10)));

        cmbJenis = new JComboBox<>(new String[]{"Penjualan","Pembelian","Barang Terlaris","Stok Rendah"});
        cmbJenis.setFont(new Font("Segoe UI",Font.PLAIN,12));

        SpinnerDateModel dari   = new SpinnerDateModel(); JSpinner spD = new JSpinner(dari);
        SpinnerDateModel sampai = new SpinnerDateModel(); JSpinner spS = new JSpinner(sampai);
        JSpinner.DateEditor deD = new JSpinner.DateEditor(spD,"dd/MM/yyyy");
        JSpinner.DateEditor deS = new JSpinner.DateEditor(spS,"dd/MM/yyyy");
        spD.setEditor(deD); spS.setEditor(deS);
        spD.setPreferredSize(new Dimension(110,28)); spS.setPreferredSize(new Dimension(110,28));
        this.spDari = spD; this.spSampai = spS;

        btnLoad   = new JButton("🔍 Tampilkan"); sb(btnLoad,new Color(30,55,95));
        btnExport = new JButton("📥 Export PDF"); sb(btnExport,new Color(46,204,113));

        filter.add(new JLabel("Jenis:")); filter.add(cmbJenis);
        filter.add(new JLabel("Dari:")); filter.add(spD);
        filter.add(new JLabel("Sampai:")); filter.add(spS);
        filter.add(btnLoad); filter.add(btnExport);

        // Summary
        JPanel sumPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,20,5)); sumPanel.setOpaque(false);
        lblTotalOmzet = new JLabel("Total Omzet: Rp 0");
        lblTotalOmzet.setFont(new Font("Segoe UI",Font.BOLD,14)); lblTotalOmzet.setForeground(new Color(30,55,95));
        lblTotalTrx = new JLabel("Total Transaksi: 0");
        lblTotalTrx.setFont(new Font("Segoe UI",Font.BOLD,14)); lblTotalTrx.setForeground(new Color(46,204,113));
        sumPanel.add(lblTotalOmzet); sumPanel.add(lblTotalTrx);

        // Table
        tableModel = new DefaultTableModel(new String[]{"No Transaksi","Kasir","Customer","Tanggal","Metode","Grand Total","Status"},0){
            public boolean isCellEditable(int r,int c){return false;}
        };
        table = new JTable(tableModel); table.setRowHeight(28);
        table.getTableHeader().setBackground(new Color(30,55,95)); table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,12));
        table.setFont(new Font("Segoe UI",Font.PLAIN,12));

        JPanel north2 = new JPanel(new BorderLayout(0,8)); north2.setOpaque(false);
        north2.add(filter,BorderLayout.CENTER); north2.add(sumPanel,BorderLayout.SOUTH);

        add(north2, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Events
        btnLoad.addActionListener(e -> loadReport());
        btnExport.addActionListener(e -> AlertUtil.show("Export PDF memerlukan JasperReports.\nSilakan integrasikan library jasperreports.jar."));
    }

    private void loadReport() {
        String jenis = (String) cmbJenis.getSelectedItem();
        tableModel.setRowCount(0);

        if ("Penjualan".equals(jenis)) {
            List<Transaksi> list = transaksiCtrl.getRiwayatPenjualan();
            double total = 0;
            for (Transaksi t : list) {
                tableModel.addRow(new Object[]{t.getNoTransaksi(), t.getNamaUser(), t.getNamaCustomer(),
                    FormatUtil.formatDateTime(t.getTanggal()), t.getMetode(),
                    FormatUtil.formatRupiah(t.getGrandTotal()), t.getStatus()});
                total += t.getGrandTotal();
            }
            lblTotalOmzet.setText("Total Omzet: " + FormatUtil.formatRupiah(total));
            lblTotalTrx.setText("Total Transaksi: " + list.size());
        } else if ("Pembelian".equals(jenis)) {
            List<Transaksi> list = transaksiCtrl.getRiwayatPembelian();
            double total = 0;
            for (Transaksi t : list) {
                tableModel.addRow(new Object[]{t.getNoTransaksi(), t.getNamaUser(), t.getNamaSupplier(),
                    FormatUtil.formatDateTime(t.getTanggal()), t.getMetode(),
                    FormatUtil.formatRupiah(t.getGrandTotal()), t.getStatus()});
                total += t.getGrandTotal();
            }
            lblTotalOmzet.setText("Total Pembelian: " + FormatUtil.formatRupiah(total));
            lblTotalTrx.setText("Total: " + list.size());
        } else if ("Barang Terlaris".equals(jenis)) {
            tableModel.setColumnIdentifiers(new String[]{"Nama Barang","Total Qty","Total Omzet","","",""});
            List<Object[]> list = dashboardCtrl.getBarangTerlaris(20);
            for (Object[] row : list)
                tableModel.addRow(new Object[]{row[0], row[1], FormatUtil.formatRupiah((double)row[2]),"","",""});
            lblTotalTrx.setText("Tampil: "+list.size()+" barang terlaris");
        } else {
            // Stok rendah
            tableModel.setColumnIdentifiers(new String[]{"Kode","Nama Barang","Stok","Stok Min","Status",""});
            // placeholder - loaded from BarangController
            JOptionPane.showMessageDialog(this,"Gunakan menu Manajemen Stok untuk detail stok rendah.");
        }
    }
    
    static class AlertUtil {
        static void show(String msg) { JOptionPane.showMessageDialog(null, msg, "Info", JOptionPane.INFORMATION_MESSAGE); }
    }

    private void sb(JButton b,Color c){b.setBackground(c);b.setForeground(Color.WHITE);b.setFont(new Font("Segoe UI",Font.BOLD,11));b.setFocusPainted(false);b.setBorderPainted(false);b.setCursor(new Cursor(Cursor.HAND_CURSOR));}
}
