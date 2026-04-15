package com.app.smartretail.view.settings;

import com.app.smartretail.utils.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class HelpForm extends JPanel {

    public HelpForm() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_SURFACE);
        setBorder(new EmptyBorder(22,24,22,24));
        build();
    }

    private void build() {
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false); hdr.setBorder(new EmptyBorder(0,0,20,0));
        JPanel ht = new JPanel(); ht.setOpaque(false); ht.setLayout(new BoxLayout(ht,BoxLayout.Y_AXIS));
        ht.add(UITheme.pageTitle("Help & User Guide"));
        JLabel sub=new JLabel("Panduan penggunaan aplikasi SRMS"); sub.setFont(UITheme.FONT_BODY); sub.setForeground(UITheme.TEXT_SECONDARY); ht.add(sub);
        hdr.add(ht, BorderLayout.WEST);
        add(hdr, BorderLayout.NORTH);

        JPanel content = new JPanel(); content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        Object[][] sections = {
            {"Memulai Aplikasi", new String[]{
                "1. Buka aplikasi dan login dengan akun yang telah diberikan admin.",
                "2. Gunakan akun default: admin/admin123 untuk pertama kali.",
                "3. Segera ganti password setelah login pertama via Settings.",
                "4. Navigasi menggunakan sidebar di sebelah kiri.",
            }},
            {"POS — Point of Sale", new String[]{
                "1. Buka menu POS dari sidebar.",
                "2. Scan atau ketik kode/PLU barang pada kolom pencarian, tekan Enter.",
                "3. Atur jumlah barang di kolom Qty pada tabel keranjang.",
                "4. Pilih metode pembayaran dan masukkan jumlah uang diterima.",
                "5. Gunakan numpad untuk input cepat jumlah pembayaran.",
                "6. Klik 'Proses Transaksi' — stok otomatis berkurang.",
            }},
            {"Purchase — Pembelian Barang", new String[]{
                "1. Buka menu Purchase → tab 'New Purchase'.",
                "2. Pilih supplier dari dropdown.",
                "3. Scan atau ketik kode barang yang dibeli.",
                "4. Atur jumlah barang di kolom Qty.",
                "5. Klik 'Simpan Pembelian' — stok otomatis bertambah.",
                "6. Lihat riwayat di tab 'Purchase History'.",
                "7. Kelola data supplier di tab 'Suppliers'.",
            }},
            {"Products — Manajemen Produk", new String[]{
                "1. Klik tombol '+ Tambah' untuk menambah produk baru.",
                "2. Isi minimal: Nama Barang dan Harga Jual.",
                "3. Masukkan URL gambar produk dari klikindomaret.com jika tersedia.",
                "4. Gunakan toggle List/Grid untuk mengubah tampilan produk.",
                "5. Klik barang di tabel untuk memilih, lalu Edit atau Hapus.",
            }},
            {"Analytics — Prediksi Penjualan", new String[]{
                "1. Buka menu Analytics untuk melihat prediksi 7 hari ke depan.",
                "2. Tabel rekomendasi menampilkan barang yang perlu direstock.",
                "3. Prioritas TINGGI = stok kritis, segera lakukan pembelian.",
                "4. Klik 'Generate PO' untuk membuat Purchase Order otomatis (v2).",
            }},
            {"Reports — Laporan", new String[]{
                "1. Pilih jenis laporan: Penjualan, Pembelian, atau Barang Terlaris.",
                "2. Gunakan filter tahun untuk menyaring data.",
                "3. Klik 'Export PDF' untuk mengunduh laporan (memerlukan JasperReports).",
                "4. Tabel dapat diurutkan dengan klik pada header kolom.",
            }},
            {"Role & Hak Akses", new String[]{
                "ADMIN: Akses penuh ke semua fitur.",
                "SUPERVISOR: Akses ke POS, Purchase, Inventory, Laporan, Analytics.",
                "KASIR: Hanya akses POS dan Customers.",
                "STAFF GUDANG: Akses Purchase, Inventory, dan Products.",
            }},
        };

        for (Object[] sec : sections) {
            String title = (String) sec[0];
            String[] items = (String[]) sec[1];
            JPanel card = UITheme.card(); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setAlignmentX(LEFT_ALIGNMENT);
            JLabel t = new JLabel(title); t.setFont(UITheme.FONT_H2); t.setForeground(UITheme.TEXT_PRIMARY); t.setAlignmentX(LEFT_ALIGNMENT);
            card.add(t); card.add(Box.createVerticalStrut(10));
            for(String item:items){
                JLabel l=new JLabel(item); l.setFont(UITheme.FONT_BODY); l.setForeground(UITheme.TEXT_SECONDARY);
                l.setBorder(new EmptyBorder(0,8,6,0)); l.setAlignmentX(LEFT_ALIGNMENT);
                card.add(l);
            }
            content.add(card); content.add(Box.createVerticalStrut(12));
        }

        // Version info
        JPanel verCard = UITheme.tintCard(UITheme.CARD_BLUE_BG);
        verCard.setLayout(new BoxLayout(verCard, BoxLayout.Y_AXIS));
        verCard.setAlignmentX(LEFT_ALIGNMENT);
        JLabel vt = new JLabel("Tentang SRMS"); vt.setFont(UITheme.FONT_H2); vt.setForeground(UITheme.TEXT_PRIMARY); vt.setAlignmentX(LEFT_ALIGNMENT);
        String[] vInfo = {"Versi: 4.0.0","Package: com.app.smartretail","Platform: Java Swing (JDK 11+)","Database: MySQL 8.0+","Build: April 2025"};
        verCard.add(vt); verCard.add(Box.createVerticalStrut(8));
        for(String vi:vInfo){ JLabel l=new JLabel(vi); l.setFont(UITheme.FONT_SMALL); l.setForeground(UITheme.TEXT_SECONDARY); l.setAlignmentX(LEFT_ALIGNMENT); verCard.add(l); verCard.add(Box.createVerticalStrut(2)); }
        content.add(verCard);
        content.setAlignmentX(LEFT_ALIGNMENT);

        add(UITheme.styledScroll(content), BorderLayout.CENTER);
    }
}
