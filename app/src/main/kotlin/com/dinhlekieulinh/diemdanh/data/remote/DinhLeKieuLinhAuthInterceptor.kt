package com.dinhlekieulinh.diemdanh.data.remote

import com.dinhlekieulinh.diemdanh.data.local.DinhLeKieuLinhTokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class DinhLeKieuLinhAuthInterceptor(
    private val tokenManager: DinhLeKieuLinhTokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")

        // Lấy JWT Bearer Token lưu trong DataStore
        val token = runBlocking { tokenManager.getToken() }
        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}
