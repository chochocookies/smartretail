package com.app.smartretail;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.app.smartretail.view.auth.LoginForm;

/**
 * Main - Entry point SmartRetailApp (SRMS)
 *
 * Smart Retail Management System
 * Versi  : 1.0.0
 * Dibuat : 2025
 */
public class Main {

    public static void main(String[] args) {
        // Set Look and Feel
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            System.err.println("NimbusLookAndFeel tidak tersedia, menggunakan default.");
        }

        // Jalankan di EDT (Event Dispatch Thread)
        SwingUtilities.invokeLater(() -> {
            LoginForm login = new LoginForm();
            login.setVisible(true);
        });
    }
}
