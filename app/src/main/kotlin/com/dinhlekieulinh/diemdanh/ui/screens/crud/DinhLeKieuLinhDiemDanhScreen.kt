package com.dinhlekieulinh.diemdanh.ui.screens.crud

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.dinhlekieulinh.diemdanh.data.model.*
import com.dinhlekieulinh.diemdanh.ui.components.*
import com.dinhlekieulinh.diemdanh.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DinhLeKieuLinhDiemDanhScreen(
    viewModel: DinhLeKieuLinhDiemDanhViewModel,
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
                onClick = viewModel::openManualAttendanceDialog,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.AddTask, contentDescription = "Điểm danh bù")
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

            // Lọc theo buổi học
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = state.selectedBuoiHocId == null,
                        onClick = { viewModel.onFilterBuoiHocChanged(null) },
                        label = { Text("Tất cả (${state.diemDanhs.size})") }
                    )
                }
                items(state.availableBuoiHocs, key = { it.id }) { buoi ->
                    FilterChip(
                        selected = state.selectedBuoiHocId == buoi.id,
                        onClick = { viewModel.onFilterBuoiHocChanged(buoi.id) },
                        label = { Text("${buoi.tenLopHoc} (${buoi.ngayHoc})") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (state.isLoading && state.diemDanhs.isEmpty()) {
                DinhLeKieuLinhInlineLoading(message = "Đang tải dữ liệu điểm danh...")
            } else if (state.diemDanhs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Chưa có lượt điểm danh nào",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.diemDanhs, key = { it.id }) { record ->
                        DinhLeKieuLinhDiemDanhCard(
                            record = record,
                            onEdit = { viewModel.openEditAttendanceDialog(record) },
                            onDelete = { viewModel.promptDelete(record) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // Modal Điểm danh bù / Thay đổi trạng thái
    if (state.isFormDialogOpen) {
        DinhLeKieuLinhDiemDanhFormDialog(
            isEditing = state.editingDiemDanh != null,
            buoiHocId = state.formBuoiHocId,
            sinhVienId = state.formSinhVienId,
            availableBuoiHocs = state.availableBuoiHocs,
            availableSinhViens = state.availableSinhViens,
            trangThai = state.formTrangThai,
            ghiChu = state.formGhiChu,
            isLoading = state.isLoading,
            onBuoiHocIdChange = viewModel::onFormBuoiHocIdChange,
            onSinhVienIdChange = viewModel::onFormSinhVienIdChange,
            onTrangThaiChange = viewModel::onFormTrangThaiChange,
            onGhiChuChange = viewModel::onFormGhiChuChange,
            onSave = viewModel::saveAttendance,
            onDismiss = viewModel::closeFormDialog
        )
    }

    if (state.deletingDiemDanh != null) {
        DinhLeKieuLinhConfirmDeleteDialog(
            title = "Xác nhận hủy điểm danh",
            message = "Bạn có chắc muốn hủy bản ghi điểm danh của sinh viên '${state.deletingDiemDanh?.sinhVienHoTen}' không?",
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::cancelDelete
        )
    }
}

@Composable
fun DinhLeKieuLinhDiemDanhCard(
    record: DinhLeKieuLinhDiemDanh,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (record.trangThai) {
        DinhLeKieuLinhTrangThaiDiemDanh.CO_MAT -> DinhLeKieuLinhSuccess
        DinhLeKieuLinhTrangThaiDiemDanh.MUON -> DinhLeKieuLinhWarning
        DinhLeKieuLinhTrangThaiDiemDanh.VANG -> DinhLeKieuLinhDanger
        DinhLeKieuLinhTrangThaiDiemDanh.CO_PHEP -> DinhLeKieuLinhInfo
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(statusColor.copy(alpha = 0.15f), shape = RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (record.trangThai) {
                            DinhLeKieuLinhTrangThaiDiemDanh.CO_MAT -> Icons.Default.CheckCircle
                            DinhLeKieuLinhTrangThaiDiemDanh.MUON -> Icons.Default.AccessTime
                            DinhLeKieuLinhTrangThaiDiemDanh.VANG -> Icons.Default.Cancel
                            DinhLeKieuLinhTrangThaiDiemDanh.CO_PHEP -> Icons.Default.Sick
                        },
                        contentDescription = null,
                        tint = statusColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.sinhVienHoTen,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "MSSV: ${record.sinhVienMaSo} • ${record.tenLopHoc}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = statusColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = record.trangThai.displayName,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Phương thức: ${record.phuongThuc.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                if (record.khoangCach != null) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Khoảng cách: ${record.khoangCach}m",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            if (record.thoiGianDiemDanh.isNotEmpty()) {
                Text(
                    text = "Thời gian: ${record.thoiGianDiemDanh}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (record.ghiChu.isNotEmpty()) {
                Text(
                    text = "Ghi chú: ${record.ghiChu}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DinhLeKieuLinhDiemDanhFormDialog(
    isEditing: Boolean,
    buoiHocId: Int,
    sinhVienId: Int,
    availableBuoiHocs: List<DinhLeKieuLinhBuoiHoc>,
    availableSinhViens: List<DinhLeKieuLinhUser>,
    trangThai: DinhLeKieuLinhTrangThaiDiemDanh,
    ghiChu: String,
    isLoading: Boolean,
    onBuoiHocIdChange: (Int) -> Unit,
    onSinhVienIdChange: (Int) -> Unit,
    onTrangThaiChange: (DinhLeKieuLinhTrangThaiDiemDanh) -> Unit,
    onGhiChuChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    var buoiDropdownExpanded by remember { mutableStateOf(false) }
    var svDropdownExpanded by remember { mutableStateOf(false) }
    var ttDropdownExpanded by remember { mutableStateOf(false) }

    val selectedBuoi = availableBuoiHocs.find { it.id == buoiHocId }
    val selectedSv = availableSinhViens.find { it.id == sinhVienId }

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
                    text = if (isEditing) "Cập nhật lượt điểm danh" else "Điểm danh thủ công / Điểm danh bù",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Buổi học
                ExposedDropdownMenuBox(
                    expanded = buoiDropdownExpanded,
                    onExpandedChange = { buoiDropdownExpanded = !buoiDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedBuoi?.let { "${it.tenLopHoc} (${it.ngayHoc})" } ?: "Chọn buổi học",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Buổi học") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = buoiDropdownExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = buoiDropdownExpanded,
                        onDismissRequest = { buoiDropdownExpanded = false }
                    ) {
                        availableBuoiHocs.forEach { b ->
                            DropdownMenuItem(
                                text = { Text("${b.tenLopHoc} - ${b.ngayHoc}") },
                                onClick = {
                                    onBuoiHocIdChange(b.id)
                                    buoiDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Sinh viên
                ExposedDropdownMenuBox(
                    expanded = svDropdownExpanded,
                    onExpandedChange = { svDropdownExpanded = !svDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedSv?.let { "${it.hoTen} (${it.maSo})" } ?: "Chọn sinh viên",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sinh viên") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = svDropdownExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = svDropdownExpanded,
                        onDismissRequest = { svDropdownExpanded = false }
                    ) {
                        availableSinhViens.forEach { sv ->
                            DropdownMenuItem(
                                text = { Text("${sv.hoTen} (${sv.maSo})") },
                                onClick = {
                                    onSinhVienIdChange(sv.id)
                                    svDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Trạng thái
                ExposedDropdownMenuBox(
                    expanded = ttDropdownExpanded,
                    onExpandedChange = { ttDropdownExpanded = !ttDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = trangThai.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Trạng thái") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ttDropdownExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = ttDropdownExpanded,
                        onDismissRequest = { ttDropdownExpanded = false }
                    ) {
                        DinhLeKieuLinhTrangThaiDiemDanh.values().forEach { tt ->
                            DropdownMenuItem(
                                text = { Text(tt.displayName) },
                                onClick = {
                                    onTrangThaiChange(tt)
                                    ttDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                DinhLeKieuLinhTextField(
                    value = ghiChu,
                    onValueChange = onGhiChuChange,
                    label = "Ghi chú (Lý do / Xác nhận)"
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
                        Text("Xác nhận")
                    }
                }
            }
        }
    }
}
