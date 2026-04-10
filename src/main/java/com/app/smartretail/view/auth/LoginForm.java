package com.app.smartretail.view.auth;

import com.app.smartretail.controller.AuthController;
import com.app.smartretail.model.User;
import com.app.smartretail.utils.AlertUtil;
import com.app.smartretail.view.dashboard.DashboardForm;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * LoginForm - Halaman login SmartRetailApp
 */
public class LoginForm extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblStatus;
    private AuthController authController;

    public LoginForm() {
        this.authController = new AuthController();
        initComponents();
        setLookAndFeel();
    }

    private void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception ignored) {}
    }

    private void initComponents() {
        setTitle("SRMS - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 480);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(30, 55, 95));

        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(30, 55, 95));
        headerPanel.setPreferredSize(new Dimension(420, 140));
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(new EmptyBorder(30, 20, 20, 20));

        JLabel lblTitle = new JLabel("Smart Retail", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubTitle = new JLabel("Management System", SwingConstants.CENTER);
        lblSubTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubTitle.setForeground(new Color(180, 200, 230));
        lblSubTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblVersion = new JLabel("v1.0.0", SwingConstants.CENTER);
        lblVersion.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblVersion.setForeground(new Color(150, 180, 210));
        lblVersion.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(lblTitle);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(lblSubTitle);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(lblVersion);

        // Form panel
        JPanel formPanel = new JPanel();
        formPanel.setBackground(Color.WHITE);
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);

        // Username
        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(lblUser, gbc);

        txtUsername = new JTextField();
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtUsername.setPreferredSize(new Dimension(300, 36));
        txtUsername.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        gbc.gridy = 1;
        formPanel.add(txtUsername, gbc);

        // Password
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridy = 2;
        formPanel.add(lblPass, gbc);

        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtPassword.setPreferredSize(new Dimension(300, 36));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        gbc.gridy = 3;
        formPanel.add(txtPassword, gbc);

        // Status label
        lblStatus = new JLabel(" ");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStatus.setForeground(Color.RED);
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 4;
        formPanel.add(lblStatus, gbc);

        // Login button
        btnLogin = new JButton("MASUK");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBackground(new Color(30, 55, 95));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setPreferredSize(new Dimension(300, 42));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setOpaque(true);
        gbc.gridy = 5;
        gbc.insets = new Insets(10, 0, 0, 0);
        formPanel.add(btnLogin, gbc);

        // Footer
        JLabel lblFooter = new JLabel("© 2025 SmartRetailApp", SwingConstants.CENTER);
        lblFooter.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblFooter.setForeground(Color.GRAY);
        gbc.gridy = 6;
        gbc.insets = new Insets(20, 0, 0, 0);
        formPanel.add(lblFooter, gbc);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        add(mainPanel);

        // Events
        btnLogin.addActionListener(e -> doLogin());
        txtPassword.addActionListener(e -> doLogin());
        txtUsername.addActionListener(e -> txtPassword.requestFocus());

        // Hover effect
        btnLogin.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnLogin.setBackground(new Color(20, 40, 80));
            }
            public void mouseExited(MouseEvent e) {
                btnLogin.setBackground(new Color(30, 55, 95));
            }
        });
    }

    private void doLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            lblStatus.setText("Username dan password tidak boleh kosong!");
            return;
        }

        // --- DEBUG START ---
        System.out.println("\n[DEBUG] === Mencoba Proses Login ===");
        System.out.println("[DEBUG] Input Username: " + username);
        System.out.println("[DEBUG] Input Password: " + (password.isEmpty() ? "Kosong" : "Terisi"));
        // --- DEBUG END ---

        btnLogin.setEnabled(false);
        lblStatus.setText("Memproses...");

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                // Di sini kita panggil controller
                User result = authController.login(username, password);
                
                // Debugging hasil pencarian di Database
                if (result == null) {
                    System.out.println("[DEBUG] Database Result: User TIDAK ditemukan (null).");
                } else {
                    System.out.println("[DEBUG] Database Result: User ditemukan! Nama: " + result.getNamaLengkap());
                }
                return result;
            }

            @Override
            protected void done() {
                try {
                    User user = get();
                    if (user != null) {
                        lblStatus.setForeground(new Color(0, 128, 0));
                        lblStatus.setText("Login berhasil! Membuka Dashboard...");
                        
                        Timer timer = new Timer(800, ev -> {
                            dispose();
                            new DashboardForm().setVisible(true);
                        });
                        timer.setRepeats(false);
                        timer.start();
                    } else {
                        lblStatus.setForeground(Color.RED);
                        lblStatus.setText("Username atau password salah!");
                        txtPassword.setText("");
                        txtPassword.requestFocus();
                    }
                } catch (Exception ex) {
                    // Menampilkan error asli di console (sangat penting!)
                    System.err.println("[CRITICAL ERROR] Terjadi kesalahan sistem saat login:");
                    ex.printStackTrace(); 
                    
                    lblStatus.setForeground(Color.RED);
                    lblStatus.setText("Error: " + ex.getCause().getMessage());
                } finally {
                    btnLogin.setEnabled(true);
                }
                System.out.println("[DEBUG] === Selesai Proses Login ===\n");
            }
        };
        worker.execute();
    }
}
