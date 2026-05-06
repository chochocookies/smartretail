package com.app.smartretail.view.master;

import com.app.smartretail.config.DatabaseConnection;
import com.app.smartretail.utils.*;
import com.app.smartretail.view.component.Icons;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.sql.*;

public class KategoriForm extends JPanel {

    private JTable table;
    private DefaultTableModel mdl;
    private JTextField txtNama, txtDesk;
    private JButton btnSimpan, btnHapus, btnBatal, btnNew;
    private JLabel lblTitle;
    private int selId = -1;

    public KategoriForm() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_SURFACE);
        setBorder(new EmptyBorder(22, 24, 22, 24));
        build();
        load();
    }

    private void build() {
        // ── Header ──────────────────────────────────────────────
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel ht = new JPanel();
        ht.setOpaque(false);
        ht.setLayout(new BoxLayout(ht, BoxLayout.Y_AXIS));
        ht.add(UITheme.pageTitle("Category"));
        JLabel sub = new JLabel("Kelola kategori produk toko");
        sub.setFont(UITheme.FONT_BODY);
        sub.setForeground(UITheme.TEXT_SECONDARY);
        ht.add(sub);

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acts.setOpaque(false);
        JButton btnRef = UITheme.ghostButton("Refresh", UITheme.TEXT_MUTED);
        btnHapus = UITheme.dangerButton("Hapus");
        btnNew   = UITheme.primaryButton("+ Kategori", UITheme.ACCENT_LIME);
        acts.add(btnRef); acts.add(btnHapus); acts.add(btnNew);

        hdr.add(ht, BorderLayout.WEST);
        hdr.add(acts, BorderLayout.EAST);
        add(hdr, BorderLayout.NORTH);

        // ── Main split ───────────────────────────────────────────
        JPanel main = new JPanel(new BorderLayout(16, 0));
        main.setOpaque(false);

        // ── LEFT: table card ─────────────────────────────────────
        JPanel tableCard = UITheme.card();
        tableCard.setLayout(new BorderLayout(0, 10));

        // Toolbar inside card
        JPanel tbr = new JPanel(new BorderLayout(8, 0));
        tbr.setOpaque(false);
        JTextField search = UITheme.styledField("Cari kategori\u2026");
        search.setPreferredSize(new Dimension(0, 34));
        JButton btnS = UITheme.primaryButton("Cari", UITheme.ACCENT_BLUE);
        tbr.add(search, BorderLayout.CENTER);
        tbr.add(btnS, BorderLayout.EAST);

        String[] cols = {"#", "Nama Kategori", "Deskripsi", "Jumlah Produk"};
        mdl = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(mdl);
        UITheme.styleTable(table);
        TableRowSorter<DefaultTableModel> katSorter = new TableRowSorter<>(mdl);
        table.setRowSorter(katSorter);
        table.setRowHeight(38);
        table.getColumnModel().getColumn(0).setMaxWidth(42);
        table.getColumnModel().getColumn(3).setMaxWidth(110);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component cp = super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                cp.setBackground(sel ? new Color(238,242,255) :
                    (r%2==0 ? UITheme.BG_CARD : UITheme.BG_ROW_ALT));
                cp.setForeground(c==3 ? UITheme.ACCENT_BLUE : UITheme.TEXT_PRIMARY);
                ((JLabel)cp).setBorder(new EmptyBorder(0, 12, 0, 12));
                if (c==0) ((JLabel)cp).setHorizontalAlignment(SwingConstants.CENTER);
                return cp;
            }
        });

        tableCard.add(tbr, BorderLayout.NORTH);
        tableCard.add(UITheme.styledScroll(table), BorderLayout.CENTER);

        // ── RIGHT: form card ─────────────────────────────────────
        JPanel fc = UITheme.card();
        fc.setLayout(new BoxLayout(fc, BoxLayout.Y_AXIS));
        fc.setPreferredSize(new Dimension(280, 0));
        fc.setMaximumSize(new Dimension(280, Integer.MAX_VALUE));

        lblTitle = new JLabel("Detail Kategori");
        lblTitle.setFont(UITheme.FONT_H2);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);
        lblTitle.setAlignmentX(LEFT_ALIGNMENT);

        // Icon preview area
        JPanel iconArea = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_INPUT);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.dispose();
            }
        };
        iconArea.setOpaque(false);
        iconArea.setPreferredSize(new Dimension(254, 80));
        iconArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        iconArea.setAlignmentX(LEFT_ALIGNMENT);
        JLabel iconLbl = new JLabel(Icons.tinted(Icons::paintFolder, 36, UITheme.ACCENT_BLUE));
        iconArea.add(iconLbl);

        JLabel lNama = UITheme.fieldLabel("Nama Kategori *");
        lNama.setAlignmentX(LEFT_ALIGNMENT);
        txtNama = UITheme.styledField("contoh: Makanan & Minuman");
        txtNama.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtNama.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lDesk = UITheme.fieldLabel("Deskripsi");
        lDesk.setAlignmentX(LEFT_ALIGNMENT);
        txtDesk = UITheme.styledField("Deskripsi singkat kategori\u2026");
        txtDesk.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtDesk.setAlignmentX(LEFT_ALIGNMENT);

        // Action buttons
        JPanel br1 = btnRow2(UITheme.ghostButton("+ Baru", UITheme.ACCENT_BLUE),
                              UITheme.dangerButton("Hapus"));
        JPanel br2 = btnRow2(UITheme.ghostButton("Batal", UITheme.TEXT_SECONDARY),
                              UITheme.primaryButton("Simpan", UITheme.ACCENT_LIME));
        btnSimpan = (JButton)((JPanel)br2.getComponent(1)).getComponent(0);
        btnBatal  = (JButton)((JPanel)br2.getComponent(0)).getComponent(0);
        JButton btnNew2 = (JButton)((JPanel)br1.getComponent(0)).getComponent(0);
        JButton btnH2   = (JButton)((JPanel)br1.getComponent(1)).getComponent(0);

        fc.add(lblTitle);
        fc.add(Box.createVerticalStrut(8));
        fc.add(UITheme.separator());
        fc.add(Box.createVerticalStrut(12));
        fc.add(iconArea);
        fc.add(Box.createVerticalStrut(14));
        fc.add(lNama);
        fc.add(Box.createVerticalStrut(5));
        fc.add(txtNama);
        fc.add(Box.createVerticalStrut(12));
        fc.add(lDesk);
        fc.add(Box.createVerticalStrut(5));
        fc.add(txtDesk);
        fc.add(Box.createVerticalGlue());
        fc.add(Box.createVerticalStrut(14));
        fc.add(br1);
        fc.add(Box.createVerticalStrut(6));
        fc.add(br2);

        main.add(tableCard, BorderLayout.CENTER);
        main.add(fc, BorderLayout.EAST);
        add(main, BorderLayout.CENTER);

        // ── Events ───────────────────────────────────────────────
        btnS.addActionListener(e -> {String kw=search.getText().trim();katSorter.setRowFilter(kw.isEmpty()?null:RowFilter.regexFilter("(?i)"+kw,1,2));});
        search.addActionListener(e -> btnS.doClick());
        search.getDocument().addDocumentListener(new DocumentListener(){
            void f(){String kw=search.getText().trim();katSorter.setRowFilter(kw.isEmpty()?null:RowFilter.regexFilter("(?i)"+kw,1,2));}
            public void insertUpdate(DocumentEvent e){f();}public void removeUpdate(DocumentEvent e){f();}public void changedUpdate(DocumentEvent e){f();}
        });
        btnRef.addActionListener(e -> load());
        btnNew.addActionListener(e -> clear());
        btnNew2.addActionListener(e -> clear());
        btnSimpan.addActionListener(e -> simpan());
        btnBatal.addActionListener(e -> clear());
        btnHapus.addActionListener(e -> hapus());
        btnH2.addActionListener(e -> hapus());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillForm();
        });
    }

    // ── Data operations ──────────────────────────────────────────
    private void load() {
        mdl.setRowCount(0);
        String sql =
            "SELECT k.id, k.nama_kategori, k.deskripsi, " +
            "       COUNT(b.id) AS jumlah " +
            "FROM kategori k LEFT JOIN barang b ON b.kategori_id=k.id " +
            "GROUP BY k.id ORDER BY k.nama_kategori";
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            int n = 1;
            while (rs.next()) {
                mdl.addRow(new Object[]{
                    n++,
                    rs.getString("nama_kategori"),
                    rs.getString("deskripsi") != null ? rs.getString("deskripsi") : "-",
                    rs.getInt("jumlah") + " produk"
                });
                // store real id in hidden — trick: keep in parallel list
                // For simplicity we reload by name on selection
            }
        } catch (SQLException ex) {
            AlertUtil.showError(this, "Gagal memuat data: " + ex.getMessage());
        }
    }

    private void filter(String kw) {
        mdl.setRowCount(0);
        String sql =
            "SELECT k.id, k.nama_kategori, k.deskripsi, COUNT(b.id) AS jumlah " +
            "FROM kategori k LEFT JOIN barang b ON b.kategori_id=k.id " +
            "WHERE k.nama_kategori LIKE ? OR k.deskripsi LIKE ? " +
            "GROUP BY k.id ORDER BY k.nama_kategori";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            String q = "%" + kw + "%";
            ps.setString(1, q); ps.setString(2, q);
            ResultSet rs = ps.executeQuery();
            int n = 1;
            while (rs.next())
                mdl.addRow(new Object[]{
                    n++, rs.getString("nama_kategori"),
                    rs.getString("deskripsi") != null ? rs.getString("deskripsi") : "-",
                    rs.getInt("jumlah") + " produk"
                });
        } catch (SQLException ex) {
            AlertUtil.showError(this, ex.getMessage());
        }
    }

    private void fillForm() {
        int row = table.getSelectedRow(); if (row < 0) return;
        String nama = mdl.getValueAt(row, 1).toString();
        lblTitle.setText(nama.length() > 22 ? nama.substring(0,21) + "\u2026" : nama);
        txtNama.setText(nama);
        Object d = mdl.getValueAt(row, 2); txtDesk.setText("-".equals(d.toString()) ? "" : d.toString());
        // fetch real id
        try (PreparedStatement ps = DatabaseConnection.getInstance()
                .prepareStatement("SELECT id FROM kategori WHERE nama_kategori=?")) {
            ps.setString(1, nama);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) selId = rs.getInt(1);
        } catch (SQLException ex) { selId = -1; }
    }

    private void simpan() {
        if (txtNama.getText().isBlank()) {
            AlertUtil.showWarning(this, "Nama kategori wajib diisi!"); return;
        }
        String sql = (selId == -1)
            ? "INSERT INTO kategori(nama_kategori,deskripsi) VALUES(?,?)"
            : "UPDATE kategori SET nama_kategori=?,deskripsi=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, txtNama.getText().trim());
            ps.setString(2, txtDesk.getText().trim());
            if (selId != -1) ps.setInt(3, selId);
            ps.executeUpdate();
            AlertUtil.showInfo(this, "Kategori berhasil disimpan!");
            load(); clear();
        } catch (SQLException ex) {
            AlertUtil.showError(this, "Gagal menyimpan: " + ex.getMessage());
        }
    }

    private void hapus() {
        if (selId == -1) { AlertUtil.showWarning(this, "Pilih kategori terlebih dahulu!"); return; }
        if (!AlertUtil.showConfirm(this, "Hapus kategori ini?\nProduk terkait akan kehilangan kategori.")) return;
        try (PreparedStatement ps = DatabaseConnection.getInstance()
                .prepareStatement("DELETE FROM kategori WHERE id=?")) {
            ps.setInt(1, selId); ps.executeUpdate();
            AlertUtil.showInfo(this, "Kategori dihapus.");
            load(); clear();
        } catch (SQLException ex) {
            AlertUtil.showError(this, "Gagal menghapus: " + ex.getMessage());
        }
    }

    private void clear() {
        selId = -1; txtNama.setText(""); txtDesk.setText("");
        lblTitle.setText("Detail Kategori"); table.clearSelection();
    }

    // ── Helpers ──────────────────────────────────────────────────
    private JPanel btnRow2(JButton left, JButton right) {
        JPanel p = new JPanel(new GridLayout(1, 2, 8, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        p.setAlignmentX(LEFT_ALIGNMENT);
        JPanel wL = new JPanel(new BorderLayout()); wL.setOpaque(false); wL.add(left);
        JPanel wR = new JPanel(new BorderLayout()); wR.setOpaque(false); wR.add(right);
        p.add(wL); p.add(wR);
        return p;
    }
}
