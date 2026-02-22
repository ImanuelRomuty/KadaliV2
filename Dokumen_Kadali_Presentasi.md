# KADALI: Smart Energy Management System
**Dokumen Presentasi Teknis & Spesifikasi Sistem**

---

## 1. Ringkasan Eksekutif
**Kadali** adalah sebuah sistem cerdas modern yang dirancang untuk melakukan pemantauan, analisis, dan simulasi penggunaan energi listrik (Electrical Usage Analysis System). Aplikasi ini dikembangkan untuk memfasilitasi manajemen energi tingkat lanjut—baik untuk lingkungan perkantoran, fasilitas pabrik, hingga residensial.

Sistem Kadali menerapkan paradigma **"Data-First"** yang memungkinkan pengkalkulasian estimasi biaya secara otomatis berdasarkan metrik perangkat nyata (Daya/Watt dan Jam Pemakaian). Dengan fitur-fitur seperti Proyeksi Biaya, Klasifikasi Beban, serta Laporan PDF Analitis, Kadali berfungsi layaknya Sistem Audit Energi Digital.

---

## 2. Kapabilitas Inti Sistem

Pemanfaatan sistem Kadali memberikan keunggulan teknis operasional dalam berbagai aspek pengelolaan aset kelistrikan:

1. **Optimalisasi Biaya Terukur:** Menyediakan transparansi data kelistrikan yang memungkinkan identifikasi area maupun perangkat spesifik penyerap daya terbesar.
2. **Generasi Laporan Audit PDF (12-Section PDF):** Sistem secara otomasi mampu menyusun laporan analitik mendalam—sempurna untuk dilaporkan secara berkala kepada pihak manajemen sebagai lembar pertanggungjawaban terstandarisasi.
3. **Mesin Simulasi Interaktif:** Fasilitas simulasi proyektif guna menilai dampak akuisisi perangkat kelistrikan baru terhadap rencana anggaran (misal: penambahan suplai daya pendingin udara pada ruangan tertentu).
4. **Resiliensi Operasi Luring (Offline Resilience):** Meskipun memanfaatkan metode antarmuka *Cloud-First*, arsitektur data Kadali menjamin ketersediaan akses dan kemampuan mutasi data ketika gawai tidak mendapatkan koneksi internet, lalu menyinkronkannya kembali tanpa interupsi.

---

## 3. Arsitektur Fungsional

### 📊 A. Dashboard Analitik Real-Time
Panel pusat kontrol data energi yang dapat dipantau dari satu titik rujukan:

* **Total Monthly Cost:** Kalkulasi matematis instan estimasi biaya keseluruhan bulanan (disajikan dalam format IDR).
* **Energy Distribution Chart:** Alat visualisasi komparatif (*Bar Chart*) untuk konsumsi energi (kWh) antar klaster ruangan secara dinamis.
* **Smart Room List:** Repositori daftar ruangan dengan fungsi pencarian langsung (*Live Search*) dan pengurutan kronologis.

### 🏢 B. Manajemen Aset & Spesifikasi Perangkat
Pendataan entitas kelistrikan yang sistematis berdasarkan letak topologi geografisnya.

* **Tree Structure Tracking:** Setiap profil perangkat secara presisi terkait pada lokasi ruangan spesifik (contoh: Gudang -> Mesin Produksi 1).
* **Detailed Parameter Recording:** Sistem menyimpan parameter spesifik seperti: Tipe Perangkat, Beban Listrik Primer (Watt), Jumlah Beban Identik (Qty), dan Estimasi Pemakaian (Hour).

### 🧮 C. Mesin Simulasi (Simulation Engine)
Utilitas komputasi beban listrik prediktif.

* **Input Variabel Bebas:** Penyesuaian skenario penambahan Watt dan durasi pemutaran slider (0-24 Jam). Sistem seketika mengkalkulasi impak harga tambahan secara proporsional.
* **Indikator Skala Impak:** Kategorisasi visual interaktif ("HIGH IMPACT" atau "LOW IMPACT") guna menunjang keputusan akuisisi secara cepat.

### 📈 D. Generator Laporan Audit PDF (12-Bagian)
Engine reporting terpadu standar tingkat lanjut yang membangkitkan dokumen dari dalam perangkat *(Native Generation)*. Cakupan laporan mencakup:

1. **Informasi Awal Laporan:** Detail metadata dan profil fasilitas pelaporan.
2. **Konfigurasi Tarif:** Baseline harga listrik yang digunakan (Rp/kWh).
3. **Ringkasan Laporan Keseluruhan:** Ekstraksi agregat biaya agregat.
4. **Direktori Ruang Operasional:** Kompilasi beban distribusi parsial.
5. **Inventaris Alat per Zona:** Breakdown alat terperinci.
6. **Top 5 Largest Node:** Penelusuran otomatis 5 titik perangkat yang menyerap daya terbanyak dalam siklus 24 jam.
7. **Sektor Konsumsi Tertinggi:** Identifikasi otomatis klaster ruangan terefektif dan terboros.
8. **Technical Device Analysis:** Detail metrik Daya Tersambung *(Connected Load)*.
9. **Klasifikasi Beban Standar:** Pengkategorian tingkat kerawanan operasi mesin.
10. **⚡ Dynamic Energy Recommendations:** Rangkuman kecerdasan algoritma sistem yang berwujud narasi teks. Memberikan rekomendasi secara gramatikal sesuai dengan temuan kejanggalan dalam konsumsi (contoh: peringatan atas *critical load*).
11. **Parameter Interpretasi & Batasan Pertanggungjawaban (Disclaimer)**.

---

## 4. Alur Interaksi Pengguna (User Journey)

Flow Chart ini merepresentasikan siklus keterlibatan dan peta rute di dalam aplikasi:

```mermaid
graph TD
    %% Styling Standard
    classDef startPage fill:#1A202C,stroke:#E2E8F0,stroke-width:2px,color:#fff;
    classDef mainNav fill:#2B6CB0,stroke:#63B3ED,stroke-width:2px,color:#fff;
    classDef subPage fill:#2C5282,stroke:#4299E1,stroke-width:2px,color:#fff;
    classDef action fill:#D69E2E,stroke:#FAF089,stroke-width:2px,color:#fff;
    classDef greenAction fill:#2F855A,stroke:#9AE6B4,stroke-width:2px,color:#fff;

    A["Inisialisasi Aplikasi (Kadali)"] --> B{"Main Bottom Navigation"}
    class A startPage;

    B --> C["Dashboard Utama"]
    B --> D["Simulasi Komputasi"]
    B --> E["Panel Konfigurasi"]
    class C,D,E mainNav;

    %% Alur Dashboard & Ruangan
    C --> C1("Evaluasi Rangkuman Biaya & Charts")
    C --> C2("Eksplorasi Katalog Ruangan")
    
    C2 -->|Add New| C3["Formulir Registrasi Ruangan"]
    class C3 action;
    
    C2 -->|Pilih Item| C4["Detail Aset Ruangan"]
    class C4 subPage;

    %% Alur Detail Ruangan (Room Detail)
    C4 -->|Update Details| C3
    C4 --> C5("Monitor Tabel Parameter Perangkat")
    
    C4 -->|Add New| C6["Formulir Tambah Instrumen"]
    C5 -->|Edit Item| C6
    class C6 action;

    %% Alur Ekstraksi PDF
    C --> F(( "TRIGGER PDF GENERATOR" ))
    F --> F1["Kalkulasi Agregat Analitik Otomatis"]
    F1 --> F2["Menyimpan ke Format /KadaliReports"]
    class F,F1,F2 greenAction;

    %% Alur Simulasi
    D --> D1("Memasukkan Variabel Beban Simulasi")
    D1 --> D2("Keluaran Hasil Impact & Estimasi")

    %% Alur Pengaturan (Settings)
    E --> E1["Menetapkan Variabel Konstanta (Tarif Listrik)"]
    E --> E2["Profil Identitas Aplikasi"]
```

---

## 5. Diagram Relasi Entitas Data (ERD)

Arsitektur ruang basis data dalam Kadali dioptimalkan secara *Cloud-First* menggunakan Firestore dari Firebase. Relasi dari koleksi tersebut dapat disimulasikan sebagai ERD berikut ini:

```mermaid
erDiagram
    ROOM {
        string id PK Document ID
        string name Nama Ruangan
        string description Deskripsi Opsional
        long createdAt Waktu Inisiasi Timestamp
    }
    
    DEVICE {
        string id PK Document ID
        string roomId FK Rujukan Parameter Ruangan
        string name Representasi Identitas Perangkat
        double powerWatt Daya Primer Node Listrik
        double usageHoursPerDay Interval Siklus Catu Daya
        int quantity Kuantitas Populasi Perangkat
    }

    TARIFF {
        int id PK Konstanta Tunggal Opsional
        double pricePerKwh Ketetapan Biaya Finansial 
    }

    ROOM ||--o{ DEVICE : mencakup
```

---

## 6. Spesifikasi Infrastruktur (Tech-Stack Foundation)

Sistem internal Kadali dibangun menggunakan himpunan standar rekayasa *software mobility* modern kelas satu, memastikan kinerja dan ekspansi terjamin untuk skala panjang:

* **Application Language:** 100% Kotlin—sepenuhnya tangguh di bawah kompiler keamanan tipe dengan eksekusi mutakhir.
* **Design Paradigm:** MVVM (Model-View-ViewModel) dipadukan dengan implementasi Clean Architecture demi kepastian modularitas dan perbaikan *bug/issue* secara tertutup.
* **State Management:** Reactive Data Flow berbasis `Kotlin Coroutines` & `Flow` guna menangkap perubahan parameter di UI sedetik setelah *Database* diperbarui, tanpa adanya penundaan *lag* pembacaan ulang (Asynchronous Architecture).
* **Database Cloud Platform:** Firebase Cloud Firestore (NoSQL basis dokumen) yang sangat *scalable*—tersinkronisasi secara seketika (*realtime-push*) dengan kemampuan penyimpanan luring.
* **User Interface Framework:** Sistem adaptif desain komponen "Material Design 3" dikombinasikan pemetaan tema palet "Electric Dark" untuk estetik dan ergonomi mata tinggi dan interaksi adaptif premium.
* **Graphic Rendering Engine:** Integrasi pustaka *MPAndroidChart* spesialis grafis visual komputasional berbasis *vector*, dan kanvas khusus internal pembuatan dokumen (PDF).

---

## 7. Estimasi Biaya Pembuatan (Development Cost Estimate)

Estimasi biaya berikut merupakan proyeksi komersial untuk pengembangan sistem perangkat lunak *Smart Energy Management* Kadali. Penawaran ini berupa penyerahan produk Minimum Viable Product (MVP) dengan opsi serah terima *Source Code* sepenuhnya.

| Komponen Pengembangan | Rincian Pekerjaan & Infrastruktur | Estimasi Biaya (IDR) |
| :--- | :--- | :--- |
| **Android Native App (Kotlin)** | Pemrograman Antarmuka (UI/UX), Arsitektur Sistem MVVM, Mesin Komputasi Simulasi & Database Dasar | Rp 4.000.000,- |
| **PDF Analytics Engine** | Pengembangan *Native Rendering*, Logika Filter Data, Analisis Kompleks Multi-Page & Format Bukti Cetak | Rp 500.000,- |
| **TOTAL BIAYA PENGEMBANGAN** | *(Termasuk Dokumen Arsitektur Teknis, ERD, Flow Chart)* | **Rp 4.500.000,-** |

**Catatan Pelaksanaan:** 
* Sistem aplikasi Kadali ini dikembangkan dan diserahkan dalam bentuk versi awal **MVP (Minimum Viable Product)** yang ditujukan sebagai basis uji coba operasional atau kelayakan lapangan.
* Proses serah terima *Full Source Code* akan diberikan secara keseluruhan *(handover)* kepada Klien setelah serah terima sistem aplikasi dinyatakan final. Klien diberikan otoritas penuh jika ingin mengembangkan aplikasi di tahap pengembangan selanjutnya.
