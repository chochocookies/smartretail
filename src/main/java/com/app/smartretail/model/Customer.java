package com.app.smartretail.model;

public class Customer {
    private int id;
    private String kodeCustomer;
    private String namaCustomer;
    private String telepon;
    private String email;
    private String alamat;
    private int poin;

    public Customer() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getKodeCustomer() { return kodeCustomer; }
    public void setKodeCustomer(String kodeCustomer) { this.kodeCustomer = kodeCustomer; }
    public String getNamaCustomer() { return namaCustomer; }
    public void setNamaCustomer(String namaCustomer) { this.namaCustomer = namaCustomer; }
    public String getTelepon() { return telepon; }
    public void setTelepon(String telepon) { this.telepon = telepon; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }
    public int getPoin() { return poin; }
    public void setPoin(int poin) { this.poin = poin; }

    @Override
    public String toString() { return namaCustomer + " (" + telepon + ")"; }
}
