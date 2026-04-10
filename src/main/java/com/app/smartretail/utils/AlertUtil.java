package com.app.smartretail.utils;

import java.awt.Component;

import javax.swing.JOptionPane;

public class AlertUtil {

    private AlertUtil() {}

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Informasi", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showWarning(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Peringatan", JOptionPane.WARNING_MESSAGE);
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean showConfirm(Component parent, String message) {
        int result = JOptionPane.showConfirmDialog(parent, message, "Konfirmasi",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }

    // Overload tanpa parent (gunakan null)
    public static void showInfo(String message)    { showInfo(null, message); }
    public static void showWarning(String message) { showWarning(null, message); }
    public static void showError(String message)   { showError(null, message); }
    public static boolean showConfirm(String msg)  { return showConfirm(null, msg); }
}
