package com.app.smartretail.view.component;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Icon;

import com.app.smartretail.utils.UITheme;

/**
 * Icons — Custom painted SVG-style icons for SRMS UI.
 * All icons render at 16x16, dark-mode ready.
 */
public class Icons {

    // ── Navigation icons ──────────────────────────────────────────────
    public static final Icon DASHBOARD   = icon((g,c) -> {
        g.setColor(c); g.fillRoundRect(1,1,6,6,2,2); g.fillRoundRect(9,1,6,6,2,2);
        g.fillRoundRect(1,9,6,6,2,2); g.fillRoundRect(9,9,6,6,2,2);
    });

    public static final Icon CART = icon((g,c) -> {
        g.setColor(c);
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(1,1,3,1); g.drawLine(3,1,4,10);
        int[] px = {4,5,11,10}; int[] py = {3,10,10,3};
        g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),60)); g.fillPolygon(px,py,4);
        g.setColor(c); g.drawPolyline(px,py,4);
        g.fillOval(4,12,3,3); g.fillOval(9,12,3,3);
    });

    public static final Icon BOX = icon((g,c) -> {
        g.setColor(c);
        g.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int[] bx = {8,14,14,8,2,2}; int[] by = {2,5,13,16,13,5};
        g.drawPolygon(bx,by,6);
        g.drawLine(8,2,8,16); g.drawLine(2,5,14,5);
        g.drawLine(5,3,11,3);
    });

    public static final Icon WAREHOUSE = icon((g,c) -> {
        g.setColor(c);
        g.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int[] rx={1,8,15,15,1}; int[] ry={8,1,8,15,15};
        g.drawPolygon(rx,ry,5);
        g.fillRoundRect(5,10,6,5,2,2);
    });

    public static final Icon TAG = icon((g,c) -> {
        g.setColor(c);
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int[] px={1,1,9,15,9}; int[] py={1,9,15,8,1};
        g.drawPolygon(px,py,5);
        g.fillOval(4,4,3,3);
        g.drawLine(9,15,15,9);
    });

    public static final Icon FOLDER = icon((g,c) -> {
        g.setColor(c);
        g.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int[] px={1,1,15,15,6,1}; int[] py={5,14,14,5,5,5};
        g.drawPolygon(px,py,6);
        g.drawLine(1,5,6,5); g.drawLine(6,5,8,2); g.drawLine(8,2,13,2); g.drawLine(13,2,13,5);
    });

    public static final Icon TRUCK = icon((g,c) -> {
        g.setColor(c);
        g.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawRoundRect(1,4,9,8,2,2);
        int[] px={10,10,15,15}; int[] py={6,12,12,8};
        g.drawPolyline(px,py,4);
        g.fillOval(2,11,3,3); g.fillOval(10,11,3,3);
    });

    public static final Icon USERS = icon((g,c) -> {
        g.setColor(c);
        g.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawOval(5,1,6,5); g.drawArc(1,9,14,8,0,180);
        g.drawOval(11,2,4,4); g.drawArc(12,8,5,5,0,180);
    });

    public static final Icon REPORT = icon((g,c) -> {
        g.setColor(c);
        g.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawRoundRect(2,1,12,14,3,3);
        g.drawLine(5,5,11,5); g.drawLine(5,8,11,8); g.drawLine(5,11,9,11);
        g.fillRoundRect(9,9,6,6,2,2);
        g.setColor(UITheme.BG_CARD); g.drawLine(11,10,13,13); g.drawLine(11,13,13,10);
    });

    public static final Icon CHART = icon((g,c) -> {
        g.setColor(c);
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(2,14,2,2); g.drawLine(2,14,14,14);
        g.drawLine(4,11,6,7); g.drawLine(6,7,9,9); g.drawLine(9,9,13,3);
    });

    public static final Icon USER_SHIELD = icon((g,c) -> {
        g.setColor(c);
        g.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawOval(5,1,6,6);
        g.drawArc(1,9,8,6,0,180);
        int[] px={10,14,12,10,8,6}; int[] py={7,9,14,15,14,9};
        g.drawPolygon(px,py,6);
        g.drawLine(10,9,10,13); g.drawLine(8,11,12,11);
    });

    // ── Action icons ──────────────────────────────────────────────────
    public static final Icon PLUS = actionIcon((g,c) -> {
        g.setColor(c);
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(8,2,8,14); g.drawLine(2,8,14,8);
    });

    public static final Icon EDIT = actionIcon((g,c) -> {
        g.setColor(c);
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int[] px={2,12,14,4}; int[] py={12,2,4,14};
        g.drawPolygon(px,py,4);
        g.drawLine(2,14,1,15); g.drawLine(12,2,14,1);
    });

    public static final Icon DELETE = actionIcon((g,c) -> {
        g.setColor(c);
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(3,4,13,4); g.drawLine(6,4,6,2); g.drawLine(10,4,10,2); g.drawLine(6,2,10,2);
        g.drawRoundRect(4,4,8,10,2,2);
        g.drawLine(6,7,6,11); g.drawLine(10,7,10,11);
    });

    public static final Icon SEARCH = actionIcon((g,c) -> {
        g.setColor(c);
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawOval(2,2,8,8); g.drawLine(9,9,14,14);
    });

    public static final Icon REFRESH = actionIcon((g,c) -> {
        g.setColor(c);
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawArc(2,2,12,12,45,270);
        int[] px={9,12,12}; int[] py={1,1,4}; g.drawPolyline(px,py,3);
    });

    public static final Icon SAVE = actionIcon((g,c) -> {
        g.setColor(c);
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawRoundRect(2,2,12,12,2,2);
        g.drawRect(5,2,6,4); g.fillRect(6,8,4,6);
    });

    public static final Icon EXPORT = actionIcon((g,c) -> {
        g.setColor(c);
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(8,1,8,11); g.drawLine(4,7,8,11); g.drawLine(12,7,8,11);
        g.drawLine(2,13,2,15); g.drawLine(14,13,14,15); g.drawLine(2,15,14,15);
    });

    public static final Icon WARNING = actionIcon((g,c) -> {
        g.setColor(c);
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int[] px={8,15,1}; int[] py={1,14,14};
        g.drawPolygon(px,py,3);
        g.drawLine(8,6,8,10); g.fillOval(7,12,2,2);
    });

    // ── Helpers ───────────────────────────────────────────────────────
    @FunctionalInterface interface PainterFn { void paint(Graphics2D g, Color c); }

    private static Icon icon(PainterFn fn) { return makeIcon(fn, 16, UITheme.TEXT_SECONDARY); }
    private static Icon actionIcon(PainterFn fn) { return makeIcon(fn, 14, UITheme.TEXT_SECONDARY); }

    public static Icon colored(Icon base, Color c) {
        // Re-paint with specific color — wrap original via label snapshot
        return base; // simplified; replace if needed
    }

    private static Icon makeIcon(PainterFn fn, int size, Color defaultColor) {
        return new Icon() {
            public int getIconWidth()  { return size; }
            public int getIconHeight() { return size; }
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.translate(x, y);
                fn.paint(g2, defaultColor);
                g2.dispose();
            }
        };
    }

    /** Returns an icon painted with a custom color */
    public static Icon tinted(PainterFn fn, int size, Color c) {
        return makeIcon(fn, size, c);
    }
}
