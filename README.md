# JudStop

**JudStop** adalah aplikasi Android yang dirancang untuk membantu pengguna mengurangi dan menghentikan kebiasaan judi online. Aplikasi ini memanfaatkan Android Accessibility Service untuk secara proaktif mendeteksi dan mencegah akses ke situs-situs judi online yang teridentifikasi, serta menyediakan dukungan melalui chatbot AI yang terintegrasi dengan Azure OpenAI (GPT-4.1).

## Daftar Isi
- [Tangkapan Layar](#tangkapan-layar)
- [Tujuan Proyek](#tujuan-proyek)
- [Fitur Utama](#fitur-utama)
- [Teknologi yang Digunakan](#teknologi-yang-digunakan)
- [Prasyarat](#prasyarat)
- [Cara Menjalankan Proyek](#cara-menjalankan-proyek)

## Tangkapan Layar

<table>
  <tbody>
    <tr>
      <td><img src="assets/screenshot/ss_1.jpeg?raw=true"/></td>
      <td><img src="assets/screenshot/ss_2.jpeg?raw=true"/></td>
      <td><img src="assets/screenshot/ss_3.jpeg?raw=true"/></td>
    </tr>
  </tbody>
</table>

## Tujuan Proyek

Proyek ini bertujuan untuk:
1.   Menyediakan alat bagi pengguna di Indonesia untuk melawan kecanduan judi online.
2.   Meningkatkan kesadaran tentang dampak negatif judi online.
3.   Memberikan dukungan motivasional melalui fitur chatbot AI.

## Fitur Utama

1.   **Pencegahan ke Situs Judi Otomatis:** Menggunakan Layanan Aksesibilitas untuk mendeteksi kata kunci dan URL terkait judi di browser populer (Chrome, Firefox, dll.) dan secara otomatis mencegah untuk masuk ke situs.
2.   **Statistik Pencegahan:** Menampilkan jumlah situs yang telah berhasil dicegah oleh aplikasi.
3.   **Chatbot AI:** Terintegrasi dengan Azure OpenAI (GPT-4.1) untuk menyediakan chatbot yang memberikan dukungan emosional, tips, dan motivasi untuk berhenti berjudi. Chatbot dirancang untuk tetap fokus pada topik menghindari judi.
4.   **Notifikasi:** Memberikan notifikasi saat situs berhasil dicegah.

## Teknologi yang Digunakan

*   **Bahasa Pemrograman:** Kotlin
*   **UI Toolkit:** Jetpack Compose
*   **Arsitektur:** MVVM (Model-View-ViewModel)
*   **Layanan Latar Belakang:** Android Accessibility Service
*   **Networking (untuk Chatbot):** Retrofit, OkHttp, Gson
*   **AI Chatbot:** Azure OpenAI Service (GPT-4.1 Deployment)
*   **Penyimpanan Lokal:** SharedPreferences

## Prasyarat

*   Android Studio (versi terbaru direkomendasikan)
*   Perangkat Android atau Emulator
*   (Untuk fitur Chatbot AI) Kunci API Azure Cognitive Services yang valid dan endpoint yang sesuai.

## Cara Menjalankan Proyek

1.  **Clone repository ini:**
    ```bash
    git clone https://github.com/harissabil/JudStop.git
    cd NAMA_REPO_ANDA
    ```
2.  **Buka proyek di Android Studio.**
3.  **(PENTING untuk Chatbot)** Konfigurasi API Key Azure OpenAI:
    *   Cara yang disarankan adalah menyimpannya di file `local.properties` (buat jika belum ada di root proyek Anda) dengan format:
        ```properties
        AZURE_API_KEY_BEARER="YOUR_AZURE_OPENAI_API_KEY_WITHOUT_BEARER"
        BASE_URL="https://your_resource_name.cognitiveservices.azure.com/"
        ```
    *   Kemudian, sesuaikan `RetrofitClient.kt` untuk membaca nilai-nilai ini dari `BuildConfig` (setelah menambahkan konfigurasi di `build.gradle` file modul).
    *   **Untuk pengembangan cepat (TIDAK DISARANKAN UNTUK PRODUKSI):** Jika Anda masih dalam tahap pengembangan awal dan ingin cepat, Anda bisa langsung mengisi nilai `AZURE_API_KEY_BEARER`, `AZURE_DEPLOYMENT_NAME`, dan `BASE_URL` di file `com.judstop.app.ui.chat.network.RetrofitClient.kt`. **Ingat untuk menghapusnya sebelum commit ke repository publik.**
4.  **Sinkronkan Gradle files.**
5.  **Build dan jalankan aplikasi** pada perangkat atau emulator.
6.  Setelah aplikasi berjalan, Anda akan diminta untuk **mengaktifkan Layanan Aksesibilitas** untuk JudStop melalui Pengaturan sistem.
