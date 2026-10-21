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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.dinhlekieulinh.diemdanh.data.model.DinhLeKieuLinhBuoiHoc
import com.dinhlekieulinh.diemdanh.data.model.DinhLeKieuLinhLopHoc
import com.dinhlekieulinh.diemdanh.data.model.DinhLeKieuLinhTrangThaiBuoiHoc
import com.dinhlekieulinh.diemdanh.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DinhLeKieuLinhBuoiHocScreen(
    viewModel: DinhLeKieuLinhBuoiHocViewModel,
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
                Icon(Icons.Default.Add, contentDescription = "Thêm buổi học")
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

            // Lọc theo lớp học
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = state.selectedLopHocId == null,
                        onClick = { viewModel.onFilterLopHocChanged(null) },
                        label = { Text("Tất cả lớp (${state.buoiHocs.size})") }
                    )
                }
                items(state.availableLopHocs, key = { it.id }) { lop ->
                    FilterChip(
                        selected = state.selectedLopHocId == lop.id,
                        onClick = { viewModel.onFilterLopHocChanged(lop.id) },
                        label = { Text(lop.tenLopHoc) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (state.isLoading && state.buoiHocs.isEmpty()) {
                DinhLeKieuLinhInlineLoading(message = "Đang tải danh sách buổi học...")
            } else if (state.buoiHocs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Chưa có buổi học nào",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.buoiHocs, key = { it.id }) { bh ->
                        DinhLeKieuLinhBuoiHocCard(
                            buoiHoc = bh,
                            onShowQr = { viewModel.openDynamicQr(bh) },
                            onEdit = { viewModel.openEditDialog(bh) },
                            onDelete = { viewModel.promptDelete(bh) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // Modal Form Tạo / Sửa buổi học
    if (state.isFormDialogOpen) {
        DinhLeKieuLinhBuoiHocFormDialog(
            isEditing = state.editingBuoiHoc != null,
            lopHocId = state.formLopHocId,
            availableLopHocs = state.availableLopHocs,
            ngayHoc = state.formNgayHoc,
            gioBatDau = state.formGioBatDau,
            gioKetThuc = state.formGioKetThuc,
            phongHoc = state.formPhongHoc,
            noiDung = state.formNoiDung,
            trangThai = state.formTrangThai,
            ngayHocError = state.formNgayHocError,
            phongHocError = state.formPhongHocError,
            isLoading = state.isLoading,
            onLopHocIdChange = viewModel::onFormLopHocIdChange,
            onNgayHocChange = viewModel::onFormNgayHocChange,
            onGioBatDauChange = viewModel::onFormGioBatDauChange,
            onGioKetThucChange = viewModel::onFormGioKetThucChange,
            onPhongHocChange = viewModel::onFormPhongHocChange,
            onNoiDungChange = viewModel::onFormNoiDungChange,
            onTrangThaiChange = viewModel::onFormTrangThaiChange,
            onSave = viewModel::saveBuoiHoc,
            onDismiss = viewModel::closeFormDialog
        )
    }

    // Modal Hiển thị Dynamic QR Code (Nâng cao 1 + Đề xuất mới)
    if (state.dynamicQrBuoiHoc != null) {
        DinhLeKieuLinhDynamicQrDialog(
            buoiHoc = state.dynamicQrBuoiHoc!!,
            qrContent = state.currentDynamicQr?.qrCodeValue ?: "QR_LOADING",
            countdownSeconds = state.qrCountdownSeconds,
            onDismiss = viewModel::closeDynamicQr
        )
    }

    if (state.deletingBuoiHoc != null) {
        DinhLeKieuLinhConfirmDeleteDialog(
            title = "Xác nhận xóa buổi học",
            message = "Bạn có chắc chắn muốn xóa buổi học ngày '${state.deletingBuoiHoc?.ngayHoc}' (${state.deletingBuoiHoc?.tenLopHoc}) không?",
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::cancelDelete
        )
    }
}

@Composable
fun DinhLeKieuLinhBuoiHocCard(
    buoiHoc: DinhLeKieuLinhBuoiHoc,
    onShowQr: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
                        .background(MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = buoiHoc.tenLopHoc,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ngày: ${buoiHoc.ngayHoc} • ${buoiHoc.gioBatDau} - ${buoiHoc.gioKetThuc}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = buoiHoc.trangThai.displayName,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Room,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Phòng: ${buoiHoc.phongHoc}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Bán kính: ${buoiHoc.banKinh.toInt()}m",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (buoiHoc.noiDung.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Nội dung: ${buoiHoc.noiDung}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Các nút thao tác
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nút tạo mã QR cho buổi học
                Button(
                    onClick = onShowQr,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tạo mã QR", style = MaterialTheme.typography.labelMedium)
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
}

// Dialog Dynamic QR tự đổi sau 30 giây
@Composable
fun DinhLeKieuLinhDynamicQrDialog(
    buoiHoc: DinhLeKieuLinhBuoiHoc,
    qrContent: String,
    countdownSeconds: Int,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Mã Điểm Danh QR Dynamic",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "${buoiHoc.tenLopHoc} • Phòng ${buoiHoc.phongHoc}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Ảnh QR Code thật sinh bằng ZXing
                Box(
                    modifier = Modifier
                        .size(230.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    DinhLeKieuLinhQrImageView(content = qrContent, sizeDp = 206.dp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Thanh tiến trình đếm ngược 30 giây
                LinearProgressIndicator(
                    progress = { countdownSeconds / 30f },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(6.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "⏱️ Mã tự làm mới sau: ${countdownSeconds}s",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (countdownSeconds <= 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Chống gian lận: Mã tự thay đổi mỗi 30s để chống chụp ảnh gửi cho bạn bè điểm danh hộ.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Đóng")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DinhLeKieuLinhBuoiHocFormDialog(
    isEditing: Boolean,
    lopHocId: Int,
    availableLopHocs: List<DinhLeKieuLinhLopHoc>,
    ngayHoc: String,
    gioBatDau: String,
    gioKetThuc: String,
    phongHoc: String,
    noiDung: String,
    trangThai: DinhLeKieuLinhTrangThaiBuoiHoc,
    ngayHocError: String?,
    phongHocError: String?,
    isLoading: Boolean,
    onLopHocIdChange: (Int) -> Unit,
    onNgayHocChange: (String) -> Unit,
    onGioBatDauChange: (String) -> Unit,
    onGioKetThucChange: (String) -> Unit,
    onPhongHocChange: (String) -> Unit,
    onNoiDungChange: (String) -> Unit,
    onTrangThaiChange: (DinhLeKieuLinhTrangThaiBuoiHoc) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    var lopDropdownExpanded by remember { mutableStateOf(false) }
    var trangThaiDropdownExpanded by remember { mutableStateOf(false) }
    val selectedLop = availableLopHocs.find { it.id == lopHocId }

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
                    text = if (isEditing) "Chỉnh sửa buổi học" else "Thêm buổi học mới",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Chọn lớp
                ExposedDropdownMenuBox(
                    expanded = lopDropdownExpanded,
                    onExpandedChange = { lopDropdownExpanded = !lopDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedLop?.tenLopHoc ?: "Chọn lớp học",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Lớp học") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = lopDropdownExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = lopDropdownExpanded,
                        onDismissRequest = { lopDropdownExpanded = false }
                    ) {
                        availableLopHocs.forEach { lop ->
                            DropdownMenuItem(
                                text = { Text(lop.tenLopHoc) },
                                onClick = {
                                    onLopHocIdChange(lop.id)
                                    lopDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                DinhLeKieuLinhTextField(
                    value = ngayHoc,
                    onValueChange = onNgayHocChange,
                    label = "Ngày học (YYYY-MM-DD)",
                    errorMessage = ngayHocError
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DinhLeKieuLinhTextField(
                        value = gioBatDau,
                        onValueChange = onGioBatDauChange,
                        label = "Bắt đầu",
                        modifier = Modifier.weight(1f)
                    )
                    DinhLeKieuLinhTextField(
                        value = gioKetThuc,
                        onValueChange = onGioKetThucChange,
                        label = "Kết thúc",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                DinhLeKieuLinhTextField(
                    value = phongHoc,
                    onValueChange = onPhongHocChange,
                    label = "Phòng học (VD: A1-402)",
                    errorMessage = phongHocError
                )

                Spacer(modifier = Modifier.height(8.dp))

                DinhLeKieuLinhTextField(
                    value = noiDung,
                    onValueChange = onNoiDungChange,
                    label = "Nội dung bài học"
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
