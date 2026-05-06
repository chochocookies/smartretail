package com.app.smartretail.dao;

import com.app.smartretail.config.DatabaseConnection;
import com.app.smartretail.model.Pegawai;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * PegawaiDAO — CRUD untuk tabel `pegawai`.
 * Kolom: id, nik, nama, jabatan, telepon, email, alamat, tgl_masuk, status, created_at
 */
public class PegawaiDAO {

    private Connection conn() { return DatabaseConnection.getInstance(); }

    // ── READ ──────────────────────────────────────────────────────
    public List<Pegawai> getAll() {
        List<Pegawai> list = new ArrayList<>();
        String sql = "SELECT * FROM pegawai ORDER BY nama";
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("[PegawaiDAO] getAll: " + e.getMessage());
        }
        return list;
    }

    public List<Pegawai> search(String kw) {
        List<Pegawai> list = new ArrayList<>();
        String sql = "SELECT * FROM pegawai WHERE nama LIKE ? OR jabatan LIKE ? OR nik LIKE ? ORDER BY nama";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            String q = "%" + kw + "%";
            ps.setString(1, q); ps.setString(2, q); ps.setString(3, q);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("[PegawaiDAO] search: " + e.getMessage());
        }
        return list;
    }

    public List<Pegawai> getByStatus(String status) {
        List<Pegawai> list = new ArrayList<>();
        String sql = "SELECT * FROM pegawai WHERE status = ? ORDER BY nama";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("[PegawaiDAO] getByStatus: " + e.getMessage());
        }
        return list;
    }

    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM pegawai WHERE status = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[PegawaiDAO] countByStatus: " + e.getMessage());
        }
        return 0;
    }

    // ── CREATE ────────────────────────────────────────────────────
    public boolean insert(Pegawai p) {
        String sql = "INSERT INTO pegawai (nik,nama,jabatan,telepon,email,alamat,tgl_masuk,status) " +
                     "VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, p.getNik());
            ps.setString(2, p.getNama());
            ps.setString(3, p.getJabatan());
            ps.setString(4, p.getTelepon());
            ps.setString(5, p.getEmail());
            ps.setString(6, p.getAlamat());
            ps.setDate(7, p.getTglMasuk() != null ? Date.valueOf(p.getTglMasuk()) : null);
            ps.setString(8, p.getStatus());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[PegawaiDAO] insert: " + e.getMessage());
            return false;
        }
    }

    // ── UPDATE ────────────────────────────────────────────────────
    public boolean update(Pegawai p) {
        String sql = "UPDATE pegawai SET nik=?,nama=?,jabatan=?,telepon=?,email=?,alamat=?,tgl_masuk=?,status=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, p.getNik());
            ps.setString(2, p.getNama());
            ps.setString(3, p.getJabatan());
            ps.setString(4, p.getTelepon());
            ps.setString(5, p.getEmail());
            ps.setString(6, p.getAlamat());
            ps.setDate(7, p.getTglMasuk() != null ? Date.valueOf(p.getTglMasuk()) : null);
            ps.setString(8, p.getStatus());
            ps.setInt(9, p.getId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[PegawaiDAO] update: " + e.getMessage());
            return false;
        }
    }

    // ── DELETE ────────────────────────────────────────────────────
    public boolean delete(int id) {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM pegawai WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[PegawaiDAO] delete: " + e.getMessage());
            return false;
        }
    }

    // ── Generate NIK ──────────────────────────────────────────────
    public String generateNik() {
        String sql = "SELECT MAX(CAST(SUBSTRING(nik,5) AS UNSIGNED)) FROM pegawai WHERE nik LIKE 'EMP-%'";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            int next = rs.next() ? rs.getInt(1) + 1 : 1;
            return String.format("EMP-%04d", next);
        } catch (SQLException e) {
            return "EMP-" + System.currentTimeMillis() % 10000;
        }
    }

    // ── Map ResultSet → Pegawai ───────────────────────────────────
    private Pegawai map(ResultSet rs) throws SQLException {
        Pegawai p = new Pegawai();
        p.setId(rs.getInt("id"));
        p.setNik(rs.getString("nik"));
        p.setNama(rs.getString("nama"));
        p.setJabatan(rs.getString("jabatan"));
        p.setTelepon(rs.getString("telepon"));
        p.setEmail(rs.getString("email"));
        p.setAlamat(rs.getString("alamat"));
        Date d = rs.getDate("tgl_masuk");
        if (d != null) p.setTglMasuk(d.toLocalDate());
        p.setStatus(rs.getString("status"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) p.setCreatedAt(ca.toLocalDateTime());
        return p;
    }
}
