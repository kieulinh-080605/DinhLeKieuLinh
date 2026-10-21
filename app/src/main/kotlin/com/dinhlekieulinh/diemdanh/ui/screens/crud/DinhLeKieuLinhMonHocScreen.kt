package com.dinhlekieulinh.diemdanh.ui.screens.crud

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.dinhlekieulinh.diemdanh.data.model.DinhLeKieuLinhMonHoc
import com.dinhlekieulinh.diemdanh.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DinhLeKieuLinhMonHocScreen(
    viewModel: DinhLeKieuLinhMonHocViewModel,
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
                Icon(Icons.Default.Add, contentDescription = "Thêm môn học")
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

            // Ô tìm kiếm
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                placeholder = { Text("Tìm kiếm môn học theo tên, mã môn...") },
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

            Spacer(modifier = Modifier.height(12.dp))

            if (state.isLoading && state.monHocs.isEmpty()) {
                DinhLeKieuLinhInlineLoading(message = "Đang tải danh sách môn học...")
            } else if (state.monHocs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Chưa có môn học nào",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.monHocs, key = { it.id }) { mh ->
                        DinhLeKieuLinhMonHocCard(
                            monHoc = mh,
                            onEdit = { viewModel.openEditDialog(mh) },
                            onDelete = { viewModel.promptDelete(mh) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (state.isFormDialogOpen) {
        DinhLeKieuLinhMonHocFormDialog(
            isEditing = state.editingMonHoc != null,
            maMonHoc = state.formMaMonHoc,
            tenMonHoc = state.formTenMonHoc,
            soTinChi = state.formSoTinChi,
            moTa = state.formMoTa,
            maMonHocError = state.formMaMonHocError,
            tenMonHocError = state.formTenMonHocError,
            soTinChiError = state.formSoTinChiError,
            isLoading = state.isLoading,
            onMaMonHocChange = viewModel::onFormMaMonHocChange,
            onTenMonHocChange = viewModel::onFormTenMonHocChange,
            onSoTinChiChange = viewModel::onFormSoTinChiChange,
            onMoTaChange = viewModel::onFormMoTaChange,
            onSave = viewModel::saveMonHoc,
            onDismiss = viewModel::closeFormDialog
        )
    }

    if (state.deletingMonHoc != null) {
        DinhLeKieuLinhConfirmDeleteDialog(
            title = "Xác nhận xóa môn học",
            message = "Bạn có chắc chắn muốn xóa môn '${state.deletingMonHoc?.tenMonHoc}' (${state.deletingMonHoc?.maMonHoc}) không?",
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::cancelDelete
        )
    }
}

@Composable
fun DinhLeKieuLinhMonHocCard(
    monHoc: DinhLeKieuLinhMonHoc,
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
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = monHoc.tenMonHoc,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Mã: ${monHoc.maMonHoc} • ${monHoc.soTinChi} tín chỉ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                if (monHoc.moTa.isNotEmpty()) {
                    Text(
                        text = monHoc.moTa,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
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

@Composable
fun DinhLeKieuLinhMonHocFormDialog(
    isEditing: Boolean,
    maMonHoc: String,
    tenMonHoc: String,
    soTinChi: String,
    moTa: String,
    maMonHocError: String?,
    tenMonHocError: String?,
    soTinChiError: String?,
    isLoading: Boolean,
    onMaMonHocChange: (String) -> Unit,
    onTenMonHocChange: (String) -> Unit,
    onSoTinChiChange: (String) -> Unit,
    onMoTaChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
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
                    text = if (isEditing) "Chỉnh sửa môn học" else "Thêm môn học mới",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(14.dp))

                DinhLeKieuLinhTextField(
                    value = maMonHoc,
                    onValueChange = onMaMonHocChange,
                    label = "Mã môn học (VD: COMP101)",
                    errorMessage = maMonHocError
                )

                Spacer(modifier = Modifier.height(8.dp))

                DinhLeKieuLinhTextField(
                    value = tenMonHoc,
                    onValueChange = onTenMonHocChange,
                    label = "Tên môn học",
                    errorMessage = tenMonHocError
                )

                Spacer(modifier = Modifier.height(8.dp))

                DinhLeKieuLinhTextField(
                    value = soTinChi,
                    onValueChange = onSoTinChiChange,
                    label = "Số tín chỉ",
                    errorMessage = soTinChiError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(8.dp))

                DinhLeKieuLinhTextField(
                    value = moTa,
                    onValueChange = onMoTaChange,
                    label = "Mô tả môn học"
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
