package com.dinhlekieulinh.diemdanh

import android.app.Application
import com.dinhlekieulinh.diemdanh.data.local.DinhLeKieuLinhTokenManager
import com.dinhlekieulinh.diemdanh.data.remote.DinhLeKieuLinhApiClient
import com.dinhlekieulinh.diemdanh.data.remote.DinhLeKieuLinhApiService
import com.dinhlekieulinh.diemdanh.data.repository.*

class DinhLeKieuLinhApplication : Application() {

    lateinit var tokenManager: DinhLeKieuLinhTokenManager
        private set

    lateinit var apiService: DinhLeKieuLinhApiService
        private set

    lateinit var authRepository: DinhLeKieuLinhAuthRepository
        private set

    lateinit var userRepository: DinhLeKieuLinhUserRepository
        private set

    lateinit var monHocRepository: DinhLeKieuLinhMonHocRepository
        private set

    lateinit var lopHocRepository: DinhLeKieuLinhLopHocRepository
        private set

    lateinit var buoiHocRepository: DinhLeKieuLinhBuoiHocRepository
        private set

    lateinit var diemDanhRepository: DinhLeKieuLinhDiemDanhRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Khởi tạo tầng DataStore & Web API Client
        tokenManager = DinhLeKieuLinhTokenManager(applicationContext)
        apiService = DinhLeKieuLinhApiClient.getApiService(tokenManager)

        // Khởi tạo các Repositories
        authRepository = DinhLeKieuLinhAuthRepository(apiService, tokenManager)
        userRepository = DinhLeKieuLinhUserRepository(apiService)
        monHocRepository = DinhLeKieuLinhMonHocRepository(apiService)
        lopHocRepository = DinhLeKieuLinhLopHocRepository(apiService)
        buoiHocRepository = DinhLeKieuLinhBuoiHocRepository(apiService)
        diemDanhRepository = DinhLeKieuLinhDiemDanhRepository(apiService)
    }

    companion object {
        lateinit var instance: DinhLeKieuLinhApplication
            private set
    }
}
