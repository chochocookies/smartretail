package com.app.smartretail.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.app.smartretail.dao.BarangDAO;
import com.app.smartretail.dao.ReportDAO;
import com.app.smartretail.model.Barang;

public class DashboardController {

    private ReportDAO reportDAO;
    private BarangDAO barangDAO;

    public DashboardController() {
        this.reportDAO = new ReportDAO();
        this.barangDAO = new BarangDAO();
    }

    public Map<String, Object> getSummary() {
        return reportDAO.getDashboardSummary();
    }

    public List<Barang> getStokRendah() {
        return barangDAO.getStokRendah();
    }

    public Map<String, Double> getOmzetMingguIni() {
        LocalDateTime sampai = LocalDateTime.now();
        LocalDateTime dari   = sampai.minusDays(6);
        return reportDAO.getOmzetPerHari(dari, sampai);
    }

    public List<Object[]> getBarangTerlaris(int limit) {
        return reportDAO.getBarangTerlaris(limit);
    }
}
