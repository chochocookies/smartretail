package com.app.smartretail.view.component;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Icon;

import com.app.smartretail.utils.UITheme;

/**
 * Icons — SF Symbols-style line icons painted with Java2D.
 * Every icon is drawn with strokes so they scale cleanly and always render.
 */
public class Icons {

    @FunctionalInterface public interface Painter {
        void paint(Graphics2D g, int cx, int cy, int r, Color c);
    }

    // ── Navigation ────────────────────────────────────────────────
    public static final Icon DASHBOARD  = line(Icons::paintDashboard);
    public static final Icon POS        = line(Icons::paintPos);
    public static final Icon BOX        = line(Icons::paintBox);
    public static final Icon WAREHOUSE  = line(Icons::paintWarehouse);
    public static final Icon TAG        = line(Icons::paintTag);
    public static final Icon FOLDER     = line(Icons::paintFolder);
    public static final Icon TRUCK      = line(Icons::paintTruck);
    public static final Icon USERS      = line(Icons::paintUsers);
    public static final Icon REPORT     = line(Icons::paintReport);
    public static final Icon CHART      = line(Icons::paintChart);
    public static final Icon SETTINGS   = line(Icons::paintSettings);
    public static final Icon HELP       = line(Icons::paintHelp);
    public static final Icon PAYROLL    = line(Icons::paintPayroll);

    // ── Actions ───────────────────────────────────────────────────
    public static final Icon PLUS       = action(Icons::paintPlus);
    public static final Icon EDIT       = action(Icons::paintEdit);
    public static final Icon DELETE     = action(Icons::paintDelete);
    public static final Icon SEARCH     = action(Icons::paintSearch);
    public static final Icon REFRESH    = action(Icons::paintRefresh);
    public static final Icon SAVE       = action(Icons::paintSave);
    public static final Icon LOGOUT     = action(Icons::paintLogout);
    public static final Icon EXPORT     = action(Icons::paintExport);
    public static final Icon WARNING    = action(Icons::paintWarning);
    public static final Icon BELL       = action(Icons::paintBell);
    public static final Icon MOON       = action(Icons::paintMoon);
    public static final Icon CALENDAR   = action(Icons::paintCalendar);
    public static final Icon CLOSE      = action(Icons::paintClose);

    // ── Logo / Avatar builders ────────────────────────────────────
    public static Icon logoIcon(String text, Color bg, int sz) {
        return new Icon() {
            public int getIconWidth()  { return sz; }
            public int getIconHeight() { return sz; }
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = setup(g);
                g2.setColor(bg);
                g2.fillRoundRect(x, y, sz, sz, sz/4, sz/4);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, sz*9/20));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text, x + sz/2 - fm.stringWidth(text)/2,
                              y + sz/2 + fm.getAscent()/2 - 2);
                g2.dispose();
            }
        };
    }

    public static Icon avatarIcon(String initials, Color bg, int sz) {
        return new Icon() {
            public int getIconWidth()  { return sz; }
            public int getIconHeight() { return sz; }
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = setup(g);
                g2.setColor(bg);
                g2.fillOval(x, y, sz, sz);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, sz*2/5));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initials, x + sz/2 - fm.stringWidth(initials)/2,
                              y + sz/2 + fm.getAscent()/2 - 2);
                g2.dispose();
            }
        };
    }

    public static Icon dot(Color c) {
        return new Icon() {
            public int getIconWidth()  { return 8; }
            public int getIconHeight() { return 8; }
            public void paintIcon(Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = setup(g);
                g2.setColor(c);
                g2.fillOval(x+1, y+1, 6, 6);
                g2.dispose();
            }
        };
    }

    public static Icon coloredLine(Painter p, Color c, int sz) {
        return new Icon() {
            public int getIconWidth()  { return sz; }
            public int getIconHeight() { return sz; }
            public void paintIcon(Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = setup(g);
                g2.translate(x, y);
                p.paint(g2, sz/2, sz/2, sz/2-2, c);
                g2.dispose();
            }
        };
    }

    // ── Paint methods ─────────────────────────────────────────────
    // Package-private (no modifier) so they can be used as method
    // references (Icons::paintXxx) from classes in the same package,
    // e.g. SidebarPanel and DashboardForm.

    public static void paintDashboard(Graphics2D g, int cx, int cy, int r, Color c) {
        g.setColor(c); g.setStroke(s(1.4f));
        int h = r-2, q = h/2-1;
        g.drawRoundRect(cx-h, cy-h, q*2, q*2, 3, 3);
        g.drawRoundRect(cx+1, cy-h, (h-q)*2, q*2, 3, 3);
        g.drawRoundRect(cx-h, cy+1, q*2, (h-q)*2, 3, 3);
        g.drawRoundRect(cx+1, cy+1, (h-q)*2, (h-q)*2, 3, 3);
    }

    public static void paintPos(Graphics2D g, int cx, int cy, int r, Color c) {
        g.setColor(c); g.setStroke(s(1.4f));
        g.drawRoundRect(cx-r+2, cy-r+4, r*2-4, r*2-6, 4, 4);
        g.drawLine(cx-r+4, cy-r+8, cx+r-4, cy-r+8);
        g.drawRoundRect(cx-3, cy-r+11, 6, 4, 2, 2);
        g.drawLine(cx-r+4, cy, cx-r+4, cy+r-4);
        g.drawLine(cx, cy, cx, cy+r-4);
        g.drawLine(cx+r-4, cy, cx+r-4, cy+r-4);
    }

    public static void paintBox(Graphics2D g, int cx, int cy, int r, Color c) {
        g.setColor(c); g.setStroke(s(1.4f));
        int[] px = {cx, cx+r-2, cx+r-2, cx, cx-(r-2), cx-(r-2)};
        int[] py = {cy-(r-2), cy-(r-2)/2, cy+r-2, cy+r-2, cy+r-2, cy-(r-2)/2};
        g.drawPolygon(px, py, 6);
        g.drawLine(cx, cy-(r-2), cx, cy+r-2);
    }

    public static void paintWarehouse(Graphics2D g, int cx, int cy, int r, Color c) {
        g.setColor(c); g.setStroke(s(1.4f));
        int[] px = {cx-(r-1), cx, cx+(r-1), cx+(r-1), cx-(r-1)};
        int[] py = {cy, cy-(r-1), cy, cy+r-1, cy+r-1};
        g.drawPolygon(px, py, 5);
        g.drawRoundRect(cx-3, cy+1, 6, r-2, 2, 2);
    }

    public static void paintTag(Graphics2D g, int cx, int cy, int r, Color c) {
        g.setColor(c); g.setStroke(s(1.4f));
        int[] px = {cx-(r-2), cx-(r-2), cx, cx+(r-2)};
        int[] py = {cy-(r/2), cy+(r/2), cy+(r-2), cy};
        g.drawPolygon(px, py, 4);
        g.fillOval(cx-r+3, cy-r+3, 4, 4);
    }

    public static void paintFolder(Graphics2D g, int cx, int cy, int r, Color c) {
        g.setColor(c); g.setStroke(s(1.4f));
        g.drawRoundRect(cx-r+2, cy-r/2+2, r*2-4, r-2, 4, 4);
        g.drawLine(cx-r+2, cy-r/2+2+2, cx-r+2+5, cy-r/2+2+2);
        g.drawLine(cx-r+2+5, cy-r/2+2+2, cx-r+2+8, cy-r/2-2);
    }

    public static void paintTruck(Graphics2D g, int cx, int cy, int r, Color c) {
        g.setColor(c); g.setStroke(s(1.4f));
        g.drawRoundRect(cx-r+2, cy-r/2, r, r-2, 3, 3);
        int[] px = {cx+2, cx+2, cx+r-2, cx+r-2};
        int[] py = {cy-r/2, cy+r/2-2, cy, cy-r/2};
        g.drawPolyline(px, py, 4);
        g.fillOval(cx-r+4, cy+r/2-3, 5, 5);
        g.fillOval(cx+r-7, cy+r/2-3, 5, 5);
    }

    public static void paintUsers(Graphics2D g, int cx, int cy, int r, Color c) {
        g.setColor(c); g.setStroke(s(1.4f));
        g.drawOval(cx-r/2-1, cy-r+1, r-2, r-3);
        g.drawArc(cx-r+1, cy+1, r*2-2, r-2, 0, 180);
        g.drawOval(cx+r/4, cy-r+3, r/2, r/2);
        g.drawArc(cx+1, cy+1, r-2, r/2, 0, 180);
    }

    public static void paintReport(Graphics2D g, int cx, int cy, int r, Color c) {
        g.setColor(c); g.setStroke(s(1.4f));
        g.drawRoundRect(cx-r+3, cy-r+2, r*2-6, r*2-4, 4, 4);
        g.drawLine(cx-r+6, cy-r+7, cx+r-4, cy-r+7);
        g.drawLine(cx-r+6, cy, cx+r-4, cy);
        g.drawLine(cx-r+6, cy+r-5, cx+2, cy+r-5);
    }

    public static void paintChart(Graphics2D g, int cx, int cy, int r, Color c) {
        g.setColor(c); g.setStroke(s(1.5f));
        g.drawLine(cx-r+2, cy+r-2, cx-r+2, cy-r+2);
        g.drawLine(cx-r+2, cy+r-2, cx+r-2, cy+r-2);
        int[] px = {cx-r+5, cx-r+5+5, cx, cx+5, cx+r-3};
        int[] py = {cy+3, cy-r+6, cy-r/2, cy+3, cy-r+10};
        g.drawPolyline(px, py, 5);
    }

    public static void paintSettings(Graphics2D g, int cx, int cy, int r, Color c) {
        g.setColor(c); g.setStroke(s(1.4f));
        g.drawOval(cx-r/3, cy-r/3, r/3*2, r/3*2);
        for (int i = 0; i < 8; i++) {
            double a = Math.PI * i / 4;
            int x1 = (int)(cx + (r/2) * Math.cos(a)), y1 = (int)(cy + (r/2) * Math.sin(a));
            int x2 = (int)(cx + (r-2) * Math.cos(a)), y2 = (int)(cy + (r-2) * Math.sin(a));
            g.drawLine(x1, y1, x2, y2);
        }
    }

    public static void paintHelp(Graphics2D g, int cx, int cy, int r, Color c) {
        g.setColor(c); g.setStroke(s(1.4f));
        g.drawOval(cx-r+2, cy-r+2, r*2-4, r*2-4);
        g.drawArc(cx-r/2, cy-r/2, r, r, 180, 180);
        g.drawLine(cx, cy, cx, cy+r/3);
        g.fillOval(cx-2, cy+r/2-2, 4, 4);
    }

    public static void paintPayroll(Graphics2D g, int cx, int cy, int r, Color c) {
        g.setColor(c); g.setStroke(s(1.4f));
        g.drawRoundRect(cx-r+3, cy-r+2, r*2-6, r*2-4, 4, 4);
        g.drawLine(cx-4, cy-r+6, cx+4, cy-r+6);
        g.drawLine(cx-r/2, cy-2, cx+r/2, cy-2);
        g.drawLine(cx-r/2, cy+3, cx+r/2, cy+3);
        g.drawLine(cx, cy-r+2, cx, cy+r-2);
    }

    public static void paintPlus(Graphics2D g, int cx, int cy, int r, Color c) {
        g.setColor(c); g.setStroke(s(2f));
        g.drawLine(cx, cy-r+2, cx, cy+r-2);
        g.drawLine(cx-r+2, cy, cx+r-2, cy);
    }

    public static void paintEdit(Graphics2D g, int cx, int cy, int r, Color c) {
        g.setColor(c); g.setStroke(s(1.4f));
        int[] px = {cx-r+3, cx+r-5, cx+r-2, cx-r+6};
        int[] py = {cy+r-3, cy-r+3, cy-r+6, cy+r};
        g.drawPolygon(px, py, 4);
        g.drawLine(cx+r-5, cy-r+3, cx+r-2, cy-r+6);
        g.drawLine(cx-r+3, cy+r, cx-r+3, cy+r-3);
    }

    public static void paintDelete(Graphics2D g, int cx, int cy, int r, Color c) {
        g.setColor(c); g.setStroke(s(1.4f));
        g.drawLine(cx-r+2, cy-r+4, cx+r-2, cy-r+4);
        g.drawLine(cx-r/3, cy-r+2, cx-r/3, cy-r+4);
        g.drawLine(cx+r/3, cy-r+2, cx+r/3, cy-r+4);
        g.drawLine(cx-r/3, cy-r+2, cx+r/3, cy-r+2);
        g.drawRoundRect(cx-r+4, cy-r+4, r*2-8, r*2-6, 3, 3);
        g.drawLine(cx-r/3, cy-r+8, cx-r/3, cy+r-4);
        g.drawLine(cx+r/3, cy-r+8, cx+r/3, cy+r-4);
    }

    public static void paintSearch(Graphics2D g, int cx, int cy, int r, Color c) {
        g.setColor(c); g.setStroke(s(1.6f));
        g.drawOval(cx-r+2, cy-r+2, r-2, r-2);
        int d = (int)((r-2) * Math.cos(Math.PI/4));
        g.drawLine(cx-r+2+d, cy-r+2+d, cx+r-2, cy+r-2);
    }

    public static void paintRefresh(Graphics2D g, int cx, int cy, int r, Color c) {
        g.setColor(c); g.setStroke(s(1.5f));
        g.drawArc(cx-r+3, cy-r+3, r*2-6, r*2-6, 40, 280);
        int x = (int)(cx + (r-3) * Math.cos(Math.toRadians(40)));
        int y = (int)(cy - (r-3) * Math.sin(Math.toRadians(40)));
        g.drawLine(x, y-3, x+4, y);
        g.drawLine(x, y-3, x-3, y-3);
    }

    public static void paintSave(Graphics2D g, int cx, int cy, int r, Color c) {
        g.setColor(c); g.setStroke(s(1.4f));
        g.drawRoundRect(cx-r+3, cy-r+2, r*2-6, r*2-4, 3, 3);
        g.drawRoundRect(cx-r/2, cy-r+2, r, r/2, 0, 0);
        g.drawRoundRect(cx-r/2+2, cy+2, r-4, r-4, 2, 2);
    }

    public static void paintLogout(Graphics2D g, int cx, int cy, int r, Color c) {
        g.setColor(c); g.setStroke(s(1.5f));
        g.drawLine(cx, cy-r/2, cx+r-3, cy);
        g.drawLine(cx, cy+r/2, cx+r-3, cy);
        g.drawLine(cx-r+3, cy-r+3, cx-r+3, cy+r-3);
        g.drawLine(cx-r+3, cy-r+3, cx+2, cy-r+3);
        g.drawLine(cx-r+3, cy+r-3, cx+2, cy+r-3);
        g.drawLine(cx-2, cy-r/2+2, cx+r-3, cy);
        g.drawLine(cx-2, cy+r/2-2, cx+r-3, cy);
    }

    public static void paintExport(Graphics2D g, int cx, int cy, int r, Color c) {
        g.setColor(c); g.setStroke(s(1.5f));
        g.drawLine(cx, cy-r+2, cx, cy+r/2);
        g.drawLine(cx-r/2, cy-r/2+2, cx, cy-r+2);
        g.drawLine(cx+r/2, cy-r/2+2, cx, cy-r+2);
        g.drawLine(cx-r+3, cy+r/2, cx-r+3, cy+r-2);
        g.drawLine(cx+r-3, cy+r/2, cx+r-3, cy+r-2);
        g.drawLine(cx-r+3, cy+r-2, cx+r-3, cy+r-2);
    }

    public static void paintWarning(Graphics2D g, int cx, int cy, int r, Color c) {
        g.setColor(c); g.setStroke(s(1.4f));
        int[] px = {cx, cx+r-2, cx-r+2};
        int[] py = {cy-r+2, cy+r-2, cy+r-2};
        g.drawPolygon(px, py, 3);
        g.drawLine(cx, cy-r/2+2, cx, cy+2);
        g.fillOval(cx-2, cy+r/2-2, 4, 4);
    }

    public static void paintBell(Graphics2D g, int cx, int cy, int r, Color c) {
        g.setColor(c); g.setStroke(s(1.4f));
        g.drawArc(cx-r+4, cy-r+2, r*2-8, r*2-6, 0, 180);
        g.drawLine(cx-r+4, cy, cx-r+4, cy+r-4);
        g.drawLine(cx+r-4, cy, cx+r-4, cy+r-4);
        g.drawLine(cx-r+4, cy+r-4, cx+r-4, cy+r-4);
        g.drawArc(cx-3, cy+r-6, 6, 5, 180, 180);
    }

    public static void paintMoon(Graphics2D g, int cx, int cy, int r, Color c) {
        g.setColor(c); g.setStroke(s(1.4f));
        g.drawArc(cx-r/4, cy-r+3, r*2-6, r*2-6, 90, 200);
        g.drawArc(cx-r+3, cy-r+3, r*2-6, r*2-6, 270, 200);
    }

    public static void paintCalendar(Graphics2D g, int cx, int cy, int r, Color c) {
        g.setColor(c); g.setStroke(s(1.4f));
        g.drawRoundRect(cx-r+3, cy-r+4, r*2-6, r*2-4, 4, 4);
        g.drawLine(cx-r+3, cy-r+9, cx+r-3, cy-r+9);
        g.drawLine(cx-r/2, cy-r+2, cx-r/2, cy-r+6);
        g.drawLine(cx+r/2, cy-r+2, cx+r/2, cy-r+6);
        g.fillOval(cx-r+6, cy-r+12, 3, 3);
        g.fillOval(cx-1, cy-r+12, 3, 3);
        g.fillOval(cx+r-8, cy-r+12, 3, 3);
        g.fillOval(cx-r+6, cy, 3, 3);
        g.fillOval(cx-1, cy, 3, 3);
    }

    public static void paintClose(Graphics2D g, int cx, int cy, int r, Color c) {
        g.setColor(c); g.setStroke(s(1.8f));
        g.drawLine(cx-r+3, cy-r+3, cx+r-3, cy+r-3);
        g.drawLine(cx+r-3, cy-r+3, cx-r+3, cy+r-3);
    }

    // ── Internal helpers ──────────────────────────────────────────
    private static BasicStroke s(float w) {
        return new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    }

    private static Graphics2D setup(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        return g2;
    }

    private static Icon line(Painter p)   { return make(p, 18, UITheme.TEXT_SECONDARY); }
    private static Icon action(Painter p) { return make(p, 16, UITheme.TEXT_SECONDARY); }

    private static Icon make(Painter p, int sz, Color c) {
        return new Icon() {
            public int getIconWidth()  { return sz; }
            public int getIconHeight() { return sz; }
            public void paintIcon(Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = setup(g);
                g2.translate(x, y);
                p.paint(g2, sz/2, sz/2, sz/2-1, c);
                g2.dispose();
            }
        };
    }

    /**
     * Returns a tinted icon from a Painter method reference.
     * Example: Icons.tinted(Icons::paintLogout, 18, UITheme.ACCENT_CORAL)
     */
    public static Icon tinted(Painter p, int sz, Color c) {
        return make(p, sz, c);
    }
}