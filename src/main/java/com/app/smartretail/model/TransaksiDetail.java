package com.app.smartretail.model;

public class TransaksiDetail {
    private int id;
    private int transaksiId;
    private int barangId;
    private String kodeBarang;
    private String namaBarang;
    private int qty;
    private double hargaSatuan;
    private double diskon;
    private double subtotal;

    public TransaksiDetail() {}

    public TransaksiDetail(int barangId, String kodeBarang, String namaBarang,
                           int qty, double hargaSatuan) {
        this.barangId = barangId;
        this.kodeBarang = kodeBarang;
        this.namaBarang = namaBarang;
        this.qty = qty;
        this.hargaSatuan = hargaSatuan;
        this.subtotal = qty * hargaSatuan;
    }

    public void hitungSubtotal() {
        this.subtotal = (qty * hargaSatuan) - diskon;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTransaksiId() { return transaksiId; }
    public void setTransaksiId(int transaksiId) { this.transaksiId = transaksiId; }
    public int getBarangId() { return barangId; }
    public void setBarangId(int barangId) { this.barangId = barangId; }
    public String getKodeBarang() { return kodeBarang; }
    public void setKodeBarang(String kodeBarang) { this.kodeBarang = kodeBarang; }
    public String getNamaBarang() { return namaBarang; }
    public void setNamaBarang(String namaBarang) { this.namaBarang = namaBarang; }
    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }
    public double getHargaSatuan() { return hargaSatuan; }
    public void setHargaSatuan(double hargaSatuan) { this.hargaSatuan = hargaSatuan; }
    public double getDiskon() { return diskon; }
    public void setDiskon(double diskon) { this.diskon = diskon; }
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
}
