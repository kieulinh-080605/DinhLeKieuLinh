package com.dinhlekieulinh.diemdanh.ui.screens.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.dinhlekieulinh.diemdanh.ui.components.DinhLeKieuLinhTopAppBar
import com.dinhlekieulinh.diemdanh.ui.navigation.DinhLeKieuLinhAdminNav
import com.dinhlekieulinh.diemdanh.ui.screens.crud.*

@Composable
fun DinhLeKieuLinhAdminDashboard(
    userViewModel: DinhLeKieuLinhUserViewModel,
    monHocViewModel: DinhLeKieuLinhMonHocViewModel,
    lopHocViewModel: DinhLeKieuLinhLopHocViewModel,
    buoiHocViewModel: DinhLeKieuLinhBuoiHocViewModel,
    diemDanhViewModel: DinhLeKieuLinhDiemDanhViewModel,
    onLogout: () -> Unit
) {
    // Dùng route String để tránh NullPointerException với sealed class
    var selectedRoute by remember { mutableStateOf("admin_users") }
    val navItems = DinhLeKieuLinhAdminNav.items

    Scaffold(
        topBar = {
            DinhLeKieuLinhTopAppBar(
                title = "Quản trị: ${navItems.find { it.route == selectedRoute }?.title ?: ""}",
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
                "admin_users"    -> DinhLeKieuLinhUserScreen(userViewModel)
                "admin_monhoc"   -> DinhLeKieuLinhMonHocScreen(monHocViewModel)
                "admin_lophoc"   -> DinhLeKieuLinhLopHocScreen(lopHocViewModel)
                "admin_buoihoc"  -> DinhLeKieuLinhBuoiHocScreen(buoiHocViewModel)
                "admin_diemdanh" -> DinhLeKieuLinhDiemDanhScreen(diemDanhViewModel)
            }
        }
    }
}
