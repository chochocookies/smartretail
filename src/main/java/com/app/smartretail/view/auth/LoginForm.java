package com.app.smartretail.view.auth;

import com.app.smartretail.controller.AuthController;
import com.app.smartretail.model.User;
import com.app.smartretail.utils.UITheme;
import com.app.smartretail.view.component.Icons;
import com.app.smartretail.view.dashboard.DashboardForm;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class LoginForm extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblStatus;
    private final AuthController authCtrl = new AuthController();

    public LoginForm() {
        UITheme.apply();
        initUI();
    }

    private void initUI() {
        setTitle("SRMS — Smart Retail Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 580);
        setMinimumSize(new Dimension(780, 500));
        setLocationRelativeTo(null);
        setResizable(true);

        // Root: page background
        JPanel root = new JPanel(new GridLayout(1, 2));
        root.setBackground(UITheme.BG_PAGE);

        // ── LEFT: illustration panel ─────────────────────────────
        JPanel left = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Soft gradient background
                g2.setColor(new Color(238, 242, 255));
                g2.fillRect(0,0,getWidth(),getHeight());
                // Decorative circles
                g2.setColor(new Color(99,102,241,25));
                g2.fillOval(-60,-60,320,320);
                g2.setColor(new Color(212,241,73,60));
                g2.fillOval(getWidth()-180, getHeight()-180, 280,280);
                g2.setColor(new Color(99,102,241,12));
                g2.fillOval(getWidth()/2-80, getHeight()/2-60, 200,200);
                g2.dispose();
            }
        };
        left.setOpaque(false);

        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));

        JLabel logoLbl = new JLabel(Icons.logoIcon("SR", UITheme.ACCENT_BLUE, 56));
        logoLbl.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lAppName = new JLabel("Starline SRMS");
        lAppName.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lAppName.setForeground(UITheme.TEXT_PRIMARY);
        lAppName.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lSub = new JLabel("Smart Retail Management");
        lSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lSub.setForeground(UITheme.TEXT_SECONDARY);
        lSub.setAlignmentX(LEFT_ALIGNMENT);

        JSeparator sep = UITheme.separator();
        sep.setMaximumSize(new Dimension(200,1));
        sep.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lDesc = new JLabel("<html><p style='width:200px;color:#6B7280;font-size:11px;line-height:1.7'>"
            + "Platform terintegrasi untuk kelola penjualan, stok, pembelian, dan laporan bisnis ritel Anda.</p></html>");
        lDesc.setAlignmentX(LEFT_ALIGNMENT);

        JPanel pills = new JPanel(new FlowLayout(FlowLayout.LEFT,6,4));
        pills.setOpaque(false); pills.setAlignmentX(LEFT_ALIGNMENT);
        String[] fs = {"POS","Stok","Laporan","Multi-Role"};
        Color[] fc = {UITheme.ACCENT_BLUE, UITheme.ACCENT_TEAL, UITheme.ACCENT_AMBER, UITheme.ACCENT_PURPLE};
        for (int i=0; i<fs.length; i++) pills.add(UITheme.badge(fs[i], fc[i], fc[i]));

        brand.add(logoLbl);
        brand.add(Box.createVerticalStrut(18));
        brand.add(lAppName);
        brand.add(Box.createVerticalStrut(4));
        brand.add(lSub);
        brand.add(Box.createVerticalStrut(16));
        brand.add(sep);
        brand.add(Box.createVerticalStrut(14));
        brand.add(lDesc);
        brand.add(Box.createVerticalStrut(14));
        brand.add(pills);
        left.add(brand);

        // ── RIGHT: Form card ─────────────────────────────────────
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(UITheme.BG_PAGE);

        JPanel formCard = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_CARD);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);
                g2.setColor(UITheme.BORDER_DEFAULT);
                g2.setStroke(new BasicStroke(0.8f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,20,20);
                g2.dispose();
            }
        };
        formCard.setOpaque(false);
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBorder(new EmptyBorder(32,32,28,32));
        formCard.setPreferredSize(new Dimension(340,420));
        formCard.setMaximumSize(new Dimension(340,420));

        JLabel lTitle = new JLabel("Selamat Datang!");
        lTitle.setFont(new Font("Segoe UI",Font.BOLD,22));
        lTitle.setForeground(UITheme.TEXT_PRIMARY);
        lTitle.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lSubT = new JLabel("Masuk ke akun Anda");
        lSubT.setFont(UITheme.FONT_BODY);
        lSubT.setForeground(UITheme.TEXT_SECONDARY);
        lSubT.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lU = UITheme.fieldLabel("Username");
        lU.setAlignmentX(LEFT_ALIGNMENT);
        txtUsername = UITheme.styledField("contoh: admin");
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtUsername.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lP = UITheme.fieldLabel("Password");
        lP.setAlignmentX(LEFT_ALIGNMENT);
        txtPassword = UITheme.styledPassword();
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtPassword.setAlignmentX(LEFT_ALIGNMENT);

        lblStatus = new JLabel(" ");
        lblStatus.setFont(UITheme.FONT_SMALL);
        lblStatus.setForeground(UITheme.ACCENT_CORAL);
        lblStatus.setAlignmentX(LEFT_ALIGNMENT);

        btnLogin = UITheme.primaryButton("Masuk", UITheme.ACCENT_LIME);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnLogin.setPreferredSize(new Dimension(276,44));
        btnLogin.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lHint = new JLabel("Login: admin / kasir1 / gudang1 / supervisor  ( password: admin123 )");
        lHint.setFont(UITheme.FONT_LABEL);
        lHint.setForeground(UITheme.TEXT_MUTED);
        lHint.setAlignmentX(LEFT_ALIGNMENT);

        formCard.add(lTitle);
        formCard.add(Box.createVerticalStrut(4));
        formCard.add(lSubT);
        formCard.add(Box.createVerticalStrut(26));
        formCard.add(lU);
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(txtUsername);
        formCard.add(Box.createVerticalStrut(14));
        formCard.add(lP);
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(txtPassword);
        formCard.add(Box.createVerticalStrut(8));
        formCard.add(lblStatus);
        formCard.add(Box.createVerticalStrut(10));
        formCard.add(btnLogin);
        formCard.add(Box.createVerticalStrut(14));
        formCard.add(lHint);

        right.add(formCard);
        root.add(left);
        root.add(right);
        setContentPane(root);

        btnLogin.addActionListener(e -> doLogin());
        txtPassword.addActionListener(e -> doLogin());
        txtUsername.addActionListener(e -> txtPassword.requestFocus());
    }

    private void doLogin() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());
        if (user.isEmpty() || pass.isEmpty()) {
            lblStatus.setForeground(UITheme.ACCENT_CORAL);
            lblStatus.setText("Username dan password wajib diisi!"); return;
        }
        btnLogin.setEnabled(false);
        lblStatus.setForeground(UITheme.TEXT_SECONDARY);
        lblStatus.setText("Memverifikasi…");
        new SwingWorker<User,Void>() {
            protected User doInBackground() { return authCtrl.login(user, pass); }
            protected void done() {
                try {
                    User u = get();
                    if (u != null) {
                        lblStatus.setForeground(UITheme.ACCENT_GREEN);
                        lblStatus.setText("Berhasil! Memuat dashboard…");
                        new Timer(700, ev -> { dispose(); new DashboardForm().setVisible(true); }){{setRepeats(false);}}.start();
                    } else {
                        lblStatus.setForeground(UITheme.ACCENT_CORAL);
                        lblStatus.setText("Username atau password salah!");
                        txtPassword.setText(""); btnLogin.setEnabled(true);
                    }
                } catch(Exception ex) { lblStatus.setText("Error."); btnLogin.setEnabled(true); }
            }
        }.execute();
    }
}
