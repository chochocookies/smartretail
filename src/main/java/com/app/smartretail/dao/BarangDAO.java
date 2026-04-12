package com.app.smartretail.dao;

import com.app.smartretail.config.DatabaseConnection;
import com.app.smartretail.model.Barang;
import java.sql.*;
import java.util.*;

public class BarangDAO {
    private Connection conn;
    public BarangDAO(){ this.conn=DatabaseConnection.getInstance(); }

    private static final String BASE_SQL =
        "SELECT b.*, k.nama_kategori, s.nama_supplier FROM barang b " +
        "LEFT JOIN kategori k ON b.kategori_id=k.id " +
        "LEFT JOIN supplier s ON b.supplier_id=s.id ";

    public List<Barang> getAll(){
        List<Barang> list=new ArrayList<>();
        try(Statement st=conn.createStatement();
            ResultSet rs=st.executeQuery(BASE_SQL+"ORDER BY b.nama_barang")){
            while(rs.next()) list.add(map(rs));
        } catch(SQLException e){System.err.println("[BarangDAO] "+e.getMessage());}
        return list;
    }

    public List<Barang> search(String kw){
        List<Barang> list=new ArrayList<>();
        String sql=BASE_SQL+"WHERE b.kode_barang LIKE ? OR b.nama_barang LIKE ? OR b.plu LIKE ? OR b.barcode LIKE ? ORDER BY b.nama_barang";
        try(PreparedStatement ps=conn.prepareStatement(sql)){
            String q="%"+kw+"%";
            ps.setString(1,q);ps.setString(2,q);ps.setString(3,q);ps.setString(4,q);
            ResultSet rs=ps.executeQuery();
            while(rs.next()) list.add(map(rs));
        } catch(SQLException e){System.err.println("[BarangDAO] "+e.getMessage());}
        return list;
    }

    public List<Barang> getStokRendah(){
        List<Barang> list=new ArrayList<>();
        try(Statement st=conn.createStatement();
            ResultSet rs=st.executeQuery(BASE_SQL+"WHERE b.stok<=b.stok_minimum")){
            while(rs.next()) list.add(map(rs));
        } catch(SQLException e){System.err.println("[BarangDAO] "+e.getMessage());}
        return list;
    }

    public Barang getByKode(String kode){
        String sql=BASE_SQL+"WHERE b.kode_barang=? OR b.plu=? OR b.barcode=?";
        try(PreparedStatement ps=conn.prepareStatement(sql)){
            ps.setString(1,kode);ps.setString(2,kode);ps.setString(3,kode);
            ResultSet rs=ps.executeQuery();
            if(rs.next()) return map(rs);
        } catch(SQLException e){System.err.println("[BarangDAO] "+e.getMessage());}
        return null;
    }

    public boolean insert(Barang b){
        String sql="INSERT INTO barang(kode_barang,plu,barcode,modis,nama_barang,kategori_id,supplier_id,harga_beli,harga_jual,stok,stok_minimum,satuan,deskripsi,image_url) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try(PreparedStatement ps=conn.prepareStatement(sql)){
            ps.setString(1,b.getKodeBarang());ps.setString(2,b.getPlu());
            ps.setString(3,b.getBarcode());ps.setString(4,b.getModis());
            ps.setString(5,b.getNamaBarang());ps.setInt(6,b.getKategoriId());
            ps.setInt(7,b.getSupplierId());ps.setDouble(8,b.getHargaBeli());
            ps.setDouble(9,b.getHargaJual());ps.setInt(10,b.getStok());
            ps.setInt(11,b.getStokMinimum());ps.setString(12,b.getSatuan());
            ps.setString(13,b.getDeskripsi());ps.setString(14,b.getImageUrl());
            return ps.executeUpdate()>0;
        } catch(SQLException e){System.err.println("[BarangDAO] insert: "+e.getMessage());return false;}
    }

    public boolean update(Barang b){
        String sql="UPDATE barang SET kode_barang=?,plu=?,nama_barang=?,kategori_id=?,supplier_id=?,harga_beli=?,harga_jual=?,stok_minimum=?,satuan=?,deskripsi=?,image_url=? WHERE id=?";
        try(PreparedStatement ps=conn.prepareStatement(sql)){
            ps.setString(1,b.getKodeBarang());ps.setString(2,b.getPlu());
            ps.setString(3,b.getNamaBarang());ps.setInt(4,b.getKategoriId());
            ps.setInt(5,b.getSupplierId());ps.setDouble(6,b.getHargaBeli());
            ps.setDouble(7,b.getHargaJual());ps.setInt(8,b.getStokMinimum());
            ps.setString(9,b.getSatuan());ps.setString(10,b.getDeskripsi());
            ps.setString(11,b.getImageUrl());ps.setInt(12,b.getId());
            return ps.executeUpdate()>0;
        } catch(SQLException e){System.err.println("[BarangDAO] update: "+e.getMessage());return false;}
    }

    public boolean updateStok(int id,int delta){
        try(PreparedStatement ps=conn.prepareStatement("UPDATE barang SET stok=stok+? WHERE id=?")){
            ps.setInt(1,delta);ps.setInt(2,id);return ps.executeUpdate()>0;
        } catch(SQLException e){System.err.println("[BarangDAO] stok: "+e.getMessage());return false;}
    }

    public boolean delete(int id){
        try(PreparedStatement ps=conn.prepareStatement("DELETE FROM barang WHERE id=?")){
            ps.setInt(1,id);return ps.executeUpdate()>0;
        } catch(SQLException e){System.err.println("[BarangDAO] delete: "+e.getMessage());return false;}
    }

    private Barang map(ResultSet rs) throws SQLException {
        Barang b=new Barang();
        b.setId(rs.getInt("id"));
        b.setKodeBarang(rs.getString("kode_barang"));
        b.setPlu(rs.getString("plu"));
        b.setBarcode(rs.getString("barcode"));
        b.setModis(rs.getString("modis"));
        b.setNamaBarang(rs.getString("nama_barang"));
        b.setKategoriId(rs.getInt("kategori_id"));
        b.setNamaKategori(rs.getString("nama_kategori"));
        b.setSupplierId(rs.getInt("supplier_id"));
        b.setNamaSupplier(rs.getString("nama_supplier"));
        b.setHargaBeli(rs.getDouble("harga_beli"));
        b.setHargaJual(rs.getDouble("harga_jual"));
        b.setStok(rs.getInt("stok"));
        b.setStokMinimum(rs.getInt("stok_minimum"));
        b.setSatuan(rs.getString("satuan"));
        b.setDeskripsi(rs.getString("deskripsi"));
        try{b.setImageUrl(rs.getString("image_url"));}catch(Exception ignored){}
        return b;
    }
}
