package com.app.smartretail.model;

public class Kategori {
    private int id;
    private String namaKategori;
    private String deskripsi;

    public Kategori() {}
    public Kategori(int id, String namaKategori) { this.id = id; this.namaKategori = namaKategori; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNamaKategori() { return namaKategori; }
    public void setNamaKategori(String namaKategori) { this.namaKategori = namaKategori; }
    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }

    @Override
    public String toString() { return namaKategori; }
}
