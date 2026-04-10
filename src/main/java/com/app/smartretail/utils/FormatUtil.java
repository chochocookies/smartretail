package com.app.smartretail.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class FormatUtil {

    private static final Locale LOCALE_ID = new Locale("id", "ID");
    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(LOCALE_ID);
    private static final DecimalFormat NUMBER = new DecimalFormat("#,###");
    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private FormatUtil() {}

    public static String formatRupiah(double amount) {
        return CURRENCY.format(amount).replace(",00", "");
    }

    public static String formatNumber(int number) {
        return NUMBER.format(number);
    }

    public static String formatDateTime(LocalDateTime dt) {
        return dt != null ? dt.format(DT_FORMAT) : "-";
    }

    public static String formatDate(LocalDateTime dt) {
        return dt != null ? dt.format(DATE_FORMAT) : "-";
    }

    public static double parseDouble(String s) {
        try { return Double.parseDouble(s.replaceAll("[^\\d.]", "")); }
        catch (NumberFormatException e) { return 0.0; }
    }

    public static int parseInt(String s) {
        try { return Integer.parseInt(s.replaceAll("[^\\d]", "")); }
        catch (NumberFormatException e) { return 0; }
    }
}
