package com.dinhlekieulinh.diemdanh.ui.screens.sinhvien

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinhlekieulinh.diemdanh.data.model.*
import com.dinhlekieulinh.diemdanh.data.repository.DinhLeKieuLinhAuthRepository
import com.dinhlekieulinh.diemdanh.data.repository.DinhLeKieuLinhBuoiHocRepository
import com.dinhlekieulinh.diemdanh.data.repository.DinhLeKieuLinhDiemDanhRepository
import com.dinhlekieulinh.diemdanh.ui.components.DinhLeKieuLinhLocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DinhLeKieuLinhSinhVienUiState(
    val currentUser: DinhLeKieuLinhUser? = null,
    val todaySessions: List<DinhLeKieuLinhBuoiHoc> = emptyList(),
    val attendanceHistory: List<DinhLeKieuLinhDiemDanh> = emptyList(),
    val stats: DinhLeKieuLinhThongKeDiemDanh? = null,
    val isLoading: Boolean = false,
    val isScanning: Boolean = false,
    val scanSuccessResult: DinhLeKieuLinhDiemDanh? = null,
    val scanErrorMessage: String? = null,
    val userCurrentLat: Double = 10.762624,
    val userCurrentLng: Double = 106.682230,
    val isBiometricEnabled: Boolean = false,
    val generalMessage: String? = null
)

class DinhLeKieuLinhSinhVienViewModel(
    private val authRepository: DinhLeKieuLinhAuthRepository,
    private val buoiHocRepository: DinhLeKieuLinhBuoiHocRepository,
    private val diemDanhRepository: DinhLeKieuLinhDiemDanhRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DinhLeKieuLinhSinhVienUiState())
    val uiState: StateFlow<DinhLeKieuLinhSinhVienUiState> = _uiState.asStateFlow()

    init {
        loadStudentData()
    }

    fun loadStudentData() {
        viewModelScope.launch {
            authRepository.currentUserFlow.collect { user ->
                _uiState.update { it.copy(currentUser = user) }
                if (user != null) {
                    loadSessions()
                    loadHistory(user.id)
                    loadStats(user.id)
                }
            }
        }
        viewModelScope.launch {
            val bio = authRepository.isBiometricEnabled()
            _uiState.update { it.copy(isBiometricEnabled = bio) }
        }
    }

    private fun loadSessions() {
        viewModelScope.launch {
            buoiHocRepository.getBuoiHocs().collect { res ->
                if (res is DinhLeKieuLinhResource.Success) {
                    _uiState.update { it.copy(todaySessions = res.data) }
                }
            }
        }
    }

    private fun loadHistory(sinhVienId: Int) {
        viewModelScope.launch {
            diemDanhRepository.getDiemDanhs(sinhVienId = sinhVienId).collect { res ->
                if (res is DinhLeKieuLinhResource.Success) {
                    _uiState.update { it.copy(attendanceHistory = res.data) }
                }
            }
        }
    }

    private fun loadStats(sinhVienId: Int) {
        viewModelScope.launch {
            diemDanhRepository.getThongKeDiemDanh(sinhVienId).collect { res ->
                if (res is DinhLeKieuLinhResource.Success) {
                    _uiState.update { it.copy(stats = res.data) }
                }
            }
        }
    }

    // Lấy vị trí GPS thực tế từ Fused Location Provider
    fun updateLocation(context: Context) {
        viewModelScope.launch {
            val loc = DinhLeKieuLinhLocationHelper.getCurrentLocation(context)
            if (loc != null) {
                _uiState.update { it.copy(userCurrentLat = loc.latitude, userCurrentLng = loc.longitude) }
            }
        }
    }

    // Điểm danh bằng QR Code + GPS định vị bán kính 100m
    fun submitAttendance(qrToken: String, selectedBuoiHocId: Int) {
        val s = _uiState.value
        val buoiHoc = s.todaySessions.find { it.id == selectedBuoiHocId } ?: s.todaySessions.firstOrNull()

        if (buoiHoc == null) {
            _uiState.update { it.copy(scanErrorMessage = "Không tìm thấy buổi học tương ứng!") }
            return
        }

        // Kiểm tra bán kính GPS 100m
        val distance = DinhLeKieuLinhLocationHelper.calculateDistanceMeters(
            s.userCurrentLat, s.userCurrentLng,
            buoiHoc.viTriLat, buoiHoc.viTriLng
        )

        if (distance > buoiHoc.banKinh) {
            _uiState.update {
                it.copy(
                    scanErrorMessage = "Điểm danh không hợp lệ: Bạn đang ở cách lớp học ${distance.toInt()}m, vượt quá bán kính quy định ${buoiHoc.banKinh.toInt()}m!"
                )
            }
            return
        }

        val request = DinhLeKieuLinhScanQrRequest(
            buoiHocId = buoiHoc.id,
            qrToken = qrToken,
            viTriLat = s.userCurrentLat,
            viTriLng = s.userCurrentLng
        )

        viewModelScope.launch {
            diemDanhRepository.scanQrAttendance(request).collect { res ->
                when (res) {
                    is DinhLeKieuLinhResource.Loading -> _uiState.update { it.copy(isScanning = true, scanErrorMessage = null) }
                    is DinhLeKieuLinhResource.Success -> {
                        _uiState.update {
                            it.copy(
                                isScanning = false,
                                scanSuccessResult = res.data,
                                scanErrorMessage = null
                            )
                        }
                        s.currentUser?.let {
                            loadHistory(it.id)
                            loadStats(it.id)
                        }
                    }
                    is DinhLeKieuLinhResource.Error -> {
                        _uiState.update { it.copy(isScanning = false, scanErrorMessage = res.message) }
                    }
                    is DinhLeKieuLinhResource.Idle -> Unit
                }
            }
        }
    }

    fun dismissScanResult() {
        _uiState.update { it.copy(scanSuccessResult = null, scanErrorMessage = null) }
    }

    fun toggleBiometric(enabled: Boolean) {
        viewModelScope.launch {
            val token = if (enabled) "BIO_KEY_${System.currentTimeMillis()}" else ""
            authRepository.saveBiometricToken(token, enabled)
            _uiState.update { it.copy(isBiometricEnabled = enabled, generalMessage = if (enabled) "Đã bật đăng nhập vân tay" else "Đã tắt đăng nhập vân tay") }
        }
    }

    fun clearGeneralMessage() {
        _uiState.update { it.copy(generalMessage = null) }
    }
}
