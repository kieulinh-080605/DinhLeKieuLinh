package com.dinhlekieulinh.diemdanh.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinhlekieulinh.diemdanh.data.model.*
import com.dinhlekieulinh.diemdanh.data.repository.DinhLeKieuLinhAuthRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// State màn hình đăng nhập
data class DinhLeKieuLinhLoginUiState(
    val usernameOrEmail: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isBiometricAvailable: Boolean = false
)

// State màn hình đăng ký
data class DinhLeKieuLinhRegisterUiState(
    val hoTen: String = "",
    val maSo: String = "",
    val email: String = "",
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val soDienThoai: String = "",
    val role: DinhLeKieuLinhRole = DinhLeKieuLinhRole.SINH_VIEN,
    val isPasswordVisible: Boolean = false,
    val hoTenError: String? = null,
    val maSoError: String? = null,
    val emailError: String? = null,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

// State màn hình xác thực OTP
data class DinhLeKieuLinhOtpUiState(
    val email: String = "",
    val otp: String = "",
    val generatedOtp: String? = null, // Mã OTP ngẫu nhiên hiển thị để người dùng/thầy cô test dễ dàng
    val countdownSeconds: Int = 60,
    val isResendEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val otpError: String? = null
)

class DinhLeKieuLinhAuthViewModel(
    private val authRepository: DinhLeKieuLinhAuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow(DinhLeKieuLinhLoginUiState())
    val loginState: StateFlow<DinhLeKieuLinhLoginUiState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow(DinhLeKieuLinhRegisterUiState())
    val registerState: StateFlow<DinhLeKieuLinhRegisterUiState> = _registerState.asStateFlow()

    private val _otpState = MutableStateFlow(DinhLeKieuLinhOtpUiState())
    val otpState: StateFlow<DinhLeKieuLinhOtpUiState> = _otpState.asStateFlow()

    private var countdownJob: Job? = null

    init {
        checkBiometricSupport()
    }

    private fun checkBiometricSupport() {
        viewModelScope.launch {
            val enabled = authRepository.isBiometricEnabled()
            _loginState.update { it.copy(isBiometricAvailable = enabled) }
        }
    }

    // ==========================================
    // CÁC HÀM XỬ LÝ ĐĂNG NHẬP
    // ==========================================
    fun onLoginUsernameChanged(value: String) {
        _loginState.update { it.copy(usernameOrEmail = value, usernameError = null, errorMessage = null) }
    }

    fun onLoginPasswordChanged(value: String) {
        _loginState.update { it.copy(password = value, passwordError = null, errorMessage = null) }
    }

    fun togglePasswordVisibility() {
        _loginState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    // Tiện ích chọn tài khoản mẫu (Demo) cho người chấm
    fun fillDemoAccount(role: DinhLeKieuLinhRole) {
        when (role) {
            DinhLeKieuLinhRole.ADMIN -> {
                _loginState.update { it.copy(usernameOrEmail = "admin@dinhlekieulinh.edu.vn", password = "adminpassword123") }
            }
            DinhLeKieuLinhRole.GIANG_VIEN -> {
                _loginState.update { it.copy(usernameOrEmail = "gv.nam@dinhlekieulinh.edu.vn", password = "gvpassword123") }
            }
            DinhLeKieuLinhRole.SINH_VIEN -> {
                _loginState.update { it.copy(usernameOrEmail = "sv.linh@dinhlekieulinh.edu.vn", password = "svpassword123") }
            }
        }
    }

    fun login(onNavigateToOtp: (email: String) -> Unit) {
        val state = _loginState.value
        var hasError = false

        if (state.usernameOrEmail.isBlank()) {
            _loginState.update { it.copy(usernameError = "Vui lòng nhập tên đăng nhập hoặc email") }
            hasError = true
        }

        if (state.password.isBlank()) {
            _loginState.update { it.copy(passwordError = "Vui lòng nhập mật khẩu") }
            hasError = true
        } else if (state.password.length < 6) {
            _loginState.update { it.copy(passwordError = "Mật khẩu phải có ít nhất 6 ký tự") }
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            authRepository.login(state.usernameOrEmail, state.password).collect { resource ->
                when (resource) {
                    is DinhLeKieuLinhResource.Loading -> {
                        _loginState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is DinhLeKieuLinhResource.Success -> {
                        _loginState.update { it.copy(isLoading = false) }
                        val email = resource.data.email.ifBlank { state.usernameOrEmail }
                        val generatedOtp = resource.data.otpCode
                        startOtpCountdown(email, generatedOtp)
                        onNavigateToOtp(email)
                    }
                    is DinhLeKieuLinhResource.Error -> {
                        _loginState.update { it.copy(isLoading = false, errorMessage = resource.message) }
                    }
                    is DinhLeKieuLinhResource.Idle -> Unit
                }
            }
        }
    }

    // ==========================================
    // CÁC HÀM XỬ LÝ ĐĂNG KÝ
    // ==========================================
    fun onRegisterHoTenChanged(v: String) = _registerState.update { it.copy(hoTen = v, hoTenError = null) }
    fun onRegisterMaSoChanged(v: String) = _registerState.update { it.copy(maSo = v, maSoError = null) }
    fun onRegisterEmailChanged(v: String) = _registerState.update { it.copy(email = v, emailError = null) }
    fun onRegisterUsernameChanged(v: String) = _registerState.update { it.copy(username = v, usernameError = null) }
    fun onRegisterPasswordChanged(v: String) = _registerState.update { it.copy(password = v, passwordError = null) }
    fun onRegisterConfirmPasswordChanged(v: String) = _registerState.update { it.copy(confirmPassword = v, confirmPasswordError = null) }
    fun onRegisterSoDienThoaiChanged(v: String) = _registerState.update { it.copy(soDienThoai = v) }
    fun onRegisterRoleChanged(v: DinhLeKieuLinhRole) = _registerState.update { it.copy(role = v) }
    fun toggleRegisterPasswordVisibility() = _registerState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

    fun register(onSuccess: () -> Unit) {
        val s = _registerState.value
        var error = false

        if (s.hoTen.isBlank()) {
            _registerState.update { it.copy(hoTenError = "Vui lòng nhập họ và tên") }
            error = true
        }
        if (s.maSo.isBlank()) {
            _registerState.update { it.copy(maSoError = "Vui lòng nhập mã số (MSSV/MSGV)") }
            error = true
        }
        if (s.email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(s.email).matches()) {
            _registerState.update { it.copy(emailError = "Email không hợp lệ") }
            error = true
        }
        if (s.username.isBlank()) {
            _registerState.update { it.copy(usernameError = "Vui lòng nhập tên đăng nhập") }
            error = true
        }
        if (s.password.length < 6) {
            _registerState.update { it.copy(passwordError = "Mật khẩu tối thiểu 6 ký tự") }
            error = true
        }
        if (s.password != s.confirmPassword) {
            _registerState.update { it.copy(confirmPasswordError = "Mật khẩu xác nhận không khớp") }
            error = true
        }

        if (error) return

        val request = DinhLeKieuLinhRegisterRequest(
            username = s.username.trim(),
            email = s.email.trim(),
            password = s.password,
            hoTen = s.hoTen.trim(),
            maSo = s.maSo.trim(),
            soDienThoai = s.soDienThoai.trim(),
            role = s.role
        )

        viewModelScope.launch {
            authRepository.register(request).collect { res ->
                when (res) {
                    is DinhLeKieuLinhResource.Loading -> {
                        _registerState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is DinhLeKieuLinhResource.Success -> {
                        _registerState.update { it.copy(isLoading = false, isSuccess = true) }
                        onSuccess()
                    }
                    is DinhLeKieuLinhResource.Error -> {
                        _registerState.update { it.copy(isLoading = false, errorMessage = res.message) }
                    }
                    is DinhLeKieuLinhResource.Idle -> Unit
                }
            }
        }
    }

    // ==========================================
    // CÁC HÀM XỬ LÝ MÃ OTP
    // ==========================================
    fun onOtpChanged(value: String) {
        if (value.length <= 6) {
            _otpState.update { it.copy(otp = value, otpError = null, errorMessage = null) }
        }
    }

    fun startOtpCountdown(email: String, generatedOtp: String? = null) {
        _otpState.update {
            it.copy(
                email = email,
                otp = "",
                generatedOtp = generatedOtp ?: it.generatedOtp,
                countdownSeconds = 60,
                isResendEnabled = false,
                errorMessage = null,
                otpError = null
            )
        }
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (sec in 60 downTo 1) {
                delay(1000)
                _otpState.update { it.copy(countdownSeconds = sec - 1) }
            }
            _otpState.update { it.copy(isResendEnabled = true) }
        }
    }

    fun resendOtp() {
        val email = _otpState.value.email
        if (email.isBlank()) return

        viewModelScope.launch {
            authRepository.resendOtp(email).collect { res ->
                if (res is DinhLeKieuLinhResource.Success) {
                    startOtpCountdown(email)
                } else if (res is DinhLeKieuLinhResource.Error) {
                    _otpState.update { it.copy(errorMessage = res.message) }
                }
            }
        }
    }

    fun verifyOtp(onSuccess: (user: DinhLeKieuLinhUser) -> Unit) {
        val s = _otpState.value
        if (s.otp.length != 6) {
            _otpState.update { it.copy(otpError = "Mã OTP gồm 6 chữ số") }
            return
        }

        viewModelScope.launch {
            authRepository.verifyOtp(s.email, s.otp).collect { res ->
                when (res) {
                    is DinhLeKieuLinhResource.Loading -> {
                        _otpState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is DinhLeKieuLinhResource.Success -> {
                        _otpState.update { it.copy(isLoading = false) }
                        countdownJob?.cancel()
                        onSuccess(res.data.user)
                    }
                    is DinhLeKieuLinhResource.Error -> {
                        _otpState.update { it.copy(isLoading = false, errorMessage = res.message) }
                    }
                    is DinhLeKieuLinhResource.Idle -> Unit
                }
            }
        }
    }

    // ==========================================
    // ĐĂNG NHẬP SINH TRẮC HỌC (BIOMETRIC)
    // ==========================================
    fun biometricLogin(email: String, biometricToken: String, onSuccess: (user: DinhLeKieuLinhUser) -> Unit) {
        viewModelScope.launch {
            authRepository.biometricLogin(email, biometricToken).collect { res ->
                when (res) {
                    is DinhLeKieuLinhResource.Loading -> {
                        _loginState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is DinhLeKieuLinhResource.Success -> {
                        _loginState.update { it.copy(isLoading = false) }
                        onSuccess(res.data.user)
                    }
                    is DinhLeKieuLinhResource.Error -> {
                        _loginState.update { it.copy(isLoading = false, errorMessage = res.message) }
                    }
                    is DinhLeKieuLinhResource.Idle -> Unit
                }
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLoggedOut()
        }
    }
}
