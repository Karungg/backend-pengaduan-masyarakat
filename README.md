# Pengaduan Masyarakat (E-Government)

Aplikasi Pengaduan Masyarakat ini adalah sebuah sistem yang memfasilitasi proses pelaporan, pengelolaan, dan peninjauan keluhan publik. Aplikasi ini menghubungkan tiga entitas utama: **Masyarakat** (sebagai pelapor), **Admin** (sebagai verifikator), dan **Instansi** (sebagai penindak lanjut).

Proyek ini dibangun sebagai bagian dari pemenuhan tugas akhir mata kuliah **Rekayasa Perangkat Lunak (RPL)**. Namun, tujuan jangka panjangnya adalah agar aplikasi ini dapat menjadi prototype, memberikan inspirasi, atau bahkan dikembangkan lebih lanjut untuk menjadi solusi nyata yang bermanfaat bagi pelayanan publik di Indonesia.

## Fitur

### 1. Admin

- **Manajemen Data Masyarakat (User):** Mengelola akun pengguna yang terdaftar.
- **Manajemen Data Kategori:** Menambah, mengubah, atau menghapus kategori pengaduan (misal: Infrastruktur, Kesehatan, Pendidikan).
- **Manajemen Data Instansi:** Mengelola daftar instansi atau dinas yang akan menerima laporan.
- **Manajemen Data Pengaduan:** Memverifikasi, memvalidasi pengaduan yang masuk dari masyarakat.

### 2.Instansi

- **Manajemen Data Pengaduan:** Menerima, melihat, dan menindaklanjuti pengaduan yang relevan berdasarkan instansinya.
- **Memberikan Tanggapan:** Memperbarui status pengaduan (misal: "Diproses", "Selesai") dan memberikan tanggapan atas laporan.

### 3. Masyarakat

- **Autentikasi:** Melakukan registrasi dan login ke dalam sistem.
- **Mengajukan Pengaduan:** Membuat dan mengirimkan laporan pengaduan baru sesuai dengan kategori yang tersedia.
- **Memantau Status:** Melihat riwayat dan melacak status dari pengaduan yang telah diajukan.

## Arsitektur

- **Frontend:** React.js
- **Backend:** Spring Boot
- **Autentikasi:** JSON Web Token (JWT)
- **Database:** [MySQL]
