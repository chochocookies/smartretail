package com.app.smartretail.controller;

import com.app.smartretail.dao.BarangDAO;
import com.app.smartretail.model.Barang;

import java.util.List;

public class BarangController {

    private BarangDAO barangDAO;

    public BarangController() {
        this.barangDAO = new BarangDAO();
    }

    public List<Barang> getAllBarang() {
        return barangDAO.getAll();
    }

    public List<Barang> searchBarang(String keyword) {
        return barangDAO.search(keyword);
    }

    public List<Barang> getStokRendah() {
        return barangDAO.getStokRendah();
    }

    public Barang getByKode(String kode) {
        return barangDAO.getByKode(kode);
    }

    public boolean tambahBarang(Barang b) {
        if (b.getNamaBarang() == null || b.getNamaBarang().isBlank()) return false;
        if (b.getHargaJual() <= 0) return false;
        return barangDAO.insert(b);
    }

    public boolean updateBarang(Barang b) {
        return barangDAO.update(b);
    }

    public boolean hapusBarang(int id) {
        return barangDAO.delete(id);
    }

    public boolean updateStok(int barangId, int jumlah) {
        return barangDAO.updateStok(barangId, jumlah);
    }

    /**
     * Generate kode barang otomatis
     */
    public String generateKode() {
        return "BRG-" + System.currentTimeMillis() % 100000;
    }
}
