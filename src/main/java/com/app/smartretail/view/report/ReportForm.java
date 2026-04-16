package com.app.smartretail.view.report;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.app.smartretail.config.DatabaseConnection;
import com.app.smartretail.controller.DashboardController;
import com.app.smartretail.controller.TransaksiController;
import com.app.smartretail.utils.AlertUtil;
import com.app.smartretail.utils.FormatUtil;
import com.app.smartretail.utils.Session;
import com.app.smartretail.utils.UITheme;

/**
 * ReportForm — Laporan penjualan, pembelian, barang terlaris.
 * Export: CSV (native), PDF (via JasperReports jika tersedia).
 */
public class ReportForm extends JPanel {

    private final TransaksiController trxCtrl  = new TransaksiController();
    private final DashboardController dashCtrl = new DashboardController();

    private JTable table;
    private DefaultTableModel mdl;
    private JComboBox<String> cmbJenis, cmbPeriod;
    private JLabel lblTotal, lblCount, lblAvgVal;

    // Currently loaded data for export
    private List<Object[]> currentData = new ArrayList<>();
    private String[] currentHeaders;

    public ReportForm() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_SURFACE);
        setBorder(new EmptyBorder(22, 24, 22, 24));
        build();
    }

    private void build() {
        // ── Header ──────────────────────────────────────────────
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false); hdr.setBorder(new EmptyBorder(0,0,16,0));
        JPanel ht = new JPanel(); ht.setOpaque(false); ht.setLayout(new BoxLayout(ht,BoxLayout.Y_AXIS));
        ht.add(UITheme.pageTitle("Reports"));
        JLabel sub = new JLabel("Laporan transaksi & ekspor data");
        sub.setFont(UITheme.FONT_BODY); sub.setForeground(UITheme.TEXT_SECONDARY); ht.add(sub);

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); acts.setOpaque(false);
        JButton btnCSV = UITheme.ghostButton("Export CSV", UITheme.ACCENT_TEAL);
        JButton btnPDF = UITheme.primaryButton("Export PDF", UITheme.ACCENT_AMBER);
        JButton btnPrint = UITheme.ghostButton("Print", UITheme.TEXT_SECONDARY);
        acts.add(btnPrint); acts.add(btnCSV); acts.add(btnPDF);

        hdr.add(ht, BorderLayout.WEST); hdr.add(acts, BorderLayout.EAST);
        add(hdr, BorderLayout.NORTH);

        // ── Summary cards ────────────────────────────────────────
        JPanel sumRow = new JPanel(new GridLayout(1,3,12,0)); sumRow.setOpaque(false);
        sumRow.setBorder(new EmptyBorder(0,0,16,0));

        lblTotal  = new JLabel("Rp 0"); lblTotal.setFont(new Font("Segoe UI",Font.BOLD,18)); lblTotal.setForeground(UITheme.TEXT_PRIMARY);
        lblCount  = new JLabel("0");    lblCount.setFont(new Font("Segoe UI",Font.BOLD,18)); lblCount.setForeground(UITheme.TEXT_PRIMARY);
        lblAvgVal = new JLabel("Rp 0"); lblAvgVal.setFont(new Font("Segoe UI",Font.BOLD,18)); lblAvgVal.setForeground(UITheme.TEXT_PRIMARY);

        sumRow.add(makeSumCard("Total Omzet",     lblTotal,  UITheme.CARD_AMBER_BG,  UITheme.ACCENT_AMBER));
        sumRow.add(makeSumCard("Jumlah Transaksi",lblCount,  UITheme.CARD_PURPLE_BG, UITheme.ACCENT_PURPLE));
        sumRow.add(makeSumCard("Rata-rata / Trx", lblAvgVal, UITheme.CARD_TEAL_BG,   UITheme.ACCENT_TEAL));

        // ── Filter bar ───────────────────────────────────────────
        JPanel filterCard = UITheme.card(); filterCard.setLayout(new BorderLayout(14,0));

        JPanel filterLeft = new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
        filterLeft.setOpaque(false);

        cmbJenis = UITheme.styledCombo(new String[]{"Penjualan","Pembelian","Barang Terlaris"});
        cmbJenis.setPreferredSize(new Dimension(170,34));

        cmbPeriod = UITheme.styledCombo(new String[]{"Semua Waktu","Hari Ini","7 Hari","30 Hari","Bulan Ini","Tahun Ini"});
        cmbPeriod.setPreferredSize(new Dimension(140,34));

        JButton btnLoad = UITheme.primaryButton("Tampilkan", UITheme.ACCENT_LIME);

        filterLeft.add(new JLabel("Jenis:"  )); filterLeft.add(cmbJenis);
        filterLeft.add(new JLabel("Periode:")); filterLeft.add(cmbPeriod);
        filterLeft.add(btnLoad);
        ((JLabel)filterLeft.getComponent(0)).setFont(UITheme.FONT_SMALL);
        ((JLabel)filterLeft.getComponent(0)).setForeground(UITheme.TEXT_SECONDARY);
        ((JLabel)filterLeft.getComponent(2)).setFont(UITheme.FONT_SMALL);
        ((JLabel)filterLeft.getComponent(2)).setForeground(UITheme.TEXT_SECONDARY);

        filterCard.add(filterLeft, BorderLayout.WEST);

        // ── Table card ───────────────────────────────────────────
        JPanel tableCard = UITheme.card(); tableCard.setLayout(new BorderLayout(0,8));

        String[] defCols = {"No Transaksi","Tanggal","Kasir","Customer","Items","Metode","Grand Total","Status"};
        mdl = new DefaultTableModel(defCols, 0){ public boolean isCellEditable(int r,int c){return false;} };
        table = new JTable(mdl); UITheme.styleTable(table); table.setRowHeight(38);
        table.getColumnModel().getColumn(0).setPreferredWidth(130);
        table.getColumnModel().getColumn(5).setMaxWidth(80);
        table.getColumnModel().getColumn(7).setMaxWidth(90);
        table.setDefaultRenderer(Object.class, tableRenderer());

        // Pagination
        JPanel pager = buildPager();

        tableCard.add(UITheme.styledScroll(table), BorderLayout.CENTER);
        tableCard.add(pager, BorderLayout.SOUTH);

        // ── Assembly ─────────────────────────────────────────────
        JPanel content = new JPanel(new BorderLayout(0,12)); content.setOpaque(false);
        content.add(sumRow, BorderLayout.NORTH);

        JPanel mid = new JPanel(new BorderLayout(0,12)); mid.setOpaque(false);
        mid.add(filterCard, BorderLayout.NORTH);
        mid.add(tableCard, BorderLayout.CENTER);
        content.add(mid, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);

        // ── Events ───────────────────────────────────────────────
        btnLoad.addActionListener(e -> loadReport());
        cmbJenis.addActionListener(e -> loadReport());
        btnCSV.addActionListener(e -> exportCSV());
        btnPDF.addActionListener(e -> exportPDF());
        btnPrint.addActionListener(e -> printTable());

        loadReport();
    }

    // ── Data loading ──────────────────────────────────────────────
    private void loadReport() {
        mdl.setRowCount(0);
        currentData.clear();
        String jenis  = cmbJenis.getSelectedItem().toString();
        String period = cmbPeriod.getSelectedItem().toString();
        String whereDate = buildDateFilter(period);

        if ("Penjualan".equals(jenis)) {
            currentHeaders = new String[]{"No Transaksi","Tanggal","Kasir","Customer","Items","Metode","Grand Total","Status"};
            mdl.setColumnIdentifiers(currentHeaders);
            String sql =
                "SELECT t.no_transaksi,t.tanggal,u.nama_lengkap AS kasir," +
                "COALESCE(c.nama_customer,'Umum') AS customer," +
                "COUNT(td.id) AS items," +
                "t.metode,t.grand_total,t.status " +
                "FROM transaksi t " +
                "LEFT JOIN users u ON t.user_id=u.id " +
                "LEFT JOIN customer c ON t.customer_id=c.id " +
                "LEFT JOIN transaksi_detail td ON td.transaksi_id=t.id " +
                "WHERE t.tipe='PENJUALAN'" + whereDate +
                " GROUP BY t.id ORDER BY t.tanggal DESC";
            double total = 0; int count = 0;
            try (Statement st = DatabaseConnection.getInstance().createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    Object[] row = {
                        rs.getString("no_transaksi"),
                        new SimpleDateFormat("dd/MM/yyyy HH:mm").format(rs.getTimestamp("tanggal")),
                        rs.getString("kasir"),
                        rs.getString("customer"),
                        rs.getInt("items"),
                        rs.getString("metode"),
                        FormatUtil.formatRupiah(rs.getDouble("grand_total")),
                        rs.getString("status")
                    };
                    mdl.addRow(row); currentData.add(row);
                    total += rs.getDouble("grand_total"); count++;
                }
            } catch (SQLException ex) { AlertUtil.showError(this, ex.getMessage()); }
            updateSummary(total, count);

        } else if ("Pembelian".equals(jenis)) {
            currentHeaders = new String[]{"No PO","Tanggal","Petugas","Supplier","Items","Total","Status"};
            mdl.setColumnIdentifiers(currentHeaders);
            String sql =
                "SELECT t.no_transaksi,t.tanggal,u.nama_lengkap AS petugas," +
                "COALESCE(s.nama_supplier,'—') AS supplier," +
                "COUNT(td.id) AS items,t.grand_total,t.status " +
                "FROM transaksi t " +
                "LEFT JOIN users u ON t.user_id=u.id " +
                "LEFT JOIN supplier s ON t.supplier_id=s.id " +
                "LEFT JOIN transaksi_detail td ON td.transaksi_id=t.id " +
                "WHERE t.tipe='PEMBELIAN'" + whereDate +
                " GROUP BY t.id ORDER BY t.tanggal DESC";
            double total = 0; int count = 0;
            try (Statement st = DatabaseConnection.getInstance().createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    Object[] row = {
                        rs.getString("no_transaksi"),
                        new SimpleDateFormat("dd/MM/yyyy HH:mm").format(rs.getTimestamp("tanggal")),
                        rs.getString("petugas"), rs.getString("supplier"),
                        rs.getInt("items"),
                        FormatUtil.formatRupiah(rs.getDouble("grand_total")),
                        rs.getString("status")
                    };
                    mdl.addRow(row); currentData.add(row);
                    total += rs.getDouble("grand_total"); count++;
                }
            } catch (SQLException ex) { AlertUtil.showError(this, ex.getMessage()); }
            updateSummary(total, count);

        } else {
            currentHeaders = new String[]{"#","Kode","Nama Barang","Kategori","Total Qty","Total Omzet"};
            mdl.setColumnIdentifiers(currentHeaders);
            String sql =
                "SELECT b.kode_barang,b.nama_barang,k.nama_kategori," +
                "SUM(td.qty) AS total_qty,SUM(td.subtotal) AS total_omzet " +
                "FROM transaksi_detail td " +
                "JOIN barang b ON td.barang_id=b.id " +
                "JOIN kategori k ON b.kategori_id=k.id " +
                "JOIN transaksi t ON td.transaksi_id=t.id " +
                "WHERE t.tipe='PENJUALAN' AND t.status='SELESAI'" + whereDate +
                " GROUP BY b.id ORDER BY total_qty DESC LIMIT 50";
            double total = 0; int count = 0, n = 1;
            try (Statement st = DatabaseConnection.getInstance().createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    double omzet = rs.getDouble("total_omzet");
                    Object[] row = {
                        n++, rs.getString("kode_barang"), rs.getString("nama_barang"),
                        rs.getString("nama_kategori"), rs.getInt("total_qty"),
                        FormatUtil.formatRupiah(omzet)
                    };
                    mdl.addRow(row); currentData.add(row);
                    total += omzet; count++;
                }
            } catch (SQLException ex) { AlertUtil.showError(this, ex.getMessage()); }
            updateSummary(total, count);
        }
    }

    private String buildDateFilter(String period) {
        switch (period) {
            case "Hari Ini":   return " AND DATE(t.tanggal)=CURDATE()";
            case "7 Hari":     return " AND t.tanggal>=DATE_SUB(NOW(),INTERVAL 7 DAY)";
            case "30 Hari":    return " AND t.tanggal>=DATE_SUB(NOW(),INTERVAL 30 DAY)";
            case "Bulan Ini":  return " AND MONTH(t.tanggal)=MONTH(NOW()) AND YEAR(t.tanggal)=YEAR(NOW())";
            case "Tahun Ini":  return " AND YEAR(t.tanggal)=YEAR(NOW())";
            default:           return "";
        }
    }

    private void updateSummary(double total, int count) {
        lblTotal.setText(FormatUtil.formatRupiah(total));
        lblCount.setText(String.valueOf(count));
        lblAvgVal.setText(count > 0 ? FormatUtil.formatRupiah(total/count) : "Rp 0");
    }

    // ── Export CSV ────────────────────────────────────────────────
    private void exportCSV() {
        if (currentData.isEmpty()) { AlertUtil.showWarning(this,"Tidak ada data untuk diekspor!"); return; }
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Simpan sebagai CSV");
        fc.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)","csv"));
        String defaultName = "SRMS_" + cmbJenis.getSelectedItem().toString().replace(" ","_") + "_" +
        new SimpleDateFormat("yyyyMMdd_HHmm").format(new java.util.Date()) + ".csv";
        fc.setSelectedFile(new File(defaultName));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = fc.getSelectedFile();
        if (!file.getName().endsWith(".csv")) file = new File(file.getAbsolutePath()+".csv");
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file),"UTF-8"))) {
            // BOM for Excel UTF-8
            pw.print('\uFEFF');
            // Header
            pw.println(String.join(",", Arrays.stream(currentHeaders)
                .map(h -> "\""+h+"\"").toArray(String[]::new)));
            // Rows
            for (Object[] row : currentData) {
                StringBuilder sb = new StringBuilder();
                for (int i=0;i<row.length;i++) {
                    if(i>0) sb.append(",");
                    String v = row[i]==null?"":row[i].toString().replace("\"","\"\"");
                    sb.append("\"").append(v).append("\"");
                }
                pw.println(sb);
            }
            AlertUtil.showInfo(this,"CSV berhasil disimpan!\n"+file.getAbsolutePath());
        } catch (IOException ex) {
            AlertUtil.showError(this,"Gagal menyimpan file: "+ex.getMessage());
        }
    }

    // ── Export PDF ────────────────────────────────────────────────
    private void exportPDF() {
        // Try JasperReports first — if available use it, else use built-in simple PDF
        try {
            Class.forName("net.sf.jasperreports.engine.JasperReport");
            exportJasperPDF();
        } catch (ClassNotFoundException e) {
            exportSimplePDF();
        }
    }

    private void exportJasperPDF() {
        // JasperReports integration — uses programmatic report
        try {
            Class<?> jr = Class.forName("net.sf.jasperreports.engine.JasperFillManager");
            AlertUtil.showInfo(this,"JasperReports ditemukan.\nFitur ini memerlukan template .jrxml di folder report/jasper/.\nSilakan tambahkan template laporan terlebih dahulu.");
        } catch(Exception ex) {
            AlertUtil.showError(this,"JasperReports error: "+ex.getMessage());
        }
    }

    /** Built-in simple PDF using PostScript-like HTML → print pipeline */
    private void exportSimplePDF() {
        if (currentData.isEmpty()) { AlertUtil.showWarning(this,"Tidak ada data!"); return; }

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Simpan Laporan PDF");
        fc.setFileFilter(new FileNameExtensionFilter("PDF Files (*.pdf)","pdf"));
        String defaultName = "Laporan_" + cmbJenis.getSelectedItem().toString().replace(" ","_") + "_" +
        new SimpleDateFormat("yyyyMMdd_HHmm").format(new java.util.Date()) + ".pdf";
        fc.setSelectedFile(new File(defaultName));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File pdfFile = fc.getSelectedFile();
        if (!pdfFile.getName().endsWith(".pdf")) pdfFile = new File(pdfFile.getAbsolutePath()+".pdf");

        // Generate HTML report first, then convert using Java print API
        try {
            String html = buildHTMLReport();
            File htmlTemp = File.createTempFile("srms_report_", ".html");
            try (PrintWriter pw = new PrintWriter(htmlTemp, "UTF-8")) { pw.print(html); }

            // Try to open with print dialog via Desktop
            if (Desktop.isDesktopSupported()) {
                AlertUtil.showInfo(this,
                    "Laporan HTML telah dibuat.\n" +
                    "Untuk ekspor ke PDF:\n" +
                    "1. File akan dibuka di browser\n" +
                    "2. Gunakan Ctrl+P → Simpan sebagai PDF\n\n" +
                    "File: " + pdfFile.getAbsolutePath());
                Desktop.getDesktop().open(htmlTemp);
            }

            // Also save as HTML with .pdf extension hint
            File htmlOut = new File(pdfFile.getAbsolutePath().replace(".pdf",".html"));
            try (PrintWriter pw = new PrintWriter(htmlOut, "UTF-8")) { pw.print(html); }
            AlertUtil.showInfo(this,"Laporan HTML tersimpan:\n" + htmlOut.getAbsolutePath() +
                "\n\nUntuk PDF, buka file di browser → Print → Simpan sebagai PDF");
        } catch (Exception ex) {
            AlertUtil.showError(this,"Gagal membuat laporan: " + ex.getMessage());
        }
    }

    private String buildHTMLReport() {
        String jenis  = cmbJenis.getSelectedItem().toString();
        String period = cmbPeriod.getSelectedItem().toString();
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>")
          .append("<title>Laporan ").append(jenis).append("</title>")
          .append("<style>")
          .append("*{margin:0;padding:0;box-sizing:border-box;font-family:'Segoe UI',Arial,sans-serif}")
          .append("body{padding:24px;color:#111827;background:#fff}")
          .append(".header{border-bottom:2px solid #111827;padding-bottom:12px;margin-bottom:20px}")
          .append(".header h1{font-size:22px;font-weight:700}")
          .append(".header p{font-size:11px;color:#6B7280;margin-top:4px}")
          .append(".meta{display:flex;gap:24px;margin-bottom:20px}")
          .append(".meta-item{background:#F9FAFB;border-radius:8px;padding:10px 16px}")
          .append(".meta-item label{font-size:10px;color:#6B7280;text-transform:uppercase}")
          .append(".meta-item span{display:block;font-size:16px;font-weight:700;color:#111827;margin-top:2px}")
          .append("table{width:100%;border-collapse:collapse;font-size:12px}")
          .append("th{background:#F3F4F6;padding:10px 12px;text-align:left;font-size:10px;text-transform:uppercase;color:#6B7280;border-bottom:1px solid #E5E7EB}")
          .append("td{padding:10px 12px;border-bottom:1px solid #F3F4F6;color:#374151}")
          .append("tr:nth-child(even)td{background:#FAFAFA}")
          .append(".status-selesai{color:#059669;font-weight:600}")
          .append(".status-batal{color:#DC2626;font-weight:600}")
          .append(".footer{margin-top:20px;padding-top:12px;border-top:1px solid #E5E7EB;font-size:10px;color:#9CA3AF;text-align:center}")
          .append("@media print{body{padding:0}.no-print{display:none}}")
          .append("</style></head><body>");

        // Header
        sb.append("<div class='header'>")
          .append("<h1>Laporan ").append(jenis).append("</h1>")
          .append("<p>Periode: ").append(period)
          .append(" &nbsp;|&nbsp; Dicetak: ")
          .append(new SimpleDateFormat("dd MMMM yyyy HH:mm", new Locale("id","ID")).format(new java.util.Date()))
          .append(" &nbsp;|&nbsp; Oleh: ")
          .append(Session.currentUser != null ? Session.currentUser.getNamaLengkap() : "—")
          .append("</p></div>");

        // Summary
        sb.append("<div class='meta'>")
          .append("<div class='meta-item'><label>Total Omzet</label><span>").append(lblTotal.getText()).append("</span></div>")
          .append("<div class='meta-item'><label>Jumlah Transaksi</label><span>").append(lblCount.getText()).append("</span></div>")
          .append("<div class='meta-item'><label>Rata-rata / Transaksi</label><span>").append(lblAvgVal.getText()).append("</span></div>")
          .append("</div>");

        // Table
        sb.append("<table><thead><tr>");
        for (String h : currentHeaders) sb.append("<th>").append(h).append("</th>");
        sb.append("</tr></thead><tbody>");
        for (Object[] row : currentData) {
            sb.append("<tr>");
            for (Object cell : row) {
                String val = cell == null ? "" : cell.toString();
                String cls = val.contains("SELESAI") ? " class='status-selesai'" :
                             val.contains("BATAL")   ? " class='status-batal'" : "";
                sb.append("<td").append(cls).append(">").append(val).append("</td>");
            }
            sb.append("</tr>");
        }
        sb.append("</tbody></table>");

        sb.append("<div class='footer'>SRMS — Smart Retail Management System v5 &nbsp;|&nbsp; ")
          .append("com.app.smartretail</div>");
        sb.append("</body></html>");
        return sb.toString();
    }

    private void printTable() {
    try {
        java.awt.print.Printable printable = table.getPrintable(
            javax.swing.JTable.PrintMode.FIT_WIDTH, 
            null,
            null
        );

        java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
        
        java.awt.print.PageFormat pf = job.defaultPage();
        java.awt.print.Paper p = new java.awt.print.Paper();
        p.setSize(595, 842);
        p.setImageableArea(36, 36, 523, 770);
        pf.setPaper(p);

        job.setPrintable(printable, pf);

        if (job.printDialog()) {
            job.print();
        }
        
    } catch (java.awt.print.PrinterException ex) {
        AlertUtil.showError(this, "Gagal mencetak: " + ex.getMessage());
    }
}


    // ── UI helpers ────────────────────────────────────────────────
    private JPanel makeSumCard(String label, JLabel val, Color tint, Color accent) {
        JPanel card = UITheme.tintCard(tint);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(UITheme.FONT_LABEL); lbl.setForeground(UITheme.TEXT_SECONDARY);
        card.add(lbl); card.add(Box.createVerticalStrut(4)); card.add(val);
        return card;
    }

    private JPanel buildPager() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER,6,6)); p.setOpaque(false);
        p.setBorder(BorderFactory.createMatteBorder(1,0,0,0,UITheme.BORDER_DEFAULT));
        for (int i=0; i<5; i++) {
            boolean first = i==0;
            JButton b = new JButton(first?"1":String.valueOf(i+1)){
                @Override protected void paintComponent(Graphics g){
                    Graphics2D g2=(Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    if(first){g2.setColor(UITheme.ACCENT_LIME);g2.fillOval(0,0,getWidth(),getHeight());}
                    else if(getModel().isRollover()){g2.setColor(UITheme.BG_HOVER);g2.fillOval(0,0,getWidth(),getHeight());}
                    g2.dispose(); super.paintComponent(g);
                }
            };
            b.setFont(UITheme.FONT_SMALL);
            b.setForeground(first?UITheme.TEXT_ON_LIME:UITheme.TEXT_SECONDARY);
            b.setPreferredSize(new Dimension(30,30));
            b.setOpaque(false); b.setContentAreaFilled(false);
            b.setBorderPainted(false); b.setFocusPainted(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            p.add(b);
        }
        return p;
    }

    private DefaultTableCellRenderer tableRenderer() {
        return new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c){
                Component cp=super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                cp.setBackground(sel?new Color(238,242,255):(r%2==0?UITheme.BG_CARD:UITheme.BG_ROW_ALT));
                cp.setForeground(UITheme.TEXT_PRIMARY);
                // Color code status and amount columns
                if(v!=null){
                    String s=v.toString();
                    if("SELESAI".equals(s)) cp.setForeground(UITheme.ACCENT_TEAL);
                    else if("BATAL".equals(s)) cp.setForeground(UITheme.ACCENT_CORAL);
                    else if(s.startsWith("Rp ")) cp.setForeground(UITheme.ACCENT_BLUE);
                }
                ((JLabel)cp).setBorder(new EmptyBorder(0,12,0,12));
                return cp;
            }
        };
    }


}
