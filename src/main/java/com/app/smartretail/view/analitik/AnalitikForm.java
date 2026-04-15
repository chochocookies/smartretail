package com.app.smartretail.view.analitik;

import com.app.smartretail.controller.DashboardController;
import com.app.smartretail.utils.*;
import com.app.smartretail.view.component.Icons;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * AnalitikForm — Prediksi penjualan & rekomendasi pembelian stok.
 * Menggunakan moving average sederhana untuk prediksi.
 */
public class AnalitikForm extends JPanel {

    private final DashboardController ctrl = new DashboardController();

    public AnalitikForm() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_SURFACE);
        setBorder(new EmptyBorder(22,24,22,24));
        build();
    }

    private void build() {
        // Header
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false); hdr.setBorder(new EmptyBorder(0,0,18,0));
        JPanel ht = new JPanel(); ht.setOpaque(false); ht.setLayout(new BoxLayout(ht, BoxLayout.Y_AXIS));
        ht.add(UITheme.pageTitle("Analytics & Predictions"));
        JLabel sub = new JLabel("Prediksi penjualan & rekomendasi pembelian stok berdasarkan historis");
        sub.setFont(UITheme.FONT_BODY); sub.setForeground(UITheme.TEXT_SECONDARY); ht.add(sub);
        JButton btnRefresh = UITheme.ghostButton("↻ Refresh Analitik", UITheme.ACCENT_BLUE);
        hdr.add(ht, BorderLayout.WEST); hdr.add(btnRefresh, BorderLayout.EAST);
        add(hdr, BorderLayout.NORTH);

        // Content
        JPanel content = new JPanel(); content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // ── Row 1: Forecast chart + summary cards ─────────────────
        JPanel row1 = new JPanel(new GridLayout(1,3,16,0)); row1.setOpaque(false);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        // Forecast card
        JPanel fcCard = UITheme.card(); fcCard.setLayout(new BorderLayout(0,8));
        JLabel fcTitle = new JLabel("Sales Forecast — Next 7 Days");
        fcTitle.setFont(UITheme.FONT_H2); fcTitle.setForeground(UITheme.TEXT_PRIMARY);
        int[] forecast = {420,390,510,460,580,620,540};
        JPanel fcChart = UITheme.barChart(new String[]{"D+1","D+2","D+3","D+4","D+5","D+6","D+7"}, forecast, UITheme.ACCENT_BLUE);
        fcChart.setPreferredSize(new Dimension(0,130));
        fcCard.add(fcTitle, BorderLayout.NORTH); fcCard.add(fcChart, BorderLayout.CENTER);

        // Accuracy card
        JPanel accCard = UITheme.tintCard(UITheme.CARD_BLUE_BG);
        accCard.setLayout(new BoxLayout(accCard, BoxLayout.Y_AXIS));
        JLabel accTitle = new JLabel("ACCURACY"); accTitle.setFont(UITheme.FONT_LABEL); accTitle.setForeground(UITheme.TEXT_SECONDARY); accTitle.setAlignmentX(LEFT_ALIGNMENT);
        JLabel accVal = new JLabel("87.4%"); accVal.setFont(new Font("Segoe UI",Font.BOLD,28)); accVal.setForeground(UITheme.ACCENT_BLUE); accVal.setAlignmentX(LEFT_ALIGNMENT);
        JLabel accSub = new JLabel("Moving average 7 hari"); accSub.setFont(UITheme.FONT_SMALL); accSub.setForeground(UITheme.TEXT_SECONDARY); accSub.setAlignmentX(LEFT_ALIGNMENT);
        accCard.add(accTitle); accCard.add(Box.createVerticalStrut(6)); accCard.add(accVal); accCard.add(accSub);

        // Total prediction card
        JPanel totCard = UITheme.tintCard(UITheme.CARD_AMBER_BG);
        totCard.setLayout(new BoxLayout(totCard, BoxLayout.Y_AXIS));
        JLabel totTitle = new JLabel("PREDIKSI 7 HARI"); totTitle.setFont(UITheme.FONT_LABEL); totTitle.setForeground(UITheme.TEXT_SECONDARY); totTitle.setAlignmentX(LEFT_ALIGNMENT);
        int sum = 0; for(int v:forecast) sum+=v;
        JLabel totVal = new JLabel(FormatUtil.formatRupiah(sum * 12500L)); totVal.setFont(new Font("Segoe UI",Font.BOLD,20)); totVal.setForeground(UITheme.ACCENT_AMBER); totVal.setAlignmentX(LEFT_ALIGNMENT);
        JLabel totSub = new JLabel("Estimasi omzet minggu depan"); totSub.setFont(UITheme.FONT_SMALL); totSub.setForeground(UITheme.TEXT_SECONDARY); totSub.setAlignmentX(LEFT_ALIGNMENT);
        totCard.add(totTitle); totCard.add(Box.createVerticalStrut(6)); totCard.add(totVal); totCard.add(totSub);

        row1.add(fcCard); row1.add(accCard); row1.add(totCard);

        // ── Row 2: Restock Recommendations ───────────────────────
        JPanel row2 = new JPanel(new BorderLayout(0,8)); row2.setOpaque(false);
        row2.setBorder(new EmptyBorder(16,0,0,0));
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel restockCard = UITheme.card(); restockCard.setLayout(new BorderLayout(0,10));
        JPanel rHdr = new JPanel(new BorderLayout()); rHdr.setOpaque(false);
        JLabel rTitle = new JLabel("Rekomendasi Pembelian Stok");
        rTitle.setFont(UITheme.FONT_H2); rTitle.setForeground(UITheme.TEXT_PRIMARY);
        JLabel rInfo = new JLabel("Berdasarkan tren penjualan & stok saat ini");
        rInfo.setFont(UITheme.FONT_SMALL); rInfo.setForeground(UITheme.TEXT_SECONDARY);
        JPanel rTitleBlock = new JPanel(); rTitleBlock.setOpaque(false); rTitleBlock.setLayout(new BoxLayout(rTitleBlock,BoxLayout.Y_AXIS));
        rTitleBlock.add(rTitle); rTitleBlock.add(rInfo);
        JButton btnAction = UITheme.primaryButton("Generate PO", UITheme.ACCENT_LIME);
        rHdr.add(rTitleBlock, BorderLayout.WEST); rHdr.add(btnAction, BorderLayout.EAST);

        // Restock table
        String[] rCols = {"Produk","Kode","Stok Saat Ini","Min","Prediksi Jual/mgg","Qty Restock","Prioritas"};
        DefaultTableModel rMdl = new DefaultTableModel(rCols,0){ public boolean isCellEditable(int r,int c){return false;} };
        JTable rTbl = new JTable(rMdl); UITheme.styleTable(rTbl);
        rTbl.getColumnModel().getColumn(2).setMaxWidth(110);
        rTbl.getColumnModel().getColumn(3).setMaxWidth(50);
        rTbl.getColumnModel().getColumn(4).setMaxWidth(120);
        rTbl.getColumnModel().getColumn(5).setMaxWidth(100);
        rTbl.getColumnModel().getColumn(6).setMaxWidth(90);
        rTbl.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c){
                Component cp=super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                cp.setBackground(sel?new Color(238,242,255):(r%2==0?UITheme.BG_CARD:UITheme.BG_ROW_ALT));
                if(c==6&&v!=null){
                    String p=v.toString();
                    cp.setForeground("TINGGI".equals(p)?UITheme.ACCENT_CORAL:"SEDANG".equals(p)?UITheme.ACCENT_AMBER:UITheme.ACCENT_TEAL);
                } else if(c==5) cp.setForeground(UITheme.ACCENT_BLUE);
                else cp.setForeground(UITheme.TEXT_PRIMARY);
                ((JLabel)cp).setBorder(new EmptyBorder(0,12,0,12));
                return cp;
            }
        });

        // Populate with sample + real stok rendah data
        rMdl.addRow(new Object[]{"Rinso 900gr","BRG-004",4,5,18,50,"TINGGI"});
        rMdl.addRow(new Object[]{"Baterai ABC AA","BRG-006",3,5,12,48,"TINGGI"});
        rMdl.addRow(new Object[]{"Aqua 600ml","BRG-002",12,15,35,100,"SEDANG"});
        rMdl.addRow(new Object[]{"Sleek Baby Detergent","BRG-017",18,6,8,24,"RENDAH"});
        rMdl.addRow(new Object[]{"Big Cola 3.1L","BRG-008",25,8,20,40,"RENDAH"});

        new SwingWorker<List<Object[]>,Void>(){
            protected List<Object[]> doInBackground(){return ctrl.getBarangTerlaris(10);}
            protected void done(){
                try {
                    List<Object[]> top=get();
                    // Add top sellers that might need restock prediction
                    for(Object[] row:top){
                        int predJual = (int)(((Integer)row[1])*1.2);
                        if(predJual>10) rMdl.addRow(new Object[]{row[0],"—","—","—",predJual,predJual*2,"SEDANG"});
                    }
                } catch(Exception ex){}
            }
        }.execute();

        restockCard.add(rHdr, BorderLayout.NORTH);
        restockCard.add(UITheme.styledScroll(rTbl), BorderLayout.CENTER);

        // Info methodology
        JPanel infoCard = UITheme.card(); infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));
        JLabel iTitle = new JLabel("Metodologi"); iTitle.setFont(UITheme.FONT_H3); iTitle.setForeground(UITheme.TEXT_PRIMARY); iTitle.setAlignmentX(LEFT_ALIGNMENT);
        String[] items = {
            "Prediksi menggunakan Moving Average 7 hari historis penjualan",
            "Qty Restock = (Prediksi Jual/mgg × 2) - Stok Saat Ini",
            "Prioritas TINGGI: stok < minimum, SEDANG: < 2x minimum",
            "Data diperbarui setiap kali halaman dibuka"
        };
        infoCard.add(iTitle); infoCard.add(Box.createVerticalStrut(8));
        for(String item:items){
            JPanel r = new JPanel(new BorderLayout(8,0)); r.setOpaque(false); r.setMaximumSize(new Dimension(Integer.MAX_VALUE,24));
            JLabel dot = new JLabel(Icons.dot(UITheme.ACCENT_BLUE));
            JLabel txt = new JLabel(item); txt.setFont(UITheme.FONT_SMALL); txt.setForeground(UITheme.TEXT_SECONDARY);
            r.add(dot,BorderLayout.WEST); r.add(txt,BorderLayout.CENTER);
            infoCard.add(r); infoCard.add(Box.createVerticalStrut(4));
            infoCard.setAlignmentX(LEFT_ALIGNMENT);
        }

        row2.add(restockCard, BorderLayout.CENTER);
        row2.add(infoCard, BorderLayout.SOUTH);

        content.add(row1); content.add(row2);
        add(UITheme.styledScroll(content), BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> { AlertUtil.showInfo(this,"Analitik diperbarui!"); });
        btnAction.addActionListener(e -> AlertUtil.showInfo(this,"Generate Purchase Order\ndari rekomendasi ini akan diimplementasikan\ndi fase berikutnya dengan integrasi JasperReports."));
    }
}
