package com.app.smartretail.utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * ImageLoader - Async image loader from URL with LRU cache.
 * Digunakan untuk memuat gambar produk dari klikindomaret.com
 */
public class ImageLoader {

    /** Basis URL pencarian produk Klik Indomaret */
    public static final String BASE_SEARCH = "https://www.klikindomaret.com/search?keyword=";

    // LRU cache — max 100 gambar
    private static final int CACHE_SIZE = 100;
    private static final Map<String, ImageIcon> CACHE = new LinkedHashMap<>(CACHE_SIZE, 0.75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<String,ImageIcon> eldest) {
            return size() > CACHE_SIZE;
        }
    };

    private static final ExecutorService POOL = Executors.newFixedThreadPool(4);

    // Placeholder icon saat loading / error
    private static final ImageIcon PLACEHOLDER = buildPlaceholder(80, 80);

    /**
     * Muat gambar secara async dari URL.
     * Callback dipanggil di EDT saat gambar selesai dimuat.
     */
    public static void loadAsync(String imageUrl, int w, int h, Consumer<ImageIcon> callback) {
        if (imageUrl == null || imageUrl.isBlank()) {
            callback.accept(PLACEHOLDER);
            return;
        }
        String key = imageUrl + "_" + w + "x" + h;
        if (CACHE.containsKey(key)) {
            callback.accept(CACHE.get(key));
            return;
        }
        callback.accept(PLACEHOLDER);
        POOL.submit(() -> {
            try {
                URL url = new URL(imageUrl);
                BufferedImage img = ImageIO.read(url);
                if (img == null) { SwingUtilities.invokeLater(() -> callback.accept(PLACEHOLDER)); return; }
                Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                ImageIcon icon = new ImageIcon(scaled);
                CACHE.put(key, icon);
                SwingUtilities.invokeLater(() -> callback.accept(icon));
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> callback.accept(PLACEHOLDER));
            }
        });
    }

    /** Load sync (blocking) — gunakan hanya untuk preview kecil */
    public static ImageIcon loadSync(String imageUrl, int w, int h) {
        if (imageUrl == null || imageUrl.isBlank()) return PLACEHOLDER;
        String key = imageUrl + "_" + w + "x" + h;
        if (CACHE.containsKey(key)) return CACHE.get(key);
        try {
            URL url = new URL(imageUrl);
            BufferedImage img = ImageIO.read(url);
            if (img == null) return PLACEHOLDER;
            Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            ImageIcon icon = new ImageIcon(scaled);
            CACHE.put(key, icon);
            return icon;
        } catch (IOException e) {
            return PLACEHOLDER;
        }
    }

    public static ImageIcon getPlaceholder() { return PLACEHOLDER; }

    private static ImageIcon buildPlaceholder(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(30, 35, 55));
        g2.fillRoundRect(0, 0, w, h, 10, 10);
        g2.setColor(new Color(55, 65, 95));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(1, 1, w-2, h-2, 10, 10);
        // Simple box icon
        int cx = w/2, cy = h/2, s = Math.min(w,h)/4;
        g2.setColor(new Color(72, 82, 112));
        g2.drawRect(cx-s, cy-s, s*2, s*2);
        g2.drawLine(cx-s, cy-s, cx, cy-s/2);
        g2.drawLine(cx+s, cy-s, cx, cy-s/2);
        g2.drawLine(cx, cy-s/2, cx, cy+s);
        g2.dispose();
        return new ImageIcon(img);
    }

    public static void shutdown() { POOL.shutdown(); }
}
