package com.app.smartretail.model;

import java.time.LocalDateTime;

/**
 * Model User - representasi tabel `users` di database
 */
public class User {

    private int id;
    private String username;
    private String password; // hashed
    private String namaLengkap;
    private String email;
    private String role;     // ADMIN, KASIR, STAFF_GUDANG, SUPERVISOR
    private boolean aktif;
    private LocalDateTime createdAt;

    public User() {}

    public User(int id, String username, String password, String namaLengkap,
                String email, String role, boolean aktif) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.namaLengkap = namaLengkap;
        this.email = email;
        this.role = role;
        this.aktif = aktif;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNamaLengkap() { return namaLengkap; }
    public void setNamaLengkap(String namaLengkap) { this.namaLengkap = namaLengkap; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isAktif() { return aktif; }
    public void setAktif(boolean aktif) { this.aktif = aktif; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return namaLengkap + " [" + role + "]";
    }
}
