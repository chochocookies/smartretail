package com.app.smartretail.view.report;

import com.app.smartretail.controller.DashboardController;
import com.app.smartretail.controller.TransaksiController;
import com.app.smartretail.model.Transaksi;
import com.app.smartretail.utils.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class ReportForm extends JPanel {

    private final TransaksiController trxCtrl = new TransaksiController();
    private final DashboardController dashCtrl = new DashboardController();

    private JTable table; private DefaultTableModel mdl;
    private JComboBox<String> cmbJenis, cmbYear;
    private JLabel lblOmzet, lblCount;

    public ReportForm() {
        setLayout(new BorderLayout()); setBackground(UITheme.BG_SURFACE);
        setBorder(new EmptyBorder(22,24,22,24));
        build();
    }

    private void build() {
        // Header
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false); hdr.setBorder(new EmptyBorder(0,0,16,0));
        JPanel ht = new JPanel(); ht.setOpaque(false); ht.setLayout(new BoxLayout(ht, BoxLayout.Y_AXIS));
        ht.add(UITheme.pageTitle("Sales & Reports"));
        JLabel sub = new JLabel("Ringkasan data transaksi dan performa toko");
        sub.setFont(UITheme.FONT_BODY); sub.setForeground(UITheme.TEXT_SECONDARY); ht.add(sub);
        hdr.add(ht, BorderLayout.WEST);

        // Tab filters
        JPanel tabRow = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        tabRow.setOpaque(false);
        String[] tabNames = {"Orders","Invoices","Promotions","Inventory","Returns","Loyalty","Category"};
        for (int i=0; i<tabNames.length; i++) {
            boolean active = i==0;
            JButton t = new JButton(tabNames[i]){
                @Override protected void paintComponent(Graphics g){
                    Graphics2D g2=(Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(UITheme.BG_CARD); g2.fillRect(0,0,getWidth(),getHeight());
                    if(active){g2.setColor(UITheme.TEXT_PRIMARY);g2.setStroke(new BasicStroke(2));g2.drawLine(0,getHeight()-2,getWidth(),getHeight()-2);}
                    g2.dispose(); super.paintComponent(g);
                }
            };
            t.setFont(active?new Font("Segoe UI",Font.BOLD,12):UITheme.FONT_BODY);
            t.setForeground(active?UITheme.TEXT_PRIMARY:UITheme.TEXT_MUTED);
            t.setOpaque(false); t.setContentAreaFilled(false);
            t.setBorderPainted(false); t.setFocusPainted(false);
            t.setBorder(new EmptyBorder(10,14,10,14));
            tabRow.add(t);
        }

        JPanel tabCard = new JPanel(new BorderLayout());
        tabCard.setBackground(UITheme.BG_CARD);
        tabCard.setBorder(BorderFactory.createCompoundBorder(
            new UITheme.RoundedBorder(12,UITheme.BORDER_DEFAULT,null),
            new EmptyBorder(0,0,0,0)));
        tabCard.add(tabRow, BorderLayout.WEST);
        hdr.add(tabCard, BorderLayout.SOUTH);
        add(hdr, BorderLayout.NORTH);

        // ── Content ──────────────────────────────────────────────
        JPanel content = new JPanel(new BorderLayout(0,16)); content.setOpaque(false);

        // Toolbar row
        JPanel toolbar = new JPanel(new BorderLayout(10,0));
        toolbar.setOpaque(false); toolbar.setBorder(new EmptyBorder(0,0,4,0));

        JButton btnNew = UITheme.primaryButton("+ Create New Order", UITheme.ACCENT_LIME);
        cmbJenis = UITheme.styledCombo(new String[]{"Penjualan","Pembelian","Barang Terlaris"});
        cmbJenis.setPreferredSize(new Dimension(160,34));
        cmbYear  = UITheme.styledCombo(new String[]{"2025","2024","2023"});
        cmbYear.setPreferredSize(new Dimension(90,34));
        JButton btnLoad = UITheme.ghostButton("Tampilkan", UITheme.ACCENT_BLUE);
        JButton btnExp  = UITheme.ghostButton("Export PDF", UITheme.ACCENT_AMBER);

        JPanel tbRight = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
        tbRight.setOpaque(false);
        tbRight.add(cmbJenis); tbRight.add(cmbYear); tbRight.add(btnLoad); tbRight.add(btnExp);

        toolbar.add(btnNew, BorderLayout.WEST);
        toolbar.add(tbRight, BorderLayout.EAST);

        // Table card
        JPanel tableCard = UITheme.card();
        tableCard.setLayout(new BorderLayout(0,0));

        String[] cols = {"Order ID","Ordering Date","Kasir / Supplier","Total Items","Status","Total Amount"};
        mdl = new DefaultTableModel(cols,0){ public boolean isCellEditable(int r,int c){return false;} };
        table = new JTable(mdl); UITheme.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setMaxWidth(90);
        table.getColumnModel().getColumn(4).setMaxWidth(110);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c){
                Component cp=super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                cp.setBackground(sel?new Color(238,242,255):(r%2==0?UITheme.BG_CARD:UITheme.BG_ROW_ALT));
                cp.setForeground(UITheme.TEXT_PRIMARY);
                if(c==4&&v!=null){
                    String s=v.toString();
                    cp.setForeground("SELESAI".equals(s)?UITheme.ACCENT_TEAL:
                                     "Quotation Sent".equals(s)?UITheme.ACCENT_AMBER:UITheme.TEXT_SECONDARY);
                }
                if(c==5) cp.setForeground(UITheme.ACCENT_BLUE);
                ((JLabel)cp).setBorder(new EmptyBorder(0,14,0,14));
                return cp;
            }
        });

        // Pagination strip
        JPanel pageStrip = buildPagination();

        tableCard.add(UITheme.styledScroll(table), BorderLayout.CENTER);
        tableCard.add(pageStrip, BorderLayout.SOUTH);

        content.add(toolbar, BorderLayout.NORTH);
        content.add(tableCard, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);

        btnLoad.addActionListener(e->loadReport());
        btnExp.addActionListener(e->AlertUtil.showInfo(this,"Export PDF memerlukan library JasperReports.\nTambahkan jasperreports.jar ke folder lib/"));
        loadReport();
    }

    private JPanel buildPagination() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER,6,8));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createMatteBorder(1,0,0,0,UITheme.BORDER_DEFAULT));
        JButton prev = pagerBtn("‹");
        p.add(prev);
        for (int i=1; i<=5; i++) {
            JButton b = pagerBtn(String.valueOf(i));
            if (i==1) {
                b.setBackground(UITheme.ACCENT_LIME);
                b.setForeground(UITheme.TEXT_ON_LIME);
                b.setOpaque(true);
            }
            p.add(b);
        }
        JButton next = pagerBtn("›");
        p.add(next);
        return p;
    }

    private JButton pagerBtn(String text) {
        JButton b = new JButton(text){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if(getBackground()==UITheme.ACCENT_LIME){g2.setColor(UITheme.ACCENT_LIME);g2.fillOval(0,0,getWidth(),getHeight());}
                else if(getModel().isRollover()){g2.setColor(UITheme.BG_HOVER);g2.fillOval(0,0,getWidth(),getHeight());}
                g2.dispose(); super.paintComponent(g);
            }
        };
        b.setFont(UITheme.FONT_SMALL); b.setForeground(UITheme.TEXT_SECONDARY);
        b.setPreferredSize(new Dimension(30,30));
        b.setOpaque(false); b.setContentAreaFilled(false);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void loadReport() {
        mdl.setRowCount(0);
        String jenis = cmbJenis.getSelectedItem().toString();
        int no = 1;
        if ("Penjualan".equals(jenis)) {
            mdl.setColumnIdentifiers(new String[]{"Order ID","Tanggal","Kasir","Total Items","Status","Total Amount"});
            for (Transaksi t : trxCtrl.getRiwayatPenjualan())
                mdl.addRow(new Object[]{t.getNoTransaksi(),FormatUtil.formatDateTime(t.getTanggal()),t.getNamaUser(),"—","SELESAI",FormatUtil.formatRupiah(t.getGrandTotal())});
        } else if ("Pembelian".equals(jenis)) {
            mdl.setColumnIdentifiers(new String[]{"Order ID","Tanggal","Supplier","Total Items","Status","Total Amount"});
            for (Transaksi t : trxCtrl.getRiwayatPembelian())
                mdl.addRow(new Object[]{t.getNoTransaksi(),FormatUtil.formatDateTime(t.getTanggal()),t.getNamaSupplier(),"—","SELESAI",FormatUtil.formatRupiah(t.getGrandTotal())});
        } else {
            mdl.setColumnIdentifiers(new String[]{"#","Nama Barang","Total Qty","Total Omzet","","" });
            for (Object[] row : dashCtrl.getBarangTerlaris(20))
                mdl.addRow(new Object[]{no++,row[0],row[1],FormatUtil.formatRupiah((double)row[2]),"",""});
        }
    }
}
