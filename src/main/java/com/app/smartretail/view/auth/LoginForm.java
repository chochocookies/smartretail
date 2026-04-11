package com.app.smartretail.view.auth;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import com.app.smartretail.controller.AuthController;
import com.app.smartretail.model.User;
import com.app.smartretail.utils.UITheme;
import com.app.smartretail.view.dashboard.DashboardForm;

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
        setTitle("SRMS — Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 560);
        setLocationRelativeTo(null);
        setResizable(false);
        setUndecorated(true);

        JPanel root = new JPanel(new GridLayout(1, 2));
        root.setBackground(UITheme.BG_DARK);

        // ── LEFT: Branding ───────────────────────────────────────────
        JPanel left = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_SIDEBAR);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(82, 130, 255, 20));
                g2.fillOval(-80, -80, 340, 340);
                g2.setColor(new Color(130, 82, 255, 15));
                g2.fillOval(180, 300, 280, 280);
                g2.setColor(new Color(255, 255, 255, 5));
                for (int x = 18; x < getWidth(); x += 26)
                    for (int y = 18; y < getHeight(); y += 26)
                        g2.fillOval(x, y, 2, 2);
                g2.dispose();
            }
        };
        left.setOpaque(false);

        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setMaximumSize(new Dimension(300, 400));

        // Logo
        JLabel logo = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.ACCENT_BLUE);
                g2.fillRoundRect(0, 0, 54, 54, 14, 14);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
                g2.drawString("SR", 10, 36);
                g2.dispose();
            }
            { setPreferredSize(new Dimension(54,54)); setMaximumSize(new Dimension(54,54)); setOpaque(false); }
        };

        JLabel lName = new JLabel("Smart Retail");
        lName.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lName.setForeground(UITheme.TEXT_PRIMARY);
        lName.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lSub = new JLabel("Management System");
        lSub.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lSub.setForeground(UITheme.TEXT_SECONDARY);
        lSub.setAlignmentX(LEFT_ALIGNMENT);

        JSeparator s1 = new JSeparator();
        s1.setForeground(UITheme.BORDER_DEFAULT);
        s1.setMaximumSize(new Dimension(220, 1));
        s1.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lDesc = new JLabel("<html><p style='width:220px;color:#8C94B2;font-size:11px;line-height:1.7'>" +
                "Platform terintegrasi untuk manajemen penjualan, stok, pembelian, dan laporan bisnis ritel.</p></html>");
        lDesc.setAlignmentX(LEFT_ALIGNMENT);

        // Feature tags
        JPanel tags = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        tags.setOpaque(false);
        tags.setAlignmentX(LEFT_ALIGNMENT);
        String[] feats = {"POS", "Stok", "Laporan", "Multi-Role"};
        Color[] tagColors = {UITheme.ACCENT_BLUE, UITheme.ACCENT_TEAL, UITheme.ACCENT_AMBER, UITheme.ACCENT_PURPLE};
        for (int i = 0; i < feats.length; i++) {
            tags.add(UITheme.badge(feats[i], tagColors[i], tagColors[i]));
        }

        brand.add(logo);
        brand.add(Box.createVerticalStrut(18));
        brand.add(lName);
        brand.add(Box.createVerticalStrut(4));
        brand.add(lSub);
        brand.add(Box.createVerticalStrut(16));
        brand.add(s1);
        brand.add(Box.createVerticalStrut(14));
        brand.add(lDesc);
        brand.add(Box.createVerticalStrut(14));
        brand.add(tags);

        left.add(brand);

        // ── RIGHT: Form ──────────────────────────────────────────────
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(UITheme.BG_DARK);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setMaximumSize(new Dimension(320, 500));

        // Close btn (since undecorated)
        JButton btnClose = closeBtn();
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        topRow.setOpaque(false);
        topRow.setMaximumSize(new Dimension(320, 28));
        topRow.add(btnClose);
        btnClose.addActionListener(e -> System.exit(0));

        JLabel lTitle = new JLabel("Selamat Datang!");
        lTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lTitle.setForeground(UITheme.TEXT_PRIMARY);
        lTitle.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lSubT = new JLabel("Masuk ke akun SRMS Anda");
        lSubT.setFont(UITheme.FONT_BODY);
        lSubT.setForeground(UITheme.TEXT_SECONDARY);
        lSubT.setAlignmentX(LEFT_ALIGNMENT);

        // Fields
        JLabel lU = UITheme.fieldLabel("Username");
        lU.setAlignmentX(LEFT_ALIGNMENT);
        txtUsername = UITheme.styledField("Masukkan username…");
        txtUsername.setMaximumSize(new Dimension(320, 42));
        txtUsername.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lP = UITheme.fieldLabel("Password");
        lP.setAlignmentX(LEFT_ALIGNMENT);
        txtPassword = UITheme.styledPassword("••••••••");
        txtPassword.setMaximumSize(new Dimension(320, 42));
        txtPassword.setAlignmentX(LEFT_ALIGNMENT);

        lblStatus = new JLabel(" ");
        lblStatus.setFont(UITheme.FONT_SMALL);
        lblStatus.setForeground(UITheme.ACCENT_CORAL);
        lblStatus.setAlignmentX(LEFT_ALIGNMENT);

        btnLogin = UITheme.primaryButton("  Masuk  ", UITheme.ACCENT_BLUE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setMaximumSize(new Dimension(320, 44));
        btnLogin.setPreferredSize(new Dimension(320, 44));
        btnLogin.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lHint = new JLabel("Default login: admin / admin123");
        lHint.setFont(UITheme.FONT_SMALL);
        lHint.setForeground(UITheme.TEXT_MUTED);
        lHint.setAlignmentX(LEFT_ALIGNMENT);

        form.add(topRow);
        form.add(Box.createVerticalStrut(4));
        form.add(lTitle);
        form.add(Box.createVerticalStrut(4));
        form.add(lSubT);
        form.add(Box.createVerticalStrut(26));
        form.add(lU);
        form.add(Box.createVerticalStrut(7));
        form.add(txtUsername);
        form.add(Box.createVerticalStrut(16));
        form.add(lP);
        form.add(Box.createVerticalStrut(7));
        form.add(txtPassword);
        form.add(Box.createVerticalStrut(8));
        form.add(lblStatus);
        form.add(Box.createVerticalStrut(14));
        form.add(btnLogin);
        form.add(Box.createVerticalStrut(16));
        form.add(lHint);

        right.add(form);

        root.add(left);
        root.add(right);

        // Drag to move
        addDrag(left); addDrag(right);
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

        new SwingWorker<User, Void>() {
            protected User doInBackground() { return authCtrl.login(user, pass); }
            protected void done() {
                try {
                    User u = get();
                    if (u != null) {
                        lblStatus.setForeground(UITheme.ACCENT_GREEN);
                        lblStatus.setText("Berhasil! Memuat dashboard…");
                        new Timer(600, ev -> { dispose(); new DashboardForm().setVisible(true); }) {{setRepeats(false);}}.start();
                    } else {
                        lblStatus.setForeground(UITheme.ACCENT_CORAL);
                        lblStatus.setText("Username atau password salah!");
                        txtPassword.setText(""); btnLogin.setEnabled(true);
                    }
                } catch (Exception ex) { lblStatus.setText("Error sistem."); btnLogin.setEnabled(true); }
            }
        }.execute();
    }

    private JButton closeBtn() {
        JButton b = new JButton("✕") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(UITheme.ACCENT_CORAL);
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),6,6);
                    g2.setColor(Color.WHITE);
                } else g2.setColor(UITheme.TEXT_MUTED);
                g2.setFont(new Font("Segoe UI",Font.PLAIN,12));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("✕", getWidth()/2 - fm.stringWidth("✕")/2, getHeight()/2 + fm.getAscent()/2 - 2);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(26, 26));
        b.setOpaque(false); b.setContentAreaFilled(false);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private int dx, dy;
    private void addDrag(JPanel p) {
        p.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { dx = e.getX(); dy = e.getY(); }
        });
        p.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                setLocation(getX() + e.getX() - dx, getY() + e.getY() - dy);
            }
        });
    }
}
