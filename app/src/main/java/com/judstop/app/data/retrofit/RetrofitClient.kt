package com.judstop.app.data.retrofit

import com.judstop.app.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // ---- PERINGATAN KEAMANAN ----
    // JANGAN GUNAKAN CARA INI DI APLIKASI PRODUKSI!
    // API Key harus disimpan dengan aman.
    const val AZURE_API_KEY_BEARER = "Bearer ${BuildConfig.AZURE_API_KEY_BEARER}"
    const val AZURE_DEPLOYMENT_NAME = "gpt-4.1" // Sesuaikan dengan nama deployment yang digunakan
    // ---- AKHIR PERINGATAN ----

    private const val BASE_URL = BuildConfig.BASE_URL

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Untuk debugging, log request/response
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS) // Waktu tunggu koneksi lebih lama
        .readTimeout(60, TimeUnit.SECONDS)    // Waktu tunggu baca lebih lama
        .writeTimeout(60, TimeUnit.SECONDS)   // Waktu tunggu tulis lebih lama
        .build()

    val instance: AzureAiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(AzureAiApiService::class.java)
    }
}