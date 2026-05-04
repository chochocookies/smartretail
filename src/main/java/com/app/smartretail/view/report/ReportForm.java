package com.app.smartretail.view.report;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.io.*;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;

// JasperReports — tersedia via pom.xml (6.21.2)
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;
import net.sf.jasperreports.engine.design.*;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.*;
import net.sf.jasperreports.engine.type.*;

import com.app.smartretail.config.DatabaseConnection;
import com.app.smartretail.controller.DashboardController;
import com.app.smartretail.controller.TransaksiController;
import com.app.smartretail.utils.AlertUtil;
import com.app.smartretail.utils.FormatUtil;
import com.app.smartretail.utils.Session;
import com.app.smartretail.utils.UITheme;

/**
 * ReportForm — Laporan penjualan, pembelian, barang terlaris.
 *
 * FIX PDF: JasperReports dipakai secara programatik (tidak butuh .jrxml).
 * Laporan di-build lewat JasperDesign API → compile → fill → export PDF.
 * Tidak perlu file template eksternal sama sekali.
 */
public class ReportForm extends JPanel {

    private final TransaksiController trxCtrl  = new TransaksiController();
    private final DashboardController dashCtrl = new DashboardController();

    private JTable table;
    private DefaultTableModel mdl;
    private JComboBox<String> cmbJenis, cmbPeriod;
    private JLabel lblTotal, lblCount, lblAvgVal;

    private List<Object[]> currentData    = new ArrayList<>();
    private String[]       currentHeaders = {};

    // ── Search/filter state ──
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtSearch;

    public ReportForm() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_SURFACE);
        setBorder(new EmptyBorder(22, 24, 22, 24));
        build();
    }

    // ════════════════════════════════════════════════════════════════
    // BUILD UI
    // ════════════════════════════════════════════════════════════════
    private void build() {
        // ── Header ──
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false); hdr.setBorder(new EmptyBorder(0, 0, 16, 0));
        JPanel ht = new JPanel(); ht.setOpaque(false);
        ht.setLayout(new BoxLayout(ht, BoxLayout.Y_AXIS));
        ht.add(UITheme.pageTitle("Reports"));
        JLabel sub = new JLabel("Laporan transaksi & ekspor data");
        sub.setFont(UITheme.FONT_BODY); sub.setForeground(UITheme.TEXT_SECONDARY); ht.add(sub);

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); acts.setOpaque(false);
        JButton btnCSV   = UITheme.ghostButton("Export CSV",  UITheme.ACCENT_TEAL);
        JButton btnPDF   = UITheme.primaryButton("Export PDF", UITheme.ACCENT_AMBER);
        JButton btnPrint = UITheme.ghostButton("Print",        UITheme.TEXT_SECONDARY);
        acts.add(btnPrint); acts.add(btnCSV); acts.add(btnPDF);
        hdr.add(ht, BorderLayout.WEST); hdr.add(acts, BorderLayout.EAST);
        add(hdr, BorderLayout.NORTH);

        // ── Summary cards ──
        JPanel sumRow = new JPanel(new GridLayout(1, 3, 12, 0));
        sumRow.setOpaque(false); sumRow.setBorder(new EmptyBorder(0, 0, 16, 0));
        lblTotal  = boldLabel("Rp 0", 18, UITheme.TEXT_PRIMARY);
        lblCount  = boldLabel("0",    18, UITheme.TEXT_PRIMARY);
        lblAvgVal = boldLabel("Rp 0", 18, UITheme.TEXT_PRIMARY);
        sumRow.add(makeSumCard("Total Omzet",      lblTotal,  UITheme.CARD_AMBER_BG,  UITheme.ACCENT_AMBER));
        sumRow.add(makeSumCard("Jumlah Transaksi", lblCount,  UITheme.CARD_PURPLE_BG, UITheme.ACCENT_PURPLE));
        sumRow.add(makeSumCard("Rata-rata / Trx",  lblAvgVal, UITheme.CARD_TEAL_BG,   UITheme.ACCENT_TEAL));

        // ── Filter bar ──
        JPanel filterCard = UITheme.card();
        filterCard.setLayout(new BorderLayout(12, 0));

        JPanel filterLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterLeft.setOpaque(false);
        cmbJenis  = UITheme.styledCombo(new String[]{"Penjualan","Pembelian","Barang Terlaris"});
        cmbJenis.setPreferredSize(new Dimension(170, 34));
        cmbPeriod = UITheme.styledCombo(new String[]{"Semua Waktu","Hari Ini","7 Hari","30 Hari","Bulan Ini","Tahun Ini"});
        cmbPeriod.setPreferredSize(new Dimension(140, 34));
        JButton btnLoad = UITheme.primaryButton("Tampilkan", UITheme.ACCENT_LIME);
        txtSearch = UITheme.styledField("Cari di hasil...");
        txtSearch.setPreferredSize(new Dimension(180, 34));

        filterLeft.add(label("Jenis:")); filterLeft.add(cmbJenis);
        filterLeft.add(label("Periode:")); filterLeft.add(cmbPeriod);
        filterLeft.add(btnLoad);
        filterLeft.add(Box.createHorizontalStrut(8));
        filterLeft.add(label("Cari:")); filterLeft.add(txtSearch);
        filterCard.add(filterLeft, BorderLayout.WEST);

        // ── Table ──
        String[] defCols = {"No Transaksi","Tanggal","Kasir","Customer","Items","Metode","Grand Total","Status"};
        mdl = new DefaultTableModel(defCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(mdl);
        UITheme.styleTable(table); table.setRowHeight(38);
        table.setDefaultRenderer(Object.class, tableRenderer());
        table.getTableHeader().setToolTipText("Klik header untuk mengurutkan");

        sorter = new TableRowSorter<>(mdl);
        table.setRowSorter(sorter);

        JPanel tableCard = UITheme.card();
        tableCard.setLayout(new BorderLayout(0, 0));
        tableCard.add(UITheme.styledScroll(table), BorderLayout.CENTER);
        tableCard.add(buildStatusBar(), BorderLayout.SOUTH);

        // ── Assembly ──
        JPanel content = new JPanel(new BorderLayout(0, 12)); content.setOpaque(false);
        content.add(sumRow, BorderLayout.NORTH);
        JPanel mid = new JPanel(new BorderLayout(0, 12)); mid.setOpaque(false);
        mid.add(filterCard, BorderLayout.NORTH);
        mid.add(tableCard,  BorderLayout.CENTER);
        content.add(mid, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);

        // ── Events ──
        btnLoad.addActionListener(e -> loadReport());
        cmbJenis.addActionListener(e -> loadReport());
        cmbPeriod.addActionListener(e -> loadReport());
        btnCSV.addActionListener(e -> exportCSV());
        btnPDF.addActionListener(e -> exportPDF());
        btnPrint.addActionListener(e -> printTable());
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterTable(); }
            public void removeUpdate(DocumentEvent e) { filterTable(); }
            public void changedUpdate(DocumentEvent e) { filterTable(); }
        });

        loadReport();
    }

    private JLabel buildStatusBar() {
        JLabel bar = new JLabel(" ");
        bar.setFont(UITheme.FONT_SMALL); bar.setForeground(UITheme.TEXT_MUTED);
        bar.setBorder(new EmptyBorder(4, 12, 4, 12));
        return bar;
    }

    private void filterTable() {
        String kw = txtSearch.getText().trim();
        sorter.setRowFilter(kw.isEmpty() ? null : RowFilter.regexFilter("(?i)" + kw));
    }

    // ════════════════════════════════════════════════════════════════
    // DATA LOADING
    // ════════════════════════════════════════════════════════════════
    private void loadReport() {
        mdl.setRowCount(0);
        currentData.clear();
        String jenis  = cmbJenis.getSelectedItem().toString();
        String period = cmbPeriod.getSelectedItem().toString();
        String where  = buildDateFilter(period);

        if ("Penjualan".equals(jenis)) {
            currentHeaders = new String[]{"No Transaksi","Tanggal","Kasir","Customer","Items","Metode","Grand Total","Status"};
            mdl.setColumnIdentifiers(currentHeaders);
            String sql =
                "SELECT t.no_transaksi,t.tanggal,u.nama_lengkap AS kasir," +
                "COALESCE(c.nama_customer,'Umum') AS customer," +
                "COUNT(td.id) AS items,t.metode,t.grand_total,t.status " +
                "FROM transaksi t " +
                "LEFT JOIN users u ON t.user_id=u.id " +
                "LEFT JOIN customer c ON t.customer_id=c.id " +
                "LEFT JOIN transaksi_detail td ON td.transaksi_id=t.id " +
                "WHERE t.tipe='PENJUALAN'" + where +
                " GROUP BY t.id ORDER BY t.tanggal DESC";
            fillTable(sql, new String[]{"no_transaksi","tanggal","kasir","customer","items","metode","grand_total","status"}, true);

        } else if ("Pembelian".equals(jenis)) {
            currentHeaders = new String[]{"No PO","Tanggal","Petugas","Supplier","Items","Grand Total","Status"};
            mdl.setColumnIdentifiers(currentHeaders);
            String sql =
                "SELECT t.no_transaksi,t.tanggal,u.nama_lengkap AS petugas," +
                "COALESCE(s.nama_supplier,'—') AS supplier," +
                "COUNT(td.id) AS items,t.grand_total,t.status " +
                "FROM transaksi t " +
                "LEFT JOIN users u ON t.user_id=u.id " +
                "LEFT JOIN supplier s ON t.supplier_id=s.id " +
                "LEFT JOIN transaksi_detail td ON td.transaksi_id=t.id " +
                "WHERE t.tipe='PEMBELIAN'" + where +
                " GROUP BY t.id ORDER BY t.tanggal DESC";
            fillTable(sql, new String[]{"no_transaksi","tanggal","petugas","supplier","items","grand_total","status"}, true);

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
                "WHERE t.tipe='PENJUALAN' AND t.status='SELESAI'" + where +
                " GROUP BY b.id ORDER BY total_qty DESC LIMIT 50";
            fillTableBarang(sql);
        }
        sorter = new TableRowSorter<>(mdl);
        table.setRowSorter(sorter);
        filterTable();
    }

    private void fillTable(String sql, String[] cols, boolean hasGrandTotal) {
        double total = 0; int count = 0;
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Object[] row = new Object[cols.length];
                for (int i = 0; i < cols.length; i++) {
                    String col = cols[i];
                    if ("tanggal".equals(col)) {
                        java.sql.Timestamp _ts = rs.getTimestamp("tanggal");
                        row[i] = _ts != null ? FormatUtil.formatDateTime(_ts.toLocalDateTime()) : "-";
                    } else if ("grand_total".equals(col)) {
                        double v = rs.getDouble("grand_total");
                        row[i] = FormatUtil.formatRupiah(v);
                        total += v; count++;
                    } else {
                        row[i] = rs.getObject(col);
                        if (row[i] == null) row[i] = "-";
                    }
                }
                mdl.addRow(row);
                currentData.add(row);
            }
        } catch (SQLException ex) { AlertUtil.showError(this, "SQL error: " + ex.getMessage()); }
        updateSummary(total, count);
    }

    private void fillTableBarang(String sql) {
        double total = 0; int count = 0, n = 1;
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                double omzet = rs.getDouble("total_omzet");
                Object[] row = {
                    n++, rs.getString("kode_barang"), rs.getString("nama_barang"),
                    rs.getString("nama_kategori"),    rs.getInt("total_qty"),
                    FormatUtil.formatRupiah(omzet)
                };
                mdl.addRow(row); currentData.add(row);
                total += omzet; count++;
            }
        } catch (SQLException ex) { AlertUtil.showError(this, "SQL error: " + ex.getMessage()); }
        updateSummary(total, count);
    }

    private String buildDateFilter(String period) {
        switch (period) {
            case "Hari Ini":  return " AND DATE(t.tanggal)=CURDATE()";
            case "7 Hari":    return " AND t.tanggal>=DATE_SUB(NOW(),INTERVAL 7 DAY)";
            case "30 Hari":   return " AND t.tanggal>=DATE_SUB(NOW(),INTERVAL 30 DAY)";
            case "Bulan Ini": return " AND MONTH(t.tanggal)=MONTH(NOW()) AND YEAR(t.tanggal)=YEAR(NOW())";
            case "Tahun Ini": return " AND YEAR(t.tanggal)=YEAR(NOW())";
            default:          return "";
        }
    }

    private void updateSummary(double total, int count) {
        lblTotal.setText(FormatUtil.formatRupiah(total));
        lblCount.setText(String.valueOf(count));
        lblAvgVal.setText(count > 0 ? FormatUtil.formatRupiah(total / count) : "Rp 0");
    }

    // ════════════════════════════════════════════════════════════════
    // EXPORT PDF — JasperReports Programatik (TIDAK butuh .jrxml)
    // ════════════════════════════════════════════════════════════════
    private void exportPDF() {
        if (currentData.isEmpty()) {
            AlertUtil.showWarning(this, "Tidak ada data untuk diekspor.\nKlik Tampilkan terlebih dahulu.");
            return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Simpan Laporan PDF");
        fc.setFileFilter(new FileNameExtensionFilter("PDF Files (*.pdf)", "pdf"));
        String fname = "Laporan_" + cmbJenis.getSelectedItem().toString().replace(" ", "_")
                     + "_" + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".pdf";
        fc.setSelectedFile(new File(System.getProperty("user.home"), fname));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File pdfFile = fc.getSelectedFile();
        if (!pdfFile.getName().toLowerCase().endsWith(".pdf"))
            pdfFile = new File(pdfFile.getAbsolutePath() + ".pdf");

        final File finalFile = pdfFile;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        JDialog progress = makeProgressDialog("Membuat PDF...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            String errMsg = null;
            @Override
            protected Void doInBackground() {
                try {
                    JasperPrint print = buildJasperPrint();
                    JRPdfExporter exporter = new JRPdfExporter();
                    exporter.setExporterInput(new SimpleExporterInput(print));
                    exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(finalFile));
                    SimplePdfReportConfiguration cfg = new SimplePdfReportConfiguration();
                    cfg.setSizePageToContent(false);
                    cfg.setForceLineBreakPolicy(false);
                    exporter.setConfiguration(cfg);
                    exporter.exportReport();
                } catch (Exception ex) {
                    errMsg = ex.getMessage();
                    ex.printStackTrace();
                }
                return null;
            }
            @Override
            protected void done() {
                progress.dispose();
                setCursor(Cursor.getDefaultCursor());
                if (errMsg != null) {
                    AlertUtil.showError(ReportForm.this, "Gagal export PDF:\n" + errMsg);
                } else {
                    int choice = JOptionPane.showConfirmDialog(ReportForm.this,
                        "PDF berhasil disimpan!\n" + finalFile.getAbsolutePath() +
                        "\n\nBuka file sekarang?",
                        "Export Berhasil", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                    if (choice == JOptionPane.YES_OPTION) {
                        try { Desktop.getDesktop().open(finalFile); }
                        catch (Exception ex) { AlertUtil.showWarning(ReportForm.this, "Buka file gagal: " + ex.getMessage()); }
                    }
                }
            }
        };
        progress.setVisible(true);
        worker.execute();
    }

    /**
     * Bangun JasperPrint secara programatik — tidak perlu file .jrxml.
     *
     * Struktur laporan:
     *   - Title band  : nama laporan, periode, metadata
     *   - Column header band: header kolom
     *   - Detail band : satu baris per record
     *   - Summary band: total omzet + jumlah transaksi
     *   - Page footer : nomor halaman + timestamp
     */
    private JasperPrint buildJasperPrint() throws JRException {
        String jenis  = cmbJenis.getSelectedItem().toString();
        String period = cmbPeriod.getSelectedItem().toString();
        String by     = Session.currentUser != null ? Session.currentUser.getNamaLengkap() : "System";
        String now    = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm", new java.util.Locale("id","ID")));

        // ── Dimensi halaman A4 landscape ──────────────────────────
        // A4 landscape: 842 x 595 pt (JasperReports pakai pixel @72dpi)
        final int PAGE_W = 842;
        final int PAGE_H = 595;
        final int MARGIN  = 30;
        final int CONTENT_W = PAGE_W - MARGIN * 2;

        JasperDesign design = new JasperDesign();
        design.setName("SmartRetailReport");
        design.setPageWidth(PAGE_W);
        design.setPageHeight(PAGE_H);
        // Landscape implied by PAGE_W(842) > PAGE_H(595)
        design.setLeftMargin(MARGIN);
        design.setRightMargin(MARGIN);
        design.setTopMargin(MARGIN);
        design.setBottomMargin(MARGIN);
        design.setColumnWidth(CONTENT_W);
        design.setColumnSpacing(0);

        // ── Hitung lebar kolom proporsional ───────────────────────
        int[] colWidths = calcColWidths(currentHeaders, CONTENT_W);

        // ── Daftarkan field sesuai kolom ─────────────────────────
        for (int i = 0; i < currentHeaders.length; i++) {
            JRDesignField field = new JRDesignField();
            field.setName("F" + i);
            field.setValueClass(String.class);
            design.addField(field);
        }

        // ── Warna ─────────────────────────────────────────────────
        Color C_HEADER_BG = new Color(30,  41,  59);   // slate-800
        Color C_HEADER_FG = Color.WHITE;
        Color C_TITLE_BG  = new Color(15,  23,  42);   // slate-900
        Color C_ALT_ROW   = new Color(248, 250, 252);  // slate-50
        Color C_BORDER    = new Color(226, 232, 240);  // slate-200
        Color C_ACCENT    = new Color(16,  185, 129);  // emerald-500

        // ────────────────────────────────────────────────────────
        // TITLE BAND
        // ────────────────────────────────────────────────────────
        JRDesignBand titleBand = new JRDesignBand();
        titleBand.setHeight(90);

        // Background rect
        JRDesignRectangle bgRect = new JRDesignRectangle();
        bgRect.setX(0); bgRect.setY(0);
        bgRect.setWidth(CONTENT_W); bgRect.setHeight(90);
        bgRect.setRadius(0);
        bgRect.getLinePen().setLineWidth(0f);
        bgRect.setBackcolor(C_TITLE_BG);
        bgRect.setMode(ModeEnum.OPAQUE);
        titleBand.addElement(bgRect);

        // Accent stripe kiri
        JRDesignRectangle accentRect = new JRDesignRectangle();
        accentRect.setX(0); accentRect.setY(0);
        accentRect.setWidth(5); accentRect.setHeight(90);
        accentRect.getLinePen().setLineWidth(0f);
        accentRect.setBackcolor(C_ACCENT);
        accentRect.setMode(ModeEnum.OPAQUE);
        titleBand.addElement(accentRect);

        // Judul laporan
        titleBand.addElement(staticText("Laporan " + jenis.toUpperCase(), 14, 10, 12,
            CONTENT_W - 20, 28, HorizontalTextAlignEnum.LEFT, Font.BOLD, C_HEADER_FG));

        // Subtitle
        titleBand.addElement(staticText(
            "Periode: " + period + "   |   Dicetak: " + now + "   |   Oleh: " + by,
            10, 14, 42, CONTENT_W - 20, 20,
            HorizontalTextAlignEnum.LEFT, Font.PLAIN, new Color(148, 163, 184)));

        // Summary cards kecil di title
        String[] sumLabels = {"Total Omzet", "Jumlah Transaksi", "Rata-rata/Trx"};
        String[] sumValues = {lblTotal.getText(), lblCount.getText(), lblAvgVal.getText()};
        int cardW = (CONTENT_W - 20) / 3;
        for (int i = 0; i < 3; i++) {
            int cx = 14 + i * cardW;
            JRDesignRectangle cardBg = new JRDesignRectangle();
            cardBg.setX(cx); cardBg.setY(62);
            cardBg.setWidth(cardW - 8); cardBg.setHeight(22);
            cardBg.getLinePen().setLineWidth(0f);
            cardBg.setBackcolor(new Color(30, 41, 59));
            cardBg.setMode(ModeEnum.OPAQUE);
            titleBand.addElement(cardBg);
            titleBand.addElement(staticText(sumLabels[i] + ": " + sumValues[i],
                9, cx + 4, 64, cardW - 16, 18,
                HorizontalTextAlignEnum.LEFT, Font.BOLD,
                i == 0 ? new Color(251, 191, 36) : i == 1 ? new Color(139, 92, 246) : C_ACCENT));
        }

        design.setTitle(titleBand);

        // ────────────────────────────────────────────────────────
        // COLUMN HEADER BAND
        // ────────────────────────────────────────────────────────
        JRDesignBand colHdrBand = new JRDesignBand();
        colHdrBand.setHeight(28);

        // Background bar
        JRDesignRectangle hdrBg = new JRDesignRectangle();
        hdrBg.setX(0); hdrBg.setY(0);
        hdrBg.setWidth(CONTENT_W); hdrBg.setHeight(28);
        hdrBg.getLinePen().setLineWidth(0f);
        hdrBg.setBackcolor(C_HEADER_BG);
        hdrBg.setMode(ModeEnum.OPAQUE);
        colHdrBand.addElement(hdrBg);

        int cx = 0;
        for (int i = 0; i < currentHeaders.length; i++) {
            colHdrBand.addElement(staticText(currentHeaders[i].toUpperCase(),
                8, cx + 4, 2, colWidths[i] - 8, 24,
                HorizontalTextAlignEnum.LEFT, Font.BOLD, C_HEADER_FG));
            cx += colWidths[i];
        }
        design.setColumnHeader(colHdrBand);

        // ────────────────────────────────────────────────────────
        // DETAIL BAND
        // ────────────────────────────────────────────────────────
        JRDesignBand detailBand = new JRDesignBand();
        detailBand.setHeight(24);
        detailBand.setSplitType(SplitTypeEnum.STRETCH);

        // Alternating background via PrintWhenExpression
        JRDesignRectangle rowBg = new JRDesignRectangle();
        rowBg.setX(0); rowBg.setY(0);
        rowBg.setWidth(CONTENT_W); rowBg.setHeight(24);
        rowBg.getLinePen().setLineWidth(0f);
        rowBg.setBackcolor(C_ALT_ROW);
        rowBg.setMode(ModeEnum.OPAQUE);
        JRDesignExpression altExpr = new JRDesignExpression();
        altExpr.setText("$V{REPORT_COUNT} % 2 == 0");
        rowBg.setPrintWhenExpression(altExpr);
        detailBand.addElement(rowBg);

        // Bottom border setiap baris
        JRDesignLine rowLine = new JRDesignLine();
        rowLine.setX(0); rowLine.setY(23);
        rowLine.setWidth(CONTENT_W); rowLine.setHeight(1);
        rowLine.getLinePen().setLineWidth(0.5f);
        rowLine.getLinePen().setLineColor(C_BORDER);
        detailBand.addElement(rowLine);

        cx = 0;
        for (int i = 0; i < currentHeaders.length; i++) {
            JRDesignTextField tf = new JRDesignTextField();
            tf.setX(cx + 4); tf.setY(2);
            tf.setWidth(colWidths[i] - 8); tf.setHeight(20);
            tf.setBlankWhenNull(true);
            tf.setStretchWithOverflow(true);

            // Detect kolom uang & status untuk warna
            String hdr = currentHeaders[i];
            boolean isMoney  = hdr.contains("Total") || hdr.contains("Grand") || hdr.contains("Omzet") || hdr.contains("Rata");
            boolean isStatus = "Status".equals(hdr);

            JRDesignExpression expr = new JRDesignExpression();
            expr.setText("$F{F" + i + "}");
            tf.setExpression(expr);
            tf.setHorizontalTextAlign(
                isMoney ? HorizontalTextAlignEnum.RIGHT : HorizontalTextAlignEnum.LEFT);
            tf.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
            tf.setForecolor(isMoney ? new Color(5, 150, 105) : UITheme.TEXT_PRIMARY);

            // Set warna kolom: uang=hijau, status=biru, lainnya=gelap
            tf.setForecolor(isMoney  ? new Color(5, 150, 105) :
                            isStatus ? new Color(7, 89, 133)   : Color.DARK_GRAY);
            tf.setFontSize(9f);

            detailBand.addElement(tf);
            cx += colWidths[i];
        }

        ((JRDesignSection) design.getDetailSection()).addBand(detailBand);

        // ────────────────────────────────────────────────────────
        // SUMMARY BAND
        // ────────────────────────────────────────────────────────
        JRDesignBand sumBand = new JRDesignBand();
        sumBand.setHeight(32);

        JRDesignRectangle sumBg = new JRDesignRectangle();
        sumBg.setX(0); sumBg.setY(0);
        sumBg.setWidth(CONTENT_W); sumBg.setHeight(32);
        sumBg.getLinePen().setLineWidth(0f);
        sumBg.setBackcolor(new Color(241, 245, 249));
        sumBg.setMode(ModeEnum.OPAQUE);
        sumBand.addElement(sumBg);

        sumBand.addElement(staticText(
            "Total: " + currentData.size() + " baris   |   " +
            lblTotal.getText() + "   |   " +
            lblCount.getText() + " transaksi",
            10, 8, 6, CONTENT_W - 16, 22,
            HorizontalTextAlignEnum.LEFT, Font.BOLD, new Color(30, 41, 59)));

        design.setSummary(sumBand);

        // ────────────────────────────────────────────────────────
        // PAGE FOOTER BAND
        // ────────────────────────────────────────────────────────
        JRDesignBand footBand = new JRDesignBand();
        footBand.setHeight(20);

        footBand.addElement(staticText("SRMS — Smart Retail Management System",
            8, 0, 4, CONTENT_W / 2, 14,
            HorizontalTextAlignEnum.LEFT, Font.PLAIN, new Color(156, 163, 175)));

        // Nomor halaman via field REPORT_COUNT (pakai static untuk sederhana)
        JRDesignTextField pageNum = new JRDesignTextField();
        pageNum.setX(CONTENT_W / 2); pageNum.setY(4);
        pageNum.setWidth(CONTENT_W / 2); pageNum.setHeight(14);
        pageNum.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
        JRDesignExpression pageExpr = new JRDesignExpression();
        pageExpr.setText("\"Halaman \" + $V{PAGE_NUMBER}");
        pageNum.setExpression(pageExpr);
        pageNum.setForecolor(new Color(156, 163, 175));
        footBand.addElement(pageNum);

        design.setPageFooter(footBand);

        // ── Compile + fill ─────────────────────────────────────
        JasperReport report = JasperCompileManager.compileReport(design);

        // Siapkan data source dari DefaultTableModel
        // Wrap model yang hanya berisi currentData (bukan seluruh mdl jika ada filter)
        DefaultTableModel dataModel = new DefaultTableModel(currentHeaders, 0);
        for (Object[] row : currentData) dataModel.addRow(row);

        Map<String, Object> params = new HashMap<>();
        params.put(JRParameter.REPORT_LOCALE, new Locale("id","ID"));

        JasperPrint print = JasperFillManager.fillReport(
            report, params, new JRTableModelDataSource(dataModel));

        return print;
    }

    // ── Hitung lebar kolom proporsional ───────────────────────────
    private int[] calcColWidths(String[] headers, int totalWidth) {
        // Bobot relatif berdasarkan jenis kolom
        int[] weights = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            String h = headers[i].toLowerCase();
            if (h.contains("no") || h.contains("#"))                weights[i] = 6;
            else if (h.contains("tanggal") || h.contains("waktu"))  weights[i] = 8;
            else if (h.contains("nama"))                             weights[i] = 12;
            else if (h.contains("total") || h.contains("grand"))    weights[i] = 8;
            else if (h.contains("omzet"))                           weights[i] = 8;
            else if (h.contains("supplier") || h.contains("customer")) weights[i] = 9;
            else if (h.contains("kasir") || h.contains("petugas"))  weights[i] = 7;
            else if (h.contains("status") || h.contains("metode"))  weights[i] = 5;
            else if (h.contains("items") || h.contains("qty"))      weights[i] = 4;
            else                                                     weights[i] = 6;
        }
        int totalWeight = Arrays.stream(weights).sum();
        int[] widths = new int[headers.length];
        int used = 0;
        for (int i = 0; i < headers.length - 1; i++) {
            widths[i] = (int)((double) weights[i] / totalWeight * totalWidth);
            used += widths[i];
        }
        widths[headers.length - 1] = totalWidth - used; // sisa ke kolom terakhir
        return widths;
    }

    // ── JasperDesign helper: static text ─────────────────────────
    private JRDesignStaticText staticText(String text, int fontSize, int x, int y, int w, int h,
                                           HorizontalTextAlignEnum align, int fontStyle, Color fg) {
        JRDesignStaticText st = new JRDesignStaticText();
        st.setText(text);
        st.setX(x); st.setY(y); st.setWidth(w); st.setHeight(h);
        st.setHorizontalTextAlign(align);
        st.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        st.setForecolor(fg);
        st.setFontSize((float) fontSize);
        st.setBold(fontStyle == Font.BOLD);
        st.setItalic(fontStyle == Font.ITALIC);
        return st;
    }

    // ════════════════════════════════════════════════════════════════
    // EXPORT CSV
    // ════════════════════════════════════════════════════════════════
    private void exportCSV() {
        if (currentData.isEmpty()) { AlertUtil.showWarning(this, "Tidak ada data untuk diekspor!"); return; }
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Simpan sebagai CSV");
        fc.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));
        String defaultName = "SRMS_" + cmbJenis.getSelectedItem().toString().replace(" ","_") + "_" +
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".csv";
        fc.setSelectedFile(new File(System.getProperty("user.home"), defaultName));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = fc.getSelectedFile();
        if (!file.getName().endsWith(".csv")) file = new File(file.getAbsolutePath() + ".csv");
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
            pw.print('\uFEFF'); // BOM for Excel
            pw.println(csvRow(currentHeaders));
            for (Object[] row : currentData) pw.println(csvRow(row));
            AlertUtil.showInfo(this, "CSV berhasil disimpan!\n" + file.getAbsolutePath());
        } catch (IOException ex) {
            AlertUtil.showError(this, "Gagal menyimpan: " + ex.getMessage());
        }
    }

    private String csvRow(Object[] cells) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) sb.append(",");
            String v = cells[i] == null ? "" : cells[i].toString().replace("\"","\"\"");
            sb.append("\"").append(v).append("\"");
        }
        return sb.toString();
    }

    private String csvRow(String[] cells) {
        Object[] o = new Object[cells.length];
        System.arraycopy(cells, 0, o, 0, cells.length);
        return csvRow(o);
    }

    // ════════════════════════════════════════════════════════════════
    // PRINT
    // ════════════════════════════════════════════════════════════════
    private void printTable() {
        try {
            Printable printable = table.getPrintable(JTable.PrintMode.FIT_WIDTH, null, null);
            PrinterJob job = PrinterJob.getPrinterJob();
            PageFormat pf = job.defaultPage();
            Paper paper = new Paper();
            paper.setSize(842, 595);
            paper.setImageableArea(36, 36, 770, 523);
            pf.setPaper(paper);
            pf.setOrientation(PageFormat.LANDSCAPE);
            job.setPrintable(printable, pf);
            if (job.printDialog()) job.print();
        } catch (PrinterException ex) {
            AlertUtil.showError(this, "Gagal mencetak: " + ex.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════
    // PROGRESS DIALOG
    // ════════════════════════════════════════════════════════════════
    private JDialog makeProgressDialog(String msg) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), false);
        dlg.setUndecorated(true);
        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setBorder(new EmptyBorder(20, 28, 20, 28));
        p.setBackground(UITheme.BG_CARD);
        JProgressBar bar = new JProgressBar(); bar.setIndeterminate(true);
        bar.setPreferredSize(new Dimension(220, 8));
        p.add(new JLabel(msg), BorderLayout.NORTH);
        p.add(bar, BorderLayout.CENTER);
        dlg.add(p); dlg.pack();
        dlg.setLocationRelativeTo(this);
        return dlg;
    }

    // ════════════════════════════════════════════════════════════════
    // UI HELPERS
    // ════════════════════════════════════════════════════════════════
    private JPanel makeSumCard(String lbl, JLabel val, Color tint, Color accent) {
        JPanel card = UITheme.tintCard(tint);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(lbl.toUpperCase());
        l.setFont(UITheme.FONT_LABEL); l.setForeground(UITheme.TEXT_SECONDARY);
        card.add(l); card.add(Box.createVerticalStrut(4)); card.add(val);
        return card;
    }

    private DefaultTableCellRenderer tableRenderer() {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component cp = super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                cp.setBackground(sel ? new Color(238,242,255) : (r%2==0 ? UITheme.BG_CARD : UITheme.BG_ROW_ALT));
                cp.setForeground(UITheme.TEXT_PRIMARY);
                if (v != null) {
                    String s = v.toString();
                    if ("SELESAI".equals(s)) cp.setForeground(UITheme.ACCENT_TEAL);
                    else if ("BATAL".equals(s)) cp.setForeground(UITheme.ACCENT_CORAL);
                    else if (s.startsWith("Rp ")) { cp.setForeground(UITheme.ACCENT_BLUE); ((JLabel)cp).setHorizontalAlignment(SwingConstants.RIGHT); }
                    else ((JLabel)cp).setHorizontalAlignment(SwingConstants.LEFT);
                }
                ((JLabel)cp).setBorder(new EmptyBorder(0, 12, 0, 12));
                return cp;
            }
        };
    }

    private JLabel boldLabel(String text, int size, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, size));
        l.setForeground(color);
        return l;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_SMALL);
        l.setForeground(UITheme.TEXT_SECONDARY);
        return l;
    }
}
