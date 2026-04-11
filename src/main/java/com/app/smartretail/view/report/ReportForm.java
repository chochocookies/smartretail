package com.app.smartretail.view.report;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.app.smartretail.controller.DashboardController;
import com.app.smartretail.controller.TransaksiController;
import com.app.smartretail.model.Transaksi;
import com.app.smartretail.utils.FormatUtil;
import com.app.smartretail.utils.UITheme;

public class ReportForm extends JPanel {

    private final TransaksiController transaksiCtrl = new TransaksiController();
    private final DashboardController dashCtrl      = new DashboardController();

    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> cmbJenis;
    private JLabel lblOmzet, lblCount;
    private JPanel chartContainer;

    public ReportForm() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.BG_DARK);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        build();
    }

    private void build() {
        // Header
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.setBorder(new EmptyBorder(0, 0, 20, 0));
        JLabel title = UITheme.pageTitle("Laporan & Analitik");
        JLabel sub = new JLabel("Ringkasan data transaksi dan performa bisnis");
        sub.setFont(UITheme.FONT_BODY); sub.setForeground(UITheme.TEXT_SECONDARY);
        JPanel ht = new JPanel(); ht.setOpaque(false);
        ht.setLayout(new BoxLayout(ht, BoxLayout.Y_AXIS));
        ht.add(title); ht.add(sub);
        hdr.add(ht, BorderLayout.WEST);

        // Filter toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbar.setOpaque(false);
        cmbJenis = UITheme.styledCombo(new String[]{"Penjualan", "Pembelian", "Barang Terlaris"});
        cmbJenis.setPreferredSize(new Dimension(170, 36));
        JButton btnLoad   = UITheme.primaryButton("Tampilkan", UITheme.ACCENT_BLUE);
        JButton btnExport = UITheme.ghostButton("Export PDF", UITheme.ACCENT_AMBER);
        toolbar.add(cmbJenis); toolbar.add(btnLoad); toolbar.add(btnExport);
        hdr.add(toolbar, BorderLayout.EAST);
        add(hdr, BorderLayout.NORTH);

        // Content
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setOpaque(false);

        // Top: summary cards + chart
        JPanel topRow = new JPanel(new GridLayout(1, 3, 16, 0));
        topRow.setOpaque(false);
        topRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        // Summary cards
        lblOmzet = new JLabel("Rp 0");
        lblOmzet.setFont(new Font("Segoe UI",Font.BOLD,20)); lblOmzet.setForeground(UITheme.TEXT_PRIMARY);
        lblCount = new JLabel("0");
        lblCount.setFont(new Font("Segoe UI",Font.BOLD,20)); lblCount.setForeground(UITheme.TEXT_PRIMARY);

        int[] sparkSales = {120,145,130,180,160,210,190,230,200,245};
        topRow.add(metricSmall("Total Omzet", lblOmzet, UITheme.ACCENT_BLUE, sparkSales));
        topRow.add(metricSmall("Jumlah Transaksi", lblCount, UITheme.ACCENT_TEAL, null));

        // Weekly bar chart placeholder
        JPanel chartCard = UITheme.card();
        chartCard.setLayout(new BorderLayout(0, 8));
        JLabel ct = new JLabel("Penjualan Minggu Ini");
        ct.setFont(UITheme.FONT_H3); ct.setForeground(UITheme.TEXT_PRIMARY);

        Map<String,Double> omzetMap = dashCtrl.getOmzetMingguIni();
        String[] days = omzetMap.keySet().stream().map(k->k.substring(8)).toArray(String[]::new);
        int[] vals   = omzetMap.values().stream().mapToInt(Double::intValue).toArray();
        if (days.length == 0) { days = new String[]{"Sen","Sel","Rab","Kam","Jum","Sab","Min"}; vals = new int[]{0,0,0,0,0,0,0}; }
        JPanel barchart = UITheme.barChart(days, vals, UITheme.ACCENT_BLUE);
        chartCard.add(ct, BorderLayout.NORTH);
        chartCard.add(barchart, BorderLayout.CENTER);
        topRow.add(chartCard);

        content.add(topRow, BorderLayout.NORTH);

        // Table
        String[] cols = {"No Transaksi","Kasir / Supplier","Tanggal","Metode","Grand Total","Status"};
        tableModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r,int c){return false;} };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(140);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setMaxWidth(100);
        table.getColumnModel().getColumn(5).setMaxWidth(90);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component cp = super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                cp.setBackground(sel ? new Color(82,130,255,50) : (r%2==0 ? UITheme.BG_CARD : UITheme.BG_ROW_ALT));
                cp.setForeground(UITheme.TEXT_PRIMARY);
                if (c==4) cp.setForeground(UITheme.ACCENT_TEAL);
                if (c==5 && v!=null && "SELESAI".equals(v.toString())) cp.setForeground(UITheme.ACCENT_TEAL);
                ((JLabel)cp).setBorder(new EmptyBorder(0,10,0,10));
                return cp;
            }
        });

        JPanel tableCard = UITheme.card();
        tableCard.setLayout(new BorderLayout(0, 8));
        JLabel tLbl = new JLabel("Riwayat Transaksi");
        tLbl.setFont(UITheme.FONT_H2); tLbl.setForeground(UITheme.TEXT_PRIMARY);
        tableCard.add(tLbl, BorderLayout.NORTH);
        tableCard.add(UITheme.styledScroll(table), BorderLayout.CENTER);

        content.add(tableCard, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);

        // Events
        btnLoad.addActionListener(e -> loadReport());
        btnExport.addActionListener(e ->
            JOptionPane.showMessageDialog(this,
                "Export PDF memerlukan library JasperReports.\nSilakan tambahkan jasperreports.jar ke folder lib/",
                "Info Export", JOptionPane.INFORMATION_MESSAGE));

        loadReport();
    }

    private void loadReport() {
        tableModel.setRowCount(0);
        String jenis = cmbJenis.getSelectedItem().toString();
        double total = 0;
        int count    = 0;

        if ("Penjualan".equals(jenis)) {
            List<Transaksi> list = transaksiCtrl.getRiwayatPenjualan();
            for (Transaksi t : list) {
                tableModel.addRow(new Object[]{
                    t.getNoTransaksi(), t.getNamaUser(),
                    FormatUtil.formatDateTime(t.getTanggal()),
                    t.getMetode(), FormatUtil.formatRupiah(t.getGrandTotal()), t.getStatus()
                });
                total += t.getGrandTotal(); count++;
            }
        } else if ("Pembelian".equals(jenis)) {
            tableModel.setColumnIdentifiers(new String[]{"No Transaksi","Supplier","Tanggal","Metode","Grand Total","Status"});
            List<Transaksi> list = transaksiCtrl.getRiwayatPembelian();
            for (Transaksi t : list) {
                tableModel.addRow(new Object[]{
                    t.getNoTransaksi(), t.getNamaSupplier(),
                    FormatUtil.formatDateTime(t.getTanggal()),
                    t.getMetode(), FormatUtil.formatRupiah(t.getGrandTotal()), t.getStatus()
                });
                total += t.getGrandTotal(); count++;
            }
        } else {
            tableModel.setColumnIdentifiers(new String[]{"Nama Barang","Total Qty","Total Omzet","","",""});
            List<Object[]> list = dashCtrl.getBarangTerlaris(20);
            for (Object[] row : list) {
                tableModel.addRow(new Object[]{row[0], row[1], FormatUtil.formatRupiah((double)row[2]),"","",""});
                total += (double)row[2]; count++;
            }
        }

        lblOmzet.setText(FormatUtil.formatRupiah(total));
        lblCount.setText(String.valueOf(count));
    }

    private JPanel metricSmall(String label, JLabel valLbl, Color accent, int[] spark) {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 6));
        JLabel lLbl = new JLabel(label.toUpperCase());
        lLbl.setFont(UITheme.FONT_LABEL); lLbl.setForeground(UITheme.TEXT_MUTED);
        card.add(lLbl, BorderLayout.NORTH);
        card.add(valLbl, BorderLayout.CENTER);
        if (spark != null) {
            JPanel sp = UITheme.sparkline(spark, accent);
            sp.setPreferredSize(new Dimension(0, 30));
            card.add(sp, BorderLayout.SOUTH);
        }
        return card;
    }
}
