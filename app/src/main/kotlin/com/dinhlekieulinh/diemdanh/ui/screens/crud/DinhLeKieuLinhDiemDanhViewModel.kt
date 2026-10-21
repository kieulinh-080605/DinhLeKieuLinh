package com.dinhlekieulinh.diemdanh.ui.screens.crud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinhlekieulinh.diemdanh.data.model.*
import com.dinhlekieulinh.diemdanh.data.repository.DinhLeKieuLinhBuoiHocRepository
import com.dinhlekieulinh.diemdanh.data.repository.DinhLeKieuLinhDiemDanhRepository
import com.dinhlekieulinh.diemdanh.data.repository.DinhLeKieuLinhUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DinhLeKieuLinhDiemDanhUiState(
    val diemDanhs: List<DinhLeKieuLinhDiemDanh> = emptyList(),
    val availableBuoiHocs: List<DinhLeKieuLinhBuoiHoc> = emptyList(),
    val availableSinhViens: List<DinhLeKieuLinhUser> = emptyList(),
    val selectedBuoiHocId: Int? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    // Form Điểm danh bù / Cập nhật
    val isFormDialogOpen: Boolean = false,
    val editingDiemDanh: DinhLeKieuLinhDiemDanh? = null,
    val formBuoiHocId: Int = 1,
    val formSinhVienId: Int = 3,
    val formTrangThai: DinhLeKieuLinhTrangThaiDiemDanh = DinhLeKieuLinhTrangThaiDiemDanh.CO_MAT,
    val formGhiChu: String = "",
    // Xóa lượt điểm danh
    val deletingDiemDanh: DinhLeKieuLinhDiemDanh? = null
)

class DinhLeKieuLinhDiemDanhViewModel(
    private val diemDanhRepository: DinhLeKieuLinhDiemDanhRepository,
    private val buoiHocRepository: DinhLeKieuLinhBuoiHocRepository,
    private val userRepository: DinhLeKieuLinhUserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DinhLeKieuLinhDiemDanhUiState())
    val uiState: StateFlow<DinhLeKieuLinhDiemDanhUiState> = _uiState.asStateFlow()

    init {
        loadDiemDanhs()
        loadBuoiHocs()
        loadSinhViens()
    }

    fun loadDiemDanhs() {
        val buoiId = _uiState.value.selectedBuoiHocId
        viewModelScope.launch {
            diemDanhRepository.getDiemDanhs(buoiId).collect { res ->
                when (res) {
                    is DinhLeKieuLinhResource.Loading -> _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    is DinhLeKieuLinhResource.Success -> _uiState.update { it.copy(isLoading = false, diemDanhs = res.data) }
                    is DinhLeKieuLinhResource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                    is DinhLeKieuLinhResource.Idle -> Unit
                }
            }
        }
    }

    private fun loadBuoiHocs() {
        viewModelScope.launch {
            buoiHocRepository.getBuoiHocs().collect { res ->
                if (res is DinhLeKieuLinhResource.Success) {
                    _uiState.update { it.copy(availableBuoiHocs = res.data) }
                }
            }
        }
    }

    private fun loadSinhViens() {
        viewModelScope.launch {
            userRepository.getUsers(role = DinhLeKieuLinhRole.SINH_VIEN.name).collect { res ->
                if (res is DinhLeKieuLinhResource.Success) {
                    _uiState.update { it.copy(availableSinhViens = res.data) }
                }
            }
        }
    }

    fun onFilterBuoiHocChanged(buoiId: Int?) {
        _uiState.update { it.copy(selectedBuoiHocId = buoiId) }
        loadDiemDanhs()
    }

    fun openManualAttendanceDialog() {
        val defaultBuoi = _uiState.value.availableBuoiHocs.firstOrNull()?.id ?: 1
        val defaultSv = _uiState.value.availableSinhViens.firstOrNull()?.id ?: 3
        _uiState.update {
            it.copy(
                isFormDialogOpen = true,
                editingDiemDanh = null,
                formBuoiHocId = defaultBuoi,
                formSinhVienId = defaultSv,
                formTrangThai = DinhLeKieuLinhTrangThaiDiemDanh.CO_MAT,
                formGhiChu = "Giảng viên điểm danh bù"
            )
        }
    }

    fun openEditAttendanceDialog(record: DinhLeKieuLinhDiemDanh) {
        _uiState.update {
            it.copy(
                isFormDialogOpen = true,
                editingDiemDanh = record,
                formBuoiHocId = record.buoiHocId,
                formSinhVienId = record.sinhVienId,
                formTrangThai = record.trangThai,
                formGhiChu = record.ghiChu
            )
        }
    }

    fun closeFormDialog() {
        _uiState.update { it.copy(isFormDialogOpen = false) }
    }

    fun onFormBuoiHocIdChange(id: Int) = _uiState.update { it.copy(formBuoiHocId = id) }
    fun onFormSinhVienIdChange(id: Int) = _uiState.update { it.copy(formSinhVienId = id) }
    fun onFormTrangThaiChange(s: DinhLeKieuLinhTrangThaiDiemDanh) = _uiState.update { it.copy(formTrangThai = s) }
    fun onFormGhiChuChange(v: String) = _uiState.update { it.copy(formGhiChu = v) }

    fun saveAttendance() {
        val s = _uiState.value
        val request = DinhLeKieuLinhManualDiemDanhRequest(
            buoiHocId = s.formBuoiHocId,
            sinhVienId = s.formSinhVienId,
            trangThai = s.formTrangThai,
            ghiChu = s.formGhiChu.trim()
        )

        viewModelScope.launch {
            if (s.editingDiemDanh == null) {
                diemDanhRepository.manualAttendance(request).collect { res ->
                    when (res) {
                        is DinhLeKieuLinhResource.Loading -> _uiState.update { it.copy(isLoading = true) }
                        is DinhLeKieuLinhResource.Success -> {
                            _uiState.update { it.copy(isLoading = false, isFormDialogOpen = false, successMessage = "Điểm danh thủ công thành công") }
                            loadDiemDanhs()
                        }
                        is DinhLeKieuLinhResource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                        is DinhLeKieuLinhResource.Idle -> Unit
                    }
                }
            } else {
                diemDanhRepository.updateDiemDanh(s.editingDiemDanh.id, request).collect { res ->
                    when (res) {
                        is DinhLeKieuLinhResource.Loading -> _uiState.update { it.copy(isLoading = true) }
                        is DinhLeKieuLinhResource.Success -> {
                            _uiState.update { it.copy(isLoading = false, isFormDialogOpen = false, successMessage = "Cập nhật lượt điểm danh thành công") }
                            loadDiemDanhs()
                        }
                        is DinhLeKieuLinhResource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                        is DinhLeKieuLinhResource.Idle -> Unit
                    }
                }
            }
        }
    }

    fun promptDelete(record: DinhLeKieuLinhDiemDanh) {
        _uiState.update { it.copy(deletingDiemDanh = record) }
    }

    fun cancelDelete() {
        _uiState.update { it.copy(deletingDiemDanh = null) }
    }

    fun confirmDelete() {
        val record = _uiState.value.deletingDiemDanh ?: return
        viewModelScope.launch {
            diemDanhRepository.deleteDiemDanh(record.id).collect { res ->
                when (res) {
                    is DinhLeKieuLinhResource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is DinhLeKieuLinhResource.Success -> {
                        _uiState.update { it.copy(isLoading = false, deletingDiemDanh = null, successMessage = "Hủy lượt điểm danh thành công") }
                        loadDiemDanhs()
                    }
                    is DinhLeKieuLinhResource.Error -> _uiState.update { it.copy(isLoading = false, deletingDiemDanh = null, errorMessage = res.message) }
                    is DinhLeKieuLinhResource.Idle -> Unit
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
