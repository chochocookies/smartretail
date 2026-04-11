package com.app.smartretail.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.app.smartretail.config.DatabaseConnection;
import com.app.smartretail.model.User;

public class UserDAO {

    private Connection conn;

    public UserDAO() {
        this.conn = DatabaseConnection.getInstance();
    }

    public User login(String username, String hashedPassword) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND aktif = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, hashedPassword);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Login error: " + e.getMessage());
        }
        return null;
    }

    public List<User> getAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY nama_lengkap";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("[UserDAO] GetAll error: " + e.getMessage());
        }
        return list;
    }

    public boolean insert(User u) {
        String sql = "INSERT INTO users (username, password, nama_lengkap, email, role, aktif) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getNamaLengkap());
            ps.setString(4, u.getEmail());
            ps.setString(5, u.getRole());
            ps.setBoolean(6, u.isAktif());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] Insert error: " + e.getMessage());
            return false;
        }
    }

    public boolean update(User u) {
        String sql = "UPDATE users SET username=?, nama_lengkap=?, email=?, role=?, aktif=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getNamaLengkap());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getRole());
            ps.setBoolean(5, u.isAktif());
            ps.setInt(6, u.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] Update error: " + e.getMessage());
            return false;
        }
    }

    public boolean updatePassword(int userId, String hashedPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] UpdatePassword error: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "UPDATE users SET aktif = 0 WHERE id = ?"; // soft delete
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] Delete error: " + e.getMessage());
            return false;
        }
    }

    private User mapResultSet(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setNamaLengkap(rs.getString("nama_lengkap"));
        u.setEmail(rs.getString("email"));
        u.setRole(rs.getString("role"));
        u.setAktif(rs.getBoolean("aktif"));
        return u;
    }
}
