package com.dinhlekieulinh.diemdanh.ui.screens.crud

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.dinhlekieulinh.diemdanh.data.model.DinhLeKieuLinhRole
import com.dinhlekieulinh.diemdanh.data.model.DinhLeKieuLinhUser
import com.dinhlekieulinh.diemdanh.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DinhLeKieuLinhUserScreen(
    viewModel: DinhLeKieuLinhUserViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.successMessage, state.errorMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::openCreateDialog,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm người dùng")
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Thanh tìm kiếm
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                placeholder = { Text("Tìm kiếm theo tên, email, MSSV...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Xóa")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Lọc theo vai trò (Role chips)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = state.selectedRoleFilter == null,
                        onClick = { viewModel.onRoleFilterChanged(null) },
                        label = { Text("Tất cả (${state.users.size})") }
                    )
                }
                item {
                    FilterChip(
                        selected = state.selectedRoleFilter == DinhLeKieuLinhRole.SINH_VIEN,
                        onClick = { viewModel.onRoleFilterChanged(DinhLeKieuLinhRole.SINH_VIEN) },
                        label = { Text("Sinh viên") }
                    )
                }
                item {
                    FilterChip(
                        selected = state.selectedRoleFilter == DinhLeKieuLinhRole.GIANG_VIEN,
                        onClick = { viewModel.onRoleFilterChanged(DinhLeKieuLinhRole.GIANG_VIEN) },
                        label = { Text("Giảng viên") }
                    )
                }
                item {
                    FilterChip(
                        selected = state.selectedRoleFilter == DinhLeKieuLinhRole.ADMIN,
                        onClick = { viewModel.onRoleFilterChanged(DinhLeKieuLinhRole.ADMIN) },
                        label = { Text("Quản trị") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Danh sách người dùng
            if (state.isLoading && state.users.isEmpty()) {
                DinhLeKieuLinhInlineLoading(message = "Đang tải danh sách người dùng...")
            } else if (state.users.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Không tìm thấy người dùng nào",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.users, key = { it.id }) { user ->
                        DinhLeKieuLinhUserCard(
                            user = user,
                            onEdit = { viewModel.openEditDialog(user) },
                            onDelete = { viewModel.promptDelete(user) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // Dialog Thêm / Sửa người dùng
    if (state.isFormDialogOpen) {
        DinhLeKieuLinhUserFormDialog(
            isEditing = state.editingUser != null,
            hoTen = state.formHoTen,
            maSo = state.formMaSo,
            email = state.formEmail,
            username = state.formUsername,
            password = state.formPassword,
            soDienThoai = state.formSoDienThoai,
            role = state.formRole,
            hoTenError = state.formHoTenError,
            maSoError = state.formMaSoError,
            emailError = state.formEmailError,
            usernameError = state.formUsernameError,
            isLoading = state.isLoading,
            onHoTenChange = viewModel::onFormHoTenChange,
            onMaSoChange = viewModel::onFormMaSoChange,
            onEmailChange = viewModel::onFormEmailChange,
            onUsernameChange = viewModel::onFormUsernameChange,
            onPasswordChange = viewModel::onFormPasswordChange,
            onSoDienThoaiChange = viewModel::onFormSoDienThoaiChange,
            onRoleChange = viewModel::onFormRoleChange,
            onSave = viewModel::saveUser,
            onDismiss = viewModel::closeFormDialog
        )
    }

    // Dialog xác nhận xóa
    if (state.deletingUser != null) {
        DinhLeKieuLinhConfirmDeleteDialog(
            title = "Xác nhận xóa người dùng",
            message = "Bạn có chắc chắn muốn xóa tài khoản '${state.deletingUser?.hoTen}' (${state.deletingUser?.maSo}) không?",
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::cancelDelete
        )
    }
}

@Composable
fun DinhLeKieuLinhUserCard(
    user: DinhLeKieuLinhUser,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon đại diện
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        when (user.role) {
                            DinhLeKieuLinhRole.ADMIN -> MaterialTheme.colorScheme.errorContainer
                            DinhLeKieuLinhRole.GIANG_VIEN -> MaterialTheme.colorScheme.secondaryContainer
                            DinhLeKieuLinhRole.SINH_VIEN -> MaterialTheme.colorScheme.primaryContainer
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (user.role) {
                        DinhLeKieuLinhRole.ADMIN -> Icons.Default.AdminPanelSettings
                        DinhLeKieuLinhRole.GIANG_VIEN -> Icons.Default.School
                        DinhLeKieuLinhRole.SINH_VIEN -> Icons.Default.Person
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.hoTen,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    SuggestionChip(
                        onClick = {},
                        label = { Text(user.role.displayName, style = MaterialTheme.typography.labelSmall) }
                    )
                }

                Text(
                    text = "Mã số: ${user.maSo} | User: ${user.username}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DinhLeKieuLinhUserFormDialog(
    isEditing: Boolean,
    hoTen: String,
    maSo: String,
    email: String,
    username: String,
    password: String,
    soDienThoai: String,
    role: DinhLeKieuLinhRole,
    hoTenError: String?,
    maSoError: String?,
    emailError: String?,
    usernameError: String?,
    isLoading: Boolean,
    onHoTenChange: (String) -> Unit,
    onMaSoChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSoDienThoaiChange: (String) -> Unit,
    onRoleChange: (DinhLeKieuLinhRole) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    var roleDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = if (isEditing) "Chỉnh sửa người dùng" else "Thêm người dùng mới",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(14.dp))

                DinhLeKieuLinhTextField(
                    value = hoTen,
                    onValueChange = onHoTenChange,
                    label = "Họ và tên",
                    errorMessage = hoTenError
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DinhLeKieuLinhTextField(
                        value = maSo,
                        onValueChange = onMaSoChange,
                        label = "Mã số",
                        errorMessage = maSoError,
                        modifier = Modifier.weight(1f)
                    )
                    DinhLeKieuLinhTextField(
                        value = username,
                        onValueChange = onUsernameChange,
                        label = "Username",
                        errorMessage = usernameError,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                DinhLeKieuLinhTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = "Email",
                    errorMessage = emailError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Role Dropdown
                ExposedDropdownMenuBox(
                    expanded = roleDropdownExpanded,
                    onExpandedChange = { roleDropdownExpanded = !roleDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = role.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Vai trò") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleDropdownExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = roleDropdownExpanded,
                        onDismissRequest = { roleDropdownExpanded = false }
                    ) {
                        DinhLeKieuLinhRole.values().forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r.displayName) },
                                onClick = {
                                    onRoleChange(r)
                                    roleDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                DinhLeKieuLinhTextField(
                    value = soDienThoai,
                    onValueChange = onSoDienThoaiChange,
                    label = "Số điện thoại",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Hủy")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onSave, enabled = !isLoading) {
                        Text(if (isEditing) "Cập nhật" else "Lưu")
                    }
                }
            }
        }
    }
}
