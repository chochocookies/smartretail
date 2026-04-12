package com.app.smartretail.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * Model Transaksi - representasi header transaksi penjualan/pembelian
 */
public class Transaksi {

    public enum TipeTransaksi { PENJUALAN, PEMBELIAN }

    private int id;
    private String noTransaksi;
    private TipeTransaksi tipe;
    private int userId;         // kasir/petugas
    private String namaUser;
    private int customerId;     // untuk penjualan
    private String namaCustomer;
    private int supplierId;     // untuk pembelian
    private String namaSupplier;
    private LocalDateTime tanggal;
    private double totalHarga;
    private double diskon;
    private double pajak;
    private double grandTotal;
    private double bayar;
    private double kembalian;
    private String metode;      // TUNAI, TRANSFER, KARTU
    private String status;      // SELESAI, BATAL, PENDING
    private String catatan;
    private List<TransaksiDetail> details = new ArrayList<>();

    public Transaksi() {}

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNoTransaksi() { return noTransaksi; }
    public void setNoTransaksi(String noTransaksi) { this.noTransaksi = noTransaksi; }
    public TipeTransaksi getTipe() { return tipe; }
    public void setTipe(TipeTransaksi tipe) { this.tipe = tipe; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getNamaUser() { return namaUser; }
    public void setNamaUser(String namaUser) { this.namaUser = namaUser; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public String getNamaCustomer() { return namaCustomer; }
    public void setNamaCustomer(String namaCustomer) { this.namaCustomer = namaCustomer; }
    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }
    public String getNamaSupplier() { return namaSupplier; }
    public void setNamaSupplier(String namaSupplier) { this.namaSupplier = namaSupplier; }
    public LocalDateTime getTanggal() { return tanggal; }
    public void setTanggal(LocalDateTime tanggal) { this.tanggal = tanggal; }
    public double getTotalHarga() { return totalHarga; }
    public void setTotalHarga(double totalHarga) { this.totalHarga = totalHarga; }
    public double getDiskon() { return diskon; }
    public void setDiskon(double diskon) { this.diskon = diskon; }
    public double getPajak() { return pajak; }
    public void setPajak(double pajak) { this.pajak = pajak; }
    public double getGrandTotal() { return grandTotal; }
    public void setGrandTotal(double grandTotal) { this.grandTotal = grandTotal; }
    public double getBayar() { return bayar; }
    public void setBayar(double bayar) { this.bayar = bayar; }
    public double getKembalian() { return kembalian; }
    public void setKembalian(double kembalian) { this.kembalian = kembalian; }
    public String getMetode() { return metode; }
    public void setMetode(String metode) { this.metode = metode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCatatan() { return catatan; }
    public void setCatatan(String catatan) { this.catatan = catatan; }
    public List<TransaksiDetail> getDetails() { return details; }
    public void setDetails(List<TransaksiDetail> details) { this.details = details; }

    public void addDetail(TransaksiDetail d) { this.details.add(d); }

    public void hitungTotal() {
        totalHarga = details.stream().mapToDouble(TransaksiDetail::getSubtotal).sum();
        grandTotal = totalHarga - diskon + pajak;
        kembalian = bayar - grandTotal;
    }
}
