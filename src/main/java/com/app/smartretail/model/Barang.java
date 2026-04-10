package com.app.smartretail.model;

import java.time.LocalDateTime;

/**
 * Model Barang - representasi tabel `barang` di database
 */
public class Barang {

    private int id;
    private String kodeBarang;
    private String namaBarang;
    private int kategoriId;
    private String namaKategori;
    private int supplierId;
    private String namaSupplier;
    private double hargaBeli;
    private double hargaJual;
    private int stok;
    private int stokMinimum;
    private String satuan;
    private String deskripsi;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Barang() {}

    public Barang(String kodeBarang, String namaBarang, int kategoriId, int supplierId,
                  double hargaBeli, double hargaJual, int stok, int stokMinimum, String satuan) {
        this.kodeBarang = kodeBarang;
        this.namaBarang = namaBarang;
        this.kategoriId = kategoriId;
        this.supplierId = supplierId;
        this.hargaBeli = hargaBeli;
        this.hargaJual = hargaJual;
        this.stok = stok;
        this.stokMinimum = stokMinimum;
        this.satuan = satuan;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getKodeBarang() { return kodeBarang; }
    public void setKodeBarang(String kodeBarang) { this.kodeBarang = kodeBarang; }

    public String getNamaBarang() { return namaBarang; }
    public void setNamaBarang(String namaBarang) { this.namaBarang = namaBarang; }

    public int getKategoriId() { return kategoriId; }
    public void setKategoriId(int kategoriId) { this.kategoriId = kategoriId; }

    public String getNamaKategori() { return namaKategori; }
    public void setNamaKategori(String namaKategori) { this.namaKategori = namaKategori; }

    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }

    public String getNamaSupplier() { return namaSupplier; }
    public void setNamaSupplier(String namaSupplier) { this.namaSupplier = namaSupplier; }

    public double getHargaBeli() { return hargaBeli; }
    public void setHargaBeli(double hargaBeli) { this.hargaBeli = hargaBeli; }

    public double getHargaJual() { return hargaJual; }
    public void setHargaJual(double hargaJual) { this.hargaJual = hargaJual; }

    public int getStok() { return stok; }
    public void setStok(int stok) { this.stok = stok; }

    public int getStokMinimum() { return stokMinimum; }
    public void setStokMinimum(int stokMinimum) { this.stokMinimum = stokMinimum; }

    public String getSatuan() { return satuan; }
    public void setSatuan(String satuan) { this.satuan = satuan; }

    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isStokRendah() {
        return stok <= stokMinimum;
    }

    @Override
    public String toString() {
        return "[" + kodeBarang + "] " + namaBarang;
    }
}
