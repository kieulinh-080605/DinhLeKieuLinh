package com.dinhlekieulinh.diemdanh.ui.screens.crud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinhlekieulinh.diemdanh.data.model.*
import com.dinhlekieulinh.diemdanh.data.repository.DinhLeKieuLinhUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DinhLeKieuLinhUserUiState(
    val users: List<DinhLeKieuLinhUser> = emptyList(),
    val searchQuery: String = "",
    val selectedRoleFilter: DinhLeKieuLinhRole? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    // Dialog Thêm/Sửa
    val isFormDialogOpen: Boolean = false,
    val editingUser: DinhLeKieuLinhUser? = null,
    val formHoTen: String = "",
    val formMaSo: String = "",
    val formEmail: String = "",
    val formUsername: String = "",
    val formPassword: String = "",
    val formSoDienThoai: String = "",
    val formRole: DinhLeKieuLinhRole = DinhLeKieuLinhRole.SINH_VIEN,
    val formHoTenError: String? = null,
    val formMaSoError: String? = null,
    val formEmailError: String? = null,
    val formUsernameError: String? = null,
    // Dialog xác nhận xóa
    val deletingUser: DinhLeKieuLinhUser? = null
)

class DinhLeKieuLinhUserViewModel(
    private val userRepository: DinhLeKieuLinhUserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DinhLeKieuLinhUserUiState())
    val uiState: StateFlow<DinhLeKieuLinhUserUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        val q = _uiState.value.searchQuery.ifBlank { null }
        val r = _uiState.value.selectedRoleFilter?.name
        viewModelScope.launch {
            userRepository.getUsers(q, r).collect { res ->
                when (res) {
                    is DinhLeKieuLinhResource.Loading -> _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    is DinhLeKieuLinhResource.Success -> _uiState.update { it.copy(isLoading = false, users = res.data) }
                    is DinhLeKieuLinhResource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                    is DinhLeKieuLinhResource.Idle -> Unit
                }
            }
        }
    }

    fun onSearchQueryChanged(q: String) {
        _uiState.update { it.copy(searchQuery = q) }
        loadUsers()
    }

    fun onRoleFilterChanged(role: DinhLeKieuLinhRole?) {
        _uiState.update { it.copy(selectedRoleFilter = role) }
        loadUsers()
    }

    fun openCreateDialog() {
        _uiState.update {
            it.copy(
                isFormDialogOpen = true,
                editingUser = null,
                formHoTen = "",
                formMaSo = "",
                formEmail = "",
                formUsername = "",
                formPassword = "",
                formSoDienThoai = "",
                formRole = DinhLeKieuLinhRole.SINH_VIEN,
                formHoTenError = null,
                formMaSoError = null,
                formEmailError = null,
                formUsernameError = null
            )
        }
    }

    fun openEditDialog(user: DinhLeKieuLinhUser) {
        _uiState.update {
            it.copy(
                isFormDialogOpen = true,
                editingUser = user,
                formHoTen = user.hoTen,
                formMaSo = user.maSo,
                formEmail = user.email,
                formUsername = user.username,
                formPassword = "",
                formSoDienThoai = user.soDienThoai,
                formRole = user.role,
                formHoTenError = null,
                formMaSoError = null,
                formEmailError = null,
                formUsernameError = null
            )
        }
    }

    fun closeFormDialog() {
        _uiState.update { it.copy(isFormDialogOpen = false) }
    }

    fun onFormHoTenChange(v: String) = _uiState.update { it.copy(formHoTen = v, formHoTenError = null) }
    fun onFormMaSoChange(v: String) = _uiState.update { it.copy(formMaSo = v, formMaSoError = null) }
    fun onFormEmailChange(v: String) = _uiState.update { it.copy(formEmail = v, formEmailError = null) }
    fun onFormUsernameChange(v: String) = _uiState.update { it.copy(formUsername = v, formUsernameError = null) }
    fun onFormPasswordChange(v: String) = _uiState.update { it.copy(formPassword = v) }
    fun onFormSoDienThoaiChange(v: String) = _uiState.update { it.copy(formSoDienThoai = v) }
    fun onFormRoleChange(v: DinhLeKieuLinhRole) = _uiState.update { it.copy(formRole = v) }

    fun saveUser() {
        val s = _uiState.value
        var hasErr = false

        if (s.formHoTen.isBlank()) {
            _uiState.update { it.copy(formHoTenError = "Vui lòng nhập họ và tên") }
            hasErr = true
        }
        if (s.formMaSo.isBlank()) {
            _uiState.update { it.copy(formMaSoError = "Vui lòng nhập mã số") }
            hasErr = true
        }
        if (s.formEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(s.formEmail).matches()) {
            _uiState.update { it.copy(formEmailError = "Email không hợp lệ") }
            hasErr = true
        }
        if (s.formUsername.isBlank()) {
            _uiState.update { it.copy(formUsernameError = "Vui lòng nhập tên đăng nhập") }
            hasErr = true
        }

        if (hasErr) return

        val request = DinhLeKieuLinhUserRequest(
            username = s.formUsername.trim(),
            email = s.formEmail.trim(),
            password = s.formPassword.ifBlank { null },
            hoTen = s.formHoTen.trim(),
            maSo = s.formMaSo.trim(),
            soDienThoai = s.formSoDienThoai.trim(),
            role = s.formRole
        )

        viewModelScope.launch {
            if (s.editingUser == null) {
                userRepository.createUser(request).collect { res ->
                    when (res) {
                        is DinhLeKieuLinhResource.Loading -> _uiState.update { it.copy(isLoading = true) }
                        is DinhLeKieuLinhResource.Success -> {
                            _uiState.update { it.copy(isLoading = false, isFormDialogOpen = false, successMessage = "Thêm người dùng thành công") }
                            loadUsers()
                        }
                        is DinhLeKieuLinhResource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                        is DinhLeKieuLinhResource.Idle -> Unit
                    }
                }
            } else {
                userRepository.updateUser(s.editingUser.id, request).collect { res ->
                    when (res) {
                        is DinhLeKieuLinhResource.Loading -> _uiState.update { it.copy(isLoading = true) }
                        is DinhLeKieuLinhResource.Success -> {
                            _uiState.update { it.copy(isLoading = false, isFormDialogOpen = false, successMessage = "Cập nhật người dùng thành công") }
                            loadUsers()
                        }
                        is DinhLeKieuLinhResource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                        is DinhLeKieuLinhResource.Idle -> Unit
                    }
                }
            }
        }
    }

    fun promptDelete(user: DinhLeKieuLinhUser) {
        _uiState.update { it.copy(deletingUser = user) }
    }

    fun cancelDelete() {
        _uiState.update { it.copy(deletingUser = null) }
    }

    fun confirmDelete() {
        val user = _uiState.value.deletingUser ?: return
        viewModelScope.launch {
            userRepository.deleteUser(user.id).collect { res ->
                when (res) {
                    is DinhLeKieuLinhResource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is DinhLeKieuLinhResource.Success -> {
                        _uiState.update { it.copy(isLoading = false, deletingUser = null, successMessage = "Xóa người dùng thành công") }
                        loadUsers()
                    }
                    is DinhLeKieuLinhResource.Error -> _uiState.update { it.copy(isLoading = false, deletingUser = null, errorMessage = res.message) }
                    is DinhLeKieuLinhResource.Idle -> Unit
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
