package com.app.smartretail.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Pegawai {
    private int    id;
    private String nik;
    private String nama;
    private String jabatan;
    private String telepon;
    private String email;
    private String alamat;
    private LocalDate tglMasuk;
    private String status;          // AKTIF | CUTI | RESIGN
    private LocalDateTime createdAt;

    // ── Getters & Setters ─────────────────────────────────────────
    public int getId()           { return id; }
    public void setId(int id)   { this.id = id; }

    public String getNik()                  { return nik; }
    public void   setNik(String nik)        { this.nik = nik; }

    public String getNama()                 { return nama; }
    public void   setNama(String nama)      { this.nama = nama; }

    public String getJabatan()              { return jabatan; }
    public void   setJabatan(String j)      { this.jabatan = j; }

    public String getTelepon()              { return telepon; }
    public void   setTelepon(String t)      { this.telepon = t; }

    public String getEmail()                { return email; }
    public void   setEmail(String e)        { this.email = e; }

    public String getAlamat()               { return alamat; }
    public void   setAlamat(String a)       { this.alamat = a; }

    public LocalDate getTglMasuk()          { return tglMasuk; }
    public void setTglMasuk(LocalDate d)    { this.tglMasuk = d; }

    public String getStatus()               { return status != null ? status : "AKTIF"; }
    public void   setStatus(String s)       { this.status = s; }

    public LocalDateTime getCreatedAt()           { return createdAt; }
    public void setCreatedAt(LocalDateTime t)      { this.createdAt = t; }

    /** Inisial untuk avatar (maks 2 huruf) */
    public String getInitials() {
        if (nama == null || nama.isBlank()) return "?";
        String[] parts = nama.trim().split(" ");
        if (parts.length == 1)
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[0].charAt(0) + parts[parts.length-1].charAt(0)).toUpperCase();
    }
}
