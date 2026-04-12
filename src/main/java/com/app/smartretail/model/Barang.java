package com.app.smartretail.model;

import java.time.LocalDateTime;

public class Barang {
    private int id;
    private String kodeBarang, plu, barcode, modis;
    private String namaBarang;
    private int kategoriId;    private String namaKategori;
    private int supplierId;    private String namaSupplier;
    private double hargaBeli, hargaJual;
    private int stok, stokMinimum;
    private String satuan, deskripsi, imageUrl;
    private LocalDateTime createdAt, updatedAt;

    public Barang() {}

    // Getters / Setters
    public int getId(){return id;} public void setId(int id){this.id=id;}
    public String getKodeBarang(){return kodeBarang;} public void setKodeBarang(String k){this.kodeBarang=k;}
    public String getPlu(){return plu;} public void setPlu(String p){this.plu=p;}
    public String getBarcode(){return barcode;} public void setBarcode(String b){this.barcode=b;}
    public String getModis(){return modis;} public void setModis(String m){this.modis=m;}
    public String getNamaBarang(){return namaBarang;} public void setNamaBarang(String n){this.namaBarang=n;}
    public int getKategoriId(){return kategoriId;} public void setKategoriId(int i){this.kategoriId=i;}
    public String getNamaKategori(){return namaKategori;} public void setNamaKategori(String n){this.namaKategori=n;}
    public int getSupplierId(){return supplierId;} public void setSupplierId(int i){this.supplierId=i;}
    public String getNamaSupplier(){return namaSupplier;} public void setNamaSupplier(String n){this.namaSupplier=n;}
    public double getHargaBeli(){return hargaBeli;} public void setHargaBeli(double h){this.hargaBeli=h;}
    public double getHargaJual(){return hargaJual;} public void setHargaJual(double h){this.hargaJual=h;}
    public int getStok(){return stok;} public void setStok(int s){this.stok=s;}
    public int getStokMinimum(){return stokMinimum;} public void setStokMinimum(int s){this.stokMinimum=s;}
    public String getSatuan(){return satuan;} public void setSatuan(String s){this.satuan=s;}
    public String getDeskripsi(){return deskripsi;} public void setDeskripsi(String d){this.deskripsi=d;}
    public String getImageUrl(){return imageUrl;} public void setImageUrl(String u){this.imageUrl=u;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime t){this.createdAt=t;}
    public LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(LocalDateTime t){this.updatedAt=t;}
    public boolean isStokRendah(){return stok<=stokMinimum;}
    @Override public String toString(){return "["+kodeBarang+"] "+namaBarang;}
}
