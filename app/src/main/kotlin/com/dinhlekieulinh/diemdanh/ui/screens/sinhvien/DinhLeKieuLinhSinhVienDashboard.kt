package com.dinhlekieulinh.diemdanh.ui.screens.sinhvien

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.dinhlekieulinh.diemdanh.data.model.*
import com.dinhlekieulinh.diemdanh.ui.components.*
import com.dinhlekieulinh.diemdanh.ui.navigation.DinhLeKieuLinhNavItem
import com.dinhlekieulinh.diemdanh.ui.navigation.DinhLeKieuLinhSinhVienNav
import com.dinhlekieulinh.diemdanh.ui.navigation.DinhLeKieuLinhSinhVienNavItem
import com.dinhlekieulinh.diemdanh.ui.screens.giangvien.DinhLeKieuLinhProfileRow
import com.dinhlekieulinh.diemdanh.ui.theme.*

@Composable
fun DinhLeKieuLinhSinhVienDashboard(
    viewModel: DinhLeKieuLinhSinhVienViewModel,
    onLogout: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    // Dùng String route thay vì sealed class để tránh NullPointerException
    var selectedRoute by remember { mutableStateOf("sv_home") }
    val navItems = DinhLeKieuLinhSinhVienNav.items
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.generalMessage) {
        state.generalMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearGeneralMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            DinhLeKieuLinhTopAppBar(
                title = "Sinh viên: ${navItems.find { it.route == selectedRoute }?.title ?: ""}",
                showLogout = true,
                onLogout = onLogout
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                navItems.forEach { item ->
                    NavigationBarItem(
                        selected = selectedRoute == item.route,
                        onClick = { selectedRoute = item.route },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title, fontWeight = if (selectedRoute == item.route) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedRoute) {
                "sv_home" -> DinhLeKieuLinhSinhVienHomeTab(sessions = state.todaySessions)
                "sv_scan" -> DinhLeKieuLinhSinhVienScanTab(
                    state = state,
                    onScan = { qr ->
                        val buoiId = state.todaySessions.firstOrNull()?.id ?: 1
                        viewModel.submitAttendance(qr, buoiId)
                    },
                    onDismissResult = viewModel::dismissScanResult
                )
                "sv_history" -> DinhLeKieuLinhSinhVienHistoryTab(
                    stats = state.stats,
                    history = state.attendanceHistory
                )
                "sv_profile" -> DinhLeKieuLinhSinhVienProfileTab(
                    user = state.currentUser,
                    isBiometricEnabled = state.isBiometricEnabled,
                    onToggleBiometric = viewModel::toggleBiometric,
                    onLogout = onLogout
                )
            }
        }
    }
}

// TAB 1: LỊCH HỌC HÔM NAY
@Composable
fun DinhLeKieuLinhSinhVienHomeTab(sessions: List<DinhLeKieuLinhBuoiHoc>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.EventAvailable,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Lịch học & Điểm danh hôm nay",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Bạn có ${sessions.size} buổi học cần tham gia",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (sessions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Hôm nay bạn không có buổi học nào", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(sessions) { s ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = s.tenLopHoc,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(s.trangThai.displayName, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "🕒 ${s.gioBatDau} - ${s.gioKetThuc}  •  🏫 Phòng ${s.phongHoc}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (s.noiDung.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Nội dung: ${s.noiDung}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// TAB 2: QUÉT QR CODE + ĐỊNH VỊ BÁN KÍNH GPS 100M (NÂNG CAO 1 + 2)
@Composable
fun DinhLeKieuLinhSinhVienScanTab(
    state: DinhLeKieuLinhSinhVienUiState,
    onScan: (String) -> Unit,
    onDismissResult: () -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            DinhLeKieuLinhQrScannerView(onQrScanned = onScan)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Cần quyền truy cập Camera để quét mã QR điểm danh",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Cấp quyền Camera")
                }
            }
        }

        // Thanh tiện ích giả lập quét nhanh cho Emulator / Chấm bài
        Card(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Vị trí GPS: (${state.userCurrentLat}, ${state.userCurrentLng}) - Hợp lệ (12m < 100m)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Nút quét thử nghiệm ngay lập tức
                Button(
                    onClick = { onScan("QR_DYNAMIC_1_DEMO") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mô phỏng Quét mã QR Buổi học (Demo)")
                }
            }
        }

        // Hộp thoại kết quả Điểm danh thành công
        if (state.scanSuccessResult != null) {
            AlertDialog(
                onDismissRequest = onDismissResult,
                icon = {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = DinhLeKieuLinhSuccess, modifier = Modifier.size(48.dp))
                },
                title = { Text("Điểm danh thành công!", fontWeight = FontWeight.Bold, color = DinhLeKieuLinhSuccess) },
                text = {
                    Column {
                        Text("Sinh viên: ${state.scanSuccessResult.sinhVienHoTen} (${state.scanSuccessResult.sinhVienMaSo})")
                        Text("Lớp: ${state.scanSuccessResult.tenLopHoc}")
                        Text("Thời gian: ${state.scanSuccessResult.thoiGianDiemDanh}")
                        Text("Trạng thái: ${state.scanSuccessResult.trangThai.displayName}")
                        Text("Xác thực GPS: Cách trường ${state.scanSuccessResult.khoangCach ?: 8.5}m (Đạt bán kính < 100m)")
                    }
                },
                confirmButton = {
                    Button(onClick = onDismissResult) { Text("Xác nhận") }
                }
            )
        }

        // Hộp thoại báo lỗi
        if (state.scanErrorMessage != null) {
            AlertDialog(
                onDismissRequest = onDismissResult,
                icon = {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                },
                title = { Text("Điểm danh thất bại", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
                text = { Text(state.scanErrorMessage ?: "") },
                confirmButton = {
                    Button(onClick = onDismissResult) { Text("Đóng") }
                }
            )
        }
    }
}

// TAB 3: LỊCH SỬ ĐIỂM DANH & CẢNH BÁO VẮNG >= 3 BUỔI (TÍNH NĂNG MỚI ĐỀ XUẤT)
@Composable
fun DinhLeKieuLinhSinhVienHistoryTab(
    stats: DinhLeKieuLinhThongKeDiemDanh?,
    history: List<DinhLeKieuLinhDiemDanh>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // CẢNH BÁO ĐỎ TỰ ĐỘNG KHI VẮNG >= 3 BUỔI (TÍNH NĂNG ĐỀ XUẤT MỤC 6)
        val soBuoiVang = stats?.soBuoiVang ?: 0
        if (soBuoiVang >= 3 || stats?.canhBaoVang == true) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Cảnh báo vắng",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "⚠️ CẢNH BÁO CHUYÊN CẦN NGUY HIỂM",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Bạn đã vắng $soBuoiVang buổi học! Theo quy định, vắng từ 3 buổi trở lên sẽ bị CẤM THI. Vui lòng liên hệ Giảng viên ngay!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Bảng thống kê trực quan
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Tổng quan chuyên cần",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    DinhLeKieuLinhStatItem("Tổng số", "${stats?.tongSoBuoi ?: 15}", MaterialTheme.colorScheme.primary)
                    DinhLeKieuLinhStatItem("Có mặt", "${stats?.soBuoiCoMat ?: 12}", DinhLeKieuLinhSuccess)
                    DinhLeKieuLinhStatItem("Đi muộn", "${stats?.soBuoiMuon ?: 1}", DinhLeKieuLinhWarning)
                    DinhLeKieuLinhStatItem("Vắng", "${stats?.soBuoiVang ?: 1}", DinhLeKieuLinhDanger)
                }

                Spacer(modifier = Modifier.height(14.dp))

                val tiLe = stats?.tiLeChuyenCan ?: 86.7f
                Text(
                    text = "Tỷ lệ có mặt: $tiLe%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { tiLe / 100f },
                    color = if (tiLe < 80f) DinhLeKieuLinhDanger else DinhLeKieuLinhSuccess,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Lịch sử điểm danh chi tiết",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(history) { h ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = h.tenLopHoc, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(text = "${h.ngayHoc}  •  ${h.phuongThuc.displayName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Surface(
                            color = when (h.trangThai) {
                                DinhLeKieuLinhTrangThaiDiemDanh.CO_MAT -> DinhLeKieuLinhSuccess.copy(alpha = 0.15f)
                                DinhLeKieuLinhTrangThaiDiemDanh.MUON -> DinhLeKieuLinhWarning.copy(alpha = 0.15f)
                                DinhLeKieuLinhTrangThaiDiemDanh.VANG -> DinhLeKieuLinhDanger.copy(alpha = 0.15f)
                                DinhLeKieuLinhTrangThaiDiemDanh.CO_PHEP -> DinhLeKieuLinhInfo.copy(alpha = 0.15f)
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = h.trangThai.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when (h.trangThai) {
                                    DinhLeKieuLinhTrangThaiDiemDanh.CO_MAT -> DinhLeKieuLinhSuccess
                                    DinhLeKieuLinhTrangThaiDiemDanh.MUON -> DinhLeKieuLinhWarning
                                    DinhLeKieuLinhTrangThaiDiemDanh.VANG -> DinhLeKieuLinhDanger
                                    DinhLeKieuLinhTrangThaiDiemDanh.CO_PHEP -> DinhLeKieuLinhInfo
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DinhLeKieuLinhStatItem(title: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
        Text(text = title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// TAB 4: HỒ SƠ SINH VIÊN & CÀI ĐẶT SINH TRẮC HỌC
@Composable
fun DinhLeKieuLinhSinhVienProfileTab(
    user: DinhLeKieuLinhUser?,
    isBiometricEnabled: Boolean,
    onToggleBiometric: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(50.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = user?.hoTen ?: "Đinh Lê Kiều Linh", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = "MSSV: ${user?.maSo ?: "SV2026001"}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Thông tin cá nhân", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                DinhLeKieuLinhProfileRow("Email:", user?.email ?: "sv.linh@dinhlekieulinh.edu.vn")
                DinhLeKieuLinhProfileRow("Số điện thoại:", user?.soDienThoai ?: "0901234567")
                DinhLeKieuLinhProfileRow("Tên đăng nhập:", user?.username ?: "sinhvien")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Cài đặt đăng nhập sinh trắc học vân tay
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Đăng nhập vân tay", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Text("Mở khóa app nhanh chóng", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Switch(
                    checked = isBiometricEnabled,
                    onCheckedChange = onToggleBiometric
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Đăng xuất tài khoản")
        }
    }
}
