package com.dinhlekieulinh.diemdanh.ui.screens.crud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinhlekieulinh.diemdanh.data.model.DinhLeKieuLinhMonHoc
import com.dinhlekieulinh.diemdanh.data.model.DinhLeKieuLinhMonHocRequest
import com.dinhlekieulinh.diemdanh.data.model.DinhLeKieuLinhResource
import com.dinhlekieulinh.diemdanh.data.repository.DinhLeKieuLinhMonHocRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DinhLeKieuLinhMonHocUiState(
    val monHocs: List<DinhLeKieuLinhMonHoc> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    // Dialog Thêm/Sửa
    val isFormDialogOpen: Boolean = false,
    val editingMonHoc: DinhLeKieuLinhMonHoc? = null,
    val formMaMonHoc: String = "",
    val formTenMonHoc: String = "",
    val formSoTinChi: String = "3",
    val formMoTa: String = "",
    val formMaMonHocError: String? = null,
    val formTenMonHocError: String? = null,
    val formSoTinChiError: String? = null,
    // Dialog Xóa
    val deletingMonHoc: DinhLeKieuLinhMonHoc? = null
)

class DinhLeKieuLinhMonHocViewModel(
    private val monHocRepository: DinhLeKieuLinhMonHocRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DinhLeKieuLinhMonHocUiState())
    val uiState: StateFlow<DinhLeKieuLinhMonHocUiState> = _uiState.asStateFlow()

    init {
        loadMonHocs()
    }

    fun loadMonHocs() {
        val q = _uiState.value.searchQuery.ifBlank { null }
        viewModelScope.launch {
            monHocRepository.getMonHocs(q).collect { res ->
                when (res) {
                    is DinhLeKieuLinhResource.Loading -> _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    is DinhLeKieuLinhResource.Success -> _uiState.update { it.copy(isLoading = false, monHocs = res.data) }
                    is DinhLeKieuLinhResource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                    is DinhLeKieuLinhResource.Idle -> Unit
                }
            }
        }
    }

    fun onSearchQueryChanged(q: String) {
        _uiState.update { it.copy(searchQuery = q) }
        loadMonHocs()
    }

    fun openCreateDialog() {
        _uiState.update {
            it.copy(
                isFormDialogOpen = true,
                editingMonHoc = null,
                formMaMonHoc = "",
                formTenMonHoc = "",
                formSoTinChi = "3",
                formMoTa = "",
                formMaMonHocError = null,
                formTenMonHocError = null,
                formSoTinChiError = null
            )
        }
    }

    fun openEditDialog(mh: DinhLeKieuLinhMonHoc) {
        _uiState.update {
            it.copy(
                isFormDialogOpen = true,
                editingMonHoc = mh,
                formMaMonHoc = mh.maMonHoc,
                formTenMonHoc = mh.tenMonHoc,
                formSoTinChi = mh.soTinChi.toString(),
                formMoTa = mh.moTa,
                formMaMonHocError = null,
                formTenMonHocError = null,
                formSoTinChiError = null
            )
        }
    }

    fun closeFormDialog() {
        _uiState.update { it.copy(isFormDialogOpen = false) }
    }

    fun onFormMaMonHocChange(v: String) = _uiState.update { it.copy(formMaMonHoc = v, formMaMonHocError = null) }
    fun onFormTenMonHocChange(v: String) = _uiState.update { it.copy(formTenMonHoc = v, formTenMonHocError = null) }
    fun onFormSoTinChiChange(v: String) = _uiState.update { it.copy(formSoTinChi = v, formSoTinChiError = null) }
    fun onFormMoTaChange(v: String) = _uiState.update { it.copy(formMoTa = v) }

    fun saveMonHoc() {
        val s = _uiState.value
        var hasErr = false

        if (s.formMaMonHoc.isBlank()) {
            _uiState.update { it.copy(formMaMonHocError = "Vui lòng nhập mã môn học") }
            hasErr = true
        }
        if (s.formTenMonHoc.isBlank()) {
            _uiState.update { it.copy(formTenMonHocError = "Vui lòng nhập tên môn học") }
            hasErr = true
        }
        val tc = s.formSoTinChi.toIntOrNull()
        if (tc == null || tc <= 0) {
            _uiState.update { it.copy(formSoTinChiError = "Số tín chỉ phải lớn hơn 0") }
            hasErr = true
        }

        if (hasErr) return

        val request = DinhLeKieuLinhMonHocRequest(
            maMonHoc = s.formMaMonHoc.trim(),
            tenMonHoc = s.formTenMonHoc.trim(),
            soTinChi = tc ?: 3,
            moTa = s.formMoTa.trim()
        )

        viewModelScope.launch {
            if (s.editingMonHoc == null) {
                monHocRepository.createMonHoc(request).collect { res ->
                    when (res) {
                        is DinhLeKieuLinhResource.Loading -> _uiState.update { it.copy(isLoading = true) }
                        is DinhLeKieuLinhResource.Success -> {
                            _uiState.update { it.copy(isLoading = false, isFormDialogOpen = false, successMessage = "Thêm môn học thành công") }
                            loadMonHocs()
                        }
                        is DinhLeKieuLinhResource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                        is DinhLeKieuLinhResource.Idle -> Unit
                    }
                }
            } else {
                monHocRepository.updateMonHoc(s.editingMonHoc.id, request).collect { res ->
                    when (res) {
                        is DinhLeKieuLinhResource.Loading -> _uiState.update { it.copy(isLoading = true) }
                        is DinhLeKieuLinhResource.Success -> {
                            _uiState.update { it.copy(isLoading = false, isFormDialogOpen = false, successMessage = "Cập nhật môn học thành công") }
                            loadMonHocs()
                        }
                        is DinhLeKieuLinhResource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                        is DinhLeKieuLinhResource.Idle -> Unit
                    }
                }
            }
        }
    }

    fun promptDelete(mh: DinhLeKieuLinhMonHoc) {
        _uiState.update { it.copy(deletingMonHoc = mh) }
    }

    fun cancelDelete() {
        _uiState.update { it.copy(deletingMonHoc = null) }
    }

    fun confirmDelete() {
        val mh = _uiState.value.deletingMonHoc ?: return
        viewModelScope.launch {
            monHocRepository.deleteMonHoc(mh.id).collect { res ->
                when (res) {
                    is DinhLeKieuLinhResource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is DinhLeKieuLinhResource.Success -> {
                        _uiState.update { it.copy(isLoading = false, deletingMonHoc = null, successMessage = "Xóa môn học thành công") }
                        loadMonHocs()
                    }
                    is DinhLeKieuLinhResource.Error -> _uiState.update { it.copy(isLoading = false, deletingMonHoc = null, errorMessage = res.message) }
                    is DinhLeKieuLinhResource.Idle -> Unit
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
