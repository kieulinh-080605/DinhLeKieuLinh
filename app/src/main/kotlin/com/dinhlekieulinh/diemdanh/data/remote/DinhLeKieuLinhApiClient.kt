package com.dinhlekieulinh.diemdanh.data.remote

import com.dinhlekieulinh.diemdanh.data.local.DinhLeKieuLinhTokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object DinhLeKieuLinhApiClient {

    // Base URL theo yêu cầu (10.0.2.2 là localhost trên Android Emulator)
    private const val BASE_URL = "http://10.0.2.2:5000/"

    private var apiService: DinhLeKieuLinhApiService? = null

    fun getApiService(tokenManager: DinhLeKieuLinhTokenManager): DinhLeKieuLinhApiService {
        return apiService ?: synchronized(this) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(DinhLeKieuLinhAuthInterceptor(tokenManager))
                .addInterceptor(DinhLeKieuLinhMockInterceptor()) // Fallback thông minh khi backend chưa chạy
                .addInterceptor(loggingInterceptor)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            retrofit.create(DinhLeKieuLinhApiService::class.java).also {
                apiService = it
            }
        }
    }
}
