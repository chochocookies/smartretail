package com.app.smartretail.model;

public class Supplier {
    private int id;
    private String kodeSupplier;
    private String namaSupplier;
    private String alamat;
    private String telepon;
    private String email;
    private String contactPerson;
    private boolean aktif;

    public Supplier() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getKodeSupplier() { return kodeSupplier; }
    public void setKodeSupplier(String kodeSupplier) { this.kodeSupplier = kodeSupplier; }
    public String getNamaSupplier() { return namaSupplier; }
    public void setNamaSupplier(String namaSupplier) { this.namaSupplier = namaSupplier; }
    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }
    public String getTelepon() { return telepon; }
    public void setTelepon(String telepon) { this.telepon = telepon; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public boolean isAktif() { return aktif; }
    public void setAktif(boolean aktif) { this.aktif = aktif; }

    @Override
    public String toString() { return "[" + kodeSupplier + "] " + namaSupplier; }
}
