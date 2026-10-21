package com.dinhlekieulinh.diemdanh.ui.screens.crud

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.dinhlekieulinh.diemdanh.data.model.DinhLeKieuLinhLopHoc
import com.dinhlekieulinh.diemdanh.data.model.DinhLeKieuLinhMonHoc
import com.dinhlekieulinh.diemdanh.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DinhLeKieuLinhLopHocScreen(
    viewModel: DinhLeKieuLinhLopHocViewModel,
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
                Icon(Icons.Default.Add, contentDescription = "Thêm lớp học")
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

            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                placeholder = { Text("Tìm kiếm theo tên lớp, mã lớp...") },
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

            if (state.isLoading && state.lopHocs.isEmpty()) {
                DinhLeKieuLinhInlineLoading(message = "Đang tải danh sách lớp học...")
            } else if (state.lopHocs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Chưa có lớp học nào",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.lopHocs, key = { it.id }) { lop ->
                        DinhLeKieuLinhLopHocCard(
                            lopHoc = lop,
                            onEdit = { viewModel.openEditDialog(lop) },
                            onDelete = { viewModel.promptDelete(lop) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (state.isFormDialogOpen) {
        DinhLeKieuLinhLopHocFormDialog(
            isEditing = state.editingLopHoc != null,
            maLopHoc = state.formMaLopHoc,
            tenLopHoc = state.formTenLopHoc,
            monHocId = state.formMonHocId,
            availableMonHocs = state.availableMonHocs,
            hocKy = state.formHocKy,
            namHoc = state.formNamHoc,
            maLopHocError = state.formMaLopHocError,
            tenLopHocError = state.formTenLopHocError,
            isLoading = state.isLoading,
            onMaLopHocChange = viewModel::onFormMaLopHocChange,
            onTenLopHocChange = viewModel::onFormTenLopHocChange,
            onMonHocIdChange = viewModel::onFormMonHocIdChange,
            onHocKyChange = viewModel::onFormHocKyChange,
            onNamHocChange = viewModel::onFormNamHocChange,
            onSave = viewModel::saveLopHoc,
            onDismiss = viewModel::closeFormDialog
        )
    }

    if (state.deletingLopHoc != null) {
        DinhLeKieuLinhConfirmDeleteDialog(
            title = "Xác nhận xóa lớp học",
            message = "Bạn có chắc chắn muốn xóa lớp '${state.deletingLopHoc?.tenLopHoc}' (${state.deletingLopHoc?.maLopHoc}) không?",
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::cancelDelete
        )
    }
}

@Composable
fun DinhLeKieuLinhLopHocCard(
    lopHoc: DinhLeKieuLinhLopHoc,
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
                    .background(MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Class,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lopHoc.tenLopHoc,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Mã: ${lopHoc.maLopHoc} • ${lopHoc.hocKy} (${lopHoc.namHoc})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Môn: ${lopHoc.tenMonHoc}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "GV: ${lopHoc.giangVienHoTen} • ${lopHoc.soLuongSinhVien} SV",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
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
fun DinhLeKieuLinhLopHocFormDialog(
    isEditing: Boolean,
    maLopHoc: String,
    tenLopHoc: String,
    monHocId: Int,
    availableMonHocs: List<DinhLeKieuLinhMonHoc>,
    hocKy: String,
    namHoc: String,
    maLopHocError: String?,
    tenLopHocError: String?,
    isLoading: Boolean,
    onMaLopHocChange: (String) -> Unit,
    onTenLopHocChange: (String) -> Unit,
    onMonHocIdChange: (Int) -> Unit,
    onHocKyChange: (String) -> Unit,
    onNamHocChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    var monHocDropdownExpanded by remember { mutableStateOf(false) }
    val selectedMonHoc = availableMonHocs.find { it.id == monHocId }

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
                    text = if (isEditing) "Chỉnh sửa lớp học" else "Thêm lớp học mới",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(14.dp))

                DinhLeKieuLinhTextField(
                    value = maLopHoc,
                    onValueChange = onMaLopHocChange,
                    label = "Mã lớp học (VD: DHKTPM18A)",
                    errorMessage = maLopHocError
                )

                Spacer(modifier = Modifier.height(8.dp))

                DinhLeKieuLinhTextField(
                    value = tenLopHoc,
                    onValueChange = onTenLopHocChange,
                    label = "Tên lớp học",
                    errorMessage = tenLopHocError
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Dropdown chọn môn học
                ExposedDropdownMenuBox(
                    expanded = monHocDropdownExpanded,
                    onExpandedChange = { monHocDropdownExpanded = !monHocDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedMonHoc?.tenMonHoc ?: "Chọn môn học",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Môn học") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monHocDropdownExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = monHocDropdownExpanded,
                        onDismissRequest = { monHocDropdownExpanded = false }
                    ) {
                        availableMonHocs.forEach { mh ->
                            DropdownMenuItem(
                                text = { Text("${mh.maMonHoc} - ${mh.tenMonHoc}") },
                                onClick = {
                                    onMonHocIdChange(mh.id)
                                    monHocDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DinhLeKieuLinhTextField(
                        value = hocKy,
                        onValueChange = onHocKyChange,
                        label = "Học kỳ (VD: HK1)",
                        modifier = Modifier.weight(1f)
                    )
                    DinhLeKieuLinhTextField(
                        value = namHoc,
                        onValueChange = onNamHocChange,
                        label = "Năm học",
                        modifier = Modifier.weight(1f)
                    )
                }

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
