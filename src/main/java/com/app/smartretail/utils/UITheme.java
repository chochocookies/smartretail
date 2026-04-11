package com.app.smartretail.utils;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.table.JTableHeader;

/**
 * UITheme — Centralized design system for SRMS
 * Modern dark-accent palette inspired by professional POS dashboards.
 */
public class UITheme {

    // ─── Color Palette ─────────────────────────────────────────────────
    public static final Color BG_DARK       = new Color(15, 17, 26);      // main background
    public static final Color BG_CARD       = new Color(22, 26, 40);      // card/panel bg
    public static final Color BG_SIDEBAR    = new Color(17, 20, 32);      // sidebar
    public static final Color BG_INPUT      = new Color(28, 33, 50);      // input fields
    public static final Color BG_ROW_ALT    = new Color(26, 31, 47);      // table alt row
    public static final Color BG_HOVER      = new Color(35, 42, 62);      // hover state

    public static final Color ACCENT_BLUE   = new Color(82, 130, 255);    // primary accent
    public static final Color ACCENT_PURPLE = new Color(130, 82, 255);    // secondary accent
    public static final Color ACCENT_TEAL   = new Color(32, 210, 178);    // success/teal
    public static final Color ACCENT_AMBER  = new Color(255, 190, 60);    // warning/amber
    public static final Color ACCENT_CORAL  = new Color(255, 88, 100);    // danger/coral
    public static final Color ACCENT_GREEN  = new Color(52, 211, 153);    // positive

    public static final Color TEXT_PRIMARY   = new Color(230, 232, 245);
    public static final Color TEXT_SECONDARY = new Color(140, 148, 178);
    public static final Color TEXT_MUTED     = new Color(80, 90, 120);
    public static final Color TEXT_ACCENT    = ACCENT_BLUE;

    public static final Color BORDER_DEFAULT = new Color(38, 44, 65);
    public static final Color BORDER_ACCENT  = new Color(60, 80, 130);

    // ─── Fonts ─────────────────────────────────────────────────────────
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_H2      = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_H3      = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO    = new Font("Consolas", Font.PLAIN, 12);
    public static final Font FONT_NUM     = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_LABEL   = new Font("Segoe UI", Font.PLAIN, 11);

    // ─── Global LAF setup ──────────────────────────────────────────────
    public static void apply() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        UIManager.put("Panel.background",          BG_DARK);
        UIManager.put("Table.background",          BG_CARD);
        UIManager.put("Table.alternateRowColor",   BG_ROW_ALT);
        UIManager.put("Table.foreground",          TEXT_PRIMARY);
        UIManager.put("Table.gridColor",           BORDER_DEFAULT);
        UIManager.put("Table.selectionBackground", ACCENT_BLUE.darker());
        UIManager.put("Table.selectionForeground", Color.WHITE);
        UIManager.put("TableHeader.background",    BG_SIDEBAR);
        UIManager.put("TableHeader.foreground",    TEXT_SECONDARY);
        UIManager.put("ScrollPane.background",     BG_CARD);
        UIManager.put("ScrollBar.background",      BG_CARD);
        UIManager.put("ScrollBar.thumb",           BORDER_ACCENT);
        UIManager.put("ScrollBar.track",           BG_CARD);
        UIManager.put("TextField.background",      BG_INPUT);
        UIManager.put("TextField.foreground",      TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground", ACCENT_BLUE);
        UIManager.put("PasswordField.background",  BG_INPUT);
        UIManager.put("PasswordField.foreground",  TEXT_PRIMARY);
        UIManager.put("ComboBox.background",       BG_INPUT);
        UIManager.put("ComboBox.foreground",       TEXT_PRIMARY);
        UIManager.put("Label.foreground",          TEXT_PRIMARY);
        UIManager.put("CheckBox.background",       BG_CARD);
        UIManager.put("CheckBox.foreground",       TEXT_PRIMARY);
        UIManager.put("OptionPane.background",     BG_CARD);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
        UIManager.put("Button.background",         BG_INPUT);
        UIManager.put("Button.foreground",         TEXT_PRIMARY);
        UIManager.put("SplitPane.background",      BG_DARK);
        UIManager.put("SplitPane.dividerSize",     1);
    }

    // ─── Factory: Metric Card ──────────────────────────────────────────
    public static JPanel metricCard(String label, String value, String subtitle, Color accent) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                // Left accent bar
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout());
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(TEXT_MUTED);

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(FONT_NUM);
        valLbl.setForeground(TEXT_PRIMARY);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(FONT_SMALL);
        subLbl.setForeground(accent);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(lbl, BorderLayout.NORTH);
        top.add(valLbl, BorderLayout.CENTER);
        top.add(subLbl, BorderLayout.SOUTH);

        card.add(top, BorderLayout.CENTER);
        return card;
    }

    // ─── Factory: Primary Button ───────────────────────────────────────
    public static JButton primaryButton(String text, Color accent) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed()  ? accent.darker() :
                           getModel().isRollover() ? accent.brighter() : accent;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_H3);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 36));
        return btn;
    }

    // ─── Factory: Ghost Button ─────────────────────────────────────────
    public static JButton ghostButton(String text, Color accent) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover() || getModel().isPressed()) {
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 30));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_H3);
        btn.setForeground(accent);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 36));
        return btn;
    }

    // ─── Factory: Danger Button ────────────────────────────────────────
    public static JButton dangerButton(String text) {
        return ghostButton(text, ACCENT_CORAL);
    }

    // ─── Factory: Styled TextField ─────────────────────────────────────
    public static JTextField styledField(String placeholder) {
        JTextField f = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_INPUT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        f.setFont(FONT_BODY);
        f.setForeground(TEXT_PRIMARY);
        f.setBackground(BG_INPUT);
        f.setCaretColor(ACCENT_BLUE);
        f.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(8, BORDER_DEFAULT),
            new EmptyBorder(6, 12, 6, 12)));
        f.putClientProperty("JTextField.placeholderText", placeholder);
        return f;
    }

    // ─── Factory: Styled PasswordField ────────────────────────────────
    public static JPasswordField styledPassword(String placeholder) {
        JPasswordField f = new JPasswordField();
        f.setFont(FONT_BODY);
        f.setForeground(TEXT_PRIMARY);
        f.setBackground(BG_INPUT);
        f.setCaretColor(ACCENT_BLUE);
        f.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(8, BORDER_DEFAULT),
            new EmptyBorder(6, 12, 6, 12)));
        return f;
    }

    // ─── Factory: Styled ComboBox ──────────────────────────────────────
    public static JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(FONT_BODY);
        combo.setBackground(BG_INPUT);
        combo.setForeground(TEXT_PRIMARY);
        combo.setBorder(new RoundedBorder(8, BORDER_DEFAULT));
        combo.setPreferredSize(new Dimension(combo.getPreferredSize().width, 36));
        return combo;
    }

    // ─── Factory: Section Label ────────────────────────────────────────
    public static JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text.toUpperCase());
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(TEXT_MUTED);
        lbl.setBorder(new EmptyBorder(14, 0, 6, 0));
        return lbl;
    }

    // ─── Factory: Field Label ──────────────────────────────────────────
    public static JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(TEXT_SECONDARY);
        return lbl;
    }

    // ─── Factory: Page Title ───────────────────────────────────────────
    public static JLabel pageTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_TITLE);
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    // ─── Factory: Card Panel ───────────────────────────────────────────
    public static JPanel card() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(BORDER_DEFAULT);
                g2.setStroke(new BasicStroke(0.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 18, 16, 18));
        return p;
    }

    // ─── Factory: Styled JTable ────────────────────────────────────────
    public static void styleTable(JTable table) {
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRIMARY);
        table.setFont(FONT_BODY);
        table.setRowHeight(36);
        table.setGridColor(BORDER_DEFAULT);
        table.setSelectionBackground(new Color(82, 130, 255, 60));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);

        JTableHeader hdr = table.getTableHeader();
        hdr.setBackground(BG_SIDEBAR);
        hdr.setForeground(TEXT_SECONDARY);
        hdr.setFont(FONT_SMALL);
        hdr.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_DEFAULT));
        hdr.setReorderingAllowed(false);
    }

    // ─── Factory: Styled JScrollPane ──────────────────────────────────
    public static JScrollPane styledScroll(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBackground(BG_CARD);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(BG_CARD);
        sp.getVerticalScrollBar().setBackground(BG_CARD);
        sp.getHorizontalScrollBar().setBackground(BG_CARD);
        styleScrollBar(sp.getVerticalScrollBar());
        styleScrollBar(sp.getHorizontalScrollBar());
        return sp;
    }

    private static void styleScrollBar(JScrollBar bar) {
        bar.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = BORDER_ACCENT;
                trackColor = BG_CARD;
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroButton(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroButton(); }
            JButton zeroButton() {
                JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b;
            }
            @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);
                g2.fillRoundRect(r.x+2, r.y+2, r.width-4, r.height-4, 6, 6);
                g2.dispose();
            }
        });
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(6, 6));
    }

    // ─── Badge component ───────────────────────────────────────────────
    public static JLabel badge(String text, Color bg, Color fg) {
        JLabel lbl = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(fg);
        lbl.setOpaque(false);
        lbl.setBorder(new EmptyBorder(2, 8, 2, 8));
        return lbl;
    }

    // ─── Separator ────────────────────────────────────────────────────
    public static JSeparator separator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_DEFAULT);
        sep.setBackground(BG_DARK);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    // ─── Inner class: Rounded Border ──────────────────────────────────
    public static class RoundedBorder implements Border {
        private final int radius;
        private final Color color;
        public RoundedBorder(int r, Color c) { this.radius = r; this.color = c; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(0.8f));
            g2.drawRoundRect(x, y, w-1, h-1, radius, radius);
            g2.dispose();
        }
        @Override public boolean isBorderOpaque() { return false; }
        @Override public Insets getBorderInsets(Component c) { return new Insets(1,1,1,1); }
    }

    // ─── Mini Chart Panel ─────────────────────────────────────────────
    public static JPanel sparkline(int[] values, Color accent) {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (values == null || values.length < 2) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int n = values.length;
                int max = 1;
                for (int v : values) max = Math.max(max, v);
                int w = getWidth(), h = getHeight();
                float dx = (float)(w-4) / (n-1);

                // Fill area
                GeneralPath area = new GeneralPath();
                area.moveTo(2, h);
                for (int i = 0; i < n; i++) {
                    float x = 2 + i * dx;
                    float y = h - 2 - (float) values[i] / max * (h - 8);
                    if (i == 0) area.lineTo(x, y); else area.lineTo(x, y);
                }
                area.lineTo(2 + (n-1)*dx, h);
                area.closePath();
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 35));
                g2.fill(area);

                // Line
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                GeneralPath line = new GeneralPath();
                for (int i = 0; i < n; i++) {
                    float x = 2 + i * dx;
                    float y = h - 2 - (float) values[i] / max * (h - 8);
                    if (i == 0) line.moveTo(x, y); else line.lineTo(x, y);
                }
                g2.draw(line);
                g2.dispose();
            }
            { setOpaque(false); }
        };
    }

    // ─── Bar Chart Panel ──────────────────────────────────────────────
    public static JPanel barChart(String[] labels, int[] values, Color accent) {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (values == null || values.length == 0) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int n = values.length;
                int max = 1;
                for (int v : values) max = Math.max(max, v);
                int w = getWidth(), h = getHeight();
                int bottomPad = 22, topPad = 10;
                int chartH = h - bottomPad - topPad;
                float barW = (float)(w - 20) / n;
                float gap  = barW * 0.25f;
                float bw   = barW - gap;

                for (int i = 0; i < n; i++) {
                    float x = 10 + i * barW + gap/2;
                    float bh = (float) values[i] / max * chartH;
                    float y  = topPad + chartH - bh;

                    // Bar fill with slight gradient effect via opacity layering
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 40));
                    g2.fillRoundRect((int)x, topPad, (int)bw, chartH, 6, 6);
                    g2.setColor(accent);
                    g2.fillRoundRect((int)x, (int)y, (int)bw, (int)bh, 6, 6);

                    // Label
                    if (labels != null && i < labels.length) {
                        g2.setFont(FONT_LABEL);
                        g2.setColor(TEXT_MUTED);
                        FontMetrics fm = g2.getFontMetrics();
                        String lbl = labels[i];
                        int lx = (int)(x + bw/2 - fm.stringWidth(lbl)/2);
                        g2.drawString(lbl, lx, h - 5);
                    }
                }
                g2.dispose();
            }
            { setOpaque(false); }
        };
    }

    // ─── Donut Chart ──────────────────────────────────────────────────
    public static JPanel donutChart(int[] values, Color[] colors, String centerText) {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int sz = Math.min(getWidth(), getHeight()) - 8;
                int x = (getWidth() - sz) / 2;
                int y = (getHeight() - sz) / 2;
                int total = 0; for (int v : values) total += v;
                if (total == 0) { g2.dispose(); return; }
                double start = -90;
                for (int i = 0; i < values.length; i++) {
                    double arc = 360.0 * values[i] / total;
                    g2.setColor(colors[i % colors.length]);
                    g2.setStroke(new BasicStroke(sz * 0.18f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
                    g2.drawArc(x + (int)(sz*0.09f), y + (int)(sz*0.09f),
                               (int)(sz*0.82f), (int)(sz*0.82f),
                               (int)start, (int)-arc);
                    start -= arc;
                }
                // Center text
                g2.setFont(new Font("Segoe UI", Font.BOLD, sz/5));
                g2.setColor(TEXT_PRIMARY);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(centerText, getWidth()/2 - fm.stringWidth(centerText)/2,
                              getHeight()/2 + fm.getAscent()/2 - 2);
                g2.dispose();
            }
            { setOpaque(false); }
        };
    }
}
