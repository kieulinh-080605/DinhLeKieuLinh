package com.dinhlekieulinh.diemdanh.ui.screens.crud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinhlekieulinh.diemdanh.data.model.*
import com.dinhlekieulinh.diemdanh.data.repository.DinhLeKieuLinhBuoiHocRepository
import com.dinhlekieulinh.diemdanh.data.repository.DinhLeKieuLinhLopHocRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DinhLeKieuLinhBuoiHocUiState(
    val buoiHocs: List<DinhLeKieuLinhBuoiHoc> = emptyList(),
    val availableLopHocs: List<DinhLeKieuLinhLopHoc> = emptyList(),
    val selectedLopHocId: Int? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    // Dialog Thêm / Sửa
    val isFormDialogOpen: Boolean = false,
    val editingBuoiHoc: DinhLeKieuLinhBuoiHoc? = null,
    val formLopHocId: Int = 1,
    val formNgayHoc: String = "2026-10-22",
    val formGioBatDau: String = "07:30",
    val formGioKetThuc: String = "11:30",
    val formPhongHoc: String = "A1-402",
    val formNoiDung: String = "",
    val formViTriLat: String = "10.762622",
    val formViTriLng: String = "106.682229",
    val formBanKinh: String = "100.0",
    val formTrangThai: DinhLeKieuLinhTrangThaiBuoiHoc = DinhLeKieuLinhTrangThaiBuoiHoc.CHUA_BAT_DAU,
    val formNgayHocError: String? = null,
    val formPhongHocError: String? = null,
    // Dialog Dynamic QR Code (Chức năng nâng cao & Tính năng mới)
    val dynamicQrBuoiHoc: DinhLeKieuLinhBuoiHoc? = null,
    val currentDynamicQr: DinhLeKieuLinhDynamicQr? = null,
    val qrCountdownSeconds: Int = 30,
    // Dialog xóa
    val deletingBuoiHoc: DinhLeKieuLinhBuoiHoc? = null
)

class DinhLeKieuLinhBuoiHocViewModel(
    private val buoiHocRepository: DinhLeKieuLinhBuoiHocRepository,
    private val lopHocRepository: DinhLeKieuLinhLopHocRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DinhLeKieuLinhBuoiHocUiState())
    val uiState: StateFlow<DinhLeKieuLinhBuoiHocUiState> = _uiState.asStateFlow()

    private var qrRefreshJob: Job? = null

    init {
        loadBuoiHocs()
        loadLopHocs()
    }

    fun loadBuoiHocs() {
        val lopId = _uiState.value.selectedLopHocId
        viewModelScope.launch {
            buoiHocRepository.getBuoiHocs(lopId).collect { res ->
                when (res) {
                    is DinhLeKieuLinhResource.Loading -> _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    is DinhLeKieuLinhResource.Success -> _uiState.update { it.copy(isLoading = false, buoiHocs = res.data) }
                    is DinhLeKieuLinhResource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                    is DinhLeKieuLinhResource.Idle -> Unit
                }
            }
        }
    }

    private fun loadLopHocs() {
        viewModelScope.launch {
            lopHocRepository.getLopHocs().collect { res ->
                if (res is DinhLeKieuLinhResource.Success) {
                    _uiState.update { it.copy(availableLopHocs = res.data) }
                }
            }
        }
    }

    fun onFilterLopHocChanged(lopId: Int?) {
        _uiState.update { it.copy(selectedLopHocId = lopId) }
        loadBuoiHocs()
    }

    fun openCreateDialog() {
        val defaultLopId = _uiState.value.availableLopHocs.firstOrNull()?.id ?: 1
        _uiState.update {
            it.copy(
                isFormDialogOpen = true,
                editingBuoiHoc = null,
                formLopHocId = defaultLopId,
                formNgayHoc = "2026-10-22",
                formGioBatDau = "07:30",
                formGioKetThuc = "11:30",
                formPhongHoc = "A1-402",
                formNoiDung = "",
                formViTriLat = "10.762622",
                formViTriLng = "106.682229",
                formBanKinh = "100.0",
                formTrangThai = DinhLeKieuLinhTrangThaiBuoiHoc.CHUA_BAT_DAU,
                formNgayHocError = null,
                formPhongHocError = null
            )
        }
    }

    fun openEditDialog(bh: DinhLeKieuLinhBuoiHoc) {
        _uiState.update {
            it.copy(
                isFormDialogOpen = true,
                editingBuoiHoc = bh,
                formLopHocId = bh.lopHocId,
                formNgayHoc = bh.ngayHoc,
                formGioBatDau = bh.gioBatDau,
                formGioKetThuc = bh.gioKetThuc,
                formPhongHoc = bh.phongHoc,
                formNoiDung = bh.noiDung,
                formViTriLat = bh.viTriLat.toString(),
                formViTriLng = bh.viTriLng.toString(),
                formBanKinh = bh.banKinh.toString(),
                formTrangThai = bh.trangThai,
                formNgayHocError = null,
                formPhongHocError = null
            )
        }
    }

    fun closeFormDialog() {
        _uiState.update { it.copy(isFormDialogOpen = false) }
    }

    fun onFormLopHocIdChange(id: Int) = _uiState.update { it.copy(formLopHocId = id) }
    fun onFormNgayHocChange(v: String) = _uiState.update { it.copy(formNgayHoc = v, formNgayHocError = null) }
    fun onFormGioBatDauChange(v: String) = _uiState.update { it.copy(formGioBatDau = v) }
    fun onFormGioKetThucChange(v: String) = _uiState.update { it.copy(formGioKetThuc = v) }
    fun onFormPhongHocChange(v: String) = _uiState.update { it.copy(formPhongHoc = v, formPhongHocError = null) }
    fun onFormNoiDungChange(v: String) = _uiState.update { it.copy(formNoiDung = v) }
    fun onFormTrangThaiChange(s: DinhLeKieuLinhTrangThaiBuoiHoc) = _uiState.update { it.copy(formTrangThai = s) }

    fun saveBuoiHoc() {
        val s = _uiState.value
        var hasErr = false

        if (s.formNgayHoc.isBlank()) {
            _uiState.update { it.copy(formNgayHocError = "Vui lòng nhập ngày học") }
            hasErr = true
        }
        if (s.formPhongHoc.isBlank()) {
            _uiState.update { it.copy(formPhongHocError = "Vui lòng nhập phòng học") }
            hasErr = true
        }

        if (hasErr) return

        val request = DinhLeKieuLinhBuoiHocRequest(
            lopHocId = s.formLopHocId,
            ngayHoc = s.formNgayHoc.trim(),
            gioBatDau = s.formGioBatDau.trim(),
            gioKetThuc = s.formGioKetThuc.trim(),
            phongHoc = s.formPhongHoc.trim(),
            noiDung = s.formNoiDung.trim(),
            viTriLat = s.formViTriLat.toDoubleOrNull() ?: 10.762622,
            viTriLng = s.formViTriLng.toDoubleOrNull() ?: 106.682229,
            banKinh = s.formBanKinh.toDoubleOrNull() ?: 100.0,
            trangThai = s.formTrangThai
        )

        viewModelScope.launch {
            if (s.editingBuoiHoc == null) {
                buoiHocRepository.createBuoiHoc(request).collect { res ->
                    when (res) {
                        is DinhLeKieuLinhResource.Loading -> _uiState.update { it.copy(isLoading = true) }
                        is DinhLeKieuLinhResource.Success -> {
                            _uiState.update { it.copy(isLoading = false, isFormDialogOpen = false, successMessage = "Tạo buổi học thành công") }
                            loadBuoiHocs()
                        }
                        is DinhLeKieuLinhResource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                        is DinhLeKieuLinhResource.Idle -> Unit
                    }
                }
            } else {
                buoiHocRepository.updateBuoiHoc(s.editingBuoiHoc.id, request).collect { res ->
                    when (res) {
                        is DinhLeKieuLinhResource.Loading -> _uiState.update { it.copy(isLoading = true) }
                        is DinhLeKieuLinhResource.Success -> {
                            _uiState.update { it.copy(isLoading = false, isFormDialogOpen = false, successMessage = "Cập nhật buổi học thành công") }
                            loadBuoiHocs()
                        }
                        is DinhLeKieuLinhResource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                        is DinhLeKieuLinhResource.Idle -> Unit
                    }
                }
            }
        }
    }

    // ==========================================
    // DYNAMIC QR CODE (Tự động đổi sau mỗi 30s)
    // ==========================================
    fun openDynamicQr(bh: DinhLeKieuLinhBuoiHoc) {
        _uiState.update { it.copy(dynamicQrBuoiHoc = bh) }
        startDynamicQrLoop(bh.id)
    }

    fun closeDynamicQr() {
        qrRefreshJob?.cancel()
        _uiState.update { it.copy(dynamicQrBuoiHoc = null, currentDynamicQr = null) }
    }

    private fun startDynamicQrLoop(buoiHocId: Int) {
        qrRefreshJob?.cancel()
        qrRefreshJob = viewModelScope.launch {
            while (true) {
                buoiHocRepository.generateDynamicQr(buoiHocId).collect { res ->
                    if (res is DinhLeKieuLinhResource.Success) {
                        _uiState.update { it.copy(currentDynamicQr = res.data, qrCountdownSeconds = 30) }
                    }
                }
                for (sec in 30 downTo 1) {
                    delay(1000)
                    _uiState.update { it.copy(qrCountdownSeconds = sec - 1) }
                }
            }
        }
    }

    fun promptDelete(bh: DinhLeKieuLinhBuoiHoc) {
        _uiState.update { it.copy(deletingBuoiHoc = bh) }
    }

    fun cancelDelete() {
        _uiState.update { it.copy(deletingBuoiHoc = null) }
    }

    fun confirmDelete() {
        val bh = _uiState.value.deletingBuoiHoc ?: return
        viewModelScope.launch {
            buoiHocRepository.deleteBuoiHoc(bh.id).collect { res ->
                when (res) {
                    is DinhLeKieuLinhResource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is DinhLeKieuLinhResource.Success -> {
                        _uiState.update { it.copy(isLoading = false, deletingBuoiHoc = null, successMessage = "Xóa buổi học thành công") }
                        loadBuoiHocs()
                    }
                    is DinhLeKieuLinhResource.Error -> _uiState.update { it.copy(isLoading = false, deletingBuoiHoc = null, errorMessage = res.message) }
                    is DinhLeKieuLinhResource.Idle -> Unit
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
