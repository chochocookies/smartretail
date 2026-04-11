package com.app.smartretail.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.app.smartretail.config.DatabaseConnection;

public class ReportDAO {

    private Connection conn;

    public ReportDAO() {
        this.conn = DatabaseConnection.getInstance();
    }

    /** Omzet per hari dalam rentang tanggal tertentu */
    public Map<String, Double> getOmzetPerHari(LocalDateTime dari, LocalDateTime sampai) {
        Map<String, Double> map = new LinkedHashMap<>();
        String sql = "SELECT DATE(tanggal) as tgl, SUM(grand_total) as total " +
                     "FROM transaksi WHERE tipe='PENJUALAN' AND status='SELESAI' " +
                     "AND tanggal BETWEEN ? AND ? GROUP BY DATE(tanggal) ORDER BY tgl";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(dari));
            ps.setTimestamp(2, Timestamp.valueOf(sampai));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) map.put(rs.getString("tgl"), rs.getDouble("total"));
        } catch (SQLException e) {
            System.err.println("[ReportDAO] GetOmzetPerHari: " + e.getMessage());
        }
        return map;
    }

    /** Top N barang terlaris */
    public List<Object[]> getBarangTerlaris(int limit) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT b.nama_barang, SUM(td.qty) as total_qty, SUM(td.subtotal) as total_omzet " +
                     "FROM transaksi_detail td " +
                     "JOIN barang b ON td.barang_id = b.id " +
                     "JOIN transaksi t ON td.transaksi_id = t.id " +
                     "WHERE t.tipe='PENJUALAN' AND t.status='SELESAI' " +
                     "GROUP BY b.id, b.nama_barang ORDER BY total_qty DESC LIMIT ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{rs.getString("nama_barang"), rs.getInt("total_qty"), rs.getDouble("total_omzet")});
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] GetBarangTerlaris: " + e.getMessage());
        }
        return list;
    }

    /** Summary dashboard: total penjualan hari ini, bulan ini */
    public Map<String, Object> getDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();
        try (Statement st = conn.createStatement()) {
            // Penjualan hari ini
            ResultSet rs = st.executeQuery(
                "SELECT COUNT(*) as cnt, COALESCE(SUM(grand_total),0) as total " +
                "FROM transaksi WHERE tipe='PENJUALAN' AND status='SELESAI' AND DATE(tanggal)=CURDATE()");
            if (rs.next()) {
                summary.put("transaksiHariIni", rs.getInt("cnt"));
                summary.put("omzetHariIni", rs.getDouble("total"));
            }

            // Penjualan bulan ini
            rs = st.executeQuery(
                "SELECT COALESCE(SUM(grand_total),0) as total FROM transaksi " +
                "WHERE tipe='PENJUALAN' AND status='SELESAI' " +
                "AND MONTH(tanggal)=MONTH(CURDATE()) AND YEAR(tanggal)=YEAR(CURDATE())");
            if (rs.next()) summary.put("omzetBulanIni", rs.getDouble("total"));

            // Jumlah stok rendah
            rs = st.executeQuery("SELECT COUNT(*) as cnt FROM barang WHERE stok <= stok_minimum");
            if (rs.next()) summary.put("stokRendah", rs.getInt("cnt"));

            // Total barang
            rs = st.executeQuery("SELECT COUNT(*) as cnt FROM barang");
            if (rs.next()) summary.put("totalBarang", rs.getInt("cnt"));

        } catch (SQLException e) {
            System.err.println("[ReportDAO] GetDashboardSummary: " + e.getMessage());
        }
        return summary;
    }
}
