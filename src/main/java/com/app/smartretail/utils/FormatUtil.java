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

    /** Format angka ke Rupiah standar Indonesia: Rp 5.000 */
    public static String formatRupiah(double amount) {
        if (amount < 0) return "-Rp " + NUM.format(-amount);
        return "Rp " + NUM.format((long) amount);
    }

    /**
     * Format ringkas untuk dashboard — agar tidak terpotong di stat card.
     * Contoh: 1.500.000 → "Rp 1,5 Jt" | 2.100.000.000 → "Rp 2,1 M"
     */
    public static String formatRupiahCompact(double amount) {
        if (amount < 0) return "-" + formatRupiahCompact(-amount);
        if (amount >= 1_000_000_000)
            return String.format("Rp %.1f M", amount / 1_000_000_000.0);
        if (amount >= 1_000_000)
            return String.format("Rp %.1f Jt", amount / 1_000_000.0);
        if (amount >= 1_000)
            return String.format("Rp %.0f", amount);   // "Rp 5000" no separator for small
        return "Rp " + NUM.format((long) amount);
    }

    public static String formatNumber(int n){ return NUM.format(n); }
    public static String formatDateTime(LocalDateTime dt){ return dt!=null?dt.format(DT):"-"; }
    public static String formatDate(LocalDateTime dt){ return dt!=null?dt.format(D):"-"; }

    public static double parseDouble(String s){
        if(s==null||s.isBlank()) return 0.0;
        try{ return Double.parseDouble(s.replaceAll("[^\\d.]","")); }
        catch(NumberFormatException e){ return 0.0; }
    }
    public static int parseInt(String s){
        if(s==null||s.isBlank()) return 0;
        try{ return Integer.parseInt(s.replaceAll("[^\\d]","")); }
        catch(NumberFormatException e){ return 0; }
    }
}
