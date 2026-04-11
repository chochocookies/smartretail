# 🛒 Smart Retail Management System (SRMS)
### Aplikasi Manajemen Toko & Gudang Berbasis Java Desktop

---

## 📋 Daftar Isi
- [Tentang Proyek](#tentang-proyek)
- [Fitur Utama](#fitur-utama)
- [Teknologi](#teknologi)
- [Arsitektur](#arsitektur)
- [Struktur Proyek](#struktur-proyek)
- [Instalasi & Setup](#instalasi--setup)
- [Role & Hak Akses](#role--hak-akses)
- [Akun Default](#akun-default)
- [Roadmap](#roadmap)
- [Screenshots Flow](#screenshots-flow)

---

## 🎯 Tentang Proyek

SRMS (Smart Retail Management System) adalah aplikasi desktop manajemen toko dan gudang yang dibangun
menggunakan Java Swing dengan arsitektur MVC + DAO. Proyek ini lahir dari pengalaman nyata bekerja
di minimarket, di mana pengelolaan penjualan, stok, dan pembelian masih belum terintegrasi dengan baik.

**Tujuan:**
- Mengelola data barang & stok secara terpusat
- Mengelola transaksi penjualan (POS) & pembelian
- Menerapkan sistem login & hak akses berbasis role
- Menyediakan laporan ringkas berbasis tabel
- Menampilkan notifikasi stok minimum otomatis

---

## ✅ Fitur Utama

| No | Fitur                     | Status  | Keterangan                                    |
|----|---------------------------|---------|-----------------------------------------------|
| 1  | Login + Hashing Password  | ✅ Done | SHA-256 hashing, session management           |
| 2  | Dashboard Summary         | ✅ Done | Omzet, jumlah transaksi, stok rendah          |
| 3  | Master Data Barang        | ✅ Done | CRUD barang + pencarian                       |
| 4  | Master Kategori           | ✅ Done | CRUD kategori                                 |
| 5  | Master Supplier           | ✅ Done | CRUD supplier                                 |
| 6  | Master Customer           | ✅ Done | CRUD customer + poin                          |
| 7  | Kelola User               | ✅ Done | CRUD user + role (khusus Admin)               |
| 8  | POS Penjualan             | ✅ Done | Kasir POS, hitung kembalian, update stok auto |
| 9  | Pembelian Barang          | ✅ Done | Input pembelian + tambah stok otomatis        |
| 10 | Manajemen Stok            | ✅ Done | Adjust stok manual + monitoring               |
| 11 | Notifikasi Stok Rendah    | ✅ Done | Alert otomatis saat stok di bawah minimum     |
| 12 | Laporan Transaksi         | ✅ Done | Tabel riwayat penjualan & pembelian           |
| 13 | Role-based Access Control | ✅ Done | Admin, Kasir, Staff Gudang, Supervisor        |
| 14 | Export PDF (JasperReports)| 🔄 TODO | Perlu integrasi jasperreports.jar             |
| 15 | Grafik Penjualan          | 🔄 TODO | Perlu integrasi jfreechart.jar                |

---

## ⚙️ Teknologi

| Teknologi        | Versi       | Kegunaan                          |
|------------------|-------------|-----------------------------------|
| Java SE          | 11+         | Bahasa pemrograman utama          |
| Java Swing       | Built-in    | UI desktop (JFrame, JPanel, dll)  |
| MySQL            | 8.0+        | Database relasional               |
| JDBC             | Built-in    | Koneksi Java ↔ MySQL              |
| JasperReports    | 6.x         | Export laporan PDF (opsional)     |
| JFreeChart       | 1.5.x       | Grafik penjualan (opsional)       |
| NetBeans IDE     | 17+         | IDE yang direkomendasikan         |

---

## 🏗️ Arsitektur

```
Aplikasi ini menggunakan pola MVC + DAO:

  VIEW (Swing/JFrame)
      ↓  ↑
  CONTROLLER (logika bisnis)
      ↓  ↑
  DAO (query database JDBC)
      ↓  ↑
  MODEL (POJO/entity)
      ↓  ↑
  DATABASE (MySQL)
```

**Keunggulan pola ini:**
- Separation of Concerns: UI, logika, dan data terpisah jelas
- Mudah di-maintain dan dikembangkan
- Cocok untuk proyek skala menengah ke atas
- Mirip arsitektur aplikasi enterprise nyata

---

## 📁 Struktur Proyek

```
SmartRetailApp/
│
├── src/
│   └── com/smartretail/
│       │
│       ├── config/
│       │   └── DatabaseConnection.java     # Singleton JDBC connection
│       │
│       ├── model/                          # POJO / Entity classes
│       │   ├── User.java
│       │   ├── Barang.java
│       │   ├── Kategori.java
│       │   ├── Supplier.java
│       │   ├── Customer.java
│       │   ├── Transaksi.java
│       │   └── TransaksiDetail.java
│       │
│       ├── dao/                            # Data Access Object (semua query SQL)
│       │   ├── UserDAO.java
│       │   ├── BarangDAO.java
│       │   ├── SupplierDAO.java
│       │   ├── TransaksiDAO.java           # Transaksi + JDBC Transaction (rollback)
│       │   └── ReportDAO.java
│       │
│       ├── controller/                     # Logika bisnis
│       │   ├── AuthController.java         # Login, logout, ganti password
│       │   ├── BarangController.java
│       │   ├── TransaksiController.java    # Penjualan & pembelian
│       │   └── DashboardController.java    # Summary dashboard
│       │
│       ├── view/
│       │   ├── auth/
│       │   │   └── LoginForm.java          # Halaman login
│       │   ├── dashboard/
│       │   │   └── DashboardForm.java      # Halaman utama + sidebar menu
│       │   ├── master/
│       │   │   ├── BarangForm.java         # CRUD barang
│       │   │   ├── KategoriForm.java       # CRUD kategori
│       │   │   ├── SupplierForm.java       # CRUD supplier
│       │   │   ├── CustomerForm.java       # CRUD customer
│       │   │   └── UserForm.java           # CRUD user (admin only)
│       │   ├── transaksi/
│       │   │   ├── PenjualanForm.java      # POS penjualan
│       │   │   ├── PembelianForm.java      # Input pembelian
│       │   │   └── StokForm.java           # Adjust stok manual
│       │   └── report/
│       │       └── ReportForm.java         # Laporan & filter
│       │
│       ├── utils/
│       │   ├── PasswordHasher.java         # SHA-256 hashing
│       │   ├── Session.java                # Simpan user login
│       │   ├── AlertUtil.java              # JOptionPane helper
│       │   └── FormatUtil.java             # Format Rupiah, tanggal, dll
│       │
│       └── Main.java                       # Entry point aplikasi
│
├── docs/
│   └── database.sql                        # Schema + seed data MySQL
│
├── lib/                                    # Taruh JAR library di sini
│   ├── mysql-connector-j-8.x.x.jar        # JDBC driver MySQL
│   ├── jasperreports-6.x.x.jar            # (opsional) PDF reports
│   └── jfreechart-1.5.x.jar               # (opsional) Grafik
│
└── README.md
```

---

## 🚀 Instalasi & Setup

### Prasyarat
- Java JDK 11 atau lebih baru
- MySQL Server 8.0+
- NetBeans IDE 17+ (atau IntelliJ IDEA)
- MySQL Connector/J JAR

### Langkah Setup

**1. Clone / Extract proyek**
```bash
# Extract ZIP ke folder pilihan Anda
```

**2. Setup Database**
```sql
-- Buka MySQL client (phpMyAdmin / MySQL Workbench / CLI)
-- Jalankan file docs/database.sql

mysql -u root -p < docs/database.sql
```

**3. Konfigurasi Koneksi**

Edit file `src/com/smartretail/config/DatabaseConnection.java`:
```java
private static final String HOST     = "localhost";   // ← sesuaikan
private static final String PORT     = "3306";         // ← sesuaikan
private static final String DATABASE = "smart_retail_db";
private static final String USERNAME = "root";         // ← sesuaikan
private static final String PASSWORD = "";             // ← isi password MySQL Anda
```

**4. Tambahkan Library JAR**

Di NetBeans:
- Klik kanan project → Properties → Libraries → Add JAR/Folder
- Tambahkan `mysql-connector-j-8.x.x.jar` dari folder `lib/`
- (Opsional) tambahkan jasperreports dan jfreechart JAR jika ingin fitur lanjutan

**5. Build & Run**
```
Klik kanan Main.java → Run File
atau Shift+F6
```

---

## 👤 Role & Hak Akses

| Menu / Fitur         | Admin | Supervisor | Kasir | Staff Gudang |
|----------------------|:-----:|:----------:|:-----:|:------------:|
| Dashboard            |  ✅   |     ✅     |  ✅   |      ✅      |
| Penjualan (POS)      |  ✅   |     ✅     |  ✅   |      ❌      |
| Pembelian            |  ✅   |     ✅     |  ❌   |      ✅      |
| Manajemen Stok       |  ✅   |     ✅     |  ❌   |      ✅      |
| Master Barang        |  ✅   |     ✅     |  ❌   |      ✅      |
| Master Kategori      |  ✅   |     ✅     |  ❌   |      ❌      |
| Master Supplier      |  ✅   |     ✅     |  ❌   |      ❌      |
| Master Customer      |  ✅   |     ✅     |  ❌   |      ❌      |
| Laporan              |  ✅   |     ✅     |  ❌   |      ❌      |
| Kelola User          |  ✅   |     ❌     |  ❌   |      ❌      |

---

## 🔑 Akun Default

> Password semua akun default adalah: `admin123`

| Username    | Role          | Keterangan              |
|-------------|---------------|-------------------------|
| `admin`     | ADMIN         | Akses penuh             |
| `kasir1`    | KASIR         | Kasir toko              |
| `gudang1`   | STAFF_GUDANG  | Staf gudang             |
| `supervisor`| SUPERVISOR    | Pengawas / manajer      |

> ⚠️ **Penting:** Ganti semua password default sebelum digunakan di produksi!

---

## 🗺️ Roadmap

Lihat file `ROADMAP.md` untuk detail lengkap setiap fase pengembangan.

### Ringkasan Fase:
- **Phase 1** (Selesai): Setup dasar, login, master data, transaksi inti
- **Phase 2** (TODO): Export PDF JasperReports, Grafik JFreeChart, barcode scanner
- **Phase 3** (TODO): Multi-toko, backup otomatis, laporan lanjutan
- **Phase 4** (TODO): REST API, mobile companion app

---

## 🤝 Kontribusi

Pull request dan saran pengembangan sangat disambut baik. Silakan buka issue terlebih dahulu
untuk mendiskusikan perubahan besar yang ingin Anda lakukan.

---

## 📄 Lisensi

MIT License — bebas digunakan untuk keperluan pendidikan dan komersial.

---

*Dibuat dengan ❤️ berdasarkan pengalaman nyata dari lapangan minimarket.*
