package com.app.smartretail.dao;

import com.app.smartretail.config.DatabaseConnection;
import com.app.smartretail.model.Kategori;
import java.sql.*;
import java.util.*;

public class KategoriDAO {
    private final Connection conn = DatabaseConnection.getInstance();

    public List<Kategori> getAll() {
        List<Kategori> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM kategori ORDER BY nama_kategori")) {
            while (rs.next()) {
                Kategori k = new Kategori();
                k.setId(rs.getInt("id"));
                k.setNamaKategori(rs.getString("nama_kategori"));
                k.setDeskripsi(rs.getString("deskripsi"));
                list.add(k);
            }
        } catch (SQLException e) { System.err.println("[KategoriDAO] " + e.getMessage()); }
        return list;
    }
}
