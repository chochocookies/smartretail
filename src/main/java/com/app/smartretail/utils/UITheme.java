package com.app.smartretail.utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.JTableHeader;

/**
 * UITheme — Starline-inspired design system.
 * Light/neutral palette, rounded pill sidebar, lime-yellow active accent.
 */
public class UITheme {

    // ── Color Palette ─────────────────────────────────────────────
    // Backgrounds
    public static final Color BG_PAGE       = new Color(240, 241, 245);   // outer page bg
    public static final Color BG_SURFACE    = new Color(248, 249, 252);   // main content bg
    public static final Color BG_CARD       = new Color(255, 255, 255);   // white card
    public static final Color BG_SIDEBAR    = new Color(255, 255, 255);   // sidebar white
    public static final Color BG_INPUT      = new Color(245, 246, 250);   // input bg
    public static final Color BG_ROW_ALT    = new Color(250, 251, 254);   // table alt row
    public static final Color BG_HOVER      = new Color(243, 244, 248);   // hover state

    // Accent — lime yellow (active nav, highlights)
    public static final Color ACCENT_LIME   = new Color(212, 241, 73);    // #D4F149 active
    public static final Color ACCENT_BLUE   = new Color(99, 102, 241);    // indigo-500
    public static final Color ACCENT_PURPLE = new Color(139, 92, 246);    // violet
    public static final Color ACCENT_TEAL   = new Color(20, 184, 166);    // teal-500
    public static final Color ACCENT_AMBER  = new Color(245, 158, 11);    // amber-500
    public static final Color ACCENT_CORAL  = new Color(239, 68, 68);     // red-500
    public static final Color ACCENT_GREEN  = new Color(34, 197, 94);     // green-500
    public static final Color ACCENT_ORANGE = new Color(249, 115, 22);    // orange

    // Card accent backgrounds (pastel tints)
    public static final Color CARD_AMBER_BG  = new Color(254, 243, 199);
    public static final Color CARD_PURPLE_BG = new Color(237, 233, 254);
    public static final Color CARD_TEAL_BG   = new Color(204, 251, 241);
    public static final Color CARD_BLUE_BG   = new Color(219, 234, 254);

    // Text
    public static final Color TEXT_PRIMARY   = new Color(17, 24, 39);     // gray-900
    public static final Color TEXT_SECONDARY = new Color(107, 114, 128);  // gray-500
    public static final Color TEXT_MUTED     = new Color(156, 163, 175);  // gray-400
    public static final Color TEXT_ON_LIME   = new Color(30, 41, 59);     // dark on lime
    public static final Color TEXT_WHITE     = Color.WHITE;

    // Borders
    public static final Color BORDER_DEFAULT = new Color(229, 231, 235);  // gray-200
    public static final Color BORDER_FOCUS   = ACCENT_BLUE;

    // ── Fonts ─────────────────────────────────────────────────────
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_H2      = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_H3      = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO    = new Font("Consolas", Font.PLAIN, 12);
    public static final Font FONT_NUM     = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_LABEL   = new Font("Segoe UI", Font.PLAIN, 10);
    public static final Font FONT_NAV     = new Font("Segoe UI", Font.PLAIN, 13);

    // ── Global LAF ────────────────────────────────────────────────
    public static void apply() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch(Exception ignored){}

        UIManager.put("Panel.background",              BG_PAGE);
        UIManager.put("Table.background",              BG_CARD);
        UIManager.put("Table.foreground",              TEXT_PRIMARY);
        UIManager.put("Table.gridColor",               BORDER_DEFAULT);
        UIManager.put("Table.selectionBackground",     new Color(238, 242, 255));
        UIManager.put("Table.selectionForeground",     TEXT_PRIMARY);
        UIManager.put("Table.alternateRowColor",       BG_ROW_ALT);
        UIManager.put("TableHeader.background",        new Color(249, 250, 251));
        UIManager.put("TableHeader.foreground",        TEXT_SECONDARY);
        UIManager.put("TableHeader.font",              FONT_LABEL);
        UIManager.put("ScrollPane.background",         BG_CARD);
        UIManager.put("Viewport.background",           BG_CARD);
        UIManager.put("TextField.background",          BG_INPUT);
        UIManager.put("TextField.foreground",          TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground",     ACCENT_BLUE);
        UIManager.put("PasswordField.background",      BG_INPUT);
        UIManager.put("PasswordField.foreground",      TEXT_PRIMARY);
        UIManager.put("PasswordField.caretForeground", ACCENT_BLUE);
        UIManager.put("ComboBox.background",           BG_INPUT);
        UIManager.put("ComboBox.foreground",           TEXT_PRIMARY);
        UIManager.put("ComboBox.selectionBackground",  new Color(238, 242, 255));
        UIManager.put("ComboBox.selectionForeground",  TEXT_PRIMARY);
        UIManager.put("Label.foreground",              TEXT_PRIMARY);
        UIManager.put("CheckBox.background",           BG_CARD);
        UIManager.put("CheckBox.foreground",           TEXT_PRIMARY);
        UIManager.put("Button.background",             BG_INPUT);
        UIManager.put("Button.foreground",             TEXT_PRIMARY);
        UIManager.put("List.background",               BG_CARD);
        UIManager.put("List.foreground",               TEXT_PRIMARY);
        UIManager.put("List.selectionBackground",      new Color(238,242,255));
        UIManager.put("TextArea.background",           BG_INPUT);
        UIManager.put("TextArea.foreground",           TEXT_PRIMARY);
        UIManager.put("TextArea.caretForeground",      ACCENT_BLUE);
        UIManager.put("PopupMenu.background",          BG_CARD);
        UIManager.put("MenuItem.background",           BG_CARD);
        UIManager.put("MenuItem.foreground",           TEXT_PRIMARY);
        UIManager.put("MenuItem.selectionBackground",  BG_HOVER);
        UIManager.put("ScrollBar.background",          BG_SURFACE);
        UIManager.put("ToolTip.background",            new Color(17,24,39));
        UIManager.put("ToolTip.foreground",            Color.WHITE);
        UIManager.put("SplitPane.background",          BG_PAGE);
        UIManager.put("SplitPane.dividerSize",         1);
    }

    // ── Buttons ───────────────────────────────────────────────────
    public static JButton primaryButton(String text, Color accent) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed()  ? accent.darker()
                         : getModel().isRollover() ? accent.brighter()
                         : accent;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        Color fg = (accent == ACCENT_LIME) ? TEXT_ON_LIME : TEXT_WHITE;
        btn.setFont(FONT_H3); btn.setForeground(fg);
        btn.setOpaque(false); btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Dimension d = btn.getPreferredSize();
        btn.setPreferredSize(new Dimension(Math.max(d.width, 80) + 16, 34));
        return btn;
    }

    public static JButton ghostButton(String text, Color accent) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover() || getModel().isPressed()) {
                    g2.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),18));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_H3); btn.setForeground(accent);
        btn.setOpaque(false); btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Dimension d = btn.getPreferredSize();
        btn.setPreferredSize(new Dimension(Math.max(d.width, 80) + 16, 34));
        return btn;
    }

    public static JButton dangerButton(String text) { return ghostButton(text, ACCENT_CORAL); }

    // ── Pill button (nav active style) ────────────────────────────
    public static JButton pillButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(BG_HOVER);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BODY); btn.setForeground(TEXT_SECONDARY);
        btn.setOpaque(false); btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Fields ────────────────────────────────────────────────────
    public static JTextField styledField(String placeholder) {
        JTextField f = new JTextField();
        f.setFont(FONT_BODY); f.setForeground(TEXT_PRIMARY);
        f.setBackground(BG_INPUT); f.setCaretColor(ACCENT_BLUE);
        f.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(8, BORDER_DEFAULT, null),
            new EmptyBorder(6, 12, 6, 12)));
        if (placeholder != null && !placeholder.isEmpty())
            f.putClientProperty("JTextField.placeholderText", placeholder);
        return f;
    }

    public static JPasswordField styledPassword() {
        JPasswordField f = new JPasswordField();
        f.setFont(FONT_BODY); f.setForeground(TEXT_PRIMARY);
        f.setBackground(BG_INPUT); f.setCaretColor(ACCENT_BLUE);
        f.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(8, BORDER_DEFAULT, null),
            new EmptyBorder(6, 12, 6, 12)));
        return f;
    }

    public static JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setFont(FONT_BODY); c.setBackground(BG_INPUT); c.setForeground(TEXT_PRIMARY);
        c.setBorder(new RoundedBorder(8, BORDER_DEFAULT, null));
        c.setPreferredSize(new Dimension(c.getPreferredSize().width, 34));
        return c;
    }

    // ── Labels ────────────────────────────────────────────────────
    public static JLabel pageTitle(String text) {
        JLabel l = new JLabel(text); l.setFont(FONT_TITLE); l.setForeground(TEXT_PRIMARY); return l;
    }
    public static JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text); l.setFont(FONT_SMALL); l.setForeground(TEXT_SECONDARY); return l;
    }
    public static JLabel badge(String text, Color bg, Color fg) {
        JLabel l = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(bg.getRed(),bg.getGreen(),bg.getBlue(),35));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),6,6);
                g2.dispose(); super.paintComponent(g);
            }
        };
        l.setFont(new Font("Segoe UI",Font.BOLD,10)); l.setForeground(fg);
        l.setOpaque(false); l.setBorder(new EmptyBorder(3,9,3,9)); return l;
    }

    // ── Cards ─────────────────────────────────────────────────────
    public static JPanel card() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),16,16);
                g2.setColor(BORDER_DEFAULT);
                g2.setStroke(new BasicStroke(0.7f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,16,16);
                g2.dispose();
            }
        };
        p.setOpaque(false); p.setBorder(new EmptyBorder(16,18,16,18)); return p;
    }

    /** Colored tinted metric card (pastel background) */
    public static JPanel tintCard(Color tint) {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(tint);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),16,16);
                g2.dispose();
            }
        };
        p.setOpaque(false); p.setBorder(new EmptyBorder(16,18,16,18)); return p;
    }

    // ── Table ─────────────────────────────────────────────────────
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

    // ── ScrollPane ────────────────────────────────────────────────
    public static JScrollPane styledScroll(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBackground(BG_CARD); sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(BG_CARD);
        thinScrollBar(sp.getVerticalScrollBar());
        thinScrollBar(sp.getHorizontalScrollBar());
        return sp;
    }

    private static void thinScrollBar(JScrollBar bar) {
        bar.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = new Color(200,205,215); trackColor = BG_SURFACE;
            }
            @Override protected JButton createDecreaseButton(int o) { return zero(); }
            @Override protected JButton createIncreaseButton(int o) { return zero(); }
            JButton zero(){ JButton b=new JButton(); b.setPreferredSize(new Dimension(0,0)); return b; }
            @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);
                g2.fillRoundRect(r.x+2,r.y+2,r.width-4,r.height-4,6,6);
                g2.dispose();
            }
        });
        bar.setOpaque(false); bar.setPreferredSize(new Dimension(5,5));
    }

    // ── Charts ────────────────────────────────────────────────────
    public static JPanel sparkline(int[] values, Color accent) {
        return new JPanel() {
            { setOpaque(false); }
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if(values==null||values.length<2) return;
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                int n=values.length,W=getWidth(),H=getHeight();
                int max=1,min=Integer.MAX_VALUE;
                for(int v:values){max=Math.max(max,v);min=Math.min(min,v);}
                if(max==min)max=min+1;
                float dx=(float)(W-4)/(n-1);
                GeneralPath area=new GeneralPath();
                area.moveTo(2,H);
                for(int i=0;i<n;i++){float x=2+i*dx;float y=H-4-(float)(values[i]-min)/(max-min)*(H-8);if(i==0)area.lineTo(x,y);else area.lineTo(x,y);}
                area.lineTo(2+(n-1)*dx,H); area.closePath();
                g2.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),30)); g2.fill(area);
                GeneralPath line=new GeneralPath();
                for(int i=0;i<n;i++){float x=2+i*dx;float y=H-4-(float)(values[i]-min)/(max-min)*(H-8);if(i==0)line.moveTo(x,y);else line.lineTo(x,y);}
                g2.setColor(accent); g2.setStroke(new BasicStroke(2f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND)); g2.draw(line);
                g2.dispose();
            }
        };
    }

    public static JPanel barChart(String[] labels, int[] values, Color accent) {
        return new JPanel() {
            { setOpaque(false); }
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if(values==null||values.length==0) return;
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                int n=values.length,W=getWidth(),H=getHeight();
                int bp=22,tp=10,max=1; for(int v:values)max=Math.max(max,v);
                int ch=H-bp-tp;
                float bw=(float)(W-20)/n,gap=bw*.28f,bwidth=bw-gap;
                for(int i=0;i<n;i++){
                    float x=10+i*bw+gap/2;
                    float bh=(float)values[i]/max*ch;
                    float y=tp+ch-bh;
                    g2.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),22));
                    g2.fillRoundRect((int)x,tp,(int)bwidth,ch,6,6);
                    g2.setColor(accent);
                    g2.fillRoundRect((int)x,(int)y,(int)bwidth,(int)bh,6,6);
                    if(labels!=null&&i<labels.length){
                        g2.setFont(FONT_LABEL); g2.setColor(TEXT_MUTED);
                        FontMetrics fm=g2.getFontMetrics(); String lbl=labels[i];
                        g2.drawString(lbl,(int)(x+bwidth/2-fm.stringWidth(lbl)/2),H-5);
                    }
                }
                g2.dispose();
            }
        };
    }

    public static JPanel donutChart(int[] values, Color[] colors, String center) {
        return new JPanel() {
            { setOpaque(false); }
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                int sz=Math.min(getWidth(),getHeight())-8;
                int x=(getWidth()-sz)/2,y=(getHeight()-sz)/2;
                int total=0; for(int v:values)total+=v;
                if(total==0){g2.dispose();return;}
                double start=-90;
                for(int i=0;i<values.length;i++){
                    double arc=360.0*values[i]/total;
                    g2.setColor(colors[i%colors.length]);
                    g2.setStroke(new BasicStroke(sz*.17f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND));
                    g2.drawArc(x+(int)(sz*.09f),y+(int)(sz*.09f),(int)(sz*.82f),(int)(sz*.82f),(int)start,(int)-arc);
                    start-=arc;
                }
                if(center!=null&&!center.isEmpty()){
                    g2.setFont(new Font("Segoe UI",Font.BOLD,sz/5));
                    g2.setColor(TEXT_PRIMARY);
                    FontMetrics fm=g2.getFontMetrics();
                    g2.drawString(center,getWidth()/2-fm.stringWidth(center)/2,getHeight()/2+fm.getAscent()/2-2);
                }
                g2.dispose();
            }
        };
    }

    // ── Separator ─────────────────────────────────────────────────
    public static JSeparator separator() {
        JSeparator s = new JSeparator();
        s.setForeground(BORDER_DEFAULT); s.setBackground(BG_SURFACE);
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE,1)); return s;
    }

    // ── RoundedBorder ─────────────────────────────────────────────
    public static class RoundedBorder implements Border {
        private final int r; private final Color stroke; private final Color fill;
        public RoundedBorder(int r, Color stroke, Color fill){this.r=r;this.stroke=stroke;this.fill=fill;}
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h){
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            if(fill!=null){g2.setColor(fill);g2.fillRoundRect(x,y,w-1,h-1,r,r);}
            g2.setColor(stroke); g2.setStroke(new BasicStroke(0.8f));
            g2.drawRoundRect(x,y,w-1,h-1,r,r); g2.dispose();
        }
        @Override public boolean isBorderOpaque(){return false;}
        @Override public Insets getBorderInsets(Component c){return new Insets(r/4,r/4,r/4,r/4);}
    }
}
