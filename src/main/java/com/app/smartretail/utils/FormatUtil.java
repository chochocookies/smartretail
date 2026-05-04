package com.app.smartretail.utils;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

/**
 * FormatUtil — satu tempat untuk semua format angka & tanggal.
 *
 * PERBAIKAN v4:
 *   1. parseDouble() — buang SEMUA non-digit agar "Rp 13.000" = 13000, bukan 13.0
 *   2. formatDateTime() — overload untuk LocalDateTime, Date, Timestamp
 *   3. formatDateTimeLong() — format panjang "dd MMMM yyyy HH:mm" untuk PDF
 *   4. formatDate() — overload untuk semua tipe
 *   5. formatForFilename() — format "yyyyMMdd_HHmm" untuk nama file
 */
public class FormatUtil {

    // ── Locale & format angka ─────────────────────────────────────
    private static final Locale        ID  = new Locale("id", "ID");
    private static final DecimalFormat NUM = new DecimalFormat(
            "#,###", new DecimalFormatSymbols(ID));

    // ── Pattern tanggal — satu definisi konsisten ────────────────
    /** "dd/MM/yyyy HH:mm"       → 29/04/2026 14:30  (tabel, riwayat) */
    public static final String PATTERN_DT       = "dd/MM/yyyy HH:mm";
    /** "dd/MM/yyyy"             → 29/04/2026        (tanggal saja)   */
    public static final String PATTERN_DATE     = "dd/MM/yyyy";
    /** "dd MMMM yyyy HH:mm"    → 29 April 2026 14:30 (PDF/laporan)  */
    public static final String PATTERN_DT_LONG  = "dd MMMM yyyy HH:mm";
    /** "yyyyMMdd_HHmm"         → 20260429_1430 (nama file)          */
    public static final String PATTERN_FILENAME = "yyyyMMdd_HHmm";

    private static final DateTimeFormatter FMT_DT       =
            DateTimeFormatter.ofPattern(PATTERN_DT);
    private static final DateTimeFormatter FMT_DATE     =
            DateTimeFormatter.ofPattern(PATTERN_DATE);
    private static final DateTimeFormatter FMT_DT_LONG  =
            DateTimeFormatter.ofPattern(PATTERN_DT_LONG, new Locale("id","ID"));
    private static final DateTimeFormatter FMT_FILENAME =
            DateTimeFormatter.ofPattern(PATTERN_FILENAME);

    private FormatUtil() {}

    // ════════════════════════════════════════════════════════════════
    // FORMAT RUPIAH
    // ════════════════════════════════════════════════════════════════

    /**
     * Format Rupiah standar: "Rp 13.000"
     * Dipakai di cart, kembalian, tabel laporan.
     */
    public static String formatRupiah(double amount) {
        if (amount < 0) return "-Rp " + NUM.format((long) (-amount));
        return "Rp " + NUM.format((long) amount);
    }

    /**
     * Format ringkas untuk stat card dashboard agar tidak terpotong.
     * 1.500.000 → "Rp 1,5 Jt"  |  2.100.000.000 → "Rp 2,1 M"
     */
    public static String formatRupiahCompact(double amount) {
        if (amount < 0) return "-" + formatRupiahCompact(-amount);
        if (amount >= 1_000_000_000)
            return String.format("Rp %.1f M",  amount / 1_000_000_000.0);
        if (amount >= 1_000_000)
            return String.format("Rp %.1f Jt", amount / 1_000_000.0);
        return "Rp " + NUM.format((long) amount);
    }

    /** Format integer ke string: "1.234" */
    public static String formatNumber(int n) { return NUM.format(n); }

    // ════════════════════════════════════════════════════════════════
    // FORMAT TANGGAL — overload untuk semua tipe Java
    // ════════════════════════════════════════════════════════════════

    // ── formatDateTime: "dd/MM/yyyy HH:mm" ──────────────────────

    /** Dari LocalDateTime (model Java) */
    public static String formatDateTime(LocalDateTime dt) {
        return dt != null ? dt.format(FMT_DT) : "-";
    }

    /**
     * Dari java.sql.Timestamp — dipakai di:
     *   rs.getTimestamp("tanggal") di DAO/ReportForm
     */
    public static String formatDateTime(Timestamp ts) {
        if (ts == null) return "-";
        return ts.toLocalDateTime().format(FMT_DT);
    }

    /** Dari java.util.Date */
    public static String formatDateTime(Date date) {
        if (date == null) return "-";
        return date.toInstant()
                   .atZone(ZoneId.systemDefault())
                   .toLocalDateTime()
                   .format(FMT_DT);
    }

    // ── formatDateTimeLong: "dd MMMM yyyy HH:mm" ─────────────────

    /** Format panjang untuk header PDF/laporan: "29 April 2026 14:30" */
    public static String formatDateTimeLong(LocalDateTime dt) {
        return dt != null ? dt.format(FMT_DT_LONG) : "-";
    }

    public static String formatDateTimeLong(Timestamp ts) {
        if (ts == null) return "-";
        return ts.toLocalDateTime().format(FMT_DT_LONG);
    }

    public static String formatDateTimeLong(Date date) {
        if (date == null) return "-";
        return date.toInstant()
                   .atZone(ZoneId.systemDefault())
                   .toLocalDateTime()
                   .format(FMT_DT_LONG);
    }

    // ── formatDate: "dd/MM/yyyy" ─────────────────────────────────

    public static String formatDate(LocalDateTime dt) {
        return dt != null ? dt.format(FMT_DATE) : "-";
    }

    public static String formatDate(LocalDate d) {
        return d != null ? d.format(FMT_DATE) : "-";
    }

    public static String formatDate(Timestamp ts) {
        if (ts == null) return "-";
        return ts.toLocalDateTime().format(FMT_DATE);
    }

    public static String formatDate(Date date) {
        if (date == null) return "-";
        return date.toInstant()
                   .atZone(ZoneId.systemDefault())
                   .toLocalDateTime()
                   .format(FMT_DATE);
    }

    // ── formatForFilename: "yyyyMMdd_HHmm" ───────────────────────

    /** Untuk nama file: "20260429_1430" */
    public static String formatForFilename() {
        return LocalDateTime.now().format(FMT_FILENAME);
    }

    public static String formatForFilename(LocalDateTime dt) {
        return dt != null ? dt.format(FMT_FILENAME) : formatForFilename();
    }

    // ════════════════════════════════════════════════════════════════
    // PARSE ANGKA
    // ════════════════════════════════════════════════════════════════

    /**
     * Parse string Rupiah ke double.
     *
     * BUG FIX KEMBALIAN:
     *   Rupiah Indonesia pakai TITIK sebagai pemisah ribuan (bukan desimal).
     *   "Rp 13.000" = 13.000 rupiah = 13000, BUKAN 13.0
     *
     *   ❌ Lama: replaceAll("[^\\d.]","") → "13.000" → 13.0   SALAH
     *   ✅ Fix:  replaceAll("[^\\d]","")  → "13000"  → 13000  BENAR
     */
    public static double parseDouble(String s) {
        if (s == null || s.isBlank()) return 0.0;
        String cleaned = s.replaceAll("[^\\d]", ""); // buang SEMUA non-digit
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
