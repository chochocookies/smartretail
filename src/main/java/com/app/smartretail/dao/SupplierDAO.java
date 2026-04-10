package com.app.smartretail.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.app.smartretail.config.DatabaseConnection;
import com.app.smartretail.model.Supplier;

public class SupplierDAO {

    private Connection conn;

    public SupplierDAO() {
        this.conn = DatabaseConnection.getInstance();
    }

    public List<Supplier> getAll() {
        List<Supplier> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM supplier WHERE aktif=1 ORDER BY nama_supplier")) {
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) { System.err.println("[SupplierDAO] " + e.getMessage()); }
        return list;
    }

    public boolean insert(Supplier s) {
        String sql = "INSERT INTO supplier (kode_supplier, nama_supplier, alamat, telepon, email, contact_person, aktif) VALUES(?,?,?,?,?,?,1)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getKodeSupplier());
            ps.setString(2, s.getNamaSupplier());
            ps.setString(3, s.getAlamat());
            ps.setString(4, s.getTelepon());
            ps.setString(5, s.getEmail());
            ps.setString(6, s.getContactPerson());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("[SupplierDAO] Insert: " + e.getMessage()); return false; }
    }

    public boolean update(Supplier s) {
        String sql = "UPDATE supplier SET kode_supplier=?, nama_supplier=?, alamat=?, telepon=?, email=?, contact_person=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getKodeSupplier());
            ps.setString(2, s.getNamaSupplier());
            ps.setString(3, s.getAlamat());
            ps.setString(4, s.getTelepon());
            ps.setString(5, s.getEmail());
            ps.setString(6, s.getContactPerson());
            ps.setInt(7, s.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("[SupplierDAO] Update: " + e.getMessage()); return false; }
    }

    public boolean delete(int id) {
        String sql = "UPDATE supplier SET aktif=0 WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("[SupplierDAO] Delete: " + e.getMessage()); return false; }
    }

    private Supplier mapResultSet(ResultSet rs) throws SQLException {
        Supplier s = new Supplier();
        s.setId(rs.getInt("id"));
        s.setKodeSupplier(rs.getString("kode_supplier"));
        s.setNamaSupplier(rs.getString("nama_supplier"));
        s.setAlamat(rs.getString("alamat"));
        s.setTelepon(rs.getString("telepon"));
        s.setEmail(rs.getString("email"));
        s.setContactPerson(rs.getString("contact_person"));
        s.setAktif(rs.getBoolean("aktif"));
        return s;
    }
}
