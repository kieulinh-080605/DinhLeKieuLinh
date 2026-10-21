package com.dinhlekieulinh.diemdanh.ui.screens.giangvien

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dinhlekieulinh.diemdanh.data.model.DinhLeKieuLinhUser
import com.dinhlekieulinh.diemdanh.ui.components.DinhLeKieuLinhTopAppBar
import com.dinhlekieulinh.diemdanh.ui.navigation.DinhLeKieuLinhGiangVienNav
import com.dinhlekieulinh.diemdanh.ui.screens.crud.*

@Composable
fun DinhLeKieuLinhGiangVienDashboard(
    lopHocViewModel: DinhLeKieuLinhLopHocViewModel,
    buoiHocViewModel: DinhLeKieuLinhBuoiHocViewModel,
    diemDanhViewModel: DinhLeKieuLinhDiemDanhViewModel,
    currentUser: DinhLeKieuLinhUser?,
    onLogout: () -> Unit
) {
    // Dùng route String để tránh NullPointerException với sealed class
    var selectedRoute by remember { mutableStateOf("gv_buoihoc") }
    val navItems = DinhLeKieuLinhGiangVienNav.items

    Scaffold(
        topBar = {
            DinhLeKieuLinhTopAppBar(
                title = "Giảng viên: ${navItems.find { it.route == selectedRoute }?.title ?: ""}",
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
                "gv_lophoc"   -> DinhLeKieuLinhLopHocScreen(lopHocViewModel)
                "gv_buoihoc"  -> DinhLeKieuLinhBuoiHocScreen(buoiHocViewModel)
                "gv_diemdanh" -> DinhLeKieuLinhDiemDanhScreen(diemDanhViewModel)
                "gv_profile"  -> DinhLeKieuLinhProfileTab(user = currentUser, onLogout = onLogout)
            }
        }
    }
}

@Composable
fun DinhLeKieuLinhProfileTab(
    user: DinhLeKieuLinhUser?,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = user?.hoTen ?: "Đang tải...",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        SuggestionChip(
            onClick = {},
            label = { Text(user?.role?.displayName ?: "Giảng viên") }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Thông tin tài khoản",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                DinhLeKieuLinhProfileRow("Mã số:", user?.maSo ?: "GV002")
                DinhLeKieuLinhProfileRow("Email:", user?.email ?: "gv@dinhlekieulinh.edu.vn")
                DinhLeKieuLinhProfileRow("Số điện thoại:", user?.soDienThoai ?: "0987654321")
                DinhLeKieuLinhProfileRow("Tên đăng nhập:", user?.username ?: "giangvien")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

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

@Composable
fun DinhLeKieuLinhProfileRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
