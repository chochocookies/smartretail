package com.app.smartretail.view.pegawai;

import com.app.smartretail.dao.PegawaiDAO;
import com.app.smartretail.model.Pegawai;
import com.app.smartretail.utils.*;
import com.app.smartretail.view.component.Icons;

// JasperReports
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;
import net.sf.jasperreports.engine.design.*;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.type.*;
import net.sf.jasperreports.export.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import java.awt.*;
import java.awt.Desktop;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * PegawaiForm — Manajemen data pegawai perusahaan.
 * Fitur: CRUD pegawai, filter, export PDF/CSV, summary count.
 */
public class PegawaiForm extends JPanel {

    private final PegawaiDAO dao = new PegawaiDAO();

    private JTable table; private DefaultTableModel mdl;
    private TableRowSorter<DefaultTableModel> sorter;

    private JTextField fNik, fNama, fJabatan, fTlp, fEmail, fAlamat, fTglMasuk;
    private JComboBox<String> cmbStatus;
    private JButton btnSimpan, btnBatal, btnNew, btnHapus;
    private JLabel lblAvatar, lblFormTitle;
    private JLabel lblAktifCount, lblCutiCount, lblResignCount;
    private int selId = -1;

    private static final DateTimeFormatter FMT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public PegawaiForm() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_SURFACE);
        setBorder(new EmptyBorder(22, 24, 22, 24));
        build();
        load();
    }

    // ════════════════════════════════════════════════════════════════
    // BUILD UI
    // ════════════════════════════════════════════════════════════════
    private void build() {
        // ── Header ──
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false); hdr.setBorder(new EmptyBorder(0, 0, 14, 0));
        JPanel ht = new JPanel(); ht.setOpaque(false);
        ht.setLayout(new BoxLayout(ht, BoxLayout.Y_AXIS));
        ht.add(UITheme.pageTitle("Employees"));
        JLabel sub = new JLabel("Daftar pegawai & manajemen SDM perusahaan");
        sub.setFont(UITheme.FONT_BODY); sub.setForeground(UITheme.TEXT_SECONDARY);
        ht.add(sub);

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acts.setOpaque(false);
        JButton btnRefresh   = UITheme.ghostButton("Refresh",     UITheme.ACCENT_BLUE);
        JButton btnExportCSV = UITheme.ghostButton("Export CSV",  UITheme.ACCENT_TEAL);
        JButton btnExportPDF = UITheme.primaryButton("Export PDF", UITheme.ACCENT_AMBER);
        btnNew = UITheme.primaryButton("+ Pegawai Baru", UITheme.ACCENT_LIME);
        acts.add(btnRefresh); acts.add(btnExportCSV); acts.add(btnExportPDF); acts.add(btnNew);
        hdr.add(ht, BorderLayout.WEST); hdr.add(acts, BorderLayout.EAST);
        add(hdr, BorderLayout.NORTH);

        // ── Summary cards ──
        JPanel summaryRow = new JPanel(new GridLayout(1, 3, 12, 0));
        summaryRow.setOpaque(false);
        summaryRow.setBorder(new EmptyBorder(0, 0, 14, 0));
        lblAktifCount  = boldLabel("0", 22, UITheme.ACCENT_TEAL);
        lblCutiCount   = boldLabel("0", 22, UITheme.ACCENT_AMBER);
        lblResignCount = boldLabel("0", 22, UITheme.ACCENT_CORAL);
        summaryRow.add(sumCard("Pegawai Aktif",  lblAktifCount,  new Color(212,237,218), UITheme.ACCENT_TEAL));
        summaryRow.add(sumCard("Sedang Cuti",    lblCutiCount,   new Color(255,243,205), UITheme.ACCENT_AMBER));
        summaryRow.add(sumCard("Resign",         lblResignCount, new Color(248,215,218), UITheme.ACCENT_CORAL));

        // ── Main area ──
        JPanel main = new JPanel(new BorderLayout(16, 0)); main.setOpaque(false);

        // LEFT: search + table
        JPanel leftArea = new JPanel(new BorderLayout(0, 10)); leftArea.setOpaque(false);

        // Toolbar
        JPanel tbr = new JPanel(new BorderLayout(8, 0)); tbr.setOpaque(false);
        JTextField txtSearch = UITheme.styledField("Cari nama, NIK, jabatan...");
        txtSearch.setPreferredSize(new Dimension(0, 36));
        JComboBox<String> cmbFilter = UITheme.styledCombo(
            new String[]{"Semua Status", "AKTIF", "CUTI", "RESIGN"});
        cmbFilter.setPreferredSize(new Dimension(150, 36));
        JPanel tbRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tbRight.setOpaque(false);
        tbRight.add(txtSearch); tbRight.add(cmbFilter);
        tbr.add(new JLabel("Daftar Pegawai"), BorderLayout.WEST);
        tbr.add(tbRight, BorderLayout.EAST);
        ((JLabel)tbr.getComponent(0)).setFont(UITheme.FONT_H2);
        ((JLabel)tbr.getComponent(0)).setForeground(UITheme.TEXT_PRIMARY);

        // Table
        String[] cols = {"ID", "NIK", "Nama Lengkap", "Jabatan", "Telepon", "Email", "Tgl Masuk", "Status"};
        mdl = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
            public Class<?> getColumnClass(int c) { return c == 0 ? Integer.class : String.class; }
        };
        table = new JTable(mdl); UITheme.styleTable(table);
        table.setRowHeight(40);
        table.getColumnModel().getColumn(0).setMaxWidth(46);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(6).setPreferredWidth(90);
        table.getColumnModel().getColumn(7).setMaxWidth(80);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component cp = super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                cp.setBackground(sel ? new Color(238,242,255) :
                    (r%2==0 ? UITheme.BG_CARD : UITheme.BG_ROW_ALT));
                cp.setForeground(UITheme.TEXT_PRIMARY);
                if (c == 7 && v != null) {
                    String st = v.toString();
                    cp.setForeground("AKTIF".equals(st) ? UITheme.ACCENT_TEAL :
                                     "CUTI".equals(st)  ? UITheme.ACCENT_AMBER :
                                                          UITheme.ACCENT_CORAL);
                    ((JLabel)cp).setHorizontalAlignment(SwingConstants.CENTER);
                }
                if (c == 0) ((JLabel)cp).setHorizontalAlignment(SwingConstants.CENTER);
                ((JLabel)cp).setBorder(new EmptyBorder(0, 10, 0, 10));
                return cp;
            }
        });

        // Sorter — klik header untuk sort
        sorter = new TableRowSorter<>(mdl);
        table.setRowSorter(sorter);
        table.getTableHeader().setToolTipText("Klik header untuk mengurutkan");

        JPanel tableCard = UITheme.card();
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.add(tbr, BorderLayout.NORTH);
        tableCard.add(UITheme.styledScroll(table), BorderLayout.CENTER);

        leftArea.add(tableCard, BorderLayout.CENTER);

        // RIGHT: form card
        JPanel fc = buildFormCard();

        main.add(leftArea, BorderLayout.CENTER);
        main.add(fc, BorderLayout.EAST);

        // ── Assembly ──
        JPanel center = new JPanel(new BorderLayout(0, 0)); center.setOpaque(false);
        center.add(summaryRow, BorderLayout.NORTH);
        center.add(main,       BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        // ── Events ──
        // Real-time filter
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(txtSearch, cmbFilter); }
            public void removeUpdate(DocumentEvent e) { applyFilter(txtSearch, cmbFilter); }
            public void changedUpdate(DocumentEvent e){ applyFilter(txtSearch, cmbFilter); }
        });
        cmbFilter.addActionListener(e -> applyFilter(txtSearch, cmbFilter));

        btnRefresh.addActionListener(e -> load());
        btnNew.addActionListener(e -> clearF());
        btnBatal.addActionListener(e -> clearF());
        btnSimpan.addActionListener(e -> simpan());
        btnHapus.addActionListener(e -> hapus());
        btnExportPDF.addActionListener(e -> exportPDF());
        btnExportCSV.addActionListener(e -> exportCSV());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillForm();
        });
    }

    private JPanel buildFormCard() {
        JPanel fc = UITheme.card();
        fc.setLayout(new BoxLayout(fc, BoxLayout.Y_AXIS));
        fc.setPreferredSize(new Dimension(278, 0));

        lblFormTitle = new JLabel("Detail Pegawai");
        lblFormTitle.setFont(UITheme.FONT_H2);
        lblFormTitle.setForeground(UITheme.TEXT_PRIMARY);
        lblFormTitle.setAlignmentX(LEFT_ALIGNMENT);

        // Avatar
        JPanel avRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        avRow.setOpaque(false); avRow.setAlignmentX(LEFT_ALIGNMENT);
        avRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        lblAvatar = new JLabel(Icons.avatarIcon("?", UITheme.ACCENT_BLUE, 56));
        avRow.add(lblAvatar);

        fNik      = fld("NIK",              fc);
        fNama     = fld("Nama Lengkap *",   fc);
        fJabatan  = fld("Jabatan",          fc);
        fTlp      = fld("Telepon",          fc);
        fEmail    = fld("Email",            fc);
        fAlamat   = fld("Alamat",           fc);
        fTglMasuk = fld("Tgl Masuk (dd/MM/yyyy)", fc);

        JLabel lStat = UITheme.fieldLabel("Status"); lStat.setAlignmentX(LEFT_ALIGNMENT);
        cmbStatus = UITheme.styledCombo(new String[]{"AKTIF", "CUTI", "RESIGN"});
        cmbStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cmbStatus.setAlignmentX(LEFT_ALIGNMENT);

        JPanel br1 = new JPanel(new GridLayout(1, 2, 8, 0)); br1.setOpaque(false);
        br1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34)); br1.setAlignmentX(LEFT_ALIGNMENT);
        btnHapus = UITheme.dangerButton("Hapus");
        JButton btnNewInner = UITheme.ghostButton("+ Baru", UITheme.ACCENT_BLUE);
        br1.add(btnNewInner); br1.add(btnHapus);

        JPanel br2 = new JPanel(new GridLayout(1, 2, 8, 0)); br2.setOpaque(false);
        br2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34)); br2.setAlignmentX(LEFT_ALIGNMENT);
        btnBatal  = UITheme.ghostButton("Batal",   UITheme.TEXT_SECONDARY);
        btnSimpan = UITheme.primaryButton("Simpan", UITheme.ACCENT_LIME);
        br2.add(btnBatal); br2.add(btnSimpan);

        fc.add(lblFormTitle); fc.add(Box.createVerticalStrut(8));
        fc.add(UITheme.separator()); fc.add(Box.createVerticalStrut(8));
        fc.add(avRow); fc.add(Box.createVerticalStrut(8));
        fc.add(lStat); fc.add(Box.createVerticalStrut(4));
        fc.add(cmbStatus); fc.add(Box.createVerticalStrut(8));
        fc.add(Box.createVerticalGlue());
        fc.add(br1); fc.add(Box.createVerticalStrut(6)); fc.add(br2);

        btnNewInner.addActionListener(e -> clearF());
        fNama.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent e) { refreshAvatar(); }
        });
        cmbStatus.addActionListener(e -> refreshAvatar());

        return fc;
    }

    // ════════════════════════════════════════════════════════════════
    // DATA LOAD
    // ════════════════════════════════════════════════════════════════
    private void load() {
        fill(dao.getAll());
        updateSummary();
    }

    private void fill(List<Pegawai> list) {
        mdl.setRowCount(0);
        for (Pegawai p : list) {
            mdl.addRow(new Object[]{
                p.getId(),
                p.getNik() != null ? p.getNik() : "-",
                p.getNama(),
                p.getJabatan() != null ? p.getJabatan() : "-",
                p.getTelepon() != null ? p.getTelepon() : "-",
                p.getEmail()   != null ? p.getEmail()   : "-",
                p.getTglMasuk() != null ? p.getTglMasuk().format(FMT_DATE) : "-",
                p.getStatus()
            });
        }
    }

    private void updateSummary() {
        lblAktifCount.setText(String.valueOf(dao.countByStatus("AKTIF")));
        lblCutiCount.setText(String.valueOf(dao.countByStatus("CUTI")));
        lblResignCount.setText(String.valueOf(dao.countByStatus("RESIGN")));
    }

    private void applyFilter(JTextField search, JComboBox<String> cmbFilter) {
        String kw     = search.getText().trim();
        String status = cmbFilter.getSelectedItem().toString();
        sorter.setRowFilter(RowFilter.andFilter(java.util.Arrays.asList(
            kw.isEmpty()                ? null : RowFilter.regexFilter("(?i)" + kw, 1, 2, 3),
            "Semua Status".equals(status) ? null : RowFilter.regexFilter("^" + status + "$", 7)
        ).stream().filter(Objects::nonNull).collect(java.util.stream.Collectors.toList())));
    }

    // ════════════════════════════════════════════════════════════════
    // FORM FILL
    // ════════════════════════════════════════════════════════════════
    private void fillForm() {
        int viewRow = table.getSelectedRow(); if (viewRow < 0) return;
        int modelRow = table.convertRowIndexToModel(viewRow);
        selId = (int) mdl.getValueAt(modelRow, 0);
        fNik.setText(s(mdl.getValueAt(modelRow, 1)));
        fNama.setText(s(mdl.getValueAt(modelRow, 2)));
        fJabatan.setText(s(mdl.getValueAt(modelRow, 3)));
        fTlp.setText(s(mdl.getValueAt(modelRow, 4)));
        fEmail.setText(s(mdl.getValueAt(modelRow, 5)));
        fTglMasuk.setText(s(mdl.getValueAt(modelRow, 6)));
        cmbStatus.setSelectedItem(s(mdl.getValueAt(modelRow, 7)));
        lblFormTitle.setText(truncate(s(mdl.getValueAt(modelRow, 2)), 22));
        // Load alamat dari DAO (tidak ada di tabel kolom, perlu query langsung)
        dao.getAll().stream().filter(p -> p.getId() == selId).findFirst()
            .ifPresent(p -> fAlamat.setText(p.getAlamat() != null ? p.getAlamat() : ""));
        refreshAvatar();
    }

    private void refreshAvatar() {
        String status = cmbStatus.getSelectedItem() != null ? cmbStatus.getSelectedItem().toString() : "AKTIF";
        Color c = "AKTIF".equals(status) ? UITheme.ACCENT_TEAL :
                  "CUTI".equals(status)  ? UITheme.ACCENT_AMBER : UITheme.TEXT_MUTED;
        String init = fNama.getText().isBlank() ? "?" :
                      new Pegawai() {{ setNama(fNama.getText()); }}.getInitials();
        lblAvatar.setIcon(Icons.avatarIcon(init, c, 56));
    }

    // ════════════════════════════════════════════════════════════════
    // CRUD
    // ════════════════════════════════════════════════════════════════
    private void simpan() {
        if (fNama.getText().isBlank()) {
            AlertUtil.showWarning(this, "Nama lengkap wajib diisi!"); return;
        }
        Pegawai p = new Pegawai();
        p.setId(selId);
        p.setNik(fNik.getText().isBlank() ? dao.generateNik() : fNik.getText().trim());
        p.setNama(fNama.getText().trim());
        p.setJabatan(fJabatan.getText().trim());
        p.setTelepon(fTlp.getText().trim());
        p.setEmail(fEmail.getText().trim());
        p.setAlamat(fAlamat.getText().trim());
        p.setStatus(cmbStatus.getSelectedItem().toString());
        // Parse tanggal masuk
        String tgl = fTglMasuk.getText().trim();
        if (!tgl.isEmpty() && !"-".equals(tgl)) {
            try { p.setTglMasuk(LocalDate.parse(tgl, FMT_DATE)); }
            catch (Exception ex) { AlertUtil.showWarning(this, "Format tanggal: dd/MM/yyyy"); return; }
        }
        boolean ok = (selId == -1) ? dao.insert(p) : dao.update(p);
        if (ok) { AlertUtil.showInfo(this, "Pegawai berhasil disimpan!"); load(); clearF(); }
        else    AlertUtil.showError(this, "Gagal menyimpan data pegawai.");
    }

    private void hapus() {
        if (selId == -1) { AlertUtil.showWarning(this, "Pilih pegawai terlebih dahulu!"); return; }
        String nama = fNama.getText();
        if (!AlertUtil.showConfirm(this, "Hapus pegawai: " + nama + "?\nTindakan tidak dapat dibatalkan.")) return;
        if (dao.delete(selId)) { AlertUtil.showInfo(this, "Pegawai dihapus."); load(); clearF(); }
        else                   AlertUtil.showError(this, "Gagal menghapus.");
    }

    private void clearF() {
        selId = -1;
        fNik.setText(""); fNama.setText(""); fJabatan.setText("");
        fTlp.setText(""); fEmail.setText(""); fAlamat.setText("");
        fTglMasuk.setText(""); cmbStatus.setSelectedIndex(0);
        lblFormTitle.setText("Detail Pegawai");
        lblAvatar.setIcon(Icons.avatarIcon("?", UITheme.ACCENT_BLUE, 56));
        table.clearSelection();
    }

    // ════════════════════════════════════════════════════════════════
    // EXPORT PDF — JasperReports programatik
    // ════════════════════════════════════════════════════════════════
    private void exportPDF() {
        if (mdl.getRowCount() == 0) {
            AlertUtil.showWarning(this, "Tidak ada data pegawai untuk diekspor."); return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Simpan Laporan Pegawai PDF");
        fc.setFileFilter(new FileNameExtensionFilter("PDF Files (*.pdf)", "pdf"));
        String fname = "Laporan_Pegawai_" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".pdf";
        fc.setSelectedFile(new File(System.getProperty("user.home"), fname));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File pdfFile = fc.getSelectedFile();
        if (!pdfFile.getName().toLowerCase().endsWith(".pdf"))
            pdfFile = new File(pdfFile.getAbsolutePath() + ".pdf");
        final File finalFile = pdfFile;

        // Progress dialog
        JDialog dlg = makeProgressDialog("Membuat laporan PDF pegawai...");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            String errMsg = null;
            @Override
            protected Void doInBackground() {
                try {
                    JasperPrint print = buildJasperPrint();
                    JRPdfExporter exp = new JRPdfExporter();
                    exp.setExporterInput(new SimpleExporterInput(print));
                    exp.setExporterOutput(new SimpleOutputStreamExporterOutput(finalFile));
                    SimplePdfReportConfiguration cfg = new SimplePdfReportConfiguration();
                    cfg.setSizePageToContent(false);
                    exp.setConfiguration(cfg);
                    exp.exportReport();
                } catch (Exception ex) {
                    StringWriter sw = new StringWriter();
                    ex.printStackTrace(new PrintWriter(sw));
                    errMsg = ex.getMessage() + "\n\n" + sw.toString();
                }
                return null;
            }
            @Override
            protected void done() {
                dlg.dispose();
                setCursor(Cursor.getDefaultCursor());
                if (errMsg != null) {
                    showDetailedError("Gagal export PDF", errMsg);
                } else {
                    int choice = JOptionPane.showConfirmDialog(PegawaiForm.this,
                        "PDF berhasil disimpan!\n" + finalFile.getAbsolutePath() +
                        "\n\nBuka sekarang?", "Export Berhasil",
                        JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                    if (choice == JOptionPane.YES_OPTION) {
                        try { Desktop.getDesktop().open(finalFile); }
                        catch (Exception ex) {
                            AlertUtil.showWarning(PegawaiForm.this,
                                "Tidak bisa dibuka otomatis.\nBuka manual di: " + finalFile.getAbsolutePath());
                        }
                    }
                }
            }
        };
        dlg.setVisible(true);
        worker.execute();
    }

    private JasperPrint buildJasperPrint() throws JRException {
        // Ambil snapshot data yang sedang tampil (setelah filter)
        String[] hdrs = {"NIK", "Nama Lengkap", "Jabatan", "Telepon", "Email", "Tgl Masuk", "Status"};
        // Indeks kolom di mdl: 1,2,3,4,5,6,7
        int[] colIdx = {1, 2, 3, 4, 5, 6, 7};

        List<Object[]> rows = new ArrayList<>();
        for (int vr = 0; vr < table.getRowCount(); vr++) {
            int mr = table.convertRowIndexToModel(vr);
            Object[] row = new Object[hdrs.length];
            for (int i = 0; i < colIdx.length; i++) {
                Object v = mdl.getValueAt(mr, colIdx[i]);
                row[i] = v != null ? v.toString() : "-";
            }
            rows.add(row);
        }

        // ── Dimensi A4 landscape ──
        final int PW = 842, PH = 595, M = 30, CW = PW - M * 2;

        // ── Warna ──
        Color C_TITLE  = new Color(15, 23, 42);
        Color C_ACCENT = new Color(16, 185, 129);   // emerald
        Color C_HDR_BG = new Color(30, 41, 59);
        Color C_MUTED  = new Color(148, 163, 184);
        Color C_ALT    = new Color(248, 250, 252);
        Color C_BORDER = new Color(226, 232, 240);

        JasperDesign d = new JasperDesign();
        d.setName("PegawaiReport");
        d.setPageWidth(PW); d.setPageHeight(PH);
        d.setLeftMargin(M); d.setRightMargin(M);
        d.setTopMargin(M); d.setBottomMargin(M);
        d.setColumnWidth(CW);

        for (int i = 0; i < hdrs.length; i++) {
            JRDesignField f = new JRDesignField();
            f.setName("F" + i); f.setValueClass(String.class);
            d.addField(f);
        }

        int[] cw = calcColWidths(hdrs, CW);

        // ── TITLE ──
        JRDesignBand title = new JRDesignBand(); title.setHeight(82);
        title.addElement(rect(0, 0, CW, 82, C_TITLE));
        title.addElement(rect(0, 0, 5, 82, C_ACCENT));
        title.addElement(st("LAPORAN DATA PEGAWAI", 15, 14, 10, CW-20, 26,
            HorizontalTextAlignEnum.LEFT, true, Color.WHITE));
        // Summary kecil di title
        int aktif  = Integer.parseInt(lblAktifCount.getText());
        int cuti   = Integer.parseInt(lblCutiCount.getText());
        int resign = Integer.parseInt(lblResignCount.getText());
        String sumText = "Aktif: " + aktif + "   Cuti: " + cuti + "   Resign: " + resign +
                         "   Total: " + rows.size() + " pegawai";
        title.addElement(st(sumText, 9, 14, 38, CW-20, 18,
            HorizontalTextAlignEnum.LEFT, false, C_MUTED));
        title.addElement(st("Dicetak: " + LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm", new Locale("id","ID"))),
            9, 14, 58, CW-20, 16, HorizontalTextAlignEnum.LEFT, false, C_MUTED));
        d.setTitle(title);

        // ── COLUMN HEADER ──
        JRDesignBand colHdr = new JRDesignBand(); colHdr.setHeight(26);
        colHdr.addElement(rect(0, 0, CW, 26, C_HDR_BG));
        int cx = 0;
        for (int i = 0; i < hdrs.length; i++) {
            colHdr.addElement(st(hdrs[i].toUpperCase(), 8, cx+4, 3, cw[i]-8, 20,
                HorizontalTextAlignEnum.LEFT, true, Color.WHITE));
            cx += cw[i];
        }
        d.setColumnHeader(colHdr);

        // ── DETAIL ──
        JRDesignBand det = new JRDesignBand(); det.setHeight(22);
        det.setSplitType(SplitTypeEnum.STRETCH);
        JRDesignRectangle altBg = rect(0, 0, CW, 22, C_ALT);
        JRDesignExpression altEx = new JRDesignExpression();
        altEx.setText("$V{REPORT_COUNT} % 2 == 0");
        altBg.setPrintWhenExpression(altEx);
        det.addElement(altBg);
        JRDesignLine ln = new JRDesignLine();
        ln.setX(0); ln.setY(21); ln.setWidth(CW); ln.setHeight(1);
        ln.getLinePen().setLineWidth(0.4f); ln.getLinePen().setLineColor(C_BORDER);
        det.addElement(ln);
        cx = 0;
        for (int i = 0; i < hdrs.length; i++) {
            JRDesignTextField tf = new JRDesignTextField();
            tf.setX(cx+4); tf.setY(2); tf.setWidth(cw[i]-8); tf.setHeight(18);
            tf.setBlankWhenNull(true); tf.setStretchWithOverflow(true);
            JRDesignExpression ex = new JRDesignExpression();
            ex.setText("$F{F" + i + "}");
            tf.setExpression(ex);
            tf.setFontSize(9f);
            boolean isStatus = "Status".equals(hdrs[i]);
            tf.setHorizontalTextAlign(isStatus ?
                HorizontalTextAlignEnum.CENTER : HorizontalTextAlignEnum.LEFT);
            tf.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
            tf.setForecolor(isStatus ? new Color(7, 89, 133) : Color.DARK_GRAY);
            det.addElement(tf);
            cx += cw[i];
        }
        ((JRDesignSection) d.getDetailSection()).addBand(det);

        // ── SUMMARY ──
        JRDesignBand sum = new JRDesignBand(); sum.setHeight(28);
        sum.addElement(rect(0, 0, CW, 28, new Color(241, 245, 249)));
        sum.addElement(st("Total " + rows.size() + " pegawai  |  Aktif: " + aktif +
            "  |  Cuti: " + cuti + "  |  Resign: " + resign,
            10, 8, 6, CW-16, 18, HorizontalTextAlignEnum.LEFT, true, new Color(30,41,59)));
        d.setSummary(sum);

        // ── PAGE FOOTER ──
        JRDesignBand footer = new JRDesignBand(); footer.setHeight(18);
        footer.addElement(st("SRMS — Smart Retail Management System",
            8, 0, 3, CW/2, 14, HorizontalTextAlignEnum.LEFT, false, C_MUTED));
        JRDesignTextField pg = new JRDesignTextField();
        pg.setX(CW/2); pg.setY(3); pg.setWidth(CW/2); pg.setHeight(14);
        pg.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
        pg.setFontSize(8f); pg.setForecolor(C_MUTED);
        JRDesignExpression pgEx = new JRDesignExpression();
        pgEx.setText("\"Halaman \" + $V{PAGE_NUMBER}");
        pg.setExpression(pgEx);
        footer.addElement(pg);
        d.setPageFooter(footer);

        // ── Compile + fill ──
        JasperReport report = JasperCompileManager.compileReport(d);
        DefaultTableModel dm = new DefaultTableModel(hdrs, 0);
        for (Object[] r : rows) dm.addRow(r);
        Map<String, Object> params = new HashMap<>();
        params.put(JRParameter.REPORT_LOCALE, new Locale("id", "ID"));
        return JasperFillManager.fillReport(report, params, new JRTableModelDataSource(dm));
    }

    // ════════════════════════════════════════════════════════════════
    // EXPORT CSV
    // ════════════════════════════════════════════════════════════════
    private void exportCSV() {
        if (mdl.getRowCount() == 0) {
            AlertUtil.showWarning(this, "Tidak ada data untuk diekspor!"); return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Simpan CSV Pegawai");
        fc.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));
        fc.setSelectedFile(new File(System.getProperty("user.home"),
            "Pegawai_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = fc.getSelectedFile();
        if (!file.getName().endsWith(".csv")) file = new File(file.getAbsolutePath() + ".csv");
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(file), "UTF-8"))) {
            pw.print('\uFEFF');
            String[] hdrs = {"ID","NIK","Nama","Jabatan","Telepon","Email","Tgl Masuk","Status"};
            pw.println(csvRow(hdrs));
            for (int i = 0; i < table.getRowCount(); i++) {
                int mr = table.convertRowIndexToModel(i);
                Object[] row = new Object[mdl.getColumnCount()];
                for (int c = 0; c < row.length; c++) row[c] = mdl.getValueAt(mr, c);
                pw.println(csvRow(row));
            }
            AlertUtil.showInfo(this, "CSV berhasil disimpan!\n" + file.getAbsolutePath());
        } catch (Exception ex) {
            showDetailedError("Gagal menyimpan CSV", ex.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════════════════
    private JPanel sumCard(String label, JLabel val, Color tint, Color accent) {
        JPanel card = UITheme.tintCard(tint);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(label.toUpperCase());
        l.setFont(UITheme.FONT_LABEL); l.setForeground(UITheme.TEXT_SECONDARY);
        card.add(l); card.add(Box.createVerticalStrut(4)); card.add(val);
        return card;
    }

    private JLabel boldLabel(String text, int size, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, size));
        l.setForeground(color);
        return l;
    }

    private JTextField fld(String label, JPanel p) {
        JLabel l = UITheme.fieldLabel(label); l.setAlignmentX(LEFT_ALIGNMENT);
        JTextField f = UITheme.styledField("");
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34)); f.setAlignmentX(LEFT_ALIGNMENT);
        p.add(l); p.add(Box.createVerticalStrut(4));
        p.add(f); p.add(Box.createVerticalStrut(7));
        return f;
    }

    private int[] calcColWidths(String[] headers, int total) {
        int[] w = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            String h = headers[i].toLowerCase();
            if (h.contains("nik"))          w[i] = 6;
            else if (h.contains("nama"))    w[i] = 15;
            else if (h.contains("jabatan")) w[i] = 11;
            else if (h.contains("telepon")) w[i] = 10;
            else if (h.contains("email"))   w[i] = 13;
            else if (h.contains("tgl"))     w[i] = 8;
            else if (h.contains("status"))  w[i] = 6;
            else                            w[i] = 8;
        }
        int sumW = Arrays.stream(w).sum();
        int[] res = new int[headers.length]; int used = 0;
        for (int i = 0; i < headers.length - 1; i++) {
            res[i] = (int)((double) w[i] / sumW * total); used += res[i];
        }
        res[headers.length - 1] = total - used;
        return res;
    }

    private JRDesignStaticText st(String text, int fontSize, int x, int y, int w, int h,
                                   HorizontalTextAlignEnum align, boolean bold, Color fg) {
        JRDesignStaticText s = new JRDesignStaticText();
        s.setText(text); s.setX(x); s.setY(y); s.setWidth(w); s.setHeight(h);
        s.setHorizontalTextAlign(align); s.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        s.setForecolor(fg); s.setFontSize((float) fontSize); s.setBold(bold);
        return s;
    }

    private JRDesignRectangle rect(int x, int y, int w, int h, Color bg) {
        JRDesignRectangle r = new JRDesignRectangle();
        r.setX(x); r.setY(y); r.setWidth(w); r.setHeight(h);
        r.getLinePen().setLineWidth(0f); r.setBackcolor(bg); r.setMode(ModeEnum.OPAQUE);
        return r;
    }

    private JDialog makeProgressDialog(String msg) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), false);
        dlg.setUndecorated(true);
        JPanel pp = new JPanel(new BorderLayout(10, 10));
        pp.setBorder(new EmptyBorder(18, 24, 18, 24));
        pp.setBackground(UITheme.BG_CARD);
        JProgressBar bar = new JProgressBar(); bar.setIndeterminate(true);
        bar.setPreferredSize(new Dimension(220, 8));
        pp.add(new JLabel(msg), BorderLayout.NORTH);
        pp.add(bar, BorderLayout.CENTER);
        dlg.add(pp); dlg.pack(); dlg.setLocationRelativeTo(this);
        return dlg;
    }

    private void showDetailedError(String title, String trace) {
        JTextArea ta = new JTextArea(trace);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 11));
        ta.setEditable(false); ta.setRows(12);
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(600, 220));
        JOptionPane.showMessageDialog(this, sp, title, JOptionPane.ERROR_MESSAGE);
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
    private String csvRow(String[] cells) { return csvRow(Arrays.stream(cells).toArray()); }
    private String s(Object o) { return o == null ? "" : o.toString(); }
    private String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max-1) + "…" : s;
    }
}
