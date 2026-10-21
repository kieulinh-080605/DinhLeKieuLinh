package com.dinhlekieulinh.diemdanh.data.repository

import com.dinhlekieulinh.diemdanh.data.local.DinhLeKieuLinhTokenManager
import com.dinhlekieulinh.diemdanh.data.model.*
import com.dinhlekieulinh.diemdanh.data.remote.DinhLeKieuLinhApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DinhLeKieuLinhAuthRepository(
    private val apiService: DinhLeKieuLinhApiService,
    private val tokenManager: DinhLeKieuLinhTokenManager
) {

    val tokenFlow: Flow<String?> = tokenManager.tokenFlow
    val roleFlow: Flow<DinhLeKieuLinhRole?> = tokenManager.roleFlow
    val currentUserFlow: Flow<DinhLeKieuLinhUser?> = tokenManager.currentUserFlow

    // Đăng nhập bước 1 (nhận OTP)
    fun login(usernameOrEmail: String, password: String): Flow<DinhLeKieuLinhResource<DinhLeKieuLinhOtpResponse>> = flow {
        emit(DinhLeKieuLinhResource.Loading)
        try {
            val response = apiService.login(DinhLeKieuLinhLoginRequest(usernameOrEmail, password))
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                emit(DinhLeKieuLinhResource.Success(body.data))
            } else {
                emit(DinhLeKieuLinhResource.Error(body?.message ?: "Đăng nhập thất bại"))
            }
        } catch (e: Exception) {
            emit(DinhLeKieuLinhResource.Error("Lỗi kết nối: ${e.localizedMessage}", e))
        }
    }

    // Đăng ký tài khoản
    fun register(request: DinhLeKieuLinhRegisterRequest): Flow<DinhLeKieuLinhResource<DinhLeKieuLinhUser>> = flow {
        emit(DinhLeKieuLinhResource.Loading)
        try {
            val response = apiService.register(request)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                emit(DinhLeKieuLinhResource.Success(body.data))
            } else {
                emit(DinhLeKieuLinhResource.Error(body?.message ?: "Đăng ký thất bại"))
            }
        } catch (e: Exception) {
            emit(DinhLeKieuLinhResource.Error("Lỗi kết nối: ${e.localizedMessage}", e))
        }
    }

    // Xác thực OTP và lưu JWT Token
    fun verifyOtp(email: String, otp: String): Flow<DinhLeKieuLinhResource<DinhLeKieuLinhAuthResponse>> = flow {
        emit(DinhLeKieuLinhResource.Loading)
        try {
            val response = apiService.verifyOtp(DinhLeKieuLinhVerifyOtpRequest(email, otp))
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                val authData = body.data
                tokenManager.saveAuthSession(authData.token, authData.user)
                emit(DinhLeKieuLinhResource.Success(authData))
            } else {
                emit(DinhLeKieuLinhResource.Error(body?.message ?: "Mã OTP không chính xác"))
            }
        } catch (e: Exception) {
            emit(DinhLeKieuLinhResource.Error("Lỗi kết nối: ${e.localizedMessage}", e))
        }
    }

    // Gửi lại OTP
    fun resendOtp(email: String): Flow<DinhLeKieuLinhResource<Boolean>> = flow {
        emit(DinhLeKieuLinhResource.Loading)
        try {
            val response = apiService.resendOtp(DinhLeKieuLinhResendOtpRequest(email))
            emit(DinhLeKieuLinhResource.Success(response.isSuccessful))
        } catch (e: Exception) {
            emit(DinhLeKieuLinhResource.Error("Lỗi gửi lại OTP: ${e.localizedMessage}", e))
        }
    }

    // Đăng nhập sinh trắc học
    fun biometricLogin(email: String, biometricToken: String): Flow<DinhLeKieuLinhResource<DinhLeKieuLinhAuthResponse>> = flow {
        emit(DinhLeKieuLinhResource.Loading)
        try {
            val response = apiService.biometricLogin(DinhLeKieuLinhBiometricLoginRequest(email, biometricToken))
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                val authData = body.data
                tokenManager.saveAuthSession(authData.token, authData.user)
                emit(DinhLeKieuLinhResource.Success(authData))
            } else {
                emit(DinhLeKieuLinhResource.Error(body?.message ?: "Xác thực sinh trắc học thất bại"))
            }
        } catch (e: Exception) {
            emit(DinhLeKieuLinhResource.Error("Lỗi kết nối: ${e.localizedMessage}", e))
        }
    }

    suspend fun saveBiometricToken(token: String, enabled: Boolean) {
        tokenManager.setBiometricToken(token, enabled)
    }

    suspend fun isBiometricEnabled(): Boolean = tokenManager.isBiometricEnabled()
    suspend fun getBiometricToken(): String? = tokenManager.getBiometricToken()

    suspend fun logout() {
        tokenManager.clearSession()
    }
}
