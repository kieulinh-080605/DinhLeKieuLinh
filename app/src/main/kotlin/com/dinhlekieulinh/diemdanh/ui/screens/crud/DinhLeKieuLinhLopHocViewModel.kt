package com.dinhlekieulinh.diemdanh.ui.screens.crud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinhlekieulinh.diemdanh.data.model.*
import com.dinhlekieulinh.diemdanh.data.repository.DinhLeKieuLinhLopHocRepository
import com.dinhlekieulinh.diemdanh.data.repository.DinhLeKieuLinhMonHocRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DinhLeKieuLinhLopHocUiState(
    val lopHocs: List<DinhLeKieuLinhLopHoc> = emptyList(),
    val availableMonHocs: List<DinhLeKieuLinhMonHoc> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    // Form Dialog
    val isFormDialogOpen: Boolean = false,
    val editingLopHoc: DinhLeKieuLinhLopHoc? = null,
    val formMaLopHoc: String = "",
    val formTenLopHoc: String = "",
    val formMonHocId: Int = 0,
    val formGiangVienId: Int = 2,
    val formHocKy: String = "HK1",
    val formNamHoc: String = "2025-2026",
    val formMaLopHocError: String? = null,
    val formTenLopHocError: String? = null,
    // Delete dialog
    val deletingLopHoc: DinhLeKieuLinhLopHoc? = null
)

class DinhLeKieuLinhLopHocViewModel(
    private val lopHocRepository: DinhLeKieuLinhLopHocRepository,
    private val monHocRepository: DinhLeKieuLinhMonHocRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DinhLeKieuLinhLopHocUiState())
    val uiState: StateFlow<DinhLeKieuLinhLopHocUiState> = _uiState.asStateFlow()

    init {
        loadLopHocs()
        loadMonHocs()
    }

    fun loadLopHocs() {
        val q = _uiState.value.searchQuery.ifBlank { null }
        viewModelScope.launch {
            lopHocRepository.getLopHocs(q).collect { res ->
                when (res) {
                    is DinhLeKieuLinhResource.Loading -> _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    is DinhLeKieuLinhResource.Success -> _uiState.update { it.copy(isLoading = false, lopHocs = res.data) }
                    is DinhLeKieuLinhResource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                    is DinhLeKieuLinhResource.Idle -> Unit
                }
            }
        }
    }

    private fun loadMonHocs() {
        viewModelScope.launch {
            monHocRepository.getMonHocs().collect { res ->
                if (res is DinhLeKieuLinhResource.Success) {
                    _uiState.update { it.copy(availableMonHocs = res.data) }
                }
            }
        }
    }

    fun onSearchQueryChanged(q: String) {
        _uiState.update { it.copy(searchQuery = q) }
        loadLopHocs()
    }

    fun openCreateDialog() {
        val defaultMonHocId = _uiState.value.availableMonHocs.firstOrNull()?.id ?: 1
        _uiState.update {
            it.copy(
                isFormDialogOpen = true,
                editingLopHoc = null,
                formMaLopHoc = "",
                formTenLopHoc = "",
                formMonHocId = defaultMonHocId,
                formGiangVienId = 2,
                formHocKy = "HK1",
                formNamHoc = "2025-2026",
                formMaLopHocError = null,
                formTenLopHocError = null
            )
        }
    }

    fun openEditDialog(lop: DinhLeKieuLinhLopHoc) {
        _uiState.update {
            it.copy(
                isFormDialogOpen = true,
                editingLopHoc = lop,
                formMaLopHoc = lop.maLopHoc,
                formTenLopHoc = lop.tenLopHoc,
                formMonHocId = lop.monHocId,
                formGiangVienId = lop.giangVienId,
                formHocKy = lop.hocKy,
                formNamHoc = lop.namHoc,
                formMaLopHocError = null,
                formTenLopHocError = null
            )
        }
    }

    fun closeFormDialog() {
        _uiState.update { it.copy(isFormDialogOpen = false) }
    }

    fun onFormMaLopHocChange(v: String) = _uiState.update { it.copy(formMaLopHoc = v, formMaLopHocError = null) }
    fun onFormTenLopHocChange(v: String) = _uiState.update { it.copy(formTenLopHoc = v, formTenLopHocError = null) }
    fun onFormMonHocIdChange(id: Int) = _uiState.update { it.copy(formMonHocId = id) }
    fun onFormHocKyChange(v: String) = _uiState.update { it.copy(formHocKy = v) }
    fun onFormNamHocChange(v: String) = _uiState.update { it.copy(formNamHoc = v) }

    fun saveLopHoc() {
        val s = _uiState.value
        var hasErr = false

        if (s.formMaLopHoc.isBlank()) {
            _uiState.update { it.copy(formMaLopHocError = "Vui lòng nhập mã lớp") }
            hasErr = true
        }
        if (s.formTenLopHoc.isBlank()) {
            _uiState.update { it.copy(formTenLopHocError = "Vui lòng nhập tên lớp") }
            hasErr = true
        }

        if (hasErr) return

        val request = DinhLeKieuLinhLopHocRequest(
            maLopHoc = s.formMaLopHoc.trim(),
            tenLopHoc = s.formTenLopHoc.trim(),
            monHocId = s.formMonHocId,
            giangVienId = s.formGiangVienId,
            hocKy = s.formHocKy.trim(),
            namHoc = s.formNamHoc.trim()
        )

        viewModelScope.launch {
            if (s.editingLopHoc == null) {
                lopHocRepository.createLopHoc(request).collect { res ->
                    when (res) {
                        is DinhLeKieuLinhResource.Loading -> _uiState.update { it.copy(isLoading = true) }
                        is DinhLeKieuLinhResource.Success -> {
                            _uiState.update { it.copy(isLoading = false, isFormDialogOpen = false, successMessage = "Thêm lớp học thành công") }
                            loadLopHocs()
                        }
                        is DinhLeKieuLinhResource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                        is DinhLeKieuLinhResource.Idle -> Unit
                    }
                }
            } else {
                lopHocRepository.updateLopHoc(s.editingLopHoc.id, request).collect { res ->
                    when (res) {
                        is DinhLeKieuLinhResource.Loading -> _uiState.update { it.copy(isLoading = true) }
                        is DinhLeKieuLinhResource.Success -> {
                            _uiState.update { it.copy(isLoading = false, isFormDialogOpen = false, successMessage = "Cập nhật lớp học thành công") }
                            loadLopHocs()
                        }
                        is DinhLeKieuLinhResource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                        is DinhLeKieuLinhResource.Idle -> Unit
                    }
                }
            }
        }
    }

    fun promptDelete(lop: DinhLeKieuLinhLopHoc) {
        _uiState.update { it.copy(deletingLopHoc = lop) }
    }

    fun cancelDelete() {
        _uiState.update { it.copy(deletingLopHoc = null) }
    }

    fun confirmDelete() {
        val lop = _uiState.value.deletingLopHoc ?: return
        viewModelScope.launch {
            lopHocRepository.deleteLopHoc(lop.id).collect { res ->
                when (res) {
                    is DinhLeKieuLinhResource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is DinhLeKieuLinhResource.Success -> {
                        _uiState.update { it.copy(isLoading = false, deletingLopHoc = null, successMessage = "Xóa lớp học thành công") }
                        loadLopHocs()
                    }
                    is DinhLeKieuLinhResource.Error -> _uiState.update { it.copy(isLoading = false, deletingLopHoc = null, errorMessage = res.message) }
                    is DinhLeKieuLinhResource.Idle -> Unit
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
