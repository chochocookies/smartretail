package com.app.smartretail.dao;

import com.app.smartretail.config.DatabaseConnection;
import com.app.smartretail.model.Transaksi;
import com.app.smartretail.model.TransaksiDetail;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransaksiDAO {

    private Connection conn;
    private BarangDAO barangDAO;

    public TransaksiDAO() {
        this.conn = DatabaseConnection.getInstance();
        this.barangDAO = new BarangDAO();
    }

    /**
     * Simpan transaksi lengkap dengan detail dalam satu transaction JDBC
     */
    public boolean saveTransaksi(Transaksi t) {
        String sqlHeader = "INSERT INTO transaksi (no_transaksi, tipe, user_id, customer_id, " +
                           "supplier_id, tanggal, total_harga, diskon, pajak, grand_total, " +
                           "bayar, kembalian, metode, status, catatan) " +
                           "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        String sqlDetail = "INSERT INTO transaksi_detail (transaksi_id, barang_id, qty, harga_satuan, diskon, subtotal) " +
                           "VALUES (?,?,?,?,?,?)";
        try {
            conn.setAutoCommit(false);

            // Insert header
            PreparedStatement psH = conn.prepareStatement(sqlHeader, Statement.RETURN_GENERATED_KEYS);
            psH.setString(1, t.getNoTransaksi());
            psH.setString(2, t.getTipe().name());
            psH.setInt(3, t.getUserId());
            psH.setInt(4, t.getCustomerId());
            psH.setInt(5, t.getSupplierId());
            psH.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            psH.setDouble(7, t.getTotalHarga());
            psH.setDouble(8, t.getDiskon());
            psH.setDouble(9, t.getPajak());
            psH.setDouble(10, t.getGrandTotal());
            psH.setDouble(11, t.getBayar());
            psH.setDouble(12, t.getKembalian());
            psH.setString(13, t.getMetode());
            psH.setString(14, "SELESAI");
            psH.setString(15, t.getCatatan());
            psH.executeUpdate();

            ResultSet keys = psH.getGeneratedKeys();
            int transaksiId = 0;
            if (keys.next()) transaksiId = keys.getInt(1);

            // Insert detail & update stok
            for (TransaksiDetail d : t.getDetails()) {
                PreparedStatement psD = conn.prepareStatement(sqlDetail);
                psD.setInt(1, transaksiId);
                psD.setInt(2, d.getBarangId());
                psD.setInt(3, d.getQty());
                psD.setDouble(4, d.getHargaSatuan());
                psD.setDouble(5, d.getDiskon());
                psD.setDouble(6, d.getSubtotal());
                psD.executeUpdate();

                // Update stok: kurangi jika penjualan, tambah jika pembelian
                int stokChange = (t.getTipe() == Transaksi.TipeTransaksi.PENJUALAN)
                                 ? -d.getQty() : d.getQty();
                barangDAO.updateStok(d.getBarangId(), stokChange);
            }

            conn.commit();
            conn.setAutoCommit(true);
            return true;

        } catch (SQLException e) {
            System.err.println("[TransaksiDAO] SaveTransaksi error: " + e.getMessage());
            try { conn.rollback(); conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        }
    }

    public List<Transaksi> getByTipe(Transaksi.TipeTransaksi tipe) {
        List<Transaksi> list = new ArrayList<>();
        String sql = "SELECT t.*, u.nama_lengkap as nama_user, " +
                     "c.nama_customer, s.nama_supplier " +
                     "FROM transaksi t " +
                     "LEFT JOIN users u ON t.user_id = u.id " +
                     "LEFT JOIN customer c ON t.customer_id = c.id " +
                     "LEFT JOIN supplier s ON t.supplier_id = s.id " +
                     "WHERE t.tipe = ? ORDER BY t.tanggal DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tipe.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("[TransaksiDAO] GetByTipe error: " + e.getMessage());
        }
        return list;
    }

    public List<Transaksi> getPenjualanByPeriode(LocalDateTime dari, LocalDateTime sampai) {
        List<Transaksi> list = new ArrayList<>();
        String sql = "SELECT t.*, u.nama_lengkap as nama_user, c.nama_customer " +
                     "FROM transaksi t LEFT JOIN users u ON t.user_id = u.id " +
                     "LEFT JOIN customer c ON t.customer_id = c.id " +
                     "WHERE t.tipe = 'PENJUALAN' AND t.tanggal BETWEEN ? AND ? " +
                     "ORDER BY t.tanggal DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(dari));
            ps.setTimestamp(2, Timestamp.valueOf(sampai));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("[TransaksiDAO] GetPenjualanByPeriode error: " + e.getMessage());
        }
        return list;
    }

    public String generateNoTransaksi(String prefix) {
        String sql = "SELECT COUNT(*) FROM transaksi WHERE tipe = ? AND DATE(tanggal) = CURDATE()";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix.equals("TRX") ? "PENJUALAN" : "PEMBELIAN");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1) + 1;
                return prefix + "-" + String.format("%s-%04d",
                       LocalDateTime.now().toLocalDate().toString().replace("-", ""), count);
            }
        } catch (SQLException e) {
            System.err.println("[TransaksiDAO] GenerateNo error: " + e.getMessage());
        }
        return prefix + "-" + System.currentTimeMillis();
    }

    private Transaksi mapResultSet(ResultSet rs) throws SQLException {
        Transaksi t = new Transaksi();
        t.setId(rs.getInt("id"));
        t.setNoTransaksi(rs.getString("no_transaksi"));
        t.setTipe(Transaksi.TipeTransaksi.valueOf(rs.getString("tipe")));
        t.setUserId(rs.getInt("user_id"));
        t.setNamaUser(rs.getString("nama_user"));
        t.setTanggal(rs.getTimestamp("tanggal").toLocalDateTime());
        t.setTotalHarga(rs.getDouble("total_harga"));
        t.setDiskon(rs.getDouble("diskon"));
        t.setPajak(rs.getDouble("pajak"));
        t.setGrandTotal(rs.getDouble("grand_total"));
        t.setBayar(rs.getDouble("bayar"));
        t.setKembalian(rs.getDouble("kembalian"));
        t.setMetode(rs.getString("metode"));
        t.setStatus(rs.getString("status"));
        return t;
    }
}
