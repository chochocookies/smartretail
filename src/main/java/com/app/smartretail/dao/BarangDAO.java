package com.app.smartretail.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.app.smartretail.config.DatabaseConnection;
import com.app.smartretail.model.Barang;

public class BarangDAO {

    private Connection conn;

    public BarangDAO() {
        this.conn = DatabaseConnection.getInstance();
    }

    public List<Barang> getAll() {
        List<Barang> list = new ArrayList<>();
        String sql = "SELECT b.*, k.nama_kategori, s.nama_supplier " +
                     "FROM barang b " +
                     "LEFT JOIN kategori k ON b.kategori_id = k.id " +
                     "LEFT JOIN supplier s ON b.supplier_id = s.id " +
                     "ORDER BY b.nama_barang";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("[BarangDAO] GetAll error: " + e.getMessage());
        }
        return list;
    }

    public List<Barang> search(String keyword) {
        List<Barang> list = new ArrayList<>();
        String sql = "SELECT b.*, k.nama_kategori, s.nama_supplier " +
                     "FROM barang b " +
                     "LEFT JOIN kategori k ON b.kategori_id = k.id " +
                     "LEFT JOIN supplier s ON b.supplier_id = s.id " +
                     "WHERE b.kode_barang LIKE ? OR b.nama_barang LIKE ? " +
                     "ORDER BY b.nama_barang";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String q = "%" + keyword + "%";
            ps.setString(1, q);
            ps.setString(2, q);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("[BarangDAO] Search error: " + e.getMessage());
        }
        return list;
    }

    public List<Barang> getStokRendah() {
        List<Barang> list = new ArrayList<>();
        String sql = "SELECT b.*, k.nama_kategori, s.nama_supplier " +
                     "FROM barang b " +
                     "LEFT JOIN kategori k ON b.kategori_id = k.id " +
                     "LEFT JOIN supplier s ON b.supplier_id = s.id " +
                     "WHERE b.stok <= b.stok_minimum";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("[BarangDAO] GetStokRendah error: " + e.getMessage());
        }
        return list;
    }

    public Barang getByKode(String kode) {
        String sql = "SELECT b.*, k.nama_kategori, s.nama_supplier FROM barang b " +
                     "LEFT JOIN kategori k ON b.kategori_id = k.id " +
                     "LEFT JOIN supplier s ON b.supplier_id = s.id " +
                     "WHERE b.kode_barang = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kode);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        } catch (SQLException e) {
            System.err.println("[BarangDAO] GetByKode error: " + e.getMessage());
        }
        return null;
    }

    public boolean insert(Barang b) {
        String sql = "INSERT INTO barang (kode_barang, nama_barang, kategori_id, supplier_id, " +
                     "harga_beli, harga_jual, stok, stok_minimum, satuan, deskripsi) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, b.getKodeBarang());
            ps.setString(2, b.getNamaBarang());
            ps.setInt(3, b.getKategoriId());
            ps.setInt(4, b.getSupplierId());
            ps.setDouble(5, b.getHargaBeli());
            ps.setDouble(6, b.getHargaJual());
            ps.setInt(7, b.getStok());
            ps.setInt(8, b.getStokMinimum());
            ps.setString(9, b.getSatuan());
            ps.setString(10, b.getDeskripsi());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BarangDAO] Insert error: " + e.getMessage());
            return false;
        }
    }

    public boolean update(Barang b) {
        String sql = "UPDATE barang SET kode_barang=?, nama_barang=?, kategori_id=?, supplier_id=?, " +
                     "harga_beli=?, harga_jual=?, stok_minimum=?, satuan=?, deskripsi=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, b.getKodeBarang());
            ps.setString(2, b.getNamaBarang());
            ps.setInt(3, b.getKategoriId());
            ps.setInt(4, b.getSupplierId());
            ps.setDouble(5, b.getHargaBeli());
            ps.setDouble(6, b.getHargaJual());
            ps.setInt(7, b.getStokMinimum());
            ps.setString(8, b.getSatuan());
            ps.setString(9, b.getDeskripsi());
            ps.setInt(10, b.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BarangDAO] Update error: " + e.getMessage());
            return false;
        }
    }

    public boolean updateStok(int barangId, int jumlah) {
        String sql = "UPDATE barang SET stok = stok + ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, jumlah);
            ps.setInt(2, barangId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BarangDAO] UpdateStok error: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM barang WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BarangDAO] Delete error: " + e.getMessage());
            return false;
        }
    }

    private Barang mapResultSet(ResultSet rs) throws SQLException {
        Barang b = new Barang();
        b.setId(rs.getInt("id"));
        b.setKodeBarang(rs.getString("kode_barang"));
        b.setNamaBarang(rs.getString("nama_barang"));
        b.setKategoriId(rs.getInt("kategori_id"));
        b.setNamaKategori(rs.getString("nama_kategori"));
        b.setSupplierId(rs.getInt("supplier_id"));
        b.setNamaSupplier(rs.getString("nama_supplier"));
        b.setHargaBeli(rs.getDouble("harga_beli"));
        b.setHargaJual(rs.getDouble("harga_jual"));
        b.setStok(rs.getInt("stok"));
        b.setStokMinimum(rs.getInt("stok_minimum"));
        b.setSatuan(rs.getString("satuan"));
        b.setDeskripsi(rs.getString("deskripsi"));
        return b;
    }
}
