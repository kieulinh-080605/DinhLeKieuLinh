package com.dinhlekieulinh.diemdanh.data.model

import com.google.gson.annotations.SerializedName

// Request đăng nhập bước 1
data class DinhLeKieuLinhLoginRequest(
    @SerializedName("usernameOrEmail") val usernameOrEmail: String,
    @SerializedName("password") val password: String
)

// Request đăng ký tài khoản
data class DinhLeKieuLinhRegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("hoTen") val hoTen: String,
    @SerializedName("maSo") val maSo: String,
    @SerializedName("soDienThoai") val soDienThoai: String,
    @SerializedName("role") val role: DinhLeKieuLinhRole = DinhLeKieuLinhRole.SINH_VIEN
)

// Request xác thực OTP
data class DinhLeKieuLinhVerifyOtpRequest(
    @SerializedName("email") val email: String,
    @SerializedName("otp") val otp: String
)

// Request gửi lại OTP
data class DinhLeKieuLinhResendOtpRequest(
    @SerializedName("email") val email: String
)

// Request đăng nhập sinh trắc học
data class DinhLeKieuLinhBiometricLoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("biometricToken") val biometricToken: String
)

// Phản hồi sau khi xác thực OTP thành công (trả về JWT Token)
data class DinhLeKieuLinhAuthResponse(
    @SerializedName("token") val token: String = "",
    @SerializedName("tokenType") val tokenType: String = "Bearer",
    @SerializedName("user") val user: DinhLeKieuLinhUser,
    @SerializedName("message") val message: String? = null
)

// Phản hồi sau khi đăng nhập bước 1 yêu cầu nhập OTP
data class DinhLeKieuLinhOtpResponse(
    @SerializedName("requiresOtp") val requiresOtp: Boolean = true,
    @SerializedName("email") val email: String = "",
    @SerializedName("message") val message: String = "",
    @SerializedName("otpCode") val otpCode: String? = null // Mã OTP ngẫu nhiên từ server/mock
)
