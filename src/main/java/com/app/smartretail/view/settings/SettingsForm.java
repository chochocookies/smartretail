package com.app.smartretail.view.settings;

import com.app.smartretail.utils.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SettingsForm extends JPanel {

    public SettingsForm() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_SURFACE);
        setBorder(new EmptyBorder(22,24,22,24));
        build();
    }

    private void build() {
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false); hdr.setBorder(new EmptyBorder(0,0,20,0));
        JPanel ht = new JPanel(); ht.setOpaque(false); ht.setLayout(new BoxLayout(ht,BoxLayout.Y_AXIS));
        ht.add(UITheme.pageTitle("Settings"));
        JLabel sub=new JLabel("Konfigurasi aplikasi & toko"); sub.setFont(UITheme.FONT_BODY); sub.setForeground(UITheme.TEXT_SECONDARY); ht.add(sub);
        hdr.add(ht, BorderLayout.WEST);
        add(hdr, BorderLayout.NORTH);

        JPanel content = new JPanel(); content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // Section: Profil Toko
        content.add(section("Profil Toko",
            new String[]{"Nama Toko","Alamat","Nomor Telepon","Email Toko","NPWP"},
            new String[]{"Toko Retail Mandiri","Jl. Sudirman No.12, Jakarta","021-5555-1234","toko@retail.com","01.234.567.8-901.000"}));
        content.add(Box.createVerticalStrut(16));

        // Section: Konfigurasi Sistem
        content.add(section("Konfigurasi Sistem",
            new String[]{"Nama Database","Host DB","Port DB","Stok Minimum Default","Pajak (%)"},
            new String[]{"smart_retail_db","localhost","3306","5","0"}));
        content.add(Box.createVerticalStrut(16));

        // Section: Tampilan
        JPanel dispCard = UITheme.card(); dispCard.setLayout(new BoxLayout(dispCard, BoxLayout.Y_AXIS));
        dispCard.setAlignmentX(LEFT_ALIGNMENT);
        JLabel dTitle = new JLabel("Tampilan"); dTitle.setFont(UITheme.FONT_H2); dTitle.setForeground(UITheme.TEXT_PRIMARY); dTitle.setAlignmentX(LEFT_ALIGNMENT);
        dispCard.add(dTitle); dispCard.add(Box.createVerticalStrut(12));
        String[] dispItems = {"Tema: Light (Starline)","Bahasa: Indonesia","Format Tanggal: dd/MM/yyyy","Format Mata Uang: Rp (Indonesia)"};
        for(String item:dispItems){
            JPanel row=new JPanel(new BorderLayout()); row.setOpaque(false); row.setMaximumSize(new Dimension(Integer.MAX_VALUE,36));
            JLabel l=new JLabel(item); l.setFont(UITheme.FONT_BODY); l.setForeground(UITheme.TEXT_PRIMARY);
            JLabel locked=new JLabel("v4.0"); locked.setFont(UITheme.FONT_LABEL); locked.setForeground(UITheme.TEXT_MUTED);
            row.add(l,BorderLayout.WEST); row.add(locked,BorderLayout.EAST);
            row.setAlignmentX(LEFT_ALIGNMENT);
            dispCard.add(row); dispCard.add(UITheme.separator());
        }
        content.add(dispCard);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setOpaque(false); btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btnRow.setBorder(new EmptyBorder(16,0,0,0));
        JButton btnSave = UITheme.primaryButton("Simpan Perubahan", UITheme.ACCENT_LIME);
        JButton btnReset = UITheme.ghostButton("Reset Default", UITheme.TEXT_SECONDARY);
        btnSave.addActionListener(e -> AlertUtil.showInfo(this,"Pengaturan berhasil disimpan!"));
        btnReset.addActionListener(e -> { if(AlertUtil.showConfirm(this,"Reset semua pengaturan ke default?")) AlertUtil.showInfo(this,"Pengaturan direset."); });
        btnRow.add(btnSave); btnRow.add(btnReset);
        content.add(btnRow);
        content.setAlignmentX(LEFT_ALIGNMENT);

        add(UITheme.styledScroll(content), BorderLayout.CENTER);
    }

    private JPanel section(String title, String[] labels, String[] values) {
        JPanel card = UITheme.card(); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(LEFT_ALIGNMENT);
        JLabel t = new JLabel(title); t.setFont(UITheme.FONT_H2); t.setForeground(UITheme.TEXT_PRIMARY); t.setAlignmentX(LEFT_ALIGNMENT);
        card.add(t); card.add(Box.createVerticalStrut(12));
        JPanel grid = new JPanel(new GridLayout(0,2,16,10)); grid.setOpaque(false); grid.setAlignmentX(LEFT_ALIGNMENT);
        for(int i=0;i<labels.length;i++){
            JPanel col=new JPanel(new BorderLayout(0,4)); col.setOpaque(false);
            col.add(UITheme.fieldLabel(labels[i]),BorderLayout.NORTH);
            JTextField f=UITheme.styledField(""); f.setText(values[i]); f.setPreferredSize(new Dimension(0,34));
            col.add(f,BorderLayout.CENTER);
            grid.add(col);
        }
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE,grid.getPreferredSize().height+20));
        card.add(grid);
        return card;
    }
}
