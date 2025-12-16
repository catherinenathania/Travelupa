# 🌍 Travelupa - Aplikasi Pengelolaan Destinasi Wisata

Travelupa adalah aplikasi Android modern yang dirancang untuk membantu administrator mengelola informasi destinasi wisata. Aplikasi ini menggabungkan teknologi penyimpanan awan (Firebase) dan penyimpanan lokal (Room) untuk memberikan pengalaman yang lancar dalam pengelolaan data dan galeri gambar.

---

## ✨ Fitur Utama

- **Autentikasi Admin**: Log masuk yang aman menggunakan Firebase Authentication.
- **Pengelolaan Destinasi (CRUD)**: Menambah, melihat, dan menghapus data destinasi wisata secara real-time menggunakan Firebase Firestore.
- **Penyimpanan Gambar Cloud**: Mengunggah gambar destinasi ke Firebase Storage.
- **Galeri Lokal (Room Database)**: Menyimpan catatan gambar yang dipilih secara lokal untuk akses cepat.
- **Integrasi Kamera (CameraX)**: Mengambil gambar secara langsung dari aplikasi untuk ditambahkan ke galeri.
- **Navigasi Modern**: Perpindahan antar layar yang lancar menggunakan Jetpack Compose Navigation.

---

## 🚀 Teknologi yang Digunakan

Aplikasi ini dibangun menggunakan **Jetpack Compose** dengan arsitektur Android yang direkomendasikan:

- **Bahasa**: Kotlin
- **UI Framework**: Jetpack Compose (Declarative UI)
- **Database (Cloud)**: Firebase Firestore & Storage
- **Database (Local)**: Room Persistence Library
- **Authentication**: Firebase Auth
- **Hardware Access**: CameraX API
- **Image Loading**: Coil (Compose Image Loader)
- **Asynchronous**: Kotlin Coroutines & Flow
- **Dependency Management**: Gradle (Kotlin DSL)

---

## 🛠️ Cara Menjalankan Proyek

1. **Clone Repository**:
   ```bash
   git clone https://github.com/catherinenathania/Travelupa.git
   ```

2. **Firebase Setup**:
* Daftarkan proyek di Firebase Console.
* Unduh file `google-services.json` dan letakkan di dalam folder `app/`.
* Aktifkan Firestore, Authentication (Email/Password), dan Storage.


3. **Build Project**:
* Buka proyek di Android Studio (Ladybug atau versi terbaru).
* Sinkronkan (Sync) Gradle dan jalankan pada Emulator atau perangkat fisik.



---

## 📂 Struktur Folder Utama `MainActivity.kt`: Logika navigasi dan alur utama aplikasi.
* `AppDatabase.kt`: Konfigurasi Room Database.
* `ImageDao.kt`: Data Access Object untuk operasi database lokal.
* `ui.theme/`: Pengelolaan tema, warna, dan tipografi aplikasi.

---

## 👤 Identitas Mahasiswa **Nama**: Catherine Nathania [235150201111042]
* **Mata Kuliah**: Pengembangan Aplikasi Perangkat Bergerak (PAPB) 2025
* **Dosen Pengampu**: Ir. Adam Hendra Brata, S.Kom., M.T., M.Sc.

---

© 2025 Travelupa Project - Dibuat untuk memenuhi tugas Proyek Individu.

