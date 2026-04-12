package com.app.smartretail.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnection - Singleton pattern untuk koneksi JDBC ke MySQL
 * SmartRetailApp - SRMS
 */
public class DatabaseConnection {

    private static final String HOST     = "localhost";
    private static final String PORT     = "3306";
    private static final String DATABASE = "smart_retail_netbeans";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    private static final String URL =
        "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
        + "?useSSL=false&serverTimezone=Asia/Jakarta&allowPublicKeyRetrieval=true";

    private static Connection instance = null;

    private DatabaseConnection() {}

    public static Connection getInstance() {
        try {
            if (instance == null || instance.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                instance = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println("[DB] Koneksi berhasil ke " + DATABASE);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] Driver tidak ditemukan: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("[DB] Koneksi gagal: " + e.getMessage());
        }
        return instance;
    }

    public static void closeConnection() {
        try {
            if (instance != null && !instance.isClosed()) {
                instance.close();
                System.out.println("[DB] Koneksi ditutup.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Gagal menutup koneksi: " + e.getMessage());
        }
    }
}
