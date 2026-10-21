package com.dinhlekieulinh.diemdanh.data.remote

import com.dinhlekieulinh.diemdanh.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface DinhLeKieuLinhApiService {

    // ==========================================
    // 1. XÁC THỰC & TÀI KHOẢN (/api/auth)
    // ==========================================
    @POST("api/auth/login")
    suspend fun login(
        @Body request: DinhLeKieuLinhLoginRequest
    ): Response<DinhLeKieuLinhApiResponse<DinhLeKieuLinhOtpResponse>>

    @POST("api/auth/register")
    suspend fun register(
        @Body request: DinhLeKieuLinhRegisterRequest
    ): Response<DinhLeKieuLinhApiResponse<DinhLeKieuLinhUser>>

    @POST("api/auth/verify-otp")
    suspend fun verifyOtp(
        @Body request: DinhLeKieuLinhVerifyOtpRequest
    ): Response<DinhLeKieuLinhApiResponse<DinhLeKieuLinhAuthResponse>>

    @POST("api/auth/resend-otp")
    suspend fun resendOtp(
        @Body request: DinhLeKieuLinhResendOtpRequest
    ): Response<DinhLeKieuLinhApiResponse<Boolean>>

    @POST("api/auth/biometric-login")
    suspend fun biometricLogin(
        @Body request: DinhLeKieuLinhBiometricLoginRequest
    ): Response<DinhLeKieuLinhApiResponse<DinhLeKieuLinhAuthResponse>>

    @GET("api/auth/me")
    suspend fun getCurrentUser(): Response<DinhLeKieuLinhApiResponse<DinhLeKieuLinhUser>>

    // ==========================================
    // 2. QUẢN LÝ NGƯỜI DÙNG (/api/users)
    // ==========================================
    @GET("api/users")
    suspend fun getUsers(
        @Query("search") search: String? = null,
        @Query("role") role: String? = null
    ): Response<DinhLeKieuLinhApiResponse<List<DinhLeKieuLinhUser>>>

    @GET("api/users/{id}")
    suspend fun getUserById(@Path("id") id: Int): Response<DinhLeKieuLinhApiResponse<DinhLeKieuLinhUser>>

    @POST("api/users")
    suspend fun createUser(@Body request: DinhLeKieuLinhUserRequest): Response<DinhLeKieuLinhApiResponse<DinhLeKieuLinhUser>>

    @PUT("api/users/{id}")
    suspend fun updateUser(
        @Path("id") id: Int,
        @Body request: DinhLeKieuLinhUserRequest
    ): Response<DinhLeKieuLinhApiResponse<DinhLeKieuLinhUser>>

    @DELETE("api/users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Response<DinhLeKieuLinhApiResponse<Boolean>>

    // ==========================================
    // 3. QUẢN LÝ MÔN HỌC (/api/monhoc)
    // ==========================================
    @GET("api/monhoc")
    suspend fun getMonHocs(
        @Query("search") search: String? = null
    ): Response<DinhLeKieuLinhApiResponse<List<DinhLeKieuLinhMonHoc>>>

    @GET("api/monhoc/{id}")
    suspend fun getMonHocById(@Path("id") id: Int): Response<DinhLeKieuLinhApiResponse<DinhLeKieuLinhMonHoc>>

    @POST("api/monhoc")
    suspend fun createMonHoc(@Body request: DinhLeKieuLinhMonHocRequest): Response<DinhLeKieuLinhApiResponse<DinhLeKieuLinhMonHoc>>

    @PUT("api/monhoc/{id}")
    suspend fun updateMonHoc(
        @Path("id") id: Int,
        @Body request: DinhLeKieuLinhMonHocRequest
    ): Response<DinhLeKieuLinhApiResponse<DinhLeKieuLinhMonHoc>>

    @DELETE("api/monhoc/{id}")
    suspend fun deleteMonHoc(@Path("id") id: Int): Response<DinhLeKieuLinhApiResponse<Boolean>>

    // ==========================================
    // 4. QUẢN LÝ LỚP HỌC (/api/lophoc)
    // ==========================================
    @GET("api/lophoc")
    suspend fun getLopHocs(
        @Query("search") search: String? = null,
        @Query("giangVienId") giangVienId: Int? = null
    ): Response<DinhLeKieuLinhApiResponse<List<DinhLeKieuLinhLopHoc>>>

    @GET("api/lophoc/{id}")
    suspend fun getLopHocById(@Path("id") id: Int): Response<DinhLeKieuLinhApiResponse<DinhLeKieuLinhLopHoc>>

    @POST("api/lophoc")
    suspend fun createLopHoc(@Body request: DinhLeKieuLinhLopHocRequest): Response<DinhLeKieuLinhApiResponse<DinhLeKieuLinhLopHoc>>

    @PUT("api/lophoc/{id}")
    suspend fun updateLopHoc(
        @Path("id") id: Int,
        @Body request: DinhLeKieuLinhLopHocRequest
    ): Response<DinhLeKieuLinhApiResponse<DinhLeKieuLinhLopHoc>>

    @DELETE("api/lophoc/{id}")
    suspend fun deleteLopHoc(@Path("id") id: Int): Response<DinhLeKieuLinhApiResponse<Boolean>>

    // ==========================================
    // 5. QUẢN LÝ BUỔI HỌC (/api/buoihoc)
    // ==========================================
    @GET("api/buoihoc")
    suspend fun getBuoiHocs(
        @Query("lopHocId") lopHocId: Int? = null,
        @Query("ngayHoc") ngayHoc: String? = null
    ): Response<DinhLeKieuLinhApiResponse<List<DinhLeKieuLinhBuoiHoc>>>

    @GET("api/buoihoc/{id}")
    suspend fun getBuoiHocById(@Path("id") id: Int): Response<DinhLeKieuLinhApiResponse<DinhLeKieuLinhBuoiHoc>>

    @POST("api/buoihoc")
    suspend fun createBuoiHoc(@Body request: DinhLeKieuLinhBuoiHocRequest): Response<DinhLeKieuLinhApiResponse<DinhLeKieuLinhBuoiHoc>>

    @PUT("api/buoihoc/{id}")
    suspend fun updateBuoiHoc(
        @Path("id") id: Int,
        @Body request: DinhLeKieuLinhBuoiHocRequest
    ): Response<DinhLeKieuLinhApiResponse<DinhLeKieuLinhBuoiHoc>>

    @DELETE("api/buoihoc/{id}")
    suspend fun deleteBuoiHoc(@Path("id") id: Int): Response<DinhLeKieuLinhApiResponse<Boolean>>

    // Tính năng nâng cao: Lấy Dynamic QR Code đổi mỗi 30s
    @GET("api/buoihoc/{id}/generate-qr")
    suspend fun generateDynamicQr(@Path("id") id: Int): Response<DinhLeKieuLinhApiResponse<DinhLeKieuLinhDynamicQr>>

    // ==========================================
    // 6. ĐIỂM DANH (/api/diemdanh)
    // ==========================================
    @GET("api/diemdanh")
    suspend fun getDiemDanhs(
        @Query("buoiHocId") buoiHocId: Int? = null,
        @Query("sinhVienId") sinhVienId: Int? = null
    ): Response<DinhLeKieuLinhApiResponse<List<DinhLeKieuLinhDiemDanh>>>

    @POST("api/diemdanh/scan-qr")
    suspend fun scanQrAttendance(
        @Body request: DinhLeKieuLinhScanQrRequest
    ): Response<DinhLeKieuLinhApiResponse<DinhLeKieuLinhDiemDanh>>

    @POST("api/diemdanh/manual")
    suspend fun manualAttendance(
        @Body request: DinhLeKieuLinhManualDiemDanhRequest
    ): Response<DinhLeKieuLinhApiResponse<DinhLeKieuLinhDiemDanh>>

    @PUT("api/diemdanh/{id}")
    suspend fun updateDiemDanh(
        @Path("id") id: Int,
        @Body request: DinhLeKieuLinhManualDiemDanhRequest
    ): Response<DinhLeKieuLinhApiResponse<DinhLeKieuLinhDiemDanh>>

    @DELETE("api/diemdanh/{id}")
    suspend fun deleteDiemDanh(@Path("id") id: Int): Response<DinhLeKieuLinhApiResponse<Boolean>>

    // Thống kê chuyên cần và cảnh báo vắng >= 3 buổi
    @GET("api/diemdanh/thong-ke/{sinhVienId}")
    suspend fun getThongKeDiemDanh(
        @Path("sinhVienId") sinhVienId: Int
    ): Response<DinhLeKieuLinhApiResponse<DinhLeKieuLinhThongKeDiemDanh>>
}
