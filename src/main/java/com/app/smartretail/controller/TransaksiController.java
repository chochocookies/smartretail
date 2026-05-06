package com.app.smartretail.controller;

import java.time.LocalDateTime;
import java.util.List;

import com.app.smartretail.dao.TransaksiDAO;
import com.app.smartretail.model.Transaksi;
import com.app.smartretail.utils.Session;

public class TransaksiController {

    private TransaksiDAO transaksiDAO;

    public TransaksiController() {
        this.transaksiDAO = new TransaksiDAO();
    }

    public boolean simpanPenjualan(Transaksi t) {
        if (t.getDetails().isEmpty()) return false;
        t.setTipe(Transaksi.TipeTransaksi.PENJUALAN);
        t.setUserId(Session.currentUser.getId());
        t.setNoTransaksi(transaksiDAO.generateNoTransaksi("TRX"));
        t.setTanggal(LocalDateTime.now());
        t.hitungTotal();
        return transaksiDAO.saveTransaksi(t);
    }

    public boolean simpanPembelian(Transaksi t) {
        if (t.getDetails().isEmpty()) return false;
        t.setTipe(Transaksi.TipeTransaksi.PEMBELIAN);
        t.setUserId(Session.currentUser.getId());
        t.setNoTransaksi(transaksiDAO.generateNoTransaksi("PBL"));
        t.setTanggal(LocalDateTime.now());
        t.hitungTotal();
        return transaksiDAO.saveTransaksi(t);
    }

    public List<Transaksi> getRiwayatPenjualan() {
        return transaksiDAO.getByTipe(Transaksi.TipeTransaksi.PENJUALAN);
    }

    public List<Transaksi> getRiwayatPembelian() {
        return transaksiDAO.getByTipe(Transaksi.TipeTransaksi.PEMBELIAN);
    }

    public List<Transaksi> getPenjualanByPeriode(LocalDateTime dari, LocalDateTime sampai) {
        return transaksiDAO.getPenjualanByPeriode(dari, sampai);
    }

    public String generateNoTransaksi(String prefix) {
        return transaksiDAO.generateNoTransaksi(prefix);
    }
}
