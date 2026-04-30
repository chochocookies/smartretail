package com.app.smartretail.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class FormatUtil {

    private static final Locale ID = new Locale("id","ID");
    private static final DecimalFormat NUM = new DecimalFormat("#,###", new DecimalFormatSymbols(ID));
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter D  = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private FormatUtil(){}

    /** Format angka ke Rupiah standar Indonesia: Rp 13.000 */
    public static String formatRupiah(double amount) {
        if (amount < 0) return "-Rp " + NUM.format((long)(-amount));
        return "Rp " + NUM.format((long) amount);
    }

    /**
     * Format ringkas untuk dashboard agar tidak terpotong di stat card.
     * 1.500.000 → "Rp 1,5 Jt" | 2.100.000.000 → "Rp 2,1 M"
     */
    public static String formatRupiahCompact(double amount) {
        if (amount < 0) return "-" + formatRupiahCompact(-amount);
        if (amount >= 1_000_000_000)
            return String.format("Rp %.1f M",  amount / 1_000_000_000.0);
        if (amount >= 1_000_000)
            return String.format("Rp %.1f Jt", amount / 1_000_000.0);
        return "Rp " + NUM.format((long) amount);
    }

    public static String formatNumber(int n) { return NUM.format(n); }
    public static String formatDateTime(LocalDateTime dt) { return dt != null ? dt.format(DT) : "-"; }
    public static String formatDate(LocalDateTime dt)     { return dt != null ? dt.format(D)  : "-"; }

    /**
     * FIX BUG KEMBALIAN: Format Rupiah Indonesia pakai TITIK sebagai pemisah ribuan.
     * "Rp 13.000" harus dibaca 13000, BUKAN 13.0
     *
     * SEBELUM (bug): replaceAll("[^\\d.]","") → "13.000" → Double.parseDouble → 13.0   SALAH
     * SESUDAH (fix): replaceAll("[^\\d]","")  → "13000"  → Double.parseDouble → 13000  BENAR
     *
     * Rupiah tidak punya desimal, aman membuang SEMUA karakter non-digit.
     */
    public static double parseDouble(String s) {
        if (s == null || s.isBlank()) return 0.0;
        String cleaned = s.replaceAll("[^\\d]", "");   // hapus SEMUA non-digit
        if (cleaned.isEmpty()) return 0.0;
        try { return Double.parseDouble(cleaned); }
        catch (NumberFormatException e) { return 0.0; }
    }

    public static int parseInt(String s) {
        if (s == null || s.isBlank()) return 0;
        String cleaned = s.replaceAll("[^\\d]", "");
        if (cleaned.isEmpty()) return 0;
        try { return Integer.parseInt(cleaned); }
        catch (NumberFormatException e) { return 0; }
    }
}
