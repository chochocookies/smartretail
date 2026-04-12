package com.app.smartretail.utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class AlertUtil {
    private AlertUtil(){}

    public static void showInfo(Component parent, String message) {
        show(parent, message, "Informasi", UITheme.ACCENT_TEAL);
    }
    public static void showWarning(Component parent, String message) {
        show(parent, message, "Peringatan", UITheme.ACCENT_AMBER);
    }
    public static void showError(Component parent, String message) {
        show(parent, message, "Error", UITheme.ACCENT_CORAL);
    }
    public static boolean showConfirm(Component parent, String message) {
        return confirm(parent, message);
    }
    public static void showInfo(String m)    { showInfo(null, m); }
    public static void showWarning(String m) { showWarning(null, m); }
    public static void showError(String m)   { showError(null, m); }
    public static boolean showConfirm(String m){ return showConfirm(null, m); }

    private static void show(Component parent, String msg, String title, Color accent) {
        JDialog d = dialog(parent);
        JLabel icon = iconLabel(accent);
        JLabel text = msgLabel(msg);
        JButton ok = UITheme.primaryButton("OK", accent);
        ok.addActionListener(e -> d.dispose());
        layout(d, icon, text, ok, null, accent);
        d.setVisible(true);
    }

    private static boolean confirm(Component parent, String msg) {
        boolean[] result = {false};
        JDialog d = dialog(parent);
        JLabel icon = iconLabel(UITheme.ACCENT_BLUE);
        JLabel text = msgLabel(msg);
        JButton yes = UITheme.primaryButton("Ya, Lanjutkan", UITheme.ACCENT_BLUE);
        JButton no  = UITheme.ghostButton("Batal", UITheme.TEXT_SECONDARY);
        yes.addActionListener(e -> { result[0] = true; d.dispose(); });
        no.addActionListener(e -> d.dispose());
        layout(d, icon, text, yes, no, UITheme.ACCENT_BLUE);
        d.setVisible(true);
        return result[0];
    }

    private static JDialog dialog(Component parent) {
        Window w = parent != null ? SwingUtilities.getWindowAncestor(parent) : null;
        JDialog d = new JDialog(w);
        d.setModal(true);
        d.setUndecorated(true);
        d.setBackground(UITheme.BG_CARD);
        d.getRootPane().setBackground(UITheme.BG_CARD);
        return d;
    }

    private static JLabel iconLabel(Color accent) {
        JLabel l = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),22));
                g2.fillOval(0,0,40,40);
                g2.setColor(accent);
                g2.setFont(new Font("Segoe UI",Font.BOLD,18));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("!", 20-fm.stringWidth("!")/2, 26);
                g2.dispose();
            }
            { setPreferredSize(new Dimension(40,40)); setOpaque(false); }
        };
        return l;
    }

    private static JLabel msgLabel(String msg) {
        JLabel l = new JLabel("<html><div style='width:240px;line-height:1.6'>"
                + msg.replace("\n","<br>") + "</div></html>");
        l.setFont(UITheme.FONT_BODY);
        l.setForeground(UITheme.TEXT_PRIMARY);
        return l;
    }

    private static void layout(JDialog d, JLabel icon, JLabel text,
                                JButton primary, JButton secondary, Color accent) {
        JPanel root = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_CARD);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),16,16);
                // top accent bar
                g2.setColor(accent);
                g2.fillRoundRect(0,0,getWidth(),4,4,4);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(20,22,18,22));
        root.setLayout(new BorderLayout(14, 12));

        JPanel top = new JPanel(new BorderLayout(12,0));
        top.setOpaque(false);
        top.add(icon, BorderLayout.WEST);
        top.add(text, BorderLayout.CENTER);
        root.add(top, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
        btns.setOpaque(false);
        if (secondary != null) btns.add(secondary);
        btns.add(primary);
        root.add(btns, BorderLayout.SOUTH);

        d.setContentPane(root);
        d.pack();
        d.setMinimumSize(new Dimension(340,130));
        d.setLocationRelativeTo(null);
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("ESCAPE"),"esc");
        root.getActionMap().put("esc", new AbstractAction(){
            public void actionPerformed(ActionEvent e){d.dispose();}
        });
    }
}
